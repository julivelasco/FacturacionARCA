package com.example.tp4spring.service;

import com.example.tp4spring.dto.TipoMonedaDTO;
import com.example.tp4spring.dto.TipoMonedaRequestDTO;
import com.example.tp4spring.entities.TipoMoneda;
import com.example.tp4spring.exception.DatosInvalidosException;
import com.example.tp4spring.exception.RecursoDadoDeBajaException;
import com.example.tp4spring.exception.RecursoNoEncontradoException;
import com.example.tp4spring.repository.TipoMonedaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service @RequiredArgsConstructor
public class TipoMonedaService {

    private final TipoMonedaRepository tipoMonedaRepository;
    private final AuditoriaService auditoriaService;

    private TipoMonedaDTO crearTipoMonedaDTO(TipoMoneda tipoMoneda) {
        TipoMonedaDTO dto = new TipoMonedaDTO();
        dto.setId(tipoMoneda.getId());
        dto.setCodigoAfip(tipoMoneda.getCodigoAfip());
        dto.setDenominacion(tipoMoneda.getDenominacion());
        dto.setSimbolo(tipoMoneda.getSimbolo());
        dto.setActivo(tipoMoneda.getFechaBaja() == null);
        return dto;
    }

    public TipoMonedaDTO obtener(Long id) {
        TipoMoneda tipoMoneda = tipoMonedaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Tipo de moneda no encontrado: " + id));
        return crearTipoMonedaDTO(tipoMoneda);
    }

    public Page<TipoMonedaDTO> buscar(String texto, boolean incluirBajas, int pagina, int tamanio) {
        if (texto == null) {
            texto = "";
        }
        Pageable pageable = PageRequest.of(pagina, tamanio, Sort.by("denominacion"));
        Page<TipoMoneda> paginaTiposMoneda = tipoMonedaRepository.buscar(texto, incluirBajas, pageable);
        return paginaTiposMoneda.map(this::crearTipoMonedaDTO);
    }

    @Transactional
    public TipoMonedaDTO crear(TipoMonedaRequestDTO datos) {
        if (datos.getCodigoAfip() == null || datos.getCodigoAfip().isBlank()) {
            throw new DatosInvalidosException("El código AFIP es obligatorio");
        }
        validarDenominacionYSimbolo(datos);

        Optional<TipoMoneda> existente = tipoMonedaRepository.findByCodigoAfip(datos.getCodigoAfip());
        if (existente.isPresent()) {
            TipoMoneda tipoMonedaExistente = existente.get();
            if (tipoMonedaExistente.getFechaBaja() != null) {
                throw new RecursoDadoDeBajaException(
                        "El tipo de moneda " + tipoMonedaExistente.getCodigoAfip() + " está dado de baja",
                        tipoMonedaExistente.getId());
            }
            throw new DatosInvalidosException("Ya existe un tipo de moneda con código AFIP " + datos.getCodigoAfip());
        }

        TipoMoneda tipoMoneda = new TipoMoneda();
        tipoMoneda.setCodigoAfip(datos.getCodigoAfip());
        tipoMoneda.setDenominacion(datos.getDenominacion());
        tipoMoneda.setSimbolo(datos.getSimbolo());

        auditoriaService.registrarAlta(tipoMoneda);

        TipoMoneda guardado = tipoMonedaRepository.save(tipoMoneda);
        return crearTipoMonedaDTO(guardado);
    }

    @Transactional
    public TipoMonedaDTO actualizar(Long id, TipoMonedaRequestDTO datos) {
        TipoMoneda tipoMoneda = tipoMonedaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Tipo de moneda no encontrado: " + id));

        if (tipoMoneda.getFechaBaja() != null) {
            throw new DatosInvalidosException("No se puede modificar un tipo de moneda dado de baja");
        }
        validarDenominacionYSimbolo(datos);
        // El código AFIP es la clave de negocio: no se modifica
        if (datos.getCodigoAfip() != null && !datos.getCodigoAfip().equals(tipoMoneda.getCodigoAfip())) {
            throw new DatosInvalidosException("El código AFIP de un tipo de moneda no se puede modificar");
        }

        tipoMoneda.setDenominacion(datos.getDenominacion());
        tipoMoneda.setSimbolo(datos.getSimbolo());

        auditoriaService.registrarModificacion(tipoMoneda);

        return crearTipoMonedaDTO(tipoMoneda);
    }

    @Transactional
    public void darDeBaja(Long id) {
        TipoMoneda tipoMoneda = tipoMonedaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Tipo de moneda no encontrado: " + id));

        if (tipoMoneda.getFechaBaja() != null) {
            throw new DatosInvalidosException("El tipo de moneda ya estaba dado de baja");
        }

        auditoriaService.registrarBaja(tipoMoneda);
    }

    @Transactional
    public TipoMonedaDTO reactivar(Long id) {
        TipoMoneda tipoMoneda = tipoMonedaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Tipo de moneda no encontrado: " + id));

        if (tipoMoneda.getFechaBaja() == null) {
            throw new DatosInvalidosException("El tipo de moneda ya está activo");
        }

        auditoriaService.quitarBaja(tipoMoneda);

        return crearTipoMonedaDTO(tipoMoneda);
    }

    private void validarDenominacionYSimbolo(TipoMonedaRequestDTO datos) {
        if (datos.getDenominacion() == null || datos.getDenominacion().isBlank()) {
            throw new DatosInvalidosException("La denominación es obligatoria");
        }
        if (datos.getSimbolo() == null || datos.getSimbolo().isBlank()) {
            throw new DatosInvalidosException("El símbolo es obligatorio");
        }
    }
}