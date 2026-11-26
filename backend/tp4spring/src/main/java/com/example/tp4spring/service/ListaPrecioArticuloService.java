package com.example.tp4spring.service;

import com.example.tp4spring.dto.ListaPrecioArticuloDTO;
import com.example.tp4spring.dto.ListaPrecioArticuloRequestDTO;
import com.example.tp4spring.entities.Articulo;
import com.example.tp4spring.entities.ListaPrecio;
import com.example.tp4spring.entities.ListaPrecioArticulo;
import com.example.tp4spring.exception.DatosInvalidosException;
import com.example.tp4spring.exception.RecursoDadoDeBajaException;
import com.example.tp4spring.exception.RecursoNoEncontradoException;
import com.example.tp4spring.repository.ArticuloRepository;
import com.example.tp4spring.repository.ListaPrecioArticuloRepository;
import com.example.tp4spring.repository.ListaPrecioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service @RequiredArgsConstructor
public class ListaPrecioArticuloService {

    private final ListaPrecioArticuloRepository listaPrecioArticuloRepository;
    private final ListaPrecioRepository listaPrecioRepository;
    private final ArticuloRepository articuloRepository;
    private final AuditoriaService auditoriaService;

    private ListaPrecioArticuloDTO crearListaPrecioArticuloDTO(ListaPrecioArticulo lpa) {
        ListaPrecioArticuloDTO dto = new ListaPrecioArticuloDTO();
        dto.setId(lpa.getId());
        dto.setListaPrecioId(lpa.getListaPrecio().getId());
        dto.setListaPrecioDenominacion(lpa.getListaPrecio().getDenominacion());
        dto.setArticuloId(lpa.getArticulo().getId());
        dto.setArticuloCodigo(lpa.getArticulo().getCodigo());
        dto.setArticuloDenominacion(lpa.getArticulo().getDenominacion());
        dto.setPrecioVenta(lpa.getPrecioVenta());
        dto.setArticuloFraccionable(lpa.getArticulo().isFraccionable());
        dto.setActivo(lpa.getFechaBaja() == null);
        return dto;
    }

    public ListaPrecioArticuloDTO obtener(Long id) {
        ListaPrecioArticulo lpa = listaPrecioArticuloRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Precio no encontrado: " + id));
        return crearListaPrecioArticuloDTO(lpa);
    }

    public Page<ListaPrecioArticuloDTO> buscar(String texto, boolean incluirBajas, Long listaPrecioId, String orden,
                                               int pagina, int tamanio) {
        if (texto == null) {
            texto = "";
        }
        Pageable pageable = PageRequest.of(pagina, tamanio, ordenPrecios(orden));
        Page<ListaPrecioArticulo> paginaPrecios = listaPrecioArticuloRepository.buscar(texto, incluirBajas, listaPrecioId, pageable);
        return paginaPrecios.map(this::crearListaPrecioArticuloDTO);
    }

    private Sort ordenPrecios(String orden) {
        Sort porArticulo = Sort.by("articulo.denominacion");
        if (orden == null) {
            return porArticulo;
        }
        return switch (orden) {
            case "articulo-desc" -> porArticulo.descending();
            case "precio-asc" -> Sort.by("precioVenta").and(porArticulo);
            case "precio-desc" -> Sort.by("precioVenta").descending().and(porArticulo);
            case "lista" -> Sort.by("listaPrecio.denominacion").and(porArticulo);
            default -> porArticulo;
        };
    }

