export function porId<T extends HTMLElement = HTMLElement>(id: string): T {
    return document.getElementById(id) as T;
}

export const formatoPesos = new Intl.NumberFormat("es-AR", { style: "currency", currency: "ARS" });

export function formatearFecha(iso: string | null): string {
    if (!iso) return "-";
    const [anio, mes, dia] = iso.split("-");
    return `${dia}/${mes}/${anio}`;
}

// Evita que un nombre con < o > rompa el HTML
export function escapar(texto: string | null | undefined): string {
    const reemplazos: Record<string, string> = { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" };
    return (texto ?? "").replace(/[&<>"']/g, (c) => reemplazos[c]);
}

let temporizadorMensaje: number | undefined;
export function mostrarMensaje(texto: string, esError = false): void {
    const caja = porId("mensaje");
    caja.textContent = texto;
    caja.classList.remove("hidden", "mensaje-ok", "mensaje-error");
    caja.classList.add(esError ? "mensaje-error" : "mensaje-ok");
    clearTimeout(temporizadorMensaje);
    temporizadorMensaje = window.setTimeout(() => caja.classList.add("hidden"), 5000);
}

export function mensajeDe(error: unknown): string {
    return error instanceof Error ? error.message : String(error);
}

// Muestra u oculta una capa que se abre encima (el panel o el explorer)
export function mostrarCapa(id: string, visible: boolean): void {
    const capa = porId(id);
    capa.classList.toggle("hidden", !visible);
    capa.classList.toggle("flex", visible);
}

// El formulario que está abierto en el panel registra cómo saber si tiene cambios sin guardar
let hayCambiosSinGuardar: () => boolean = () => false;
export function vigilarCambios(funcion: () => boolean): void {
    hayCambiosSinGuardar = funcion;
}

export function panelTieneCambios(): boolean {
    return hayCambiosSinGuardar();
}

export function abrirPanel(titulo: string, contenidoHtml: string): void {
    hayCambiosSinGuardar = () => false;   // contenido nuevo: nadie vigila hasta que un formulario se registre
    porId("panelTitulo").textContent = titulo;
    porId("panelContenido").innerHTML = contenidoHtml;
    mostrarCapa("panel", true);
}

export function cerrarPanel(): void {
    hayCambiosSinGuardar = () => false;
    mostrarCapa("panel", false);
    // al cerrar, vuelve al tamaño normal
    const ventana = porId("panelVentana");
    ventana.classList.add("max-w-5xl");
    ventana.classList.remove("max-w-none", "min-h-[85vh]");
}

// Cierra una capa (el panel, el explorer o la confirmación) al tocar afuera de la ventanita
export function cerrarAlTocarAfuera(idCapa: string, cerrar: () => void): void {
    const capa = porId(idCapa);
    let apretoAfuera = false;
    capa.addEventListener("mousedown", (evento) => {
        apretoAfuera = evento.target === capa;
    });
    capa.addEventListener("click", (evento) => {
        if (apretoAfuera && evento.target === capa) cerrar();
    });
}