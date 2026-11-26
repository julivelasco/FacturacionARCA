const URL_FACTURAS = "http://localhost:8080/api/facturas";

import { FacturaService } from "./FacturaService.js";
import { elegirCliente } from "./exploradores.js";
import { cerrarExplorer, explorerAbierto } from "./explorer.js";
import { abrirFormularioFactura } from "./formularioFactura.js";
import { abrirPanel, cerrarPanel, escapar, formatearFecha, formatoPesos, mensajeDe, mostrarMensaje, porId, cerrarAlTocarAfuera } from "./utilidades.js";
import { cerrarPanelConCuidado, confirmacionAbierta, confirmar, responder } from "./confirmacion.js";
import { abrirNuevo, mostrarCatalogo } from "./pantallaCatalogo.js";
import { CATALOGOS, CONFIG_CLIENTES, CONFIG_PRECIOS } from "./catalogos.js";
import type { Pagina } from "./models/pagina.js";
import type { ClienteDTO } from "./models/cliente.dto.js";
import type { FacturaDTO, FacturaFiltrosDTO, FacturaReporteDTO } from "./models/factura.dto.js";

// ======================= ESTADO DE LA PANTALLA =======================
let paginaActual = 0;
let tamanioPagina = 10;
let filtros: FacturaFiltrosDTO = {};
let facturas: FacturaReporteDTO[] = [];
let seleccionada: FacturaReporteDTO | null = null;
let clienteFiltro: ClienteDTO | null = null;
let sinCliente = false;

// ======================= FILTROS =======================
function leerFiltros(): FacturaFiltrosDTO {
    const valor = (id: string) => porId<HTMLInputElement | HTMLSelectElement>(id).value.trim();
    const nuevos: FacturaFiltrosDTO = {};
    if (valor("filtroNumero")) nuevos.numero = Number(valor("filtroNumero"));
    if (valor("filtroDesde")) nuevos.fechaDesde = valor("filtroDesde");
    if (valor("filtroHasta")) nuevos.fechaHasta = valor("filtroHasta");
    if (clienteFiltro) nuevos.clienteId = clienteFiltro.id;
    if (valor("filtroEstado")) nuevos.estado = valor("filtroEstado");
    if (valor("filtroArca")) nuevos.informadaArca = valor("filtroArca") === "true";
    if (valor("filtroMonto")) nuevos.montoMinimo = Number(valor("filtroMonto"));
    if (sinCliente) nuevos.sinCliente = true;
    return nuevos;
}

function actualizarClienteFiltro(): void {
    const select = porId<HTMLSelectElement>("filtroCliente");
    select.innerHTML = `<option value="">Todos</option>
        <option value="sin">Sin cliente</option>
        ${clienteFiltro ? `<option value="cliente">${escapar(clienteFiltro.denominacion)}</option>` : ""}
        <option value="elegir">Elegir cliente...</option>`;
    select.value = sinCliente ? "sin" : clienteFiltro ? "cliente" : "";
}

// ======================= GRILLA =======================
async function cargarFacturas(): Promise<void> {
    try {
        const resultado = await FacturaService.buscar(filtros, paginaActual, tamanioPagina);
        facturas = resultado.content;
        seleccionada = facturas.find((f) => f.id === seleccionada?.id) ?? null;
        dibujarGrilla();
        dibujarPaginacion(resultado);
        actualizarBotones();
    } catch (error) {
        mostrarMensaje(mensajeDe(error), true);
    }
    // Al abrirse el panel, el cursor va al primer campo (sin tener que dar Tab 10 veces)
    new MutationObserver(() => {
        const panel = porId("panel");
        if (panel.classList.contains("hidden")) return;
        const primero = panel.querySelector<HTMLElement>(
            "#panelContenido :is(input, select, textarea, button):not([disabled]):not([type=hidden])");
        primero?.focus();
    }).observe(porId("panel"), { attributes: true, attributeFilter: ["class"] });
}

