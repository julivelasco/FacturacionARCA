package com.example.tp4spring.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public abstract class AuditoriaApp extends EntityId {
    @Column(nullable = false) @Temporal(TemporalType.TIMESTAMP)
    protected Date fechaAlta;

    @Temporal(TemporalType.TIMESTAMP)
    protected Date fechaBaja;

    @Column(nullable = false) @Temporal(TemporalType.TIMESTAMP)
    protected Date fechaModificacion;

    @ManyToOne @JoinColumn(nullable = false)
    protected Usuario usuarioCarga;

    @ManyToOne
    protected Usuario usuarioBaja;

    @ManyToOne @JoinColumn(nullable = false)
    protected Usuario usuarioModificacion;
}
