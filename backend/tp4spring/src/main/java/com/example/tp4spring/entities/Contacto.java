package com.example.tp4spring.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString(callSuper = true)
@Entity @Table(name="contacto", schema = "Clientes")
public class Contacto extends EntityId {
    private String email;

    private String telefono;

    private String celular;
}
