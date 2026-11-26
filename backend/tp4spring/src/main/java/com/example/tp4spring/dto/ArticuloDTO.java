package com.example.tp4spring.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class ArticuloDTO {
    private Long id;
    private String codigo;
    private String denominacion;
    private Long rubroId;
    private String rubroDenominacion;
    private Long marcaId;
    private String marcaDenominacion;
    private boolean activo;
    private boolean fraccionable;
}