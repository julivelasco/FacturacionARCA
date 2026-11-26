package com.example.tp4spring.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.util.Date;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class FacturaDTO {
    private Long id;
    private Long numero;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date fechaEmision;
    private String estado;
    private String observaciones;

    private Long clienteId;
    private String clienteDenominacion;
    private Long puntoVentaId;
    private String puntoVentaDescripcion;
    private Long tipoMonedaId;
    private String tipoMonedaDenominacion;
    private Long condicionIvaId;
    private String condicionIvaDenominacion;

    private double importeTotal;
    private double importeCobrado;
    private double importeSaldo;

    private String cae;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date caeFechaVencimiento;
    private String resultadoAfip;

    private Long numeroComprobanteArca;
    private String motivoRechazo;

    private List<FacturaDetalleDTO> detalles;

    private String clienteCuitCuil;
    private Integer puntoVentaNumero;
    private String tipoMonedaSimbolo;
}