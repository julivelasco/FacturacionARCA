package com.example.tp4spring.controller;

import com.example.tp4spring.dto.ListaPrecioDTO;
import com.example.tp4spring.dto.ListaPrecioRequestDTO;
import com.example.tp4spring.service.ListaPrecioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/listas-precio")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ListaPrecioRestController {

    private final ListaPrecioService listaPrecioService;

    @GetMapping
    public Page<ListaPrecioDTO> buscar(
            @RequestParam(required = false) String texto,
            @RequestParam(defaultValue = "false") boolean incluirBajas,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanio) {
        return listaPrecioService.buscar(texto, incluirBajas, pagina, tamanio);
    }

    @GetMapping("/{id}")
    public ListaPrecioDTO obtener(@PathVariable Long id) {
        return listaPrecioService.obtener(id);
    }

    @PostMapping
    public ResponseEntity<ListaPrecioDTO> crear(@RequestBody ListaPrecioRequestDTO datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(listaPrecioService.crear(datos));
    }

    @PutMapping("/{id}")
    public ListaPrecioDTO actualizar(@PathVariable Long id, @RequestBody ListaPrecioRequestDTO datos) {
        return listaPrecioService.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> darDeBaja(@PathVariable Long id) {
        listaPrecioService.darDeBaja(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/reactivar")
    public ListaPrecioDTO reactivar(@PathVariable Long id) {
        return listaPrecioService.reactivar(id);
    }
}