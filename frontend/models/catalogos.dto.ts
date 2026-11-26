export interface PuntoVentaDTO {
    id: number;
    numero: number;
    descripcion: string | null;
    tipoEmision: string | null;
    domicilioComercial: string | null;
    activo: boolean;
}

export interface TipoMonedaDTO {
    id: number;
    codigoAfip: string;
    denominacion: string;
    simbolo: string;
    activo: boolean;
}

export interface CondicionIvaDTO {
    id: number;
    codigoAfip: number;
    denominacion: string;
    activo: boolean;
}

export interface ListaPrecioArticuloDTO {
    id: number;
    listaPrecioId: number;
    listaPrecioDenominacion: string;
    articuloId: number;
    articuloCodigo: string;
    articuloDenominacion: string;
    precioVenta: number;
    articuloFraccionable: boolean;
    activo: boolean;
}

export interface ListaPrecioDTO {
    id: number;
    codigo: string;
    denominacion: string;
    activo: boolean;
}