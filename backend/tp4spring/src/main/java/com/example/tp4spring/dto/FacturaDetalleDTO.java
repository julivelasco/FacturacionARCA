package com.example.tp4spring.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class FacturaDetalleDTO {
    private Long id;
    private Long listaPrecioArticuloId;
    private String articuloCodigo;
    private String articuloDenominacion;
    private String listaPrecioDenominacion;
    private double cantidad;
    private double precioUnitario;
    private double importeNeto;
    private double importeIva;
    private double importeSubtotal;
    private boolean articuloFraccionable;
}