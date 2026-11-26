package com.example.tp4spring.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class CondicionIvaRequestDTO {
    private Integer codigoAfip;
    private String denominacion;
}