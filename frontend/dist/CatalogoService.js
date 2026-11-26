import { pedir } from "./api.js";
export class CatalogoService {
    // Para los desplegables: los activos, todos de una (son pocos)
    static async puntosVenta() {
        return (await pedir("/puntos-venta?tamanio=100")).content;
    }
    static async monedas() {
        return (await pedir("/tipos-moneda?tamanio=100")).content;
    }
    static async condicionesIva() {
        return (await pedir("/condiciones-iva?tamanio=100")).content;
    }
    static async listasPrecio() {
        return (await pedir("/listas-precio?tamanio=100")).content;
    }
    static buscarPrecios(texto, incluirBajas, pagina, tamanio, listaPrecioId) {
        const params = new URLSearchParams({
            texto,
            incluirBajas: String(incluirBajas),
            pagina: String(pagina),
            tamanio: String(tamanio),
        });
        if (listaPrecioId !== undefined)
            params.append("listaPrecioId", String(listaPrecioId));
        return pedir(`/precios?${params}`);
    }
}
