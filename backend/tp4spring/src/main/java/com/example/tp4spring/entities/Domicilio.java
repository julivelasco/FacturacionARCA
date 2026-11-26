package com.example.tp4spring.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString(callSuper = true)
@Entity @Table(name = "domicilio", schema = "Clientes")
public class Domicilio extends EntityId {
    private String nombreCalle;

    private String numeroCalle;
}
