import type { ConfigCatalogo, Opcion } from "./pantallaCatalogo.js";
import { CrudService } from "./CrudService.js";
import { elegirArticulo } from "./exploradores.js";
import { escapar, formatoPesos } from "./utilidades.js";

export const CONFIG_CLIENTES: ConfigCatalogo = {
    titulo: "Clientes",
    singular: "cliente",
    ruta: "/clientes",
    descripcion: (c) => c.denominacion,
    columnas: [
        { titulo: "Cliente", valor: (c) => escapar(c.denominacion), ancho: "minmax(0, 1.4fr)" },
        { titulo: "CUIT / CUIL", valor: (c) => escapar(c.cuitCuil), ancho: "150px" },
        { titulo: "Email", valor: (c) => escapar(c.emailContacto) || "-", ancho: "minmax(0, 1fr)" },
        {
            titulo: "Domicilio",
            valor: (c) => escapar([c.nombreCalleDomicilio, c.numeroCalleDomicilio].filter(Boolean).join(" ")) || "-",
            ancho: "minmax(0, 1fr)",
        },
    ],
    ordenes: [
        { valor: "nombre-asc", texto: "Nombre (A → Z)" },
        { valor: "nombre-desc", texto: "Nombre (Z → A)" },
        { valor: "mas-compras", texto: "Más compras" },
        { valor: "compra-reciente", texto: "Compra más reciente" },
        { valor: "nuevos", texto: "Más nuevos" },
        { valor: "antiguos", texto: "Más antiguos" },
    ],
    campos: [
        { nombre: "cuitCuil", etiqueta: "CUIT / CUIL", tipo: "texto", requerido: true, claveNegocio: true },
        { nombre: "denominacion", etiqueta: "Denominación", tipo: "texto", requerido: true },
        { nombre: "emailContacto", etiqueta: "Email", tipo: "email" },
        { nombre: "telefonoContacto", etiqueta: "Teléfono", tipo: "texto" },
        { nombre: "celularContacto", etiqueta: "Celular", tipo: "texto" },
        { nombre: "nombreCalleDomicilio", etiqueta: "Calle", tipo: "texto" },
        { nombre: "numeroCalleDomicilio", etiqueta: "Número", tipo: "texto" },
    ],
};
// Carga las opciones de un desplegable desde un catálogo (solo los activos)
async function opcionesDe(ruta: string, texto: (item: any) => string): Promise<Opcion[]> {
    const pagina = await CrudService.buscar<any>(ruta, "", false, 0, 100);
    return pagina.content.map((item) => ({ valor: item.id, texto: texto(item) }));
}

// ======================= PRECIOS (Lista Precio Artículo) =======================
export const CONFIG_PRECIOS: ConfigCatalogo = {
    titulo: "Precios",
    singular: "precio",
    ruta: "/precios",
    descripcion: (p) => `${p.articuloDenominacion} (${p.listaPrecioDenominacion})`,
    columnas: [
        { titulo: "Código", valor: (p) => escapar(p.articuloCodigo), ancho: "90px" },
        { titulo: "Artículo", valor: (p) => escapar(p.articuloDenominacion), ancho: "minmax(0, 1.4fr)" },
        { titulo: "Lista", valor: (p) => escapar(p.listaPrecioDenominacion), ancho: "minmax(0, 1fr)" },
        { titulo: "Precio", valor: (p) => formatoPesos.format(p.precioVenta), ancho: "130px", alinear: "right" },
    ],
    ordenes: [
        { valor: "articulo-asc", texto: "Artículo (A → Z)" },
        { valor: "articulo-desc", texto: "Artículo (Z → A)" },
        { valor: "precio-asc", texto: "Precio: menor a mayor" },
        { valor: "precio-desc", texto: "Precio: mayor a menor" },
        { valor: "lista", texto: "Por lista" },
    ],
    campos: [
        {
            nombre: "listaPrecioId", etiqueta: "Lista de precios", tipo: "seleccion",
            requerido: true, claveNegocio: true,
            opciones: () => opcionesDe("/listas-precio", (l) => l.denominacion),
        },
        {
            nombre: "articuloId", etiqueta: "Artículo", tipo: "explorer",
            requerido: true, claveNegocio: true,
            elegir: async () => {
                const articulo = await elegirArticulo();
                return articulo ? { valor: articulo.id, texto: `${articulo.codigo} - ${articulo.denominacion}` } : null;
            },
            textoInicial: (p) => `${p.articuloCodigo} - ${p.articuloDenominacion}`,
        },
        { nombre: "precioVenta", etiqueta: "Precio de venta", tipo: "decimal", requerido: true },
    ],
};

