package com.example.tp4spring;

import com.example.tp4spring.dto.FacturaFiltrosDTO;
import com.example.tp4spring.service.FacturaService;
import com.example.tp4spring.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;

@Component
@RequiredArgsConstructor
@Order(2)
public class GeneradorReporte implements CommandLineRunner {
    private final ReporteService reporteService;
    private final FacturaService facturaService;

    @Override
    public void run(String... args) throws Exception {
        facturaService.buscarFacturasFiltradas(new FacturaFiltrosDTO(), Pageable.unpaged()).getContent().forEach(r ->
                System.out.println("FACTURA " + r.getNumeroFactura()
                        + " | " + r.getFechaEmision()
                        + " | " + r.getClienteDenominacion()
                        + " | " + r.getCondicionIva()
                        + " | " + r.getPuntoVentaDescripcion()
                        + " | $" + r.getImporteTotal()
                        + " | items: " + r.getCantidadItems())
        );
        reporteService.generarTxt("reporte_facturas.txt");
        reporteService.generarPdf("reporte_facturas.pdf");
    }
}