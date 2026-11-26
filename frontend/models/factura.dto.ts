// Los tres estados posibles: TypeScript no deja poner otro texto
export type EstadoFactura = "PENDIENTE" | "EMITIDA" | "ANULADA";

// Una fila de la grilla (espejo de FacturaReporteDTO)
export interface FacturaReporteDTO {
    id: number;
    numeroFactura: number;
    fechaEmision: string;
    clienteDenominacion: string;
    condicionIva: string;
    puntoVentaDescripcion: string;
    importeTotal: number;
    cantidadItems: number;
    estado: EstadoFactura;
}

// Un renglón de la factura completa (espejo de FacturaDetalleDTO)
export interface FacturaDetalleDTO {
    id: number;
    listaPrecioArticuloId: number;
    articuloCodigo: string;
    articuloDenominacion: string;
    listaPrecioDenominacion: string;
    cantidad: number;
    precioUnitario: number;
    importeNeto: number;
    importeIva: number;
    importeSubtotal: number;
    articuloFraccionable: boolean;
}

// La factura completa (espejo de FacturaDTO)
export interface FacturaDTO {
    id: number;
    numero: number;
    fechaEmision: string;
    estado: EstadoFactura;
    observaciones: string | null;
    clienteId: number | null;
    clienteDenominacion: string;
    puntoVentaId: number;
    puntoVentaDescripcion: string;
    tipoMonedaId: number;
    tipoMonedaDenominacion: string;
    condicionIvaId: number;
    condicionIvaDenominacion: string;
    importeTotal: number;
    importeCobrado: number;
    importeSaldo: number;
    cae: string | null;
    caeFechaVencimiento: string | null;
    resultadoAfip: string | null;
    numeroComprobanteArca: number | null;
    motivoRechazo: string | null;
    detalles: FacturaDetalleDTO[];
    clienteCuitCuil: string | null;
    puntoVentaNumero: number | null;
    tipoMonedaSimbolo: string | null;
}

// Lo que se manda para crear o modificar
export interface FacturaDetalleRequestDTO {
    listaPrecioArticuloId: number;
    cantidad: number;
}

export interface FacturaRequestDTO {
    puntoVentaId?: number;
    clienteId?: number;
    tipoMonedaId?: number;
    condicionIvaId?: number;
    observaciones?: string;
    detalles: FacturaDetalleRequestDTO[];
}

// Los filtros de la grilla (espejo de FacturaFiltrosDTO)
export interface FacturaFiltrosDTO {
    numero?: number;
    fechaDesde?: string;
    fechaHasta?: string;
    clienteId?: number;
    estado?: string;
    montoMinimo?: number;
    informadaArca?: boolean;
    sinCliente?: boolean;
}