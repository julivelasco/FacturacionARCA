package com.example.tp4spring.service;
import com.example.tp4spring.dto.PuntoVentaDTO;
import com.example.tp4spring.dto.PuntoVentaRequestDTO;
import com.example.tp4spring.entities.PuntoVenta;
import com.example.tp4spring.exception.DatosInvalidosException;
import com.example.tp4spring.exception.RecursoDadoDeBajaException;
import com.example.tp4spring.exception.RecursoNoEncontradoException;
import com.example.tp4spring.repository.PuntoVentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service @RequiredArgsConstructor
public class PuntoVentaService {

    private final PuntoVentaRepository puntoVentaRepository;
    private final AuditoriaService auditoriaService;

    private PuntoVentaDTO crearPuntoVentaDTO(PuntoVenta puntoVenta) {
        PuntoVentaDTO dto = new PuntoVentaDTO();
        dto.setId(puntoVenta.getId());
        dto.setNumero(puntoVenta.getNumero());
        dto.setDescripcion(puntoVenta.getDescripcion());
        dto.setTipoEmision(puntoVenta.getTipoEmision());
        dto.setDomicilioComercial(puntoVenta.getDomicilioComercial());
        dto.setActivo(puntoVenta.getFechaBaja() == null);
        return dto;
    }

    public PuntoVentaDTO obtener(Long id) {
        PuntoVenta puntoVenta = puntoVentaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Punto de venta no encontrado: " + id));
        return crearPuntoVentaDTO(puntoVenta);
    }

    public Page<PuntoVentaDTO> buscar(String texto, boolean incluirBajas, int pagina, int tamanio) {
        if (texto == null) {
            texto = "";
        }
        Pageable pageable = PageRequest.of(pagina, tamanio, Sort.by("numero"));
        Page<PuntoVenta> paginaPuntosVenta = puntoVentaRepository.buscar(texto, incluirBajas, pageable);
        return paginaPuntosVenta.map(this::crearPuntoVentaDTO);
    }

    @Transactional
    public PuntoVentaDTO crear(PuntoVentaRequestDTO datos) {
        if (datos.getNumero() == null || datos.getNumero() <= 0) {
            throw new DatosInvalidosException("El número de punto de venta es obligatorio y debe ser mayor a 0");
        }
        if (datos.getDescripcion() == null || datos.getDescripcion().isBlank()) {
            throw new DatosInvalidosException("La descripción es obligatoria");
        }

        Optional<PuntoVenta> existente = puntoVentaRepository.findByNumero(datos.getNumero());
        if (existente.isPresent()) {
            PuntoVenta puntoVentaExistente = existente.get();
            if (puntoVentaExistente.getFechaBaja() != null) {
                throw new RecursoDadoDeBajaException(
                        "El punto de venta " + puntoVentaExistente.getNumero() + " está dado de baja",
                        puntoVentaExistente.getId());
            }
            throw new DatosInvalidosException("Ya existe el punto de venta número " + datos.getNumero());
        }

        PuntoVenta puntoVenta = new PuntoVenta();
        puntoVenta.setNumero(datos.getNumero());
        puntoVenta.setDescripcion(datos.getDescripcion());
        puntoVenta.setTipoEmision(datos.getTipoEmision());
        puntoVenta.setDomicilioComercial(datos.getDomicilioComercial());

        auditoriaService.registrarAlta(puntoVenta);

        PuntoVenta guardado = puntoVentaRepository.save(puntoVenta);
        return crearPuntoVentaDTO(guardado);
    }

    @Transactional
    public PuntoVentaDTO actualizar(Long id, PuntoVentaRequestDTO datos) {
        PuntoVenta puntoVenta = puntoVentaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Punto de venta no encontrado: " + id));

        if (puntoVenta.getFechaBaja() != null) {
            throw new DatosInvalidosException("No se puede modificar un punto de venta dado de baja");
        }
        // El número es la clave de negocio: no se modifica
        if (datos.getNumero() != null && datos.getNumero() != puntoVenta.getNumero()) {
            throw new DatosInvalidosException("El número de un punto de venta no se puede modificar");
        }
        if (datos.getDescripcion() == null || datos.getDescripcion().isBlank()) {
            throw new DatosInvalidosException("La descripción es obligatoria");
        }

        puntoVenta.setDescripcion(datos.getDescripcion());
        puntoVenta.setTipoEmision(datos.getTipoEmision());
        puntoVenta.setDomicilioComercial(datos.getDomicilioComercial());

        auditoriaService.registrarModificacion(puntoVenta);

        return crearPuntoVentaDTO(puntoVenta);
    }

    @Transactional
    public void darDeBaja(Long id) {
        PuntoVenta puntoVenta = puntoVentaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Punto de venta no encontrado: " + id));

        if (puntoVenta.getFechaBaja() != null) {
            throw new DatosInvalidosException("El punto de venta ya estaba dado de baja");
        }

        auditoriaService.registrarBaja(puntoVenta);
    }

    @Transactional
    public PuntoVentaDTO reactivar(Long id) {
        PuntoVenta puntoVenta = puntoVentaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Punto de venta no encontrado: " + id));

        if (puntoVenta.getFechaBaja() == null) {
            throw new DatosInvalidosException("El punto de venta ya está activo");
        }

        auditoriaService.quitarBaja(puntoVenta);

        return crearPuntoVentaDTO(puntoVenta);
    }
}
