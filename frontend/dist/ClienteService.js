import { pedir } from "./api.js";
export class ClienteService {
    static buscar(texto, incluirBajas, pagina, tamanio) {
        const params = new URLSearchParams({
            texto,
            incluirBajas: String(incluirBajas),
            pagina: String(pagina),
            tamanio: String(tamanio),
        });
        return pedir(`/clientes?${params}`);
    }
}
