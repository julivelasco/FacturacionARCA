package com.example.tp4spring.service;

import com.example.tp4spring.entities.AuditoriaApp;
import com.example.tp4spring.entities.Usuario;
import com.example.tp4spring.exception.RecursoNoEncontradoException;
import com.example.tp4spring.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private final UsuarioRepository usuarioRepository;

    public void registrarAlta(AuditoriaApp entidad) {
        Date ahora = new Date();
        Usuario usuario = buscarUsuarioActual();
        entidad.setFechaAlta(ahora);
        entidad.setUsuarioCarga(usuario);
        entidad.setFechaModificacion(ahora);
        entidad.setUsuarioModificacion(usuario);
    }

    public void registrarModificacion(AuditoriaApp entidad) {
        entidad.setFechaModificacion(new Date());
        entidad.setUsuarioModificacion(buscarUsuarioActual());
    }

    public void registrarBaja(AuditoriaApp entidad) {
        Date ahora = new Date();
        Usuario usuario = buscarUsuarioActual();
        entidad.setFechaBaja(ahora);
        entidad.setUsuarioBaja(usuario);
        entidad.setFechaModificacion(ahora);
        entidad.setUsuarioModificacion(usuario);
    }

    public void quitarBaja(AuditoriaApp entidad) {
        entidad.setFechaBaja(null);
        entidad.setUsuarioBaja(null);
        registrarModificacion(entidad);
    }

    // No hay login: el usuario actual es siempre el admin.
    private Usuario buscarUsuarioActual() {
        return usuarioRepository.findByUsuario("admin")
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el usuario admin"));
    }
}