package com.example.tp4spring.service;

import com.example.tp4spring.dto.ListaPrecioDTO;
import com.example.tp4spring.dto.ListaPrecioRequestDTO;
import com.example.tp4spring.entities.ListaPrecio;
import com.example.tp4spring.exception.DatosInvalidosException;
import com.example.tp4spring.exception.RecursoNoEncontradoException;
import com.example.tp4spring.repository.ListaPrecioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class ListaPrecioService {

    private final ListaPrecioRepository listaPrecioRepository;
    private final AuditoriaService auditoriaService;
    private final CodigoService codigoService;

    private ListaPrecioDTO crearListaPrecioDTO(ListaPrecio listaPrecio) {
        ListaPrecioDTO dto = new ListaPrecioDTO();
        dto.setId(listaPrecio.getId());
        dto.setCodigo(listaPrecio.getCodigo());
        dto.setDenominacion(listaPrecio.getDenominacion());
        dto.setActivo(listaPrecio.getFechaBaja() == null);
        return dto;
    }

    public ListaPrecioDTO obtener(Long id) {
        ListaPrecio listaPrecio = listaPrecioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Lista de precios no encontrada: " + id));
        return crearListaPrecioDTO(listaPrecio);
    }

    public Page<ListaPrecioDTO> buscar(String texto, boolean incluirBajas, int pagina, int tamanio) {
        if (texto == null) {
            texto = "";
        }
        Pageable pageable = PageRequest.of(pagina, tamanio, Sort.by("denominacion"));
        Page<ListaPrecio> paginaListas = listaPrecioRepository.buscar(texto, incluirBajas, pageable);
        return paginaListas.map(this::crearListaPrecioDTO);
    }

    @Transactional
    public ListaPrecioDTO crear(ListaPrecioRequestDTO datos) {
        validarDenominacion(datos);

        ListaPrecio listaPrecio = new ListaPrecio();
        listaPrecio.setCodigo(codigoService.siguiente(ListaPrecio.class, "L", 2));   // el código lo pone el sistema
        listaPrecio.setDenominacion(datos.getDenominacion());

        auditoriaService.registrarAlta(listaPrecio);

        ListaPrecio guardada = listaPrecioRepository.save(listaPrecio);
        return crearListaPrecioDTO(guardada);
    }

    @Transactional
    public ListaPrecioDTO actualizar(Long id, ListaPrecioRequestDTO datos) {
        ListaPrecio listaPrecio = listaPrecioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Lista de precios no encontrada: " + id));

        if (listaPrecio.getFechaBaja() != null) {
            throw new DatosInvalidosException("No se puede modificar una lista de precios dada de baja");
        }
        validarDenominacion(datos);
        // El código es la clave de negocio: no se modifica
        if (datos.getCodigo() != null && !datos.getCodigo().equals(listaPrecio.getCodigo())) {
            throw new DatosInvalidosException("El código de una lista de precios no se puede modificar");
        }

        listaPrecio.setDenominacion(datos.getDenominacion());

        auditoriaService.registrarModificacion(listaPrecio);

        return crearListaPrecioDTO(listaPrecio);
    }

    @Transactional
    public void darDeBaja(Long id) {
        ListaPrecio listaPrecio = listaPrecioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Lista de precios no encontrada: " + id));

        if (listaPrecio.getFechaBaja() != null) {
            throw new DatosInvalidosException("La lista de precios ya estaba dada de baja");
        }

        auditoriaService.registrarBaja(listaPrecio);
    }

    @Transactional
    public ListaPrecioDTO reactivar(Long id) {
        ListaPrecio listaPrecio = listaPrecioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Lista de precios no encontrada: " + id));

        if (listaPrecio.getFechaBaja() == null) {
            throw new DatosInvalidosException("La lista de precios ya está activa");
        }

        auditoriaService.quitarBaja(listaPrecio);

        return crearListaPrecioDTO(listaPrecio);
    }

    private void validarDenominacion(ListaPrecioRequestDTO datos) {
        if (datos.getDenominacion() == null || datos.getDenominacion().isBlank()) {
            throw new DatosInvalidosException("La denominación es obligatoria");
        }
    }
}