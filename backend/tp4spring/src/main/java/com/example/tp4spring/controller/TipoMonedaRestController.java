package com.example.tp4spring.controller;

import com.example.tp4spring.dto.TipoMonedaDTO;
import com.example.tp4spring.dto.TipoMonedaRequestDTO;
import com.example.tp4spring.service.TipoMonedaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tipos-moneda")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TipoMonedaRestController {

    private final TipoMonedaService tipoMonedaService;

    @GetMapping
    public Page<TipoMonedaDTO> buscar(
            @RequestParam(required = false) String texto,
            @RequestParam(defaultValue = "false") boolean incluirBajas,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanio) {
        return tipoMonedaService.buscar(texto, incluirBajas, pagina, tamanio);
    }

    @GetMapping("/{id}")
    public TipoMonedaDTO obtener(@PathVariable Long id) {
        return tipoMonedaService.obtener(id);
    }

    @PostMapping
    public ResponseEntity<TipoMonedaDTO> crear(@RequestBody TipoMonedaRequestDTO datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tipoMonedaService.crear(datos));
    }

    @PutMapping("/{id}")
    public TipoMonedaDTO actualizar(@PathVariable Long id, @RequestBody TipoMonedaRequestDTO datos) {
        return tipoMonedaService.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> darDeBaja(@PathVariable Long id) {
        tipoMonedaService.darDeBaja(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/reactivar")
    public TipoMonedaDTO reactivar(@PathVariable Long id) {
        return tipoMonedaService.reactivar(id);
    }
}