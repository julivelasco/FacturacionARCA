import { pedir } from "./api.js";
import type { Pagina } from "./models/pagina.js";
import type { ClienteDTO } from "./models/cliente.dto.js";

export class ClienteService {

    static buscar(texto: string, incluirBajas: boolean, pagina: number, tamanio: number): Promise<Pagina<ClienteDTO>> {
        const params = new URLSearchParams({
            texto,
            incluirBajas: String(incluirBajas),
            pagina: String(pagina),
            tamanio: String(tamanio),
        });
        return pedir(`/clientes?${params}`);
    }
}