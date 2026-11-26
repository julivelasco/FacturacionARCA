import { cerrarAlTocarAfuera, cerrarPanel, mostrarCapa, panelTieneCambios, porId } from "./utilidades.js";

export interface OpcionesConfirmacion {
    textoAceptar?: string;   // el texto del botón de aceptar (por defecto "Aceptar")
    peligro?: boolean;       // true: el botón de aceptar sale en rojo
}

let resolver: ((respuesta: boolean) => void) | null = null;

// Abre la ventanita y devuelve "un vale" que se cobra con true (aceptó) o false (canceló)
export function confirmar(titulo: string, mensaje: string, opciones: OpcionesConfirmacion = {}): Promise<boolean> {
    porId("confirmacionTitulo").textContent = titulo;
    porId("confirmacionMensaje").textContent = mensaje;

    const botonSi = porId("confirmacionSi");
    botonSi.textContent = opciones.textoAceptar ?? "Aceptar";
    botonSi.classList.toggle("boton-peligro", !!opciones.peligro);
    botonSi.classList.toggle("boton-principal", !opciones.peligro);

    mostrarCapa("confirmacion", true);
    botonSi.focus();   // así, con Enter se acepta

    return new Promise((resolve) => {
        resolver = resolve;
    });
}

export function confirmacionAbierta(): boolean {
    return resolver !== null;
}

export function responder(respuesta: boolean): void {
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
export async function cerrarPanelConCuidado(): Promise<void> {
    if (panelTieneCambios()) {
        const acepto = await confirmar(
            "¿Descartar los cambios?",
            "Hay cambios sin guardar. Si cierra, se perderán\n ¿Desea descartar los cambios?",
            { textoAceptar: "Descartar", peligro: true });
        if (!acepto) return;
    }
    cerrarPanel();
}