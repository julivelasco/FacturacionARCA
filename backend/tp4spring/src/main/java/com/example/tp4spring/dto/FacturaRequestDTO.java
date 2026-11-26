package com.example.tp4spring.dto;

import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Getter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor

public class FacturaRequestDTO {
    private Long puntoVentaId;
    private String observaciones;
    private List<FacturaDetalleRequestDTO> detalles;
    private Long clienteId;
    private Long tipoMonedaId;
    private Long condicionIvaId;



}