function dibujarGrilla(): void {
    const contenedor = porId("filas");
    if (facturas.length === 0) {
        contenedor.innerHTML = `<div class="vidrio rounded-2xl p-8 text-center text-slate-500">No hay facturas con esos filtros.</div>`;
        return;
    }
    contenedor.innerHTML = facturas.map((f) => `
        <div class="fila vidrio columnas rounded-2xl px-4 py-3 items-center fila-${f.estado} ${seleccionada?.id === f.id ? "seleccionada" : ""}" data-id="${f.id}">
            <span class="font-semibold">${f.numeroFactura}</span>
            <span>${formatearFecha(f.fechaEmision)}</span>
            <span class="truncate">${escapar(f.clienteDenominacion)}</span>
            <span class="truncate text-slate-600">${escapar(f.condicionIva)}</span>
            <span class="truncate text-slate-600">${escapar(f.puntoVentaDescripcion)}</span>
            <span class="text-center">${f.cantidadItems}</span>
            <span class="text-right font-semibold">${formatoPesos.format(f.importeTotal)}</span>
            <span class="text-center"><span class="insignia insignia-${f.estado}">${f.estado}</span></span>
        </div>`).join("");
}

function dibujarPaginacion(resultado: Pagina<FacturaReporteDTO>): void {
    const { number, totalPages, totalElements } = resultado.page;
    porId("textoPagina").textContent = totalPages === 0 ? "Sin resultados" : `Página ${number + 1} de ${totalPages} · ${totalElements} facturas`;
    porId<HTMLButtonElement>("botonAnterior").disabled = number === 0;
    porId<HTMLButtonElement>("botonSiguiente").disabled = number + 1 >= totalPages;
}

// Prende o apaga cada botón según el estado de la factura seleccionada (las reglas del back)
function actualizarBotones(): void {
    const estado = seleccionada?.estado;
    porId<HTMLButtonElement>("botonVer").disabled = !seleccionada;
    porId<HTMLButtonElement>("botonModificar").disabled = estado !== "PENDIENTE";
    porId<HTMLButtonElement>("botonInformar").disabled = estado !== "PENDIENTE";
    porId<HTMLButtonElement>("botonEliminar").disabled = estado !== "PENDIENTE";
    porId<HTMLButtonElement>("botonAnular").disabled = estado !== "EMITIDA";
    porId<HTMLButtonElement>("botonImprimir").disabled = !seleccionada;

}

// ======================= PANEL =======================

function dato(etiqueta: string, valor: string): string {
    return `<div>
                <div class="text-xs uppercase tracking-wide text-slate-500">${etiqueta}</div>
                <div class="mt-0.5">${valor}</div>
            </div>`;
}

function htmlDetalle(f: FacturaDTO): string {
    const arca = f.cae
        ? `<div class="vidrio fila-EMITIDA rounded-2xl p-4 grid grid-cols-2 md:grid-cols-4 gap-4">
               ${dato("CAE", f.cae)}
               ${dato("Vence", formatearFecha(f.caeFechaVencimiento))}
               ${dato("N° en ARCA", String(f.numeroComprobanteArca ?? "-"))}
               ${dato("Resultado", f.resultadoAfip ?? "-")}
           </div>`
        : f.motivoRechazo
            ? `<div class="vidrio fila-ANULADA rounded-2xl p-4">${dato("Rechazo de ARCA", escapar(f.motivoRechazo))}</div>`
            : "";

    const renglones = f.detalles.map((d) => `
        <tr class="border-t border-slate-300/60">
            <td class="py-2">${escapar(d.articuloCodigo)}</td>
            <td>${escapar(d.articuloDenominacion)}</td>
            <td class="text-right">${d.cantidad}</td>
            <td class="text-right">${formatoPesos.format(d.precioUnitario)}</td>
            <td class="text-right font-semibold">${formatoPesos.format(d.importeSubtotal)}</td>
        </tr>`).join("");

    return `<div class="space-y-5">
                <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
                    ${dato("Estado", `<span class="insignia insignia-${f.estado}">${f.estado}</span>`)}
                    ${dato("Fecha", formatearFecha(f.fechaEmision))}
                    ${dato("Cliente", `${escapar(f.clienteDenominacion)}
                    <div class="text-xs text-slate-500">
                        ${escapar(f.clienteCuitCuil ?? "")}
                    </div>`)}
                    ${dato("Condición IVA", escapar(f.condicionIvaDenominacion))}
                    ${dato("Punto de venta", `${f.puntoVentaNumero ?? ""} - ${escapar(f.puntoVentaDescripcion)}`)}
                    ${dato("Moneda", escapar(f.tipoMonedaDenominacion))}
                    ${dato("Observaciones", escapar(f.observaciones) || "-")}
                </div>
                ${arca}
                <div class="vidrio rounded-2xl p-4 overflow-x-auto">
                    <table class="w-full text-sm">
                        <thead class="text-xs uppercase tracking-wide text-slate-500">
                            <tr>
                                <th class="text-left pb-2">Código</th><th class="text-left">Artículo</th>
                                <th class="text-right">Cantidad</th><th class="text-right">Precio</th>
                                <th class="text-right">Subtotal</th>
                            </tr>
                        </thead>
                        <tbody>${renglones}</tbody>
                    </table>
                </div>
                <div class="flex justify-end gap-8 text-right">
                    ${dato("Cobrado", formatoPesos.format(f.importeCobrado))}
                    ${dato("Saldo", formatoPesos.format(f.importeSaldo))}
                    ${dato("Total", `<span class="text-2xl font-semibold">${formatoPesos.format(f.importeTotal)}</span>`)}
                </div>
                ${htmlAcciones(f)}
            </div>`;
}
function htmlAcciones(f: FacturaDTO): string {
    const imprimir = `<button class="boton" data-accion="imprimir">Imprimir</button>`;
    const botones = f.estado === "PENDIENTE"
        ? `<button class="boton" data-accion="modificar">Modificar</button>
           <button class="boton" data-accion="informar">Informar a ARCA</button>
           <button class="boton boton-peligro" data-accion="eliminar">Eliminar</button>`
        : f.estado === "EMITIDA"
            ? `<button class="boton boton-peligro" data-accion="anular">Anular</button>`
            : "";
    return `<div id="detalleAcciones" class="flex flex-wrap justify-end gap-2 pt-4 border-t border-slate-300/60">${imprimir}${botones}</div>`;
}
// Lo mínimo que necesita una acción: qué factura y su número (para los mensajes)
type FacturaRef = { id: number; numeroFactura: number };

