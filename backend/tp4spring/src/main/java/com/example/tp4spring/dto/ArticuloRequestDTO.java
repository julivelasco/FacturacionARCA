package com.example.tp4spring.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class ArticuloRequestDTO {
    private String codigo;
    private String denominacion;
    private Long rubroId;
    private Long marcaId;
    private Boolean fraccionable;
}