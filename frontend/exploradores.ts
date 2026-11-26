import { abrirExplorer } from "./explorer.js";
import { ClienteService } from "./ClienteService.js";
import { escapar, formatoPesos } from "./utilidades.js";
import { CrudService } from "./CrudService.js";
import { CatalogoService } from "./CatalogoService.js";
import type { ClienteDTO } from "./models/cliente.dto.js";
import type { ListaPrecioArticuloDTO } from "./models/catalogos.dto.js";

export function elegirCliente(permiteVerBajas = false): Promise<ClienteDTO | null> {
    return abrirExplorer<ClienteDTO>({
        titulo: "Elegir cliente",
        buscar: (texto, incluirBajas, pagina, tamanio) => ClienteService.buscar(texto, incluirBajas, pagina, tamanio),
        permiteVerBajas,
        columnas: [
            {
                titulo: "Cliente",
                valor: (c) => escapar(c.denominacion) +
                    (c.activo ? "" : ` <span class="insignia insignia-ANULADA">inactivo</span>`),
                ancho: "minmax(0, 1fr)",
            },
            { titulo: "CUIT / CUIL", valor: (c) => escapar(c.cuitCuil), ancho: "150px" },
        ],
    });
}
export function elegirPrecio(): Promise<ListaPrecioArticuloDTO | null> {
    return abrirExplorer<ListaPrecioArticuloDTO>({
        titulo: "Agregar artículo",
        buscar: (texto, incluirBajas, pagina, tamanio, listaPrecioId) =>
            CatalogoService.buscarPrecios(texto, incluirBajas, pagina, tamanio, listaPrecioId),
        filtro: {
            etiqueta: "Todas las listas",
            opciones: async () => (await CatalogoService.listasPrecio()).map((l) => ({ valor: l.id, texto: l.denominacion })),},
        columnas: [
            { titulo: "Código", valor: (p) => escapar(p.articuloCodigo), ancho: "80px" },
            { titulo: "Artículo", valor: (p) => escapar(p.articuloDenominacion), ancho: "minmax(0, 1fr)" },
            { titulo: "Lista", valor: (p) => escapar(p.listaPrecioDenominacion), ancho: "160px" },
            { titulo: "Precio", valor: (p) => formatoPesos.format(p.precioVenta), ancho: "120px", alinear: "right" },
        ],
    });
}
export function elegirArticulo(): Promise<any | null> {
    return abrirExplorer<any>({
        titulo: "Elegir artículo",
        buscar: (texto, incluirBajas, pagina, tamanio) =>
            CrudService.buscar<any>("/articulos", texto, incluirBajas, pagina, tamanio),
        columnas: [
            { titulo: "Código", valor: (a) => escapar(a.codigo), ancho: "90px" },
            { titulo: "Artículo", valor: (a) => escapar(a.denominacion), ancho: "minmax(0, 1fr)" },
            { titulo: "Rubro", valor: (a) => escapar(a.rubroDenominacion) || "-", ancho: "160px" },
        ],
    });
}