// ======================= ACCIONES =======================
// ¿Está abierto el panel mostrando el detalle de una factura (y no el formulario)?
function detalleVisible(): boolean {
    return !porId("panel").classList.contains("hidden") && document.getElementById("detalleAcciones") !== null;
}

async function verFactura(id: number | undefined = seleccionada?.id): Promise<void> {
    if (!id) return;
    try {
        const factura = await FacturaService.obtener(id);
        abrirPanel(`Factura ${factura.numero}`, htmlDetalle(factura));

        const ref: FacturaRef = { id: factura.id, numeroFactura: factura.numero };
        porId("detalleAcciones").addEventListener("click", (evento) => {
            const accion = (evento.target as HTMLElement).closest<HTMLElement>("[data-accion]")?.dataset.accion;
            if (accion === "modificar") abrirFormularioFactura(factura.id, cargarFacturas);
            if (accion === "informar") informarArca(ref);
            if (accion === "anular") anularFactura(ref);
            if (accion === "eliminar") eliminarFactura(ref);
            if (accion === "imprimir") imprimirFactura(ref);
        });
    } catch (error) {
        mostrarMensaje(mensajeDe(error), true);
    }
}

async function imprimirFactura(factura: FacturaRef | null = seleccionada): Promise<void> {
    if (!factura) return;
    try {
        const respuesta = await fetch(`${URL_FACTURAS}/${factura.id}/pdf`);
        if (!respuesta.ok) throw new Error("No se pudo generar el PDF");
        const url = URL.createObjectURL(await respuesta.blob());
        const enlace = document.createElement("a");
        enlace.href = url;
        enlace.download = `factura-${factura.numeroFactura}.pdf`;
        enlace.click();
        setTimeout(() => URL.revokeObjectURL(url), 1000);
    } catch (error) {
        mostrarMensaje(mensajeDe(error), true);
    }
}

async function informarArca(factura: FacturaRef | null = seleccionada): Promise<void> {
    if (!factura) return;
    const acepto = await confirmar(
        `¿Informar factura ${factura.numeroFactura} a ARCA?`,
        "Le vamos a pedir el CAE a ARCA. Una vez emitida la factura, es un documento oficial y ya no se puede editar. \n¿Está seguro/a que desea continuar?",
        { textoAceptar: "Informar a ARCA" });
    if (!acepto) return;

    const boton = porId<HTMLButtonElement>("botonInformar");
    boton.disabled = true;
    boton.textContent = "Informando...";
    try {
        const resultado = await FacturaService.informarArca(factura.id);
        if (resultado.estado === "EMITIDA") {
            mostrarMensaje(`¡Factura ${resultado.numero} emitida! CAE ${resultado.cae}`);
        } else {
            mostrarMensaje(`ARCA rechazó la factura: ${resultado.motivoRechazo}`, true);
        }
        await cargarFacturas();
        if (detalleVisible()) await verFactura(factura.id);
    } catch (error) {
        mostrarMensaje(mensajeDe(error), true);
    } finally {
        boton.textContent = "Informar a ARCA";
        actualizarBotones();
    }
}

