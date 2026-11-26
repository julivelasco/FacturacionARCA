package com.example.tp4spring.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class ListaPrecioArticuloRequestDTO {
    private Long listaPrecioId;
    private Long articuloId;
    private Double precioVenta;
}