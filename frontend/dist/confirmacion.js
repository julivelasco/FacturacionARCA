import { cerrarAlTocarAfuera, cerrarPanel, mostrarCapa, panelTieneCambios, porId } from "./utilidades.js";
let resolver = null;
// Abre la ventanita y devuelve "un vale" que se cobra con true (aceptó) o false (canceló)
export function confirmar(titulo, mensaje, opciones = {}) {
    porId("confirmacionTitulo").textContent = titulo;
    porId("confirmacionMensaje").textContent = mensaje;
    const botonSi = porId("confirmacionSi");
    botonSi.textContent = opciones.textoAceptar ?? "Aceptar";
    botonSi.classList.toggle("boton-peligro", !!opciones.peligro);
    botonSi.classList.toggle("boton-principal", !opciones.peligro);
    mostrarCapa("confirmacion", true);
    botonSi.focus(); // así, con Enter se acepta
    return new Promise((resolve) => {
        resolver = resolve;
    });
}
export function confirmacionAbierta() {
    return resolver !== null;
}
export function responder(respuesta) {
    mostrarCapa("confirmacion", false);
    const cobrarVale = resolver;
    resolver = null;
    cobrarVale?.(respuesta);
}
porId("confirmacionSi").addEventListener("click", () => responder(true));
porId("confirmacionNo").addEventListener("click", () => responder(false));
porId("confirmacionCerrar").addEventListener("click", () => responder(false));
cerrarAlTocarAfuera("confirmacion", () => responder(false));
// Cerrar el panel "con cuidado": si hay cambios sin guardar, pregunta antes
export async function cerrarPanelConCuidado() {
    if (panelTieneCambios()) {
        const acepto = await confirmar("¿Descartar los cambios?", "Hay cambios sin guardar. Si cierra, se perderán\n ¿Desea descartar los cambios?", { textoAceptar: "Descartar", peligro: true });
        if (!acepto)
            return;
    }
    cerrarPanel();
}
