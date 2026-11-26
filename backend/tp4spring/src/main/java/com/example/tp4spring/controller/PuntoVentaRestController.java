package com.example.tp4spring.controller;

import com.example.tp4spring.dto.PuntoVentaDTO;
import com.example.tp4spring.dto.PuntoVentaRequestDTO;
import com.example.tp4spring.service.PuntoVentaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/puntos-venta")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PuntoVentaRestController {

    private final PuntoVentaService puntoVentaService;

    @GetMapping
    public Page<PuntoVentaDTO> buscar(
            @RequestParam(required = false) String texto,
            @RequestParam(defaultValue = "false") boolean incluirBajas,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanio) {
        return puntoVentaService.buscar(texto, incluirBajas, pagina, tamanio);
    }

    @GetMapping("/{id}")
    public PuntoVentaDTO obtener(@PathVariable Long id) {
        return puntoVentaService.obtener(id);
    }

    @PostMapping
    public ResponseEntity<PuntoVentaDTO> crear(@RequestBody PuntoVentaRequestDTO datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(puntoVentaService.crear(datos));
    }

    @PutMapping("/{id}")
    public PuntoVentaDTO actualizar(@PathVariable Long id, @RequestBody PuntoVentaRequestDTO datos) {
        return puntoVentaService.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> darDeBaja(@PathVariable Long id) {
        puntoVentaService.darDeBaja(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/reactivar")
    public PuntoVentaDTO reactivar(@PathVariable Long id) {
        return puntoVentaService.reactivar(id);
    }
}
