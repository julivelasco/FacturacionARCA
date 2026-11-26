package com.example.tp4spring.entities;

import jakarta.persistence.*;
import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = {"factura"})
@Entity @Table(name = "factura_venta_detalle", schema = "Facturacion")
public class FacturaVentaDetalle extends EntityId {
    @ManyToOne @JoinColumn(nullable = false)
    private FacturaVenta factura;

    @ManyToOne @JoinColumn(nullable = false)
    private ListaPrecioArticulo listaPrecioArticulo;

    private String descripcion;

    @Column(nullable = false)
    private double cantidad;

    @Column(nullable = false)
    private double precioUnitario;

    private double porcentajeBonificacion;

    private double importeNeto;

    private double importeIva;

    @Column(nullable = false)
    private double importeSubtotal;

    //esto es para las copias
    private String articuloCodigo;

    private String articuloDenominacion;

    public void hacerCopia() {
        this.articuloCodigo = listaPrecioArticulo.getArticulo().getCodigo();
        this.articuloDenominacion = listaPrecioArticulo.getArticulo().getDenominacion();
    }
}
