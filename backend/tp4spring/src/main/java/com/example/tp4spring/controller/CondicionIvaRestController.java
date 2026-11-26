package com.example.tp4spring.controller;

import com.example.tp4spring.dto.CondicionIvaDTO;
import com.example.tp4spring.dto.CondicionIvaRequestDTO;
import com.example.tp4spring.service.CondicionIvaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/condiciones-iva")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CondicionIvaRestController {

    private final CondicionIvaService condicionIvaService;

    @GetMapping
    public Page<CondicionIvaDTO> buscar(
            @RequestParam(required = false) String texto,
            @RequestParam(defaultValue = "false") boolean incluirBajas,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanio) {
        return condicionIvaService.buscar(texto, incluirBajas, pagina, tamanio);
    }

    @GetMapping("/{id}")
    public CondicionIvaDTO obtener(@PathVariable Long id) {
        return condicionIvaService.obtener(id);
    }

    @PostMapping
    public ResponseEntity<CondicionIvaDTO> crear(@RequestBody CondicionIvaRequestDTO datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(condicionIvaService.crear(datos));
    }

    @PutMapping("/{id}")
    public CondicionIvaDTO actualizar(@PathVariable Long id, @RequestBody CondicionIvaRequestDTO datos) {
        return condicionIvaService.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> darDeBaja(@PathVariable Long id) {
        condicionIvaService.darDeBaja(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/reactivar")
    public CondicionIvaDTO reactivar(@PathVariable Long id) {
        return condicionIvaService.reactivar(id);
    }
}