package com.example.tp4spring.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString(callSuper = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity @Table(name = "condicion_iva", schema = "Facturacion")
public class CondicionIva extends AuditoriaApp {
    @Column(nullable = false, unique = true) @EqualsAndHashCode.Include
    private int codigoAfip;

    @Column(nullable = false)
    private String denominacion;
}
