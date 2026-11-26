package com.example.tp4spring.service;

import com.example.tp4spring.arca.ArcaFacturacionService;
import com.example.tp4spring.dto.*;
import com.example.tp4spring.entities.*;
import com.example.tp4spring.exception.DatosInvalidosException;
import com.example.tp4spring.exception.RecursoNoEncontradoException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FacturaService {

    private final EntityManager em;
    private final AuditoriaService auditoriaService;
    private final ArcaFacturacionService arcaFacturacionService;

    public Page<FacturaReporteDTO> buscarFacturasFiltradas (FacturaFiltrosDTO filtros, Pageable pageable) {
        StringBuilder where = new StringBuilder("WHERE 1=1   ");
        Map<String, Object> params = new HashMap<>();

        if (filtros.getNumero() != null) {
            where.append("AND f.numero = :numero ");
            params.put("numero", filtros.getNumero());
        }
        if (filtros.getFechaDesde() != null) {
            where.append("AND f.fechaEmision >= :fechaDesde ");
            params.put("fechaDesde", filtros.getFechaDesde());
        }
        if (filtros.getFechaHasta() != null) {
            // "hasta" incluye el día completo: menor al día siguiente a las 00:00
            Date diaSiguiente = new Date(filtros.getFechaHasta().getTime() + 24L * 60 * 60 * 1000);
            where.append("AND f.fechaEmision < :fechaHasta ");
            params.put("fechaHasta", diaSiguiente);
        }
        if (filtros.getClienteId() != null) {
            where.append("AND f.cliente.id = :clienteId ");
            params.put("clienteId", filtros.getClienteId());
        }
        if (Boolean.TRUE.equals(filtros.getSinCliente())) {
            where.append("AND f.cliente IS NULL ");
        }
        if (filtros.getEstado() != null && !filtros.getEstado().isBlank()) {
            where.append("AND LOWER(f.estado) = LOWER(:estado) ");
            params.put("estado", filtros.getEstado());
        }
        if (filtros.getMontoMinimo() != null) {
            where.append("AND f.importeTotal >= :montoMinimo ");
            params.put("montoMinimo", filtros.getMontoMinimo());
        }
        if (filtros.getInformadaArca() != null) {
            where.append(filtros.getInformadaArca() ? "AND f.cae IS NOT NULL " : "AND f.cae IS NULL ");
        }

        String jpql = "SELECT new com.example.tp4spring.dto.FacturaReporteDTO(" +
                " f.id, f.numero, f.fechaEmision, f.clienteDenominacion, " +
                " f.condicionIvaDenominacion, f.puntoVentaDescripcion, f.importeTotal, COUNT(d), f.estado) " +
                "FROM FacturaVenta f " +
                "JOIN f.detalles d " +
                where +
                "GROUP BY f.id, f.numero, f.fechaEmision, f.clienteDenominacion, " +
                "f.condicionIvaDenominacion, f.puntoVentaDescripcion, f.importeTotal, f.estado " +
                "ORDER BY f.numero DESC";

        TypedQuery<FacturaReporteDTO> query = em.createQuery(jpql, FacturaReporteDTO.class);
        params.forEach(query::setParameter);

        // Sin paginar (para los reportes PDF y txt): muestro todas las facturas
        if (pageable.isUnpaged()) {
            return new PageImpl<>(query.getResultList());
        }

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<FacturaReporteDTO> facturas = query.getResultList();

        String jpqlTotal = "SELECT COUNT(f) FROM FacturaVenta f " + where;
        TypedQuery<Long> queryTotal = em.createQuery(jpqlTotal, Long.class);
        params.forEach(queryTotal::setParameter);
        long total = queryTotal.getSingleResult();

        return new PageImpl<>(facturas, pageable, total);
    }

    @Transactional(readOnly = true)
    public FacturaDTO obtener (Long id) {
        return crearFacturaDTO(buscarFactura(id));
    }

    @Transactional
    public FacturaReporteDTO crear (FacturaRequestDTO datos) {
        Long ultimoNumero = em.createQuery("SELECT MAX(f.numero) FROM FacturaVenta f", Long.class).getSingleResult();
        if (ultimoNumero == null) {
            ultimoNumero = 0L;
        }

        Date hoy = new Date();
        FacturaVenta factura = new FacturaVenta();
        factura.setNumero(ultimoNumero + 1);
        factura.setFechaEmision(hoy);
        factura.setEstado("PENDIENTE");   // siempre nace pendiente: solo ARCA la emite
        factura.setDetalles(new ArrayList<>());

        cargarCabecera(factura, datos);
        cargarRenglones(factura, datos);
        calcularTotales(factura);
        factura.hacerCopia();

        auditoriaService.registrarAlta(factura);

        em.persist(factura);
        return crearFacturaReporteDTO(factura);
    }

    @Transactional
    public FacturaReporteDTO actualizar (Long id, FacturaRequestDTO datos) {
        FacturaVenta factura = buscarFactura(id);
        validarEstado(factura, "PENDIENTE", "Solo se pueden modificar las facturas con estado pendiente");

        cargarCabecera(factura, datos);
        factura.getDetalles().clear();
        cargarRenglones(factura, datos);
        calcularTotales(factura);
        factura.hacerCopia();

        auditoriaService.registrarModificacion(factura);

        return crearFacturaReporteDTO(factura);
    }

    @Transactional
    public void eliminar (Long id) {
        FacturaVenta factura = buscarFactura(id);
        validarEstado(factura, "PENDIENTE", "Solo se pueden eliminar las facturas con estado pendiente");
        em.remove(factura);   // este es el borrado físico (los renglones se borran solos por la cascada)
    }

    @Transactional
    public FacturaReporteDTO anular (Long id) {
        FacturaVenta factura = buscarFactura(id);
        validarEstado(factura, "EMITIDA", "Solo se pueden anular las facturas con estado emitida");

        factura.setEstado("ANULADA");
        factura.setFechaAnulacion(new Date());
        auditoriaService.registrarBaja(factura);

        return crearFacturaReporteDTO(factura);
    }

    @Transactional
    public FacturaDTO informarArca(Long id) {
        FacturaVenta factura = buscarFactura(id);
        validarEstado(factura, "PENDIENTE", "Solo se pueden informar a ARCA facturas pendientes");
        factura.hacerCopia();

        if (!"PES".equalsIgnoreCase(factura.getTipoMoneda().getCodigoAfip())) {
            throw new DatosInvalidosException("Por ahora solo se pueden informar a ARCA facturas en pesos");
        }

        // Consumidor final (código 5) va sin identificar; el resto, con su CUIT
        boolean consumidorFinal = factura.getCondicionIva().getCodigoAfip() == 5;
        int docTipo = consumidorFinal ? 99 : 80;
        long docNro = consumidorFinal ? 0 : Long.parseLong(factura.getCliente().getCuitCuil().replace("-", ""));

        ArcaFacturacionService.SolicitudCae solicitud = new ArcaFacturacionService.SolicitudCae(
                factura.getPuntoVenta().getNumero(),
                ArcaFacturacionService.FACTURA_C,
                docTipo,
                docNro,
                factura.getCondicionIva().getCodigoAfip(),
                factura.getTipoMoneda().getCodigoAfip(),
                factura.getImporteTotal());

        ArcaFacturacionService.ResultadoCae resultado = arcaFacturacionService.solicitarCae(solicitud);

        factura.setResultadoAfip(resultado.resultado());
        if (resultado.aprobado()) {
            factura.setCae(resultado.cae());
            factura.setCaeFechaVencimiento(Date.from(
                    resultado.vencimientoCae().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            factura.setNumeroComprobanteArca(resultado.numeroComprobante());
            factura.setMotivoRechazo(null);
            factura.setEstado("EMITIDA");
            factura.setFechaEmision(new Date());   // la fecha del comprobante es la de la emisión ante ARCA
            calcularTotales(factura);              // al quedar emitida, se recalcula cobrado y saldo
        } else {
            factura.setMotivoRechazo(resultado.mensaje());
        }
        auditoriaService.registrarModificacion(factura);

        return crearFacturaDTO(factura);
    }

    private FacturaVenta buscarFactura (Long id) {
        FacturaVenta factura = em.find(FacturaVenta.class, id);
        if (factura == null) {
            throw new RecursoNoEncontradoException("Factura no encontrada: " + id);
        }
        return factura;
    }

    private void validarEstado (FacturaVenta factura, String estadoEsperado, String mensaje) {
        if (!estadoEsperado.equalsIgnoreCase(factura.getEstado())) {
            throw new DatosInvalidosException(mensaje + " (estado actual: " + factura.getEstado() + ")");
        }
    }

    // Busca cualquier entidad con auditoría: que venga el id, que exista y que esté activa
    private <T extends AuditoriaApp> T buscarActivo (Class<T> clase, Long id, String nombre) {
        if (id == null) {
            throw new DatosInvalidosException("Falta indicar " + nombre);
        }
        T entidad = em.find(clase, id);
        if (entidad == null) {
            throw new RecursoNoEncontradoException("No se encontró " + nombre + " con id " + id);
        }
        if (entidad.getFechaBaja() != null) {
            throw new DatosInvalidosException("No se puede usar " + nombre + " (id " + id + "): está de baja");
        }
        return entidad;
    }

    private void cargarCabecera (FacturaVenta factura, FacturaRequestDTO datos) {
        factura.setPuntoVenta(buscarActivo(PuntoVenta.class, datos.getPuntoVentaId(), "el punto de venta"));
        factura.setCliente(datos.getClienteId() == null ? null : buscarActivo(Cliente.class, datos.getClienteId(), "el cliente"));
        factura.setTipoMoneda(buscarActivo(TipoMoneda.class, datos.getTipoMonedaId(), "la moneda"));
        factura.setCondicionIva(buscarActivo(CondicionIva.class, datos.getCondicionIvaId(), "la condición IVA"));
        if (factura.getCliente() == null && factura.getCondicionIva().getCodigoAfip() != 5) {
            throw new DatosInvalidosException("Sin cliente solo se puede facturar a Consumidor Final");
        }
        factura.setObservaciones(datos.getObservaciones());
    }

    private void cargarRenglones (FacturaVenta factura, FacturaRequestDTO datos) {
        if (datos.getDetalles() == null || datos.getDetalles().isEmpty()) {
            throw new DatosInvalidosException("La factura debe tener al menos un renglón");
        }
        for (FacturaDetalleRequestDTO renglon : datos.getDetalles()) {
            if (renglon.getListaPrecioArticuloId() == null) {
                throw new DatosInvalidosException("No se eligió ningún precio para el renglón");
            }
            ListaPrecioArticulo lpa = em.find(ListaPrecioArticulo.class, renglon.getListaPrecioArticuloId());
            if (lpa == null) {
                throw new RecursoNoEncontradoException("Precio no encontrado: " + renglon.getListaPrecioArticuloId());
            }
            String nombreArticulo = lpa.getArticulo().getDenominacion();

            // El precio, su lista y su artículo tienen que estar los tres activos
            if (lpa.getFechaBaja() != null || lpa.getListaPrecio().getFechaBaja() != null || lpa.getArticulo().getFechaBaja() != null) {
                throw new DatosInvalidosException("El precio de " + nombreArticulo + " no está vigente");
            }
            if (renglon.getCantidad() <= 0.00) {
                throw new DatosInvalidosException("La cantidad elegida del artículo " + nombreArticulo + " es inválida");
            }
            if (!lpa.getArticulo().isFraccionable() && renglon.getCantidad() != Math.floor(renglon.getCantidad())) {
                throw new DatosInvalidosException(nombreArticulo + " se vende por unidad: la cantidad tiene que ser un número entero");
            }

            double precio = lpa.getPrecioVenta();
            double subtotal = Math.round(precio * renglon.getCantidad() * 100.0) / 100.0;
            double neto = Math.round(subtotal / 1.21 * 100.0) / 100.0;

            FacturaVentaDetalle detalle = new FacturaVentaDetalle();
            detalle.setFactura(factura);
            detalle.setListaPrecioArticulo(lpa);
            detalle.setCantidad(renglon.getCantidad());
            detalle.setPrecioUnitario(precio);
            detalle.setImporteNeto(neto);
            detalle.setImporteIva(Math.round((subtotal - neto) * 100.0) / 100.0);
            detalle.setImporteSubtotal(subtotal);

            factura.agregarDetalle(detalle);
        }
    }

    private void calcularTotales (FacturaVenta factura) {
        double total = 0;
        for (FacturaVentaDetalle detalle : factura.getDetalles()) {
            total = total + detalle.getImporteSubtotal();
        }
        total = Math.round(total * 100.0) / 100.0;
        factura.setImporteCobrado("EMITIDA".equalsIgnoreCase(factura.getEstado()) ? total : 0);
        factura.setImporteSaldo("EMITIDA".equalsIgnoreCase(factura.getEstado()) ? 0 : total);
        factura.setImporteTotal(total);
    }

    private FacturaReporteDTO crearFacturaReporteDTO (FacturaVenta factura) {
        FacturaReporteDTO dto = new FacturaReporteDTO();
        dto.setId(factura.getId());
        dto.setNumeroFactura(factura.getNumero());
        dto.setFechaEmision(factura.getFechaEmision());
        dto.setClienteDenominacion(factura.getClienteDenominacion());
        dto.setCondicionIva(factura.getCondicionIvaDenominacion());
        dto.setPuntoVentaDescripcion(factura.getPuntoVentaDescripcion());
        dto.setImporteTotal(factura.getImporteTotal());
        dto.setCantidadItems(factura.getDetalles().size());
        dto.setEstado(factura.getEstado());
        return dto;
    }

    private FacturaDTO crearFacturaDTO (FacturaVenta factura) {
        FacturaDTO dto = new FacturaDTO();
        dto.setId(factura.getId());
        dto.setNumero(factura.getNumero());
        dto.setFechaEmision(factura.getFechaEmision());
        dto.setEstado(factura.getEstado());
        dto.setObservaciones(factura.getObservaciones());

        dto.setClienteId(factura.getCliente() == null ? null : factura.getCliente().getId());
        dto.setClienteDenominacion(factura.getClienteDenominacion());
        dto.setClienteCuitCuil(factura.getClienteCuitCuil());
        dto.setPuntoVentaId(factura.getPuntoVenta().getId());
        dto.setPuntoVentaNumero(factura.getPuntoVentaNumero());
        dto.setPuntoVentaDescripcion(factura.getPuntoVentaDescripcion());
        dto.setTipoMonedaId(factura.getTipoMoneda().getId());
        dto.setTipoMonedaDenominacion(factura.getTipoMonedaDenominacion());
        dto.setTipoMonedaSimbolo(factura.getTipoMonedaSimbolo());
        dto.setCondicionIvaId(factura.getCondicionIva().getId());
        dto.setCondicionIvaDenominacion(factura.getCondicionIvaDenominacion());

        dto.setImporteTotal(factura.getImporteTotal());
        dto.setImporteCobrado(factura.getImporteCobrado());
        dto.setImporteSaldo(factura.getImporteSaldo());

        dto.setCae(factura.getCae());
        dto.setCaeFechaVencimiento(factura.getCaeFechaVencimiento());
        dto.setResultadoAfip(factura.getResultadoAfip());
        dto.setNumeroComprobanteArca(factura.getNumeroComprobanteArca());
        dto.setMotivoRechazo(factura.getMotivoRechazo());

        dto.setDetalles(factura.getDetalles().stream()
                .map(this::crearFacturaDetalleDTO)
                .toList());
        return dto;
    }

    private FacturaDetalleDTO crearFacturaDetalleDTO (FacturaVentaDetalle detalle) {
        FacturaDetalleDTO dto = new FacturaDetalleDTO();
        dto.setId(detalle.getId());
        dto.setListaPrecioArticuloId(detalle.getListaPrecioArticulo().getId());
        dto.setArticuloCodigo(detalle.getArticuloCodigo());
        dto.setArticuloDenominacion(detalle.getArticuloDenominacion());
        dto.setListaPrecioDenominacion(detalle.getListaPrecioArticulo().getListaPrecio().getDenominacion());
        dto.setCantidad(detalle.getCantidad());
        dto.setPrecioUnitario(detalle.getPrecioUnitario());
        dto.setImporteNeto(detalle.getImporteNeto());
        dto.setImporteIva(detalle.getImporteIva());
        dto.setImporteSubtotal(detalle.getImporteSubtotal());
        dto.setArticuloFraccionable(detalle.getListaPrecioArticulo().getArticulo().isFraccionable());
        return dto;
    }
}