    @Transactional
    public ListaPrecioArticuloDTO crear(ListaPrecioArticuloRequestDTO datos) {
        if (datos.getListaPrecioId() == null) {
            throw new DatosInvalidosException("La lista de precios es obligatoria");
        }
        if (datos.getArticuloId() == null) {
            throw new DatosInvalidosException("El artículo es obligatorio");
        }
        validarPrecio(datos);

        ListaPrecio listaPrecio = listaPrecioRepository.findById(datos.getListaPrecioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Lista de precios no encontrada: " + datos.getListaPrecioId()));
        if (listaPrecio.getFechaBaja() != null) {
            throw new DatosInvalidosException("La lista de precios " + listaPrecio.getDenominacion() + " está dada de baja");
        }

        Articulo articulo = articuloRepository.findById(datos.getArticuloId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Artículo no encontrado: " + datos.getArticuloId()));
        if (articulo.getFechaBaja() != null) {
            throw new DatosInvalidosException("El artículo " + articulo.getDenominacion() + " está dado de baja");
        }

        // Un artículo tiene un solo precio por lista
        Optional<ListaPrecioArticulo> existente =
                listaPrecioArticuloRepository.findByListaPrecioIdAndArticuloId(listaPrecio.getId(), articulo.getId());
        if (existente.isPresent()) {
            ListaPrecioArticulo lpaExistente = existente.get();
            if (lpaExistente.getFechaBaja() != null) {
                throw new RecursoDadoDeBajaException(
                        "El precio de " + articulo.getDenominacion() + " en la lista " + listaPrecio.getDenominacion() + " está dado de baja",
                        lpaExistente.getId());
            }
            throw new DatosInvalidosException(
                    "El artículo " + articulo.getDenominacion() + " ya tiene precio en la lista " + listaPrecio.getDenominacion());
        }

        ListaPrecioArticulo lpa = new ListaPrecioArticulo();
        lpa.setListaPrecio(listaPrecio);
        lpa.setArticulo(articulo);
        lpa.setPrecioVenta(datos.getPrecioVenta());

        auditoriaService.registrarAlta(lpa);

        ListaPrecioArticulo guardado = listaPrecioArticuloRepository.save(lpa);
        return crearListaPrecioArticuloDTO(guardado);
    }

    @Transactional
    public ListaPrecioArticuloDTO actualizar(Long id, ListaPrecioArticuloRequestDTO datos) {
        ListaPrecioArticulo lpa = listaPrecioArticuloRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Precio no encontrado: " + id));

        if (lpa.getFechaBaja() != null) {
            throw new DatosInvalidosException("No se puede modificar un precio dado de baja");
        }
        validarPrecio(datos);
        // La lista y el artículo son la clave de negocio: solo se modifica el precio
        if (datos.getListaPrecioId() != null && !datos.getListaPrecioId().equals(lpa.getListaPrecio().getId())) {
            throw new DatosInvalidosException("No se puede cambiar la lista de un precio. Dalo de baja y creá uno nuevo");
        }
        if (datos.getArticuloId() != null && !datos.getArticuloId().equals(lpa.getArticulo().getId())) {
            throw new DatosInvalidosException("No se puede cambiar el artículo de un precio. Dalo de baja y creá uno nuevo");
        }

        lpa.setPrecioVenta(datos.getPrecioVenta());

        auditoriaService.registrarModificacion(lpa);

        return crearListaPrecioArticuloDTO(lpa);
    }

    @Transactional
    public void darDeBaja(Long id) {
        ListaPrecioArticulo lpa = listaPrecioArticuloRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Precio no encontrado: " + id));

        if (lpa.getFechaBaja() != null) {
            throw new DatosInvalidosException("El precio ya estaba dado de baja");
        }

        auditoriaService.registrarBaja(lpa);
    }

    @Transactional
    public ListaPrecioArticuloDTO reactivar(Long id) {
        ListaPrecioArticulo lpa = listaPrecioArticuloRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Precio no encontrado: " + id));

        if (lpa.getFechaBaja() == null) {
            throw new DatosInvalidosException("El precio ya está activo");
        }

        auditoriaService.quitarBaja(lpa);

        return crearListaPrecioArticuloDTO(lpa);
    }

    private void validarPrecio(ListaPrecioArticuloRequestDTO datos) {
        if (datos.getPrecioVenta() == null || datos.getPrecioVenta() <= 0) {
            throw new DatosInvalidosException("El precio de venta es obligatorio y debe ser mayor a 0");
        }
    }
}