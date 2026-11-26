export const API = "http://localhost:8080/api";

// Un error del back que, además del mensaje, guarda el código HTTP y los datos de la respuesta
export class ErrorApi extends Error {
    constructor(mensaje: string, public status: number, public datos: any) {
        super(mensaje);
    }
}

// Hace el pedido al back y, si falla, tira un Error con el mensaje que mandó el back
export async function pedir<T>(url: string, opciones: RequestInit = {}): Promise<T> {
    const response = await fetch(API + url, {
        headers: { "Content-Type": "application/json" },
        ...opciones,
    });

    if (!response.ok) {
        let datos: any = null;
        try {
            datos = await response.json();
        } catch {
            // la respuesta no traía JSON
        }
        throw new ErrorApi(datos?.mensaje ?? `Error ${response.status}`, response.status, datos);
    }

    if (response.status === 204) {
        return undefined as T;   // 204 No Content: no hay nada que leer
    }
    return response.json() as Promise<T>;
}