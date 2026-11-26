import { FacturaService } from "./FacturaService.js";
import { CatalogoService } from "./CatalogoService.js";
import { elegirCliente, elegirPrecio } from "./exploradores.js";
import { abrirPanel, cerrarPanel, escapar, formatoPesos, mensajeDe, mostrarMensaje, porId, vigilarCambios } from "./utilidades.js";
import { cerrarPanelConCuidado } from "./confirmacion.js";
// ======================= ESTADO DEL FORMULARIO =======================
let idEditando = null; // null = factura nueva
let cliente = null;
let renglones = [];
let alGuardar = () => { };
// ======================= ABRIR =======================
export async function abrirFormularioFactura(idFactura, despuesDeGuardar) {
    idEditando = idFactura;
    alGuardar = despuesDeGuardar;
    cliente = null;
    renglones = [];
    try {
        // Los tres desplegables se piden a la vez, no uno después del otro
        const [puntosVenta, monedas, condiciones] = await Promise.all([
            CatalogoService.puntosVenta(),
            CatalogoService.monedas(),
            CatalogoService.condicionesIva(),
        ]);
        let titulo = "Nueva factura";
        let valores = {
            puntoVentaId: puntosVenta[0]?.id,
            tipoMonedaId: (monedas.find((m) => m.codigoAfip === "PES") ?? monedas[0])?.id,
            condicionIvaId: undefined,
            observaciones: "",
        };
        if (idFactura !== null) {
            const factura = await FacturaService.obtener(idFactura);
            titulo = `Modificar factura ${factura.numero}`;
            cliente = factura.clienteId !== null ? { id: factura.clienteId, denominacion: factura.clienteDenominacion } : null;
            valores = {
                puntoVentaId: factura.puntoVentaId,
                tipoMonedaId: factura.tipoMonedaId,
                condicionIvaId: factura.condicionIvaId,
                observaciones: factura.observaciones ?? "",
            };
            renglones = factura.detalles.map((d) => ({
                listaPrecioArticuloId: d.listaPrecioArticuloId,
                articuloCodigo: d.articuloCodigo,
                articuloDenominacion: d.articuloDenominacion,
                listaPrecioDenominacion: d.listaPrecioDenominacion,
                precioUnitario: d.precioUnitario,
                cantidad: d.cantidad,
                fraccionable: d.articuloFraccionable,
            }));
        }
        abrirPanel(titulo, htmlFormulario(puntosVenta, monedas, condiciones, valores));
        conectarEventos();
        dibujarCliente();
        dibujarRenglones();
        porId("formClienteBoton").focus();
        const estadoInicial = estadoActual(); // la "foto" de cómo arrancó
        vigilarCambios(() => estadoActual() !== estadoInicial);
    }
    catch (error) {
        mostrarMensaje(mensajeDe(error), true);
    }
}
// ======================= HTML =======================
function opciones(lista, seleccionado, texto) {
    return `<option value="" disabled ${seleccionado === undefined ? "selected" : ""}>Elegí una opción</option>` +
        lista.map((item) => `<option value="${item.id}" ${item.id === seleccionado ? "selected" : ""}>${escapar(texto(item))}</option>`).join("");
}
function htmlFormulario(puntosVenta, monedas, condiciones, valores) {
    return `
    <form id="formFactura" class="space-y-5">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div class="flex flex-col gap-1">
                <span class="etiqueta-filtro">Cliente</span>
                <div class="flex gap-2">
                    <button type="button" id="formClienteBoton" class="campo text-left truncate flex-1 min-w-0"></button>
                    <button type="button" id="formClienteQuitar" class="boton" title="Sin cliente (consumidor final)">✕</button>
                </div>
            </div>
            <label class="flex flex-col gap-1">
                <span class="etiqueta-filtro">Condición IVA *</span>
                <select id="formCondicionIva" class="campo" required>
                    ${opciones(condiciones, valores.condicionIvaId, (c) => c.denominacion)}
                </select>
            </label>
            <label class="flex flex-col gap-1">
                <span class="etiqueta-filtro">Punto de venta *</span>
                <select id="formPuntoVenta" class="campo" required>
                    ${opciones(puntosVenta, valores.puntoVentaId, (p) => `${p.numero} - ${p.descripcion ?? ""}`)}
                </select>
            </label>
            <label class="flex flex-col gap-1">
                <span class="etiqueta-filtro">Moneda *</span>
                <select id="formMoneda" class="campo" required>
                    ${opciones(monedas, valores.tipoMonedaId, (m) => `${m.denominacion} (${m.simbolo})`)}
                </select>
            </label>
            <label class="flex flex-col gap-1 md:col-span-2">
                <span class="etiqueta-filtro">Observaciones</span>
                <textarea id="formObservaciones" class="campo" rows="2" maxlength="255">${escapar(valores.observaciones)}</textarea>
            </label>
        </div>

        <div class="vidrio rounded-2xl p-4 space-y-3">
            <div class="flex items-center justify-between gap-3">
                <h3 class="font-semibold">Renglones</h3>
                <div class="flex gap-2">
                    <button type="button" id="formJuntar" class="boton" title="Une en un solo renglón los artículos repetidos">Juntar iguales</button>
                    <button type="button" id="formAgregarRenglon" class="boton boton-principal">+ Agregar artículo</button>
                </div>
            </div>
            <div class="overflow-x-auto">
                <table class="w-full text-sm tabla-renglones table-fixed">
                    <colgroup>
                        <col style="width: 80px">
                        <col>
                        <col style="width: 170px">
                        <col style="width: 120px">
                        <col style="width: 120px">
                        <col style="width: 120px">
                        <col style="width: 150px">
                    </colgroup>
                    <thead class="text-xs uppercase tracking-wide text-slate-500">
                        <tr>
                            <th class="text-left">Código</th>
                            <th class="text-left">Artículo</th>
                            <th class="text-left">Lista</th>
                            <th class="text-center">Precio</th>
                            <th class="text-center">Cantidad</th>
                            <th class="text-center">Subtotal</th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody id="formRenglones"></tbody>
                </table>
            </div>
        </div>

        <div class="flex flex-wrap items-end justify-between gap-4">
            <div>
                <span class="etiqueta-filtro">Total estimado</span>
                <div id="formTotal" class="text-2xl font-semibold"></div>
            </div>
            <div class="flex gap-2">
                <button type="button" id="formCancelar" class="boton">Cancelar</button>
                <button type="submit" id="formGuardar" class="boton boton-principal">Guardar</button>
            </div>
        </div>
    </form>`;
}
// ======================= DIBUJAR =======================
function dibujarCliente() {
    const boton = porId("formClienteBoton");
    boton.textContent = cliente ? cliente.denominacion : "Elegir cliente...";
    boton.classList.toggle("text-slate-400", !cliente);
}
// Cada fila tiene 7 celdas, en este orden:
// 0 Código · 1 Artículo · 2 Lista · 3 Precio · 4 Cantidad · 5 Subtotal · 6 Botones
function dibujarRenglones() {
    const cuerpo = porId("formRenglones");
    if (renglones.length === 0) {
        cuerpo.innerHTML = `<tr><td colspan="7" class="py-6 text-center text-slate-500">Todavía no agregaste artículos.</td></tr>`;
    }
    else {
        cuerpo.innerHTML = renglones.map((r, i) => `
            <tr class="border-t border-slate-300/60">
                <td>${escapar(r.articuloCodigo)}</td>
                <td class="truncate">${escapar(r.articuloDenominacion)}</td>
                <td class="truncate text-slate-500">${escapar(r.listaPrecioDenominacion)}</td>
                <td class="text-center">${formatoPesos.format(r.precioUnitario)}</td>
                <td class="text-center">
                      <input type="number" class="campo campo-cantidad text-center" min="${r.fraccionable ? "0.01" : "1"}" step="${r.fraccionable ? "0.01" : "1"}" required value="${r.cantidad}" data-indice="${i}">
                </td>
                <td class="text-center font-semibold">${formatoPesos.format(r.precioUnitario * r.cantidad)}</td>
                <td class="text-right whitespace-nowrap">
                    <button type="button" class="boton px-3 py-1 ${r.cantidad > 1 && Number.isInteger(r.cantidad) ? "" : "hidden"}"
                            data-separar="${i}" title="Un renglón por cada unidad">Separar</button>
                    <button type="button" class="boton px-3 py-1" data-quitar="${i}" title="Quitar">✕</button>
                </td>
            </tr>`).join("");
    }
    porId("formJuntar").disabled = !hayRepetidos();
    actualizarTotal();
}
function actualizarTotal() {
    const total = renglones.reduce((suma, r) => suma + r.precioUnitario * r.cantidad, 0);
    porId("formTotal").textContent = formatoPesos.format(total);
}
// Todo lo que se puede cambiar en el formulario, convertido en un texto para comparar
function estadoActual() {
    return JSON.stringify({
        cliente: cliente?.id ?? null,
        condicionIva: porId("formCondicionIva").value,
        puntoVenta: porId("formPuntoVenta").value,
        moneda: porId("formMoneda").value,
        observaciones: porId("formObservaciones").value,
        renglones: renglones.map((r) => [r.listaPrecioArticuloId, r.cantidad]),
    });
}
// ======================= JUNTAR Y SEPARAR =======================
// Redondea a 2 decimales, para que 0.1 + 0.2 no quede 0.30000000000000004
function redondear(numero) {
    return Math.round(numero * 100) / 100;
}
// ¿Hay algún precio que aparezca en más de un renglón?
function hayRepetidos() {
    const ids = renglones.map((r) => r.listaPrecioArticuloId);
    return new Set(ids).size !== ids.length;
}
// Une los renglones del mismo precio en uno solo, sumando las cantidades
function juntarRenglones() {
    const juntos = [];
    for (const r of renglones) {
        const existente = juntos.find((j) => j.listaPrecioArticuloId === r.listaPrecioArticuloId);
        if (existente) {
            existente.cantidad = redondear(existente.cantidad + r.cantidad);
        }
        else {
            juntos.push({ ...r }); // una copia, no el mismo objeto
        }
    }
    renglones = juntos;
    dibujarRenglones();
}
// Parte un renglón en uno por cada unidad (2,5 → 1 + 1 + 0,5)
function separarRenglon(indice) {
    const original = renglones[indice];
    const partes = [];
    let resto = original.cantidad;
    while (resto > 1) {
        partes.push({ ...original, cantidad: 1 });
        resto = redondear(resto - 1);
    }
    partes.push({ ...original, cantidad: resto });
    renglones.splice(indice, 1, ...partes); // saca el original y pone las partes en su lugar
    dibujarRenglones();
}
// ======================= EVENTOS =======================
// Se conectan cada vez que se abre, porque el HTML del formulario se crea de nuevo
function conectarEventos() {
    porId("formClienteBoton").addEventListener("click", async () => {
        const elegido = await elegirCliente(false); // sin dados de baja: no se les factura
        if (elegido) {
            cliente = { id: elegido.id, denominacion: elegido.denominacion };
            dibujarCliente();
        }
    });
    porId("formClienteQuitar").addEventListener("click", () => {
        cliente = null;
        dibujarCliente();
    });
    porId("formAgregarRenglon").addEventListener("click", async () => {
        const precio = await elegirPrecio();
        if (precio) {
            renglones.push({
                listaPrecioArticuloId: precio.id,
                articuloCodigo: precio.articuloCodigo,
                articuloDenominacion: precio.articuloDenominacion,
                listaPrecioDenominacion: precio.listaPrecioDenominacion,
                precioUnitario: precio.precioVenta,
                cantidad: 1,
                fraccionable: precio.articuloFraccionable,
            });
            dibujarRenglones();
        }
    });
    porId("formJuntar").addEventListener("click", juntarRenglones);
    const cuerpo = porId("formRenglones");
    // Cambiar la cantidad: actualiza solo esa fila y el total
    // (no se redibuja la tabla, así no se pierde el cursor mientras escribís)
    cuerpo.addEventListener("input", (evento) => {
        const campo = evento.target;
        if (campo.dataset.indice === undefined)
            return;
        const renglon = renglones[Number(campo.dataset.indice)];
        renglon.cantidad = Number(campo.value) || 0;
        const fila = campo.closest("tr");
        const celdaSubtotal = fila.children[5]; // la 6ª celda: Subtotal
        celdaSubtotal.textContent = formatoPesos.format(renglon.precioUnitario * renglon.cantidad);
        fila.querySelector("[data-separar]")?.classList.toggle("hidden", !(renglon.cantidad > 1 && Number.isInteger(renglon.cantidad)));
        actualizarTotal();
    });
    // Botones de cada fila: separar o quitar
    cuerpo.addEventListener("click", (evento) => {
        const objetivo = evento.target;
        const botonSeparar = objetivo.closest("[data-separar]");
        if (botonSeparar) {
            separarRenglon(Number(botonSeparar.dataset.separar));
            return;
        }
        const botonQuitar = objetivo.closest("[data-quitar]");
        if (botonQuitar) {
            renglones.splice(Number(botonQuitar.dataset.quitar), 1);
            dibujarRenglones();
        }
    });
    porId("formCancelar").addEventListener("click", cerrarPanelConCuidado);
    porId("formFactura").addEventListener("submit", guardar);
}
// ======================= GUARDAR =======================
async function guardar(evento) {
    evento.preventDefault();
    // Lo que el "required" del HTML no puede controlar solo
    if (renglones.length === 0) {
        mostrarMensaje("Agregá al menos un artículo", true);
        return;
    }
    if (renglones.some((r) => !(r.cantidad > 0))) {
        mostrarMensaje("Revisá las cantidades: tienen que ser mayores a 0", true);
        return;
    }
    const malCargado = renglones.find((r) => !r.fraccionable && !Number.isInteger(r.cantidad));
    if (malCargado) {
        mostrarMensaje(`${malCargado.articuloDenominacion} se vende por unidad: poné una cantidad entera`, true);
        return;
    }
    const datos = {
        clienteId: cliente?.id,
        condicionIvaId: Number(porId("formCondicionIva").value),
        puntoVentaId: Number(porId("formPuntoVenta").value),
        tipoMonedaId: Number(porId("formMoneda").value),
        observaciones: porId("formObservaciones").value.trim() || undefined,
        detalles: renglones.map((r) => ({ listaPrecioArticuloId: r.listaPrecioArticuloId, cantidad: r.cantidad })),
    };
    const boton = porId("formGuardar");
    boton.disabled = true;
    boton.textContent = "Guardando...";
    try {
        const guardada = idEditando === null
            ? await FacturaService.crear(datos)
            : await FacturaService.actualizar(idEditando, datos);
        mostrarMensaje(idEditando === null
            ? `Factura ${guardada.numeroFactura} creada`
            : `Factura ${guardada.numeroFactura} modificada`);
        cerrarPanel();
        alGuardar(); // la grilla se vuelve a cargar
    }
    catch (error) {
        // el back rechazó algo: se muestra el motivo y el formulario queda abierto para corregir
        mostrarMensaje(mensajeDe(error), true);
        boton.disabled = false;
        boton.textContent = "Guardar";
    }
}
