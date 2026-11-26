package com.example.tp4spring.entities;

import jakarta.persistence.*;

import java.util.Date;
import java.util.List;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@ToString(callSuper = true, exclude = {"detalles"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity @Table(name = "factura_venta", schema = "Facturacion")
public class FacturaVenta extends AuditoriaApp {
    @Column(nullable = false, unique = true) @EqualsAndHashCode.Include
    private Long numero;

    @Temporal(TemporalType.TIMESTAMP) @Column(nullable = false)
    private Date fechaEmision;

    @ManyToOne @JoinColumn(nullable = false)
    private PuntoVenta puntoVenta;

    private double importeCobrado;

    private double importeSaldo;

    @Column(nullable = false)
    private double importeTotal;

    private String cae;

    @Temporal(TemporalType.TIMESTAMP)
    private Date caeFechaVencimiento;

    private String resultadoAfip;

    private String motivoRechazo;

    @Column(nullable = false)
    private String estado;

    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaAnulacion;

    private String observaciones;

    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FacturaVentaDetalle> detalles;

    @ManyToOne
    private Cliente cliente;

    @ManyToOne @JoinColumn(nullable = false)
    private TipoMoneda tipoMoneda;

    @ManyToOne @JoinColumn(nullable = false)
    private CondicionIva condicionIva;

    private Long numeroComprobanteArca;

    // esto es para las copias, las relaciones (cliente, puntoVenta...) se mantienen para filtrar, editar e informar a ARCA.
    // Lo que se MUESTRA sale de estas copias, así no cambia si después se modifica el original.
    private String clienteDenominacion;

    private String clienteCuitCuil;

    private Integer puntoVentaNumero;

    private String puntoVentaDescripcion;

    private String condicionIvaDenominacion;

    private String tipoMonedaDenominacion;

    private String tipoMonedaSimbolo;

    public void hacerCopia() {
        this.clienteDenominacion = cliente != null ? cliente.getDenominacion() : "Consumidor Final";
        this.clienteCuitCuil = cliente != null ? cliente.getCuitCuil() : null;
        this.puntoVentaNumero = puntoVenta.getNumero();
        this.puntoVentaDescripcion = puntoVenta.getDescripcion();
        this.condicionIvaDenominacion = condicionIva.getDenominacion();
        this.tipoMonedaDenominacion = tipoMoneda.getDenominacion();
        this.tipoMonedaSimbolo = tipoMoneda.getSimbolo();
        for (FacturaVentaDetalle detalle : detalles) {
            detalle.hacerCopia();
        }
    }

    public void agregarDetalle(FacturaVentaDetalle fvd){
        getDetalles().add(fvd);
    }
}
