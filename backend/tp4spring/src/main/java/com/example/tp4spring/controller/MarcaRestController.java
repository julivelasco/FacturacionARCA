package com.example.tp4spring.controller;

import com.example.tp4spring.dto.MarcaDTO;
import com.example.tp4spring.dto.MarcaRequestDTO;
import com.example.tp4spring.service.MarcaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/marcas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MarcaRestController {

    private final MarcaService marcaService;

    @GetMapping
    public Page<MarcaDTO> buscar(
            @RequestParam(required = false) String texto,
            @RequestParam(defaultValue = "false") boolean incluirBajas,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanio) {
        return marcaService.buscar(texto, incluirBajas, pagina, tamanio);
    }

    @GetMapping("/{id}")
    public MarcaDTO obtener(@PathVariable Long id) {
        return marcaService.obtener(id);
    }

    @PostMapping
    public ResponseEntity<MarcaDTO> crear(@RequestBody MarcaRequestDTO datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(marcaService.crear(datos));
    }

    @PutMapping("/{id}")
    public MarcaDTO actualizar(@PathVariable Long id, @RequestBody MarcaRequestDTO datos) {
        return marcaService.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> darDeBaja(@PathVariable Long id) {
        marcaService.darDeBaja(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/reactivar")
    public MarcaDTO reactivar(@PathVariable Long id) {
        return marcaService.reactivar(id);
    }
}