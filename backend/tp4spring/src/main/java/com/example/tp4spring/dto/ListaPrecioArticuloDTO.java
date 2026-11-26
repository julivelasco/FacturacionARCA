package com.example.tp4spring.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class ListaPrecioArticuloDTO {
    private Long id;
    private Long listaPrecioId;
    private String listaPrecioDenominacion;
    private Long articuloId;
    private String articuloCodigo;
    private String articuloDenominacion;
    private double precioVenta;
    private boolean activo;
    private boolean articuloFraccionable;
}