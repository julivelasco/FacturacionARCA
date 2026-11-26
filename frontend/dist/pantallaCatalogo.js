import { CrudService } from "./CrudService.js";
import { ErrorApi } from "./api.js";
import { cerrarPanelConCuidado, confirmar } from "./confirmacion.js";
import { abrirPanel, cerrarPanel, escapar, mensajeDe, mostrarMensaje, porId, vigilarCambios } from "./utilidades.js";
let camposFijos = new Set(); // campos precargados que no se pueden cambiar
// ======================= ESTADO DE LA PANTALLA =======================
// Uso "any" a propósito: la pantalla es genérica y no sabe de qué tipo es cada catálogo. La config sí.
const TAMANIO = 10;
let config = null;
let items = [];
let seleccionado = null;
let pedidoActual = 0; // numera las búsquedas, para descartar las que llegan tarde
let pagina = 0;
let demoraBusqueda;
let editando = null; // null = nuevo
let elegidosExplorer = {};
// ======================= MOSTRAR UN CATÁLOGO =======================
export function mostrarCatalogo(nuevaConfig) {
    config = nuevaConfig;
    pagina = 0;
    seleccionado = null;
    porId("catalogoTitulo").textContent = nuevaConfig.titulo;
    porId("catalogoTexto").value = "";
    porId("catalogoBajas").checked = false;
    porId("catalogoNuevo").textContent = `+ ${nuevaConfig.femenino ? "Nueva" : "Nuevo"}`;
    const encabezado = porId("catalogoEncabezado");
    encabezado.style.gridTemplateColumns = plantillaColumnas(nuevaConfig);
    encabezado.innerHTML = nuevaConfig.columnas
        .map((c) => `<span class="${alineacion(c)}">${c.titulo}</span>`)
        .join("") + `<span class="text-right">Estado</span>`;
    const selectOrden = porId("catalogoOrden");
    porId("catalogoOrdenContenedor").classList.toggle("hidden", !nuevaConfig.ordenes);
    selectOrden.innerHTML = (nuevaConfig.ordenes ?? [])
        .map((o) => `<option value="${o.valor}">${escapar(o.texto)}</option>`)
        .join("");
    buscar();
}
function plantillaColumnas(cfg) {
    return [...cfg.columnas.map((c) => c.ancho ?? "minmax(0, 1fr)"), "100px"].join(" ");
}
function alineacion(columna) {
    if (columna.alinear === "right")
        return "text-right";
    if (columna.alinear === "center")
        return "text-center";
    return "";
}
// ======================= GRILLA =======================
async function buscar() {
    const cfg = config;
    if (!cfg)
        return;
    const miPedido = ++pedidoActual;
    try {
        const texto = porId("catalogoTexto").value.trim();
        const incluirBajas = porId("catalogoBajas").checked;
        const orden = porId("catalogoOrden").value || undefined;
        const resultado = await CrudService.buscar(cfg.ruta, texto, incluirBajas, pagina, TAMANIO, orden);
        // Si mientras esperábamos salió otra búsqueda (o cambiaron de pestaña), esta ya no sirve
        if (miPedido !== pedidoActual)
            return;
        items = resultado.content;
        seleccionado = items.find((i) => i.id === seleccionado?.id) ?? null;
        dibujar();
        const { number, totalPages, totalElements } = resultado.page;
        porId("catalogoPagina").textContent = totalPages === 0
            ? "Sin resultados"
            : `Página ${number + 1} de ${totalPages} · ${totalElements} en total`;
        porId("catalogoAnterior").disabled = number === 0;
        porId("catalogoSiguiente").disabled = number + 1 >= totalPages;
    }
    catch (error) {
        if (miPedido === pedidoActual)
            mostrarMensaje(mensajeDe(error), true);
    }
}
function dibujar() {
    const cfg = config;
    if (!cfg)
        return;
    const contenedor = porId("catalogoFilas");
    if (items.length === 0) {
        contenedor.innerHTML = `<div class="vidrio rounded-2xl p-8 text-center text-slate-500">No se encontró nada.</div>`;
    }
    else {
        const plantilla = plantillaColumnas(cfg);
        contenedor.innerHTML = items.map((item) => `
            <div class="fila vidrio rounded-2xl px-4 py-3 grid gap-3 items-center
                        ${item.activo ? "fila-neutral" : "fila-ANULADA"} ${seleccionado?.id === item.id ? "seleccionada" : ""}"
                 style="grid-template-columns: ${plantilla}" data-id="${item.id}">
                ${cfg.columnas.map((c) => `<span class="truncate ${alineacion(c)}">${c.valor(item)}</span>`).join("")}
                <span class="text-right">
                    ${item.activo
            ? `<span class="insignia insignia-ACTIVO">activo</span>`
            : `<span class="insignia insignia-ANULADA">inactivo</span>`}
                </span>
            </div>`).join("");
    }
    actualizarBotones();
}
function actualizarBotones() {
    porId("catalogoModificar").disabled = !seleccionado?.activo;
    porId("catalogoBaja").disabled = !seleccionado?.activo;
    porId("catalogoReactivar").disabled = !seleccionado || seleccionado.activo;
}
// ======================= FORMULARIO =======================
async function abrirFormulario(item, precargados = {}) {
    const cfg = config;
    if (!cfg)
        return;
    editando = item;
    camposFijos = new Set(Object.keys(precargados));
    try {
        // Las opciones de todos los desplegables se piden a la vez
        const opcionesPorCampo = await Promise.all(cfg.campos.map((c) => c.tipo === "seleccion" && c.opciones ? c.opciones() : Promise.resolve([])));
        // Lo que ya estaba elegido en los campos de tipo explorer: lo precargado, o lo del item (al modificar)
        elegidosExplorer = {};
        for (const c of cfg.campos) {
            if (c.tipo === "explorer") {
                elegidosExplorer[c.nombre] = precargados[c.nombre]
                    ?? (item && item[c.nombre] != null
                        ? { valor: item[c.nombre], texto: c.textoInicial ? c.textoInicial(item) : String(item[c.nombre]) }
                        : null);
            }
        }
        const titulo = item ? `Modificar ${cfg.singular}` : `${cfg.femenino ? "Nueva" : "Nuevo"} ${cfg.singular}`;
        abrirPanel(titulo, htmlFormulario(cfg, item, opcionesPorCampo));
        conectarFormulario(cfg);
        const estadoInicial = JSON.stringify(leerFormulario(cfg)); // la "foto" de cómo arrancó
        vigilarCambios(() => JSON.stringify(leerFormulario(cfg)) !== estadoInicial);
    }
    catch (error) {
        mostrarMensaje(mensajeDe(error), true);
    }
}
// Abre el formulario de "nuevo" con algunos campos de tipo explorer ya elegidos
export function abrirNuevo(precargados) {
    abrirFormulario(null, precargados);
}
function htmlFormulario(cfg, item, opcionesPorCampo) {
    return `
    <form id="formCatalogo" class="space-y-5">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            ${cfg.campos.map((c, i) => htmlCampo(c, item, opcionesPorCampo[i])).join("")}
        </div>
        <div class="flex flex-wrap items-center justify-between gap-3">
            <span class="text-sm text-slate-500">* Obligatorio</span>
            <div class="flex gap-2">
                <button type="button" id="formCatalogoCancelar" class="boton">Cancelar</button>
                <button type="submit" id="formCatalogoGuardar" class="boton boton-principal">Guardar</button>
            </div>
        </div>
    </form>`;
}
function htmlCampo(c, item, opciones) {
    const id = `campo-${c.nombre}`;
    const valor = item?.[c.nombre] ?? "";
    // Bloqueado si: es la clave y estoy modificando, si es autogenerado y estoy creando, o si vino precargado
    const bloqueado = (item !== null && !!c.claveNegocio) || (item === null && !!c.autogenerado) || camposFijos.has(c.nombre);
    let control;
    switch (c.tipo) {
        case "seleccion": {
            const primera = c.requerido
                ? `<option value="" disabled ${valor === "" ? "selected" : ""}>Elegí una opción</option>`
                : `<option value="">(ninguno)</option>`;
            control = `<select id="${id}" class="campo" ${c.requerido ? "required" : ""} ${bloqueado ? "disabled" : ""}>
                ${primera}
                ${opciones.map((o) => `<option value="${o.valor}" ${o.valor === valor ? "selected" : ""}>${escapar(o.texto)}</option>`).join("")}
            </select>`;
            break;
        }
        case "explorer": {
            const elegido = elegidosExplorer[c.nombre];
            control = `<button type="button" id="${id}" class="campo text-left truncate" data-explorer="${c.nombre}"
                               ${bloqueado ? "disabled" : ""}>${elegido ? escapar(elegido.texto) : "Elegir..."}</button>`;
            break;
        }
        case "check": {
            control = `<label class="campo flex items-center gap-2 cursor-pointer">
                            <input id="${id}" type="checkbox" class="accent-indigo-500" ${valor === true ? "checked" : ""}>
                            ${escapar(c.textoCheck ?? "Sí")}
                        </label>`;
            break;
        }
        default: {
            const tipoInput = c.tipo === "email" ? "email" : c.tipo === "texto" ? "text" : "number";
            const extra = c.tipo === "numero" ? `step="1" min="1"` : c.tipo === "decimal" ? `step="0.01" min="0.01"` : "";
            const ayuda = item === null && c.autogenerado ? `placeholder="Se genera automáticamente"` : "";
            control = `<input id="${id}" type="${tipoInput}" class="campo" value="${escapar(String(valor))}" ${extra} ${ayuda}
                ${c.requerido ? "required" : ""} ${bloqueado ? "disabled" : ""}>`;
        }
    }
    // La ✕ para borrar un dato opcional (por el PUT completo, se manda vacío y se borra)
    const limpiar = !c.requerido && !bloqueado && c.tipo !== "explorer" && c.tipo !== "check"
        ? `<button type="button" class="boton px-3" data-limpiar="${id}" title="Borrar este dato">✕</button>`
        : "";
    return `
        <div class="flex flex-col gap-1 ${c.ancho === "completo" ? "md:col-span-2" : ""}">
            <span class="etiqueta-filtro">${escapar(c.etiqueta)}${c.requerido ? " *" : ""}${bloqueado ? " 🔒" : ""}</span>
            <div class="flex gap-2">${control}${limpiar}</div>
        </div>`;
}
function conectarFormulario(cfg) {
    const form = porId("formCatalogo");
    // Un solo "escuchador" para las ✕ y los botones de explorer
    form.addEventListener("click", async (evento) => {
        const objetivo = evento.target;
        const botonLimpiar = objetivo.closest("[data-limpiar]");
        if (botonLimpiar) {
            const campo = porId(botonLimpiar.dataset.limpiar);
            campo.value = "";
            campo.focus();
            return;
        }
        const botonExplorer = objetivo.closest("[data-explorer]");
        if (botonExplorer && !botonExplorer.disabled) {
            const campo = cfg.campos.find((c) => c.nombre === botonExplorer.dataset.explorer);
            if (!campo?.elegir)
                return;
            const elegido = await campo.elegir();
            if (elegido) {
                elegidosExplorer[campo.nombre] = elegido;
                botonExplorer.textContent = elegido.texto;
            }
        }
    });
    porId("formCatalogoCancelar").addEventListener("click", cerrarPanelConCuidado);
    form.addEventListener("submit", (evento) => {
        evento.preventDefault();
        guardar(cfg);
    });
}
// Arma el objeto para el back: un atributo por campo, con el mismo nombre
function leerFormulario(cfg) {
    const datos = {};
    for (const c of cfg.campos) {
        if (c.tipo === "explorer") {
            datos[c.nombre] = elegidosExplorer[c.nombre]?.valor ?? null;
            continue;
        }
        if (c.tipo === "check") {
            datos[c.nombre] = porId(`campo-${c.nombre}`).checked;
            continue;
        }
        const texto = porId(`campo-${c.nombre}`).value.trim();
        if (texto === "") {
            datos[c.nombre] = null; // vacío = borrar el dato (PUT completo)
        }
        else if (c.tipo === "numero" || c.tipo === "decimal" || c.tipo === "seleccion") {
            datos[c.nombre] = Number(texto);
        }
        else {
            datos[c.nombre] = texto;
        }
    }
    return datos;
}
async function guardar(cfg) {
    // Lo que el "required" del HTML no controla: los campos de tipo explorer
    for (const c of cfg.campos) {
        if (c.tipo === "explorer" && c.requerido && !elegidosExplorer[c.nombre]) {
            mostrarMensaje(`Falta elegir: ${c.etiqueta}`, true);
            return;
        }
    }
    const datos = leerFormulario(cfg);
    const boton = porId("formCatalogoGuardar");
    boton.disabled = true;
    boton.textContent = "Guardando...";
    try {
        const guardado = editando
            ? await CrudService.actualizar(cfg.ruta, editando.id, datos)
            : await CrudService.crear(cfg.ruta, datos);
        mostrarMensaje(editando
            ? "Cambios guardados"
            : `${cfg.descripcion(guardado)}: ${cfg.femenino ? "creada" : "creado"}`);
        cerrarPanel();
        seleccionado = guardado;
        buscar();
        if (!editando && cfg.despuesDeCrear) {
            await cfg.despuesDeCrear(guardado);
        }
    }
    catch (error) {
        boton.disabled = false;
        boton.textContent = "Guardar";
        // 409 al crear: ya existe, pero dado de baja → ofrecer reactivarlo
        if (error instanceof ErrorApi && error.status === 409 && !editando) {
            await ofrecerReactivar(cfg, error);
            return;
        }
        mostrarMensaje(mensajeDe(error), true);
    }
}
async function ofrecerReactivar(cfg, error) {
    const acepto = await confirmar(`${cfg.femenino ? "Esta" : "Este"} ${cfg.singular} ya existe`, `${error.message}.\n¿Querés ${cfg.femenino ? "reactivarla" : "reactivarlo"}?`, { textoAceptar: "Reactivar" });
    if (!acepto)
        return;
    try {
        const reactivado = await CrudService.reactivar(cfg.ruta, error.datos.id);
        mostrarMensaje(`${cfg.descripcion(reactivado)}: ${cfg.femenino ? "reactivada" : "reactivado"}`);
        cerrarPanel();
        seleccionado = reactivado;
        buscar();
    }
    catch (otroError) {
        mostrarMensaje(mensajeDe(otroError), true);
    }
}
// ======================= DAR DE BAJA Y REACTIVAR =======================
async function darDeBajaSeleccionado() {
    const cfg = config;
    const item = seleccionado;
    if (!cfg || !item)
        return;
    const acepto = await confirmar(`¿Dar de baja ${cfg.singular}?`, `Si da de baja a ${cfg.descripcion(item)} no estará más disponible en el sistema. \n¿Está seguro/a que desea darlo/a de baja?`, { textoAceptar: "Dar de baja", peligro: true });
    if (!acepto)
        return;
    try {
        await CrudService.darDeBaja(cfg.ruta, item.id);
        mostrarMensaje(`${cfg.descripcion(item)}: dado/a de baja`);
        buscar();
    }
    catch (error) {
        mostrarMensaje(mensajeDe(error), true);
    }
}
async function reactivarSeleccionado() {
    const cfg = config;
    const item = seleccionado;
    if (!cfg || !item)
        return;
    const acepto = await confirmar(`¿Reactivar ${cfg.singular}?`, `Si reactiva a ${cfg.descripcion(item)} vuelve a estar disponible. \n¿Está seguro/a que desea reactivarlo/a?`, { textoAceptar: "Reactivar" });
    if (!acepto)
        return;
    try {
        await CrudService.reactivar(cfg.ruta, item.id);
        mostrarMensaje(`${cfg.descripcion(item)}: reactivado/a`);
        buscar();
    }
    catch (error) {
        mostrarMensaje(mensajeDe(error), true);
    }
}
// ======================= EVENTOS (se registran una sola vez) =======================
porId("catalogoTexto").addEventListener("input", () => {
    clearTimeout(demoraBusqueda);
    demoraBusqueda = window.setTimeout(() => {
        pagina = 0;
        buscar();
    }, 300);
});
porId("catalogoBajas").addEventListener("change", () => {
    pagina = 0;
    buscar();
});
porId("catalogoFilas").addEventListener("click", (evento) => {
    const fila = evento.target.closest(".fila");
    if (!fila)
        return;
    seleccionado = items.find((i) => i.id === Number(fila.dataset.id)) ?? null;
    dibujar();
});
porId("catalogoFilas").addEventListener("dblclick", () => {
    if (seleccionado?.activo)
        abrirFormulario(seleccionado);
});
porId("catalogoAnterior").addEventListener("click", () => {
    if (pagina > 0) {
        pagina--;
        buscar();
    }
});
porId("catalogoSiguiente").addEventListener("click", () => {
    pagina++;
    buscar();
});
porId("catalogoNuevo").addEventListener("click", () => abrirFormulario(null));
porId("catalogoModificar").addEventListener("click", () => {
    if (seleccionado?.activo)
        abrirFormulario(seleccionado);
});
porId("catalogoBaja").addEventListener("click", () => darDeBajaSeleccionado());
porId("catalogoReactivar").addEventListener("click", () => reactivarSeleccionado());
porId("catalogoOrden").addEventListener("change", () => {
    pagina = 0;
    buscar();
});
