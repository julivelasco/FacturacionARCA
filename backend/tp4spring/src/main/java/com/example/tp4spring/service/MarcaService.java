package com.example.tp4spring.service;

import com.example.tp4spring.dto.MarcaDTO;
import com.example.tp4spring.dto.MarcaRequestDTO;
import com.example.tp4spring.entities.Marca;
import com.example.tp4spring.exception.DatosInvalidosException;
import com.example.tp4spring.exception.RecursoNoEncontradoException;
import com.example.tp4spring.repository.MarcaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class MarcaService {

    private final MarcaRepository marcaRepository;
    private final AuditoriaService auditoriaService;
    private final CodigoService codigoService;

    private MarcaDTO crearMarcaDTO(Marca marca) {
        MarcaDTO dto = new MarcaDTO();
        dto.setId(marca.getId());
        dto.setCodigo(marca.getCodigo());
        dto.setDenominacion(marca.getDenominacion());
        dto.setActivo(marca.getFechaBaja() == null);
        return dto;
    }

    public MarcaDTO obtener(Long id) {
        Marca marca = marcaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Marca no encontrada: " + id));
        return crearMarcaDTO(marca);
    }

    public Page<MarcaDTO> buscar(String texto, boolean incluirBajas, int pagina, int tamanio) {
        if (texto == null) {
            texto = "";
        }
        Pageable pageable = PageRequest.of(pagina, tamanio, Sort.by("denominacion"));
        Page<Marca> paginaMarcas = marcaRepository.buscar(texto, incluirBajas, pageable);
        return paginaMarcas.map(this::crearMarcaDTO);
    }

    @Transactional
    public MarcaDTO crear(MarcaRequestDTO datos) {
        validarDenominacion(datos);

        Marca marca = new Marca();
        marca.setCodigo(codigoService.siguiente(Marca.class, "M", 2));   // el código lo pone el sistema
        marca.setDenominacion(datos.getDenominacion());

        auditoriaService.registrarAlta(marca);

        Marca guardada = marcaRepository.save(marca);
        return crearMarcaDTO(guardada);
    }

    @Transactional
    public MarcaDTO actualizar(Long id, MarcaRequestDTO datos) {
        Marca marca = marcaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Marca no encontrada: " + id));

        if (marca.getFechaBaja() != null) {
            throw new DatosInvalidosException("No se puede modificar una marca dada de baja");
        }
        validarDenominacion(datos);
        // El código es la clave de negocio: no se modifica
        if (datos.getCodigo() != null && !datos.getCodigo().equals(marca.getCodigo())) {
            throw new DatosInvalidosException("El código de una marca no se puede modificar");
        }

        marca.setDenominacion(datos.getDenominacion());

        auditoriaService.registrarModificacion(marca);

        return crearMarcaDTO(marca);
    }

    @Transactional
    public void darDeBaja(Long id) {
        Marca marca = marcaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Marca no encontrada: " + id));

        if (marca.getFechaBaja() != null) {
            throw new DatosInvalidosException("La marca ya estaba dada de baja");
        }

        auditoriaService.registrarBaja(marca);
    }

    @Transactional
    public MarcaDTO reactivar(Long id) {
        Marca marca = marcaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Marca no encontrada: " + id));

        if (marca.getFechaBaja() == null) {
            throw new DatosInvalidosException("La marca ya está activa");
        }

        auditoriaService.quitarBaja(marca);

        return crearMarcaDTO(marca);
    }

    private void validarDenominacion(MarcaRequestDTO datos) {
        if (datos.getDenominacion() == null || datos.getDenominacion().isBlank()) {
            throw new DatosInvalidosException("La denominación es obligatoria");
        }
    }
}