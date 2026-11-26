package com.example.tp4spring.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class ListaPrecioRequestDTO {
    private String codigo;
    private String denominacion;
}