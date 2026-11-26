import { pedir } from "./api.js";
export class FacturaService {
    static buscar(filtros, pagina, tamanio) {
        const params = new URLSearchParams();
        Object.entries(filtros).forEach(([clave, valor]) => {
            if (valor !== undefined && valor !== null && valor !== "") {
                params.append(clave, String(valor));
            }
        });
        params.append("pagina", String(pagina));
        params.append("tamanio", String(tamanio));
        return pedir(`/facturas?${params}`);
    }
    static obtener(id) {
        return pedir(`/facturas/${id}`);
    }
    static crear(datos) {
        return pedir("/facturas", { method: "POST", body: JSON.stringify(datos) });
    }
    static actualizar(id, datos) {
        return pedir(`/facturas/${id}`, { method: "PUT", body: JSON.stringify(datos) });
    }
    static eliminar(id) {
        return pedir(`/facturas/${id}`, { method: "DELETE" });
    }
    static anular(id) {
        return pedir(`/facturas/${id}/anular`, { method: "PUT" });
    }
    static informarArca(id) {
        return pedir(`/facturas/${id}/informar-arca`, { method: "POST" });
    }
}
