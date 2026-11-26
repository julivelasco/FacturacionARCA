package com.example.tp4spring.service;

import com.example.tp4spring.dto.CondicionIvaDTO;
import com.example.tp4spring.dto.CondicionIvaRequestDTO;
import com.example.tp4spring.entities.CondicionIva;
import com.example.tp4spring.exception.DatosInvalidosException;
import com.example.tp4spring.exception.RecursoDadoDeBajaException;
import com.example.tp4spring.exception.RecursoNoEncontradoException;
import com.example.tp4spring.repository.CondicionIvaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service @RequiredArgsConstructor
public class CondicionIvaService {

    private final CondicionIvaRepository condicionIvaRepository;
    private final AuditoriaService auditoriaService;

    private CondicionIvaDTO crearCondicionIvaDTO(CondicionIva condicionIva) {
        CondicionIvaDTO dto = new CondicionIvaDTO();
        dto.setId(condicionIva.getId());
        dto.setCodigoAfip(condicionIva.getCodigoAfip());
        dto.setDenominacion(condicionIva.getDenominacion());
        dto.setActivo(condicionIva.getFechaBaja() == null);
        return dto;
    }

    public CondicionIvaDTO obtener(Long id) {
        CondicionIva condicionIva = condicionIvaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Condición IVA no encontrada: " + id));
        return crearCondicionIvaDTO(condicionIva);
    }

    public Page<CondicionIvaDTO> buscar(String texto, boolean incluirBajas, int pagina, int tamanio) {
        if (texto == null) {
            texto = "";
        }
        Pageable pageable = PageRequest.of(pagina, tamanio, Sort.by("denominacion"));
        Page<CondicionIva> paginaCondiciones = condicionIvaRepository.buscar(texto, incluirBajas, pageable);
        return paginaCondiciones.map(this::crearCondicionIvaDTO);
    }

    @Transactional
    public CondicionIvaDTO crear(CondicionIvaRequestDTO datos) {
        if (datos.getCodigoAfip() == null || datos.getCodigoAfip() <= 0) {
            throw new DatosInvalidosException("El código AFIP es obligatorio y debe ser mayor a 0");
        }
        validarDenominacion(datos);

        Optional<CondicionIva> existente = condicionIvaRepository.findByCodigoAfip(datos.getCodigoAfip());
        if (existente.isPresent()) {
            CondicionIva condicionExistente = existente.get();
            if (condicionExistente.getFechaBaja() != null) {
                throw new RecursoDadoDeBajaException(
                        "La condición IVA con código " + condicionExistente.getCodigoAfip() + " está dada de baja",
                        condicionExistente.getId());
            }
            throw new DatosInvalidosException("Ya existe una condición IVA con código AFIP " + datos.getCodigoAfip());
        }

        CondicionIva condicionIva = new CondicionIva();
        condicionIva.setCodigoAfip(datos.getCodigoAfip());
        condicionIva.setDenominacion(datos.getDenominacion());

        auditoriaService.registrarAlta(condicionIva);

        CondicionIva guardada = condicionIvaRepository.save(condicionIva);
        return crearCondicionIvaDTO(guardada);
    }

    @Transactional
    public CondicionIvaDTO actualizar(Long id, CondicionIvaRequestDTO datos) {
        CondicionIva condicionIva = condicionIvaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Condición IVA no encontrada: " + id));

        if (condicionIva.getFechaBaja() != null) {
            throw new DatosInvalidosException("No se puede modificar una condición IVA dada de baja");
        }
        validarDenominacion(datos);
        // El código AFIP es la clave de negocio: no se modifica
        if (datos.getCodigoAfip() != null && datos.getCodigoAfip() != condicionIva.getCodigoAfip()) {
            throw new DatosInvalidosException("El código AFIP de una condición IVA no se puede modificar");
        }

        condicionIva.setDenominacion(datos.getDenominacion());

        auditoriaService.registrarModificacion(condicionIva);

        return crearCondicionIvaDTO(condicionIva);
    }

    @Transactional
    public void darDeBaja(Long id) {
        CondicionIva condicionIva = condicionIvaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Condición IVA no encontrada: " + id));

        if (condicionIva.getFechaBaja() != null) {
            throw new DatosInvalidosException("La condición IVA ya estaba dada de baja");
        }

        auditoriaService.registrarBaja(condicionIva);
    }

    @Transactional
    public CondicionIvaDTO reactivar(Long id) {
        CondicionIva condicionIva = condicionIvaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Condición IVA no encontrada: " + id));

        if (condicionIva.getFechaBaja() == null) {
            throw new DatosInvalidosException("La condición IVA ya está activa");
        }

        auditoriaService.quitarBaja(condicionIva);

        return crearCondicionIvaDTO(condicionIva);
    }

    private void validarDenominacion(CondicionIvaRequestDTO datos) {
        if (datos.getDenominacion() == null || datos.getDenominacion().isBlank()) {
            throw new DatosInvalidosException("La denominación es obligatoria");
        }
    }
}