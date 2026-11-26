package com.example.tp4spring.controller;

import com.example.tp4spring.dto.ArticuloDTO;
import com.example.tp4spring.dto.ArticuloRequestDTO;
import com.example.tp4spring.service.ArticuloService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/articulos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ArticuloRestController {

    private final ArticuloService articuloService;

    @GetMapping
    public Page<ArticuloDTO> buscar(
            @RequestParam(required = false) String texto,
            @RequestParam(defaultValue = "false") boolean incluirBajas,
            @RequestParam(required = false) String orden,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanio) {
        return articuloService.buscar(texto, incluirBajas, orden, pagina, tamanio);
    }

    @GetMapping("/{id}")
    public ArticuloDTO obtener(@PathVariable Long id) {
        return articuloService.obtener(id);
    }

    @PostMapping
    public ResponseEntity<ArticuloDTO> crear(@RequestBody ArticuloRequestDTO datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(articuloService.crear(datos));
    }

    @PutMapping("/{id}")
    public ArticuloDTO actualizar(@PathVariable Long id, @RequestBody ArticuloRequestDTO datos) {
        return articuloService.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> darDeBaja(@PathVariable Long id) {
        articuloService.darDeBaja(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/reactivar")
    public ArticuloDTO reactivar(@PathVariable Long id) {
        return articuloService.reactivar(id);
    }
}