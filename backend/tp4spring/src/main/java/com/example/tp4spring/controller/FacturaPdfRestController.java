package com.example.tp4spring.controller;

import com.example.tp4spring.service.FacturaPdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/facturas")
@CrossOrigin
@RequiredArgsConstructor
public class FacturaPdfRestController {

    private final FacturaPdfService facturaPdfService;

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable Long id) throws Exception {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(facturaPdfService.generar(id));
    }
}