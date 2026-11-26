package com.example.tp4spring.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PuntoVentaDTO {
    private Long id;
    private int numero;
    private String descripcion;
    private String tipoEmision;
    private String domicilioComercial;
    private boolean activo;
}
