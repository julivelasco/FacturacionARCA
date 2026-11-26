package com.example.tp4spring.service;

import com.example.tp4spring.dto.ArticuloDTO;
import com.example.tp4spring.dto.ArticuloRequestDTO;
import com.example.tp4spring.entities.Articulo;
import com.example.tp4spring.entities.Marca;
import com.example.tp4spring.entities.Rubro;
import com.example.tp4spring.exception.DatosInvalidosException;
import com.example.tp4spring.exception.RecursoNoEncontradoException;
import com.example.tp4spring.repository.ArticuloRepository;
import com.example.tp4spring.repository.MarcaRepository;
import com.example.tp4spring.repository.RubroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class ArticuloService {

    private final ArticuloRepository articuloRepository;
    private final RubroRepository rubroRepository;
    private final MarcaRepository marcaRepository;
    private final AuditoriaService auditoriaService;
    private final CodigoService codigoService;

    private ArticuloDTO crearArticuloDTO(Articulo articulo) {
        ArticuloDTO dto = new ArticuloDTO();
        dto.setId(articulo.getId());
        dto.setCodigo(articulo.getCodigo());
        dto.setDenominacion(articulo.getDenominacion());
        if (articulo.getRubro() != null) {
            dto.setRubroId(articulo.getRubro().getId());
            dto.setRubroDenominacion(articulo.getRubro().getDenominacion());
        }
        if (articulo.getMarca() != null) {
            dto.setMarcaId(articulo.getMarca().getId());
            dto.setMarcaDenominacion(articulo.getMarca().getDenominacion());
        }
        dto.setActivo(articulo.getFechaBaja() == null);
        dto.setFraccionable(articulo.isFraccionable());
        return dto;
    }

    public ArticuloDTO obtener(Long id) {
        Articulo articulo = articuloRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Artículo no encontrado: " + id));
        return crearArticuloDTO(articulo);
    }

    public Page<ArticuloDTO> buscar(String texto, boolean incluirBajas, String orden, int pagina, int tamanio) {
        if (texto == null) {
            texto = "";
        }
        Pageable sinOrden = PageRequest.of(pagina, tamanio);   // para las consultas que ya traen su ORDER BY
        Page<Articulo> paginaArticulos = switch (orden == null ? "" : orden) {
            case "rubro" -> articuloRepository.buscarPorRubro(texto, incluirBajas, sinOrden);
            case "marca" -> articuloRepository.buscarPorMarca(texto, incluirBajas, sinOrden);
            case "codigo" -> articuloRepository.buscar(texto, incluirBajas, PageRequest.of(pagina, tamanio, Sort.by("codigo")));
            default -> articuloRepository.buscar(texto, incluirBajas, PageRequest.of(pagina, tamanio, Sort.by("denominacion")));
        };
        return paginaArticulos.map(this::crearArticuloDTO);
    }

    @Transactional
    public ArticuloDTO crear(ArticuloRequestDTO datos) {
        validarDenominacion(datos);

        Articulo articulo = new Articulo();
        articulo.setCodigo(codigoService.siguiente(Articulo.class, "A", 3));   // el código lo pone el sistema
        articulo.setDenominacion(datos.getDenominacion());
        articulo.setRubro(buscarRubroActivo(datos.getRubroId()));
        articulo.setMarca(buscarMarcaActiva(datos.getMarcaId()));
        articulo.setFraccionable(Boolean.TRUE.equals(datos.getFraccionable()));

        auditoriaService.registrarAlta(articulo);

        Articulo guardado = articuloRepository.save(articulo);
        return crearArticuloDTO(guardado);
    }

    @Transactional
    public ArticuloDTO actualizar(Long id, ArticuloRequestDTO datos) {
        Articulo articulo = articuloRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Artículo no encontrado: " + id));

        if (articulo.getFechaBaja() != null) {
            throw new DatosInvalidosException("No se puede modificar un artículo dado de baja");
        }
        validarDenominacion(datos);
        // El código es la clave de negocio: no se modifica
        if (datos.getCodigo() != null && !datos.getCodigo().equals(articulo.getCodigo())) {
            throw new DatosInvalidosException("El código de un artículo no se puede modificar");
        }

        articulo.setDenominacion(datos.getDenominacion());
        articulo.setRubro(buscarRubroActivo(datos.getRubroId()));
        articulo.setMarca(buscarMarcaActiva(datos.getMarcaId()));
        articulo.setFraccionable(Boolean.TRUE.equals(datos.getFraccionable()));

        auditoriaService.registrarModificacion(articulo);

        return crearArticuloDTO(articulo);
    }

    @Transactional
    public void darDeBaja(Long id) {
        Articulo articulo = articuloRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Artículo no encontrado: " + id));

        if (articulo.getFechaBaja() != null) {
            throw new DatosInvalidosException("El artículo ya estaba dado de baja");
        }

        auditoriaService.registrarBaja(articulo);
    }

    @Transactional
    public ArticuloDTO reactivar(Long id) {
        Articulo articulo = articuloRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Artículo no encontrado: " + id));

        if (articulo.getFechaBaja() == null) {
            throw new DatosInvalidosException("El artículo ya está activo");
        }

        auditoriaService.quitarBaja(articulo);

        return crearArticuloDTO(articulo);
    }

    // Si no mandaron rubro, devuelve null (el artículo queda sin rubro)
    private Rubro buscarRubroActivo(Long rubroId) {
        if (rubroId == null) {
            return null;
        }
        Rubro rubro = rubroRepository.findById(rubroId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Rubro no encontrado: " + rubroId));
        if (rubro.getFechaBaja() != null) {
            throw new DatosInvalidosException("El rubro " + rubro.getDenominacion() + " está dado de baja");
        }
        return rubro;
    }

    // Si no mandaron marca, devuelve null (el artículo queda sin marca)
    private Marca buscarMarcaActiva(Long marcaId) {
        if (marcaId == null) {
            return null;
        }
        Marca marca = marcaRepository.findById(marcaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Marca no encontrada: " + marcaId));
        if (marca.getFechaBaja() != null) {
            throw new DatosInvalidosException("La marca " + marca.getDenominacion() + " está dada de baja");
        }
        return marca;
    }

    private void validarDenominacion(ArticuloRequestDTO datos) {
        if (datos.getDenominacion() == null || datos.getDenominacion().isBlank()) {
            throw new DatosInvalidosException("La denominación es obligatoria");
        }
    }
}