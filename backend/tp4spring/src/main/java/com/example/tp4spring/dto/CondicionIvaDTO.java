package com.example.tp4spring.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class CondicionIvaDTO {
    private Long id;
    private int codigoAfip;
    private String denominacion;
    private boolean activo;
}