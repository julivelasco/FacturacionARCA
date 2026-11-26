package com.example.tp4spring.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class TipoMonedaRequestDTO {
    private String codigoAfip;
    private String denominacion;
    private String simbolo;
}