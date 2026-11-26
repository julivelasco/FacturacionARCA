package com.example.tp4spring.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> manejarNoEncontrado (RecursoNoEncontradoException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("status", 404, "mensaje", e.getMessage()));
    }

    @ExceptionHandler(DatosInvalidosException.class)
    public ResponseEntity<Map<String, Object>> manejarDatoInvalido (DatosInvalidosException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("status", 400, "mensaje", e.getMessage()));
    }

    @ExceptionHandler(RecursoDadoDeBajaException.class)
    public ResponseEntity<Map<String, Object>> manejarRecursoDadoDeBaja(RecursoDadoDeBajaException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("status", 409, "mensaje", e.getMessage(), "id", e.getId()));
    }

    @ExceptionHandler(ArcaException.class)
    public ResponseEntity<Map<String, Object>> manejarArca(ArcaException e) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("status", 502, "mensaje", e.getMessage()));
    }
}
