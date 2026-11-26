package com.example.tp4spring.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class ClienteRequestDTO {
    private String cuitCuil;
    private String denominacion;
    private String emailContacto;
    private String telefonoContacto;
    private String celularContacto;
    private String nombreCalleDomicilio;
    private String numeroCalleDomicilio;

}
