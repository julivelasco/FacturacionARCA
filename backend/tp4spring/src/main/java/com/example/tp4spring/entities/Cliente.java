package com.example.tp4spring.entities;

import jakarta.persistence.*;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString(callSuper = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity @Table(name = "cliente", schema = "Clientes")
public class Cliente extends AuditoriaApp {
    @Column(nullable = false, unique = true) @EqualsAndHashCode.Include
    private String cuitCuil;

    @Column(nullable = false)
    private String denominacion;

    @OneToOne(cascade = CascadeType.ALL) @JoinColumn(nullable = false)
    private Contacto contacto;

    @OneToOne(cascade = CascadeType.ALL) @JoinColumn(nullable = false)
    private Domicilio domicilio;
}
