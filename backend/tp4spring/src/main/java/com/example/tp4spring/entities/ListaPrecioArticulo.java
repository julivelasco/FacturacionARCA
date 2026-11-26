package com.example.tp4spring.entities;

import jakarta.persistence.*;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString(callSuper = true)
@Entity @Table(name = "lista_precio_articulo", schema = "Articulos")
public class ListaPrecioArticulo extends AuditoriaApp {
    @ManyToOne @JoinColumn(nullable = false)
    private ListaPrecio listaPrecio;

    @Column(nullable = false)
    private double precioVenta;

    @ManyToOne @JoinColumn(nullable = false)
    private Articulo articulo;
}
