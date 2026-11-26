package com.example.tp4spring.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PuntoVentaRequestDTO {
    private Integer numero;
    private String descripcion;
    private String tipoEmision;
    private String domicilioComercial;
}