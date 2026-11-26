package com.example.tp4spring.service;

import com.example.tp4spring.dto.FacturaFiltrosDTO;
import com.example.tp4spring.dto.FacturaReporteDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.lowagie.text.Document;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;

@Service
@RequiredArgsConstructor
public class ReporteService {

    private final FacturaService facturaService;

    public void generarTxt(String ruta) throws IOException {
        List<String> lineas = new ArrayList<>();
        lineas.add("Numero\tFecha\tCliente\tCondicionIVA\tPuntoVenta\tImporteTotal\tItems");

        for (FacturaReporteDTO r : facturaService.buscarFacturasFiltradas(new FacturaFiltrosDTO(), Pageable.unpaged()).getContent()) {
            lineas.add(r.getNumeroFactura() + "\t"
                    + r.getFechaEmision() + "\t"
                    + r.getClienteDenominacion() + "\t"
                    + r.getCondicionIva() + "\t"
                    + r.getPuntoVentaDescripcion() + "\t"
                    + r.getImporteTotal() + "\t"
                    + r.getCantidadItems());
        }

        Files.write(Path.of(ruta), lineas, StandardCharsets.UTF_8);
    }
    public void generarPdf(String ruta) throws Exception {
        Document doc = new Document();
        PdfWriter.getInstance(doc, new FileOutputStream(ruta));
        doc.open();

        doc.add(new Paragraph("Reporte de Facturas",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
        doc.add(new Paragraph(" "));

        PdfPTable tabla = new PdfPTable(7);
        tabla.setWidthPercentage(100);

        String[] encabezados = {"Número", "Fecha", "Cliente", "Condición IVA",
                "Punto de venta", "Importe total", "Ítems"};
        for (String h : encabezados) {
            tabla.addCell(new Phrase(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
        }

        for (FacturaReporteDTO r : facturaService.buscarFacturasFiltradas(new FacturaFiltrosDTO(), Pageable.unpaged()).getContent()) {
            tabla.addCell(String.valueOf(r.getNumeroFactura()));
            tabla.addCell(String.valueOf(r.getFechaEmision()));
            tabla.addCell(r.getClienteDenominacion());
            tabla.addCell(r.getCondicionIva());
            tabla.addCell(r.getPuntoVentaDescripcion());
            tabla.addCell(String.valueOf(r.getImporteTotal()));
            tabla.addCell(String.valueOf(r.getCantidadItems()));
        }

        doc.add(tabla);
        doc.close();
    }
}