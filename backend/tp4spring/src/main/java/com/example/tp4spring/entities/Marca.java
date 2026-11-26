package com.example.tp4spring.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString(callSuper = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity @Table(name = "marca", schema = "Articulos")
public class Marca extends AuditoriaApp {
    @Column(nullable = false, unique = true) @EqualsAndHashCode.Include
    private String codigo;

    @Column(nullable = false)
    private String denominacion;
}
