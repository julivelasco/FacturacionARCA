import { mensajeDe, mostrarCapa, mostrarMensaje, porId, cerrarAlTocarAfuera, escapar } from "./utilidades.js";
const TAMANIO = 8;
let config = null;
let items = [];
let elegido = null;
let pagina = 0;
let totalPaginas = 0;
let resolver = null;
let focoPrevio = null; // el campo que abrió el explorer, para devolverle el cursor
let demoraBusqueda;
// La última opción elegida en el filtro de cada explorer (por título), para no elegirla cada vez
const filtroRecordado = {};
// Abre el explorer y devuelve "un vale" (Promise) que se cobra cuando el usuario elige o cierra
let pedidoActual = 0; // numera las búsquedas, para descartar las que llegan tarde
export function abrirExplorer(nuevaConfig) {
    config = nuevaConfig;
    pagina = 0;
    elegido = null;
    items = [];
    porId("explorerTitulo").textContent = nuevaConfig.titulo;
    porId("explorerTexto").value = "";
    porId("explorerFilas").innerHTML = ""; // que no se vea lo de la vez anterior
    const encabezado = porId("explorerEncabezado");
    encabezado.style.gridTemplateColumns = plantillaColumnas(nuevaConfig.columnas);
    encabezado.innerHTML = nuevaConfig.columnas
        .map((c) => `<span class="${alineacion(c)}">${c.titulo}</span>`)
        .join("");
    porId("explorerBajas").checked = false;
    const contenedorBajas = porId("explorerBajasContenedor");
    contenedorBajas.classList.toggle("hidden", !nuevaConfig.permiteVerBajas);
    contenedorBajas.classList.toggle("flex", !!nuevaConfig.permiteVerBajas);
    focoPrevio = document.activeElement;
    mostrarCapa("explorer", true);
    porId("explorerTexto").focus();
    // Primero el desplegable (con la opción recordada) y RECIÉN DESPUÉS una sola búsqueda
    prepararFiltro(config).then(() => buscar());
    return new Promise((resolve) => {
        resolver = (item) => resolve(item);
    });
}
async function prepararFiltro(cfg) {
    const select = porId("explorerFiltro");
    select.classList.toggle("hidden", !cfg.filtro);
    select.innerHTML = "";
    if (!cfg.filtro)
        return;
    try {
        const opciones = await cfg.filtro.opciones();
        select.innerHTML = `<option value="">${cfg.filtro.etiqueta}</option>` +
            opciones.map((o) => `<option value="${o.valor}">${escapar(o.texto)}</option>`).join("");
        select.value = filtroRecordado[cfg.titulo] ?? "";
    }
    catch (error) {
        mostrarMensaje(mensajeDe(error), true);
    }
}
export function explorerAbierto() {
    return config !== null;
}
export function cerrarExplorer() {
    terminar(null);
}
function terminar(item) {
    mostrarCapa("explorer", false);
    const cobrarVale = resolver;
    const volverA = focoPrevio;
    resolver = null;
    config = null;
    focoPrevio = null;
    cobrarVale?.(item); // acá "se cobra el vale": el await de quien abrió el explorer sigue
    setTimeout(() => volverA?.focus(), 0); // el cursor vuelve al campo, y con Tab seguís al próximo
}
function plantillaColumnas(columnas) {
    return columnas.map((c) => c.ancho ?? "minmax(0, 1fr)").join(" ");
}
function alineacion(columna) {
    if (columna.alinear === "right")
        return "text-right";
    if (columna.alinear === "center")
        return "text-center";
    return "";
}
async function buscar() {
    const actual = config;
    if (!actual)
        return;
    const miPedido = ++pedidoActual; // este es el pedido número N
    try {
        const texto = porId("explorerTexto").value.trim();
        const incluirBajas = porId("explorerBajas").checked;
        const valorFiltro = porId("explorerFiltro").value;
        const resultado = await actual.buscar(texto, incluirBajas, pagina, TAMANIO, valorFiltro ? Number(valorFiltro) : undefined);
        // Si mientras esperábamos salió otro pedido más nuevo, esta respuesta ya no sirve
        if (miPedido !== pedidoActual)
            return;
        items = resultado.content;
        totalPaginas = resultado.page.totalPages;
        elegido = items.find((i) => i.id === elegido?.id) ?? null;
        dibujar();
        porId("explorerPagina").textContent = totalPaginas === 0 ? "Sin resultados" : `${pagina + 1} de ${totalPaginas}`;
        porId("explorerAnterior").disabled = pagina === 0;
        porId("explorerSiguiente").disabled = pagina + 1 >= totalPaginas;
    }
    catch (error) {
        if (miPedido === pedidoActual)
            mostrarMensaje(mensajeDe(error), true);
    }
}
function dibujar() {
    const actual = config;
    const contenedor = porId("explorerFilas");
    porId("explorerElegir").disabled = !elegido;
    if (!actual)
        return;
    if (items.length === 0) {
        contenedor.innerHTML = `<div class="vidrio rounded-2xl p-6 text-center text-slate-500">No se encontró nada.</div>`;
        return;
    }
    const plantilla = plantillaColumnas(actual.columnas);
    contenedor.innerHTML = items.map((item) => `
        <div class="fila fila-neutral vidrio rounded-2xl px-4 py-2 grid gap-3 items-center ${elegido?.id === item.id ? "seleccionada" : ""}"
             style="grid-template-columns: ${plantilla}" data-id="${item.id}">
            ${actual.columnas.map((c) => `<span class="truncate ${alineacion(c)}">${c.valor(item)}</span>`).join("")}
        </div>`).join("");
}
// ======================= EVENTOS (se registran una sola vez) =======================
porId("explorerFiltro").addEventListener("change", () => {
    if (config)
        filtroRecordado[config.titulo] = porId("explorerFiltro").value;
    pagina = 0;
    buscar();
});
porId("explorerBajas").addEventListener("change", () => {
    pagina = 0;
    buscar();
});
porId("explorerBusqueda").addEventListener("submit", (evento) => {
    evento.preventDefault();
    pagina = 0;
    buscar();
});
// Busca solo mientras escribís, con una pausita para no consultar en cada letra
porId("explorerTexto").addEventListener("input", () => {
    elegido = null;
    dibujar();
    clearTimeout(demoraBusqueda);
    demoraBusqueda = window.setTimeout(() => {
        pagina = 0;
        buscar();
    }, 300);
});
porId("explorerFilas").addEventListener("click", (evento) => {
    const fila = evento.target.closest(".fila");
    if (!fila)
        return;
    elegido = items.find((i) => i.id === Number(fila.dataset.id)) ?? null;
    dibujar();
});
porId("explorerFilas").addEventListener("dblclick", () => {
    if (elegido)
        terminar(elegido);
});
porId("explorerElegir").addEventListener("click", () => {
    if (elegido)
        terminar(elegido);
});
porId("botonCerrarExplorer").addEventListener("click", () => terminar(null));
porId("explorerAnterior").addEventListener("click", () => {
    if (pagina > 0) {
        pagina--;
        buscar();
    }
});
porId("explorerSiguiente").addEventListener("click", () => {
    pagina++;
    buscar();
});
// Teclado: ↑ ↓ para marcar una fila y Enter para elegirla
porId("explorer").addEventListener("keydown", (evento) => {
    if (!config)
        return;
    const objetivo = evento.target;
    if (objetivo instanceof HTMLSelectElement)
        return; // en el desplegable las flechas cambian la opción
    if (evento.key === "ArrowDown" || evento.key === "ArrowUp") {
        if (items.length === 0)
            return;
        evento.preventDefault();
        const actual = items.findIndex((i) => i.id === elegido?.id);
        const siguiente = evento.key === "ArrowDown"
            ? Math.min(actual + 1, items.length - 1)
            : Math.max(actual - 1, 0);
        elegido = items[siguiente];
        dibujar();
        porId("explorerFilas").querySelector(".seleccionada")?.scrollIntoView({ block: "nearest" });
    }
    else if (evento.key === "Enter" && objetivo.id === "explorerTexto" && elegido) {
        evento.preventDefault();
        terminar(elegido);
    }
});
cerrarAlTocarAfuera("explorer", () => terminar(null));