// ======================= ARTÍCULOS =======================
const CONFIG_ARTICULOS: ConfigCatalogo = {
    titulo: "Artículos",
    singular: "artículo",
    ruta: "/articulos",
    descripcion: (a) => a.denominacion,
    columnas: [
        { titulo: "Código", valor: (a) => escapar(a.codigo), ancho: "90px" },
        { titulo: "Artículo", valor: (a) => escapar(a.denominacion), ancho: "minmax(0, 1.4fr)" },
        { titulo: "Rubro", valor: (a) => escapar(a.rubroDenominacion) || "-", ancho: "minmax(0, 1fr)" },
        { titulo: "Marca", valor: (a) => escapar(a.marcaDenominacion) || "-", ancho: "minmax(0, 1fr)" },
        { titulo: "Venta", valor: (a) => a.fraccionable ? "por kg" : "por unidad", ancho: "110px" },
    ],
    ordenes: [
        { valor: "nombre", texto: "Nombre" },
        { valor: "codigo", texto: "Código" },
        { valor: "rubro", texto: "Rubro" },
        { valor: "marca", texto: "Marca" },
    ],
    campos: [
        { nombre: "codigo", etiqueta: "Código", tipo: "texto", claveNegocio: true, autogenerado: true },
        { nombre: "denominacion", etiqueta: "Denominación", tipo: "texto", requerido: true },
        {
            nombre: "rubroId", etiqueta: "Rubro", tipo: "seleccion",
            opciones: () => opcionesDe("/rubros", (r) => r.denominacion),
        },
        {
            nombre: "marcaId", etiqueta: "Marca", tipo: "seleccion",
            opciones: () => opcionesDe("/marcas", (m) => m.denominacion),
        },
        { nombre: "fraccionable", etiqueta: "Forma de venta", tipo: "check", textoCheck: "Se vende fraccionado (por kg)" },
    ],
};

// ======================= RUBROS =======================
const CONFIG_RUBROS: ConfigCatalogo = {
    titulo: "Rubros",
    singular: "rubro",
    ruta: "/rubros",
    descripcion: (r) => r.denominacion,
    columnas: [
        { titulo: "Código", valor: (r) => escapar(r.codigo), ancho: "110px" },
        { titulo: "Denominación", valor: (r) => escapar(r.denominacion), ancho: "minmax(0, 1fr)" },
    ],
    campos: [
        { nombre: "codigo", etiqueta: "Código", tipo: "texto", claveNegocio: true, autogenerado: true },
        { nombre: "denominacion", etiqueta: "Denominación", tipo: "texto", requerido: true },
    ],
};

// ======================= MARCAS =======================
const CONFIG_MARCAS: ConfigCatalogo = {
    titulo: "Marcas",
    singular: "marca",
    femenino: true,
    ruta: "/marcas",
    descripcion: (m) => m.denominacion,
    columnas: [
        { titulo: "Código", valor: (m) => escapar(m.codigo), ancho: "110px" },
        { titulo: "Denominación", valor: (m) => escapar(m.denominacion), ancho: "minmax(0, 1fr)" },
    ],
    campos: [
        { nombre: "codigo", etiqueta: "Código", tipo: "texto", claveNegocio: true, autogenerado: true },
        { nombre: "denominacion", etiqueta: "Denominación", tipo: "texto", requerido: true },
    ],
};