async function anularFactura(factura: FacturaRef | null = seleccionada): Promise<void> {
    if (!factura) return;
    const acepto = await confirmar(
        `¿Anular factura ${factura.numeroFactura}?`,
        "Si se anula la factura, queda registrada, pero sin efecto.\nSi fue un error, se debe hacer una nueva. \n¿Está seguro/a que desea continuar?",
        { textoAceptar: "Anular", peligro: true });
    if (!acepto) return;
    try {
        await FacturaService.anular(factura.id);
        mostrarMensaje(`Factura ${factura.numeroFactura} anulada`);
        await cargarFacturas();
        if (detalleVisible()) await verFactura(factura.id);
    } catch (error) {
        mostrarMensaje(mensajeDe(error), true);
    }
}

async function eliminarFactura(factura: FacturaRef | null = seleccionada): Promise<void> {
    if (!factura) return;
    const acepto = await confirmar(
        `¿Eliminar factura ${factura.numeroFactura}?`,
        "Esta factura todavía no se informó a ARCA, por lo que se borra por completo. \n¿Está seguro/a que desea continuar?",
        { textoAceptar: "Eliminar", peligro: true });
    if (!acepto) return;
    try {
        await FacturaService.eliminar(factura.id);
        if (seleccionada?.id === factura.id) seleccionada = null;
        mostrarMensaje(`Factura ${factura.numeroFactura} eliminada`);
        await cargarFacturas();
        if (detalleVisible()) cerrarPanel();
    } catch (error) {
        mostrarMensaje(mensajeDe(error), true);
    }
}

// ======================= EVENTOS =======================

    // ======================= PESTAÑAS =======================
    function cambiarPestania(nombre: string): void {
        document.querySelectorAll<HTMLButtonElement>(".pestania").forEach((boton) =>
            boton.classList.toggle("activa", boton.dataset.pestania === nombre));
        const esFacturas = nombre === "facturas";
        porId("vistaFacturas").classList.toggle("hidden", !esFacturas);
        porId("vistaCatalogo").classList.toggle("hidden", esFacturas);
        porId("catalogoTipoContenedor").classList.toggle("hidden", nombre !== "catalogos");
        if (nombre === "clientes") {
            mostrarCatalogo(CONFIG_CLIENTES);
        } else if (nombre === "precios") {
            mostrarCatalogo(CONFIG_PRECIOS);
        } else if (nombre === "catalogos") {
            mostrarCatalogo(CATALOGOS[porId<HTMLSelectElement>("catalogoTipo").value]);
        } else if (esFacturas) {
            cargarFacturas();   // al volver, se recarga por si algo cambió
        }
    }   

    porId("pestanias").addEventListener("click", (evento) => {
        const boton = (evento.target as HTMLElement).closest<HTMLButtonElement>(".pestania");
        if (boton && !boton.disabled && boton.dataset.pestania) {
            cambiarPestania(boton.dataset.pestania);
        }
    });

porId("filtros").addEventListener("submit", (evento) => {
    evento.preventDefault();
    filtros = leerFiltros();
    paginaActual = 0;
    cargarFacturas();
});

porId("botonLimpiar").addEventListener("click", () => {
    porId<HTMLFormElement>("filtros").reset();
    clienteFiltro = null;
    sinCliente = false;
    actualizarClienteFiltro();
    filtros = {};
    paginaActual = 0;
    cargarFacturas();
});

porId<HTMLSelectElement>("filtroCliente").addEventListener("change", async (evento) => {
    const valor = (evento.target as HTMLSelectElement).value;
    if (valor === "elegir") {
        const cliente = await elegirCliente(true);   // con bajas, para ver facturas viejas
        if (cliente) {
            clienteFiltro = cliente;
            sinCliente = false;
        }
    } else if (valor === "sin") {
        clienteFiltro = null;
        sinCliente = true;
    } else if (valor === "") {
        clienteFiltro = null;
        sinCliente = false;
    }
    actualizarClienteFiltro();
});

