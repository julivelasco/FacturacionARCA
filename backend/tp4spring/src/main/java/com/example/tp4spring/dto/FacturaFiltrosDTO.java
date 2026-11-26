package com.example.tp4spring.dto;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Getter @Setter @NoArgsConstructor @ToString
public class FacturaFiltrosDTO {
    private Long numero;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private Date fechaDesde;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private Date fechaHasta;
    private Long clienteId;
    private String estado;
    private Double montoMinimo;
    private Boolean informadaArca;
    private Boolean sinCliente;
}