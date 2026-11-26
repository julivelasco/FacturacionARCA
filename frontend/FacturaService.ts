import { pedir } from "./api.js";
import type { Pagina } from "./models/pagina.js";
import type {
    FacturaDTO, FacturaFiltrosDTO, FacturaReporteDTO, FacturaRequestDTO
} from "./models/factura.dto.js";

export class FacturaService {

    static buscar(filtros: FacturaFiltrosDTO, pagina: number, tamanio: number): Promise<Pagina<FacturaReporteDTO>> {
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

    static obtener(id: number): Promise<FacturaDTO> {
        return pedir(`/facturas/${id}`);
    }

    static crear(datos: FacturaRequestDTO): Promise<FacturaReporteDTO> {
        return pedir("/facturas", { method: "POST", body: JSON.stringify(datos) });
    }

    static actualizar(id: number, datos: FacturaRequestDTO): Promise<FacturaReporteDTO> {
        return pedir(`/facturas/${id}`, { method: "PUT", body: JSON.stringify(datos) });
    }

    static eliminar(id: number): Promise<void> {
        return pedir(`/facturas/${id}`, { method: "DELETE" });
    }

    static anular(id: number): Promise<FacturaReporteDTO> {
        return pedir(`/facturas/${id}/anular`, { method: "PUT" });
    }

    static informarArca(id: number): Promise<FacturaDTO> {
        return pedir(`/facturas/${id}/informar-arca`, { method: "POST" });
    }
}