// ======================= LISTAS DE PRECIOS =======================
const CONFIG_LISTAS: ConfigCatalogo = {
    titulo: "Listas de precios",
    singular: "lista de precios",
    femenino: true,
    ruta: "/listas-precio",
    descripcion: (l) => l.denominacion,
    columnas: [
        { titulo: "Código", valor: (l) => escapar(l.codigo), ancho: "110px" },
        { titulo: "Denominación", valor: (l) => escapar(l.denominacion), ancho: "minmax(0, 1fr)" },
    ],
    campos: [
        { nombre: "codigo", etiqueta: "Código", tipo: "texto", claveNegocio: true, autogenerado: true },
        { nombre: "denominacion", etiqueta: "Denominación", tipo: "texto", requerido: true },
    ],
};

// ======================= PUNTOS DE VENTA =======================
const CONFIG_PUNTOS_VENTA: ConfigCatalogo = {
    titulo: "Puntos de venta",
    singular: "punto de venta",
    ruta: "/puntos-venta",
    descripcion: (p) => `Punto de venta ${p.numero}`,
    columnas: [
        { titulo: "Número", valor: (p) => String(p.numero), ancho: "90px" },
        { titulo: "Descripción", valor: (p) => escapar(p.descripcion) || "-", ancho: "minmax(0, 1fr)" },
        { titulo: "Tipo de emisión", valor: (p) => escapar(p.tipoEmision) || "-", ancho: "minmax(0, 1fr)" },
        { titulo: "Domicilio comercial", valor: (p) => escapar(p.domicilioComercial) || "-", ancho: "minmax(0, 1.2fr)" },
    ],
    campos: [
        { nombre: "numero", etiqueta: "Número", tipo: "numero", requerido: true, claveNegocio: true },
        { nombre: "descripcion", etiqueta: "Descripción", tipo: "texto", requerido: true },
        { nombre: "tipoEmision", etiqueta: "Tipo de emisión", tipo: "texto" },
        { nombre: "domicilioComercial", etiqueta: "Domicilio comercial", tipo: "texto", ancho: "completo" },
    ],
};

// ======================= MONEDAS =======================
const CONFIG_MONEDAS: ConfigCatalogo = {
    titulo: "Monedas",
    singular: "moneda",
    femenino: true,
    ruta: "/tipos-moneda",
    descripcion: (m) => m.denominacion,
    columnas: [
        { titulo: "Código AFIP", valor: (m) => escapar(m.codigoAfip), ancho: "120px" },
        { titulo: "Denominación", valor: (m) => escapar(m.denominacion), ancho: "minmax(0, 1fr)" },
        { titulo: "Símbolo", valor: (m) => escapar(m.simbolo), ancho: "100px", alinear: "center" },
    ],
    campos: [
        { nombre: "codigoAfip", etiqueta: "Código AFIP", tipo: "texto", requerido: true, claveNegocio: true },
        { nombre: "denominacion", etiqueta: "Denominación", tipo: "texto", requerido: true },
        { nombre: "simbolo", etiqueta: "Símbolo", tipo: "texto", requerido: true },
    ],
};

// ======================= CONDICIONES IVA =======================
const CONFIG_CONDICIONES_IVA: ConfigCatalogo = {
    titulo: "Condiciones IVA",
    singular: "condición IVA",
    femenino: true,
    ruta: "/condiciones-iva",
    descripcion: (c) => c.denominacion,
    columnas: [
        { titulo: "Código AFIP", valor: (c) => String(c.codigoAfip), ancho: "120px" },
        { titulo: "Denominación", valor: (c) => escapar(c.denominacion), ancho: "minmax(0, 1fr)" },
    ],
    campos: [
        { nombre: "codigoAfip", etiqueta: "Código AFIP", tipo: "numero", requerido: true, claveNegocio: true },
        { nombre: "denominacion", etiqueta: "Denominación", tipo: "texto", requerido: true },
    ],
};

// Los que van en la pestaña "Catálogos", con el desplegable para elegir cuál
export const CATALOGOS: Record<string, ConfigCatalogo> = {
    articulos: CONFIG_ARTICULOS,
    rubros: CONFIG_RUBROS,
    marcas: CONFIG_MARCAS,
    listas: CONFIG_LISTAS,
    puntosVenta: CONFIG_PUNTOS_VENTA,
    monedas: CONFIG_MONEDAS,
    condicionesIva: CONFIG_CONDICIONES_IVA,
};