package com.example.tp4spring.dto;

import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Getter;
@Setter
@NoArgsConstructor
@Getter

public class FacturaDetalleRequestDTO {
    private Long listaPrecioArticuloId;
    private double cantidad;
}
