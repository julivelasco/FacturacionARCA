import { pedir } from "./api.js";
import type { Pagina } from "./models/pagina.js";
import type {
    CondicionIvaDTO, ListaPrecioArticuloDTO, PuntoVentaDTO, TipoMonedaDTO, ListaPrecioDTO
} from "./models/catalogos.dto.js";

export class CatalogoService {

    // Para los desplegables: los activos, todos de una (son pocos)
    static async puntosVenta(): Promise<PuntoVentaDTO[]> {
        return (await pedir<Pagina<PuntoVentaDTO>>("/puntos-venta?tamanio=100")).content;
    }

    static async monedas(): Promise<TipoMonedaDTO[]> {
        return (await pedir<Pagina<TipoMonedaDTO>>("/tipos-moneda?tamanio=100")).content;
    }

    static async condicionesIva(): Promise<CondicionIvaDTO[]> {
        return (await pedir<Pagina<CondicionIvaDTO>>("/condiciones-iva?tamanio=100")).content;
    }

    static async listasPrecio(): Promise<ListaPrecioDTO[]> {
        return (await pedir<Pagina<ListaPrecioDTO>>("/listas-precio?tamanio=100")).content;
}

    static buscarPrecios(texto: string, incluirBajas: boolean, pagina: number, tamanio: number, listaPrecioId?: number): Promise<Pagina<ListaPrecioArticuloDTO>> {
        const params = new URLSearchParams({
            texto,
            incluirBajas: String(incluirBajas),
            pagina: String(pagina),
            tamanio: String(tamanio),
        });
        if (listaPrecioId !== undefined) params.append("listaPrecioId", String(listaPrecioId));
        return pedir(`/precios?${params}`);
}
}