// Un solo "escuchador" para todas las filas: se fija en cuál se hizo clic
porId("filas").addEventListener("click", (evento) => {
    const fila = (evento.target as HTMLElement).closest<HTMLElement>(".fila");
    if (!fila) return;
    const id = Number(fila.dataset.id);
    seleccionada = facturas.find((f) => f.id === id) ?? null;
    dibujarGrilla();
    actualizarBotones();
});

porId("filas").addEventListener("dblclick", () => verFactura());

porId("botonAnterior").addEventListener("click", () => {
    if (paginaActual > 0) {
        paginaActual--;
        cargarFacturas();
    }
});

porId("botonSiguiente").addEventListener("click", () => {
    paginaActual++;
    cargarFacturas();
});

porId<HTMLSelectElement>("selectTamanio").addEventListener("change", (evento) => {
    tamanioPagina = Number((evento.target as HTMLSelectElement).value);
    paginaActual = 0;
    cargarFacturas();
});
porId("botonImprimir").addEventListener("click", () => {
    if (seleccionada) imprimirFactura({ id: seleccionada.id, numeroFactura: seleccionada.numeroFactura });
});
porId("botonVer").addEventListener("click", () => verFactura());
porId("botonInformar").addEventListener("click", () => informarArca());
porId("botonAnular").addEventListener("click", () => anularFactura());
porId("botonEliminar").addEventListener("click", () => eliminarFactura());

// El formulario de nueva y modificar es el próximo paso
porId("botonNueva").addEventListener("click", () => abrirFormularioFactura(null, cargarFacturas));
porId("botonModificar").addEventListener("click", () => {
    if (seleccionada) abrirFormularioFactura(seleccionada.id, cargarFacturas);
});

// Semáforo del panel: rojo cierra, verde agranda / achica
porId("botonCerrarPanel").addEventListener("click", cerrarPanelConCuidado);
porId("botonAgrandarPanel").addEventListener("click", () => {
    const ventana = porId("panelVentana");
    ventana.classList.toggle("max-w-5xl");
    ventana.classList.toggle("max-w-none");
    ventana.classList.toggle("min-h-[85vh]");
});

// Esc cierra primero el explorer (si está abierto) y si no, el panel
// Esc cierra lo que esté más arriba: la confirmación, el explorer o el panel
document.addEventListener("keydown", (evento) => {
    if (evento.key !== "Escape") return;
    if (confirmacionAbierta()) {
        responder(false);
    } else if (explorerAbierto()) {
        cerrarExplorer();
    } else {
        cerrarPanelConCuidado();
    }
});
document.addEventListener("keydown", (evento) => {
    if (!evento.altKey || evento.key.toLowerCase() !== "n") return;
    if (confirmacionAbierta() || explorerAbierto()) return;
    if (!porId("panel").classList.contains("hidden")) return;   // ya hay un panel abierto
    evento.preventDefault();
    const enFacturas = !porId("vistaFacturas").classList.contains("hidden");
    porId<HTMLButtonElement>(enFacturas ? "botonNueva" : "catalogoNuevo").click();
});

cerrarAlTocarAfuera("panel", cerrarPanelConCuidado);

// ======================= ARRANQUE =======================
// El desplegable de la pestaña Catálogos: una opción por catálogo
const selectCatalogo = porId<HTMLSelectElement>("catalogoTipo");
selectCatalogo.innerHTML = Object.entries(CATALOGOS)
    .map(([clave, cfg]) => `<option value="${clave}">${cfg.titulo}</option>`)
    .join("");
selectCatalogo.addEventListener("change", () => mostrarCatalogo(CATALOGOS[selectCatalogo.value]));

// Después de crear un artículo: ofrecer cargarle el precio
CATALOGOS.articulos.despuesDeCrear = async (articulo) => {
    const acepto = await confirmar(
        "¿Cargar precio ahora?",
        `${articulo.denominacion} todavía no tiene precio, así que no se puede facturar.\n¿Desea cargarlo ahora?`,
        { textoAceptar: "Cargar precio" });
    if (!acepto) return;
    cambiarPestania("precios");
    abrirNuevo({ articuloId: { valor: articulo.id, texto: `${articulo.codigo} - ${articulo.denominacion}` } });
};

cargarFacturas();