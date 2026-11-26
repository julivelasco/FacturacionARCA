package com.example.tp4spring.controller;

import com.example.tp4spring.dto.FacturaDTO;
import com.example.tp4spring.dto.FacturaFiltrosDTO;
import com.example.tp4spring.dto.FacturaReporteDTO;
import com.example.tp4spring.dto.FacturaRequestDTO;
import com.example.tp4spring.service.FacturaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/facturas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FacturaRestController {

    private final FacturaService facturaService;

    @GetMapping
    public Page<FacturaReporteDTO> buscar(
            FacturaFiltrosDTO filtros,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanio) {
        return facturaService.buscarFacturasFiltradas(filtros, PageRequest.of(pagina, tamanio));
    }

    @GetMapping("/{id}")
    public FacturaDTO obtener(@PathVariable Long id) {
        return facturaService.obtener(id);
    }

    @PostMapping
    public ResponseEntity<FacturaReporteDTO> crear(@RequestBody FacturaRequestDTO facturaDatos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(facturaService.crear(facturaDatos));
    }

    @PostMapping("/{id}/informar-arca")
    public FacturaDTO informarArca(@PathVariable Long id) {
        return facturaService.informarArca(id);
    }

    @PutMapping("/{id}")
    public FacturaReporteDTO actualizar(@PathVariable Long id, @RequestBody FacturaRequestDTO facturaActualizaciones) {
        return facturaService.actualizar(id, facturaActualizaciones);
    }

    @PutMapping("/{id}/anular")
    public FacturaReporteDTO anular(@PathVariable Long id) {
        return facturaService.anular(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        facturaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}