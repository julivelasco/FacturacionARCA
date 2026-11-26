package com.example.tp4spring.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class RubroDTO {
    private Long id;
    private String codigo;
    private String denominacion;
    private boolean activo;
}