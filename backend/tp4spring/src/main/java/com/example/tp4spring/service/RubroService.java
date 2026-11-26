package com.example.tp4spring.service;

import com.example.tp4spring.dto.RubroDTO;
import com.example.tp4spring.dto.RubroRequestDTO;
import com.example.tp4spring.entities.Rubro;
import com.example.tp4spring.exception.DatosInvalidosException;
import com.example.tp4spring.exception.RecursoNoEncontradoException;
import com.example.tp4spring.repository.RubroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class RubroService {

    private final RubroRepository rubroRepository;
    private final AuditoriaService auditoriaService;
    private final CodigoService codigoService;

    private RubroDTO crearRubroDTO(Rubro rubro) {
        RubroDTO dto = new RubroDTO();
        dto.setId(rubro.getId());
        dto.setCodigo(rubro.getCodigo());
        dto.setDenominacion(rubro.getDenominacion());
        dto.setActivo(rubro.getFechaBaja() == null);
        return dto;
    }

    public RubroDTO obtener(Long id) {
        Rubro rubro = rubroRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Rubro no encontrado: " + id));
        return crearRubroDTO(rubro);
    }

    public Page<RubroDTO> buscar(String texto, boolean incluirBajas, int pagina, int tamanio) {
        if (texto == null) {
            texto = "";
        }
        Pageable pageable = PageRequest.of(pagina, tamanio, Sort.by("denominacion"));
        Page<Rubro> paginaRubros = rubroRepository.buscar(texto, incluirBajas, pageable);
        return paginaRubros.map(this::crearRubroDTO);
    }

    @Transactional
    public RubroDTO crear(RubroRequestDTO datos) {
        validarDenominacion(datos);

        Rubro rubro = new Rubro();
        rubro.setCodigo(codigoService.siguiente(Rubro.class, "R", 2));   // el código lo pone el sistema
        rubro.setDenominacion(datos.getDenominacion());

        auditoriaService.registrarAlta(rubro);

        Rubro guardado = rubroRepository.save(rubro);
        return crearRubroDTO(guardado);
    }

    @Transactional
    public RubroDTO actualizar(Long id, RubroRequestDTO datos) {
        Rubro rubro = rubroRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Rubro no encontrado: " + id));

        if (rubro.getFechaBaja() != null) {
            throw new DatosInvalidosException("No se puede modificar un rubro dado de baja");
        }
        validarDenominacion(datos);
        // El código es la clave de negocio: no se modifica
        if (datos.getCodigo() != null && !datos.getCodigo().equals(rubro.getCodigo())) {
            throw new DatosInvalidosException("El código de un rubro no se puede modificar");
        }

        rubro.setDenominacion(datos.getDenominacion());

        auditoriaService.registrarModificacion(rubro);

        return crearRubroDTO(rubro);
    }

    @Transactional
    public void darDeBaja(Long id) {
        Rubro rubro = rubroRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Rubro no encontrado: " + id));

        if (rubro.getFechaBaja() != null) {
            throw new DatosInvalidosException("El rubro ya estaba dado de baja");
        }

        auditoriaService.registrarBaja(rubro);
    }

    @Transactional
    public RubroDTO reactivar(Long id) {
        Rubro rubro = rubroRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Rubro no encontrado: " + id));

        if (rubro.getFechaBaja() == null) {
            throw new DatosInvalidosException("El rubro ya está activo");
        }

        auditoriaService.quitarBaja(rubro);

        return crearRubroDTO(rubro);
    }

    private void validarDenominacion(RubroRequestDTO datos) {
        if (datos.getDenominacion() == null || datos.getDenominacion().isBlank()) {
            throw new DatosInvalidosException("La denominación es obligatoria");
        }
    }
}