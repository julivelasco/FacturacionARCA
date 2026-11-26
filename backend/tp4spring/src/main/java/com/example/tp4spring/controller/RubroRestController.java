package com.example.tp4spring.controller;

import com.example.tp4spring.dto.RubroDTO;
import com.example.tp4spring.dto.RubroRequestDTO;
import com.example.tp4spring.service.RubroService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rubros")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RubroRestController {

    private final RubroService rubroService;

    @GetMapping
    public Page<RubroDTO> buscar(
            @RequestParam(required = false) String texto,
            @RequestParam(defaultValue = "false") boolean incluirBajas,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanio) {
        return rubroService.buscar(texto, incluirBajas, pagina, tamanio);
    }

    @GetMapping("/{id}")
    public RubroDTO obtener(@PathVariable Long id) {
        return rubroService.obtener(id);
    }

    @PostMapping
    public ResponseEntity<RubroDTO> crear(@RequestBody RubroRequestDTO datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rubroService.crear(datos));
    }

    @PutMapping("/{id}")
    public RubroDTO actualizar(@PathVariable Long id, @RequestBody RubroRequestDTO datos) {
        return rubroService.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> darDeBaja(@PathVariable Long id) {
        rubroService.darDeBaja(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/reactivar")
    public RubroDTO reactivar(@PathVariable Long id) {
        return rubroService.reactivar(id);
    }
}