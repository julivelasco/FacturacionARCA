package com.example.tp4spring.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString(callSuper = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity @Table(name = "articulo", schema = "Articulos")
public class Articulo extends AuditoriaApp {
    @ManyToOne
    private Rubro rubro;

    @Column(nullable = false, unique = true) @EqualsAndHashCode.Include
    private String codigo;

    @Column(nullable = false)
    private String denominacion;

    @ManyToOne
    private Marca marca;

    // true: se vende por kilo (acepta decimales). false: se vende por unidad (solo enteros)
    private boolean fraccionable;
}
