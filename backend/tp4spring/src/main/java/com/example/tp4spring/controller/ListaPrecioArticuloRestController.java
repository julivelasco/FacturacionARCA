package com.example.tp4spring.controller;

import com.example.tp4spring.dto.ListaPrecioArticuloDTO;
import com.example.tp4spring.dto.ListaPrecioArticuloRequestDTO;
import com.example.tp4spring.service.ListaPrecioArticuloService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/precios")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ListaPrecioArticuloRestController {

    private final ListaPrecioArticuloService listaPrecioArticuloService;

    @GetMapping
    public Page<ListaPrecioArticuloDTO> buscar(
            @RequestParam(required = false) String texto,
            @RequestParam(defaultValue = "false") boolean incluirBajas,
            @RequestParam(required = false) Long listaPrecioId,
            @RequestParam(required = false) String orden,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanio) {
        return listaPrecioArticuloService.buscar(texto, incluirBajas, listaPrecioId, orden, pagina, tamanio);
    }

    @GetMapping("/{id}")
    public ListaPrecioArticuloDTO obtener(@PathVariable Long id) {
        return listaPrecioArticuloService.obtener(id);
    }

    @PostMapping
    public ResponseEntity<ListaPrecioArticuloDTO> crear(@RequestBody ListaPrecioArticuloRequestDTO datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(listaPrecioArticuloService.crear(datos));
    }

    @PutMapping("/{id}")
    public ListaPrecioArticuloDTO actualizar(@PathVariable Long id, @RequestBody ListaPrecioArticuloRequestDTO datos) {
        return listaPrecioArticuloService.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> darDeBaja(@PathVariable Long id) {
        listaPrecioArticuloService.darDeBaja(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/reactivar")
    public ListaPrecioArticuloDTO reactivar(@PathVariable Long id) {
        return listaPrecioArticuloService.reactivar(id);
    }
}