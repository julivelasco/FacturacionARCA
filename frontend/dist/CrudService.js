import { pedir } from "./api.js";
// Un solo service para todos los catálogos: los endpoints son iguales, solo cambia la ruta
export class CrudService {
    static buscar(ruta, texto, incluirBajas, pagina, tamanio, orden) {
        const params = new URLSearchParams({
            texto,
            incluirBajas: String(incluirBajas),
            pagina: String(pagina),
            tamanio: String(tamanio),
        });
        if (orden)
            params.append("orden", orden);
        return pedir(`${ruta}?${params}`);
    }
    static crear(ruta, datos) {
        return pedir(ruta, { method: "POST", body: JSON.stringify(datos) });
    }
    static actualizar(ruta, id, datos) {
        return pedir(`${ruta}/${id}`, { method: "PUT", body: JSON.stringify(datos) });
    }
    static darDeBaja(ruta, id) {
        return pedir(`${ruta}/${id}`, { method: "DELETE" });
    }
    static reactivar(ruta, id) {
        return pedir(`${ruta}/${id}/reactivar`, { method: "PUT" });
    }
}
