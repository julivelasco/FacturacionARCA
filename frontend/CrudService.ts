import { pedir } from "./api.js";
import type { Pagina } from "./models/pagina.js";

// Un solo service para todos los catálogos: los endpoints son iguales, solo cambia la ruta
export class CrudService {

    static buscar<T>(ruta: string, texto: string, incluirBajas: boolean, pagina: number, tamanio: number, orden?: string): Promise<Pagina<T>> {
        const params = new URLSearchParams({
            texto,
            incluirBajas: String(incluirBajas),
            pagina: String(pagina),
            tamanio: String(tamanio),
        });
        if (orden) params.append("orden", orden);
        return pedir(`${ruta}?${params}`);
    }

    static crear<T>(ruta: string, datos: object): Promise<T> {
        return pedir(ruta, { method: "POST", body: JSON.stringify(datos) });
    }

    static actualizar<T>(ruta: string, id: number, datos: object): Promise<T> {
        return pedir(`${ruta}/${id}`, { method: "PUT", body: JSON.stringify(datos) });
    }

    static darDeBaja(ruta: string, id: number): Promise<void> {
        return pedir(`${ruta}/${id}`, { method: "DELETE" });
    }

    static reactivar<T>(ruta: string, id: number): Promise<T> {
        return pedir(`${ruta}/${id}/reactivar`, { method: "PUT" });
    }
}