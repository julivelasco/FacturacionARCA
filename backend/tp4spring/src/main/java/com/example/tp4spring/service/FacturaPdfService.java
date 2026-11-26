package com.example.tp4spring.service;

import com.example.tp4spring.dto.FacturaDTO;
import com.example.tp4spring.dto.FacturaDetalleDTO;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class FacturaPdfService {

    private static final String EMISOR = "Supermercado";   // ← poné acá el nombre del comercio

    private static final Font TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15);
    private static final Font LETRA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 30);
    private static final Font NEGRITA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
    private static final Font NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 9);
    private static final Font CHICA = FontFactory.getFont(FontFactory.HELVETICA, 7);
    private static final Font TOTAL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font AVISO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, Font.BOLD, Color.RED);
    private static final Color GRIS = new Color(225, 225, 225);

    private final FacturaService facturaService;

    @Value("${arca.cuit}")
    private String cuitEmisor;

    public byte[] generar(Long id) throws Exception {
        FacturaDTO f = facturaService.obtener(id);
        boolean pendiente = "PENDIENTE".equals(f.getEstado());
        boolean anulada = "ANULADA".equals(f.getEstado());
        String simbolo = f.getTipoMonedaSimbolo() != null ? f.getTipoMonedaSimbolo() : "$";
        long nro = f.getNumeroComprobanteArca() != null ? f.getNumeroComprobanteArca() : f.getNumero();
        String pv = f.getPuntoVentaNumero() != null ? String.format("%05d", f.getPuntoVentaNumero()) : "-----";

        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 30, 30, 30, 30);
        PdfWriter.getInstance(doc, salida);
        doc.open();

        // Aviso (pendiente o anulada)
        if (pendiente || anulada) {
            PdfPTable aviso = new PdfPTable(1);
            aviso.setWidthPercentage(100);
            aviso.setSpacingAfter(8);
            PdfPCell c = new PdfPCell(new Phrase(pendiente ? "DOCUMENTO NO VÁLIDO COMO FACTURA" : "FACTURA ANULADA", AVISO));
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            c.setPadding(6);
            c.setBorderColor(Color.RED);
            aviso.addCell(c);
            doc.add(aviso);
        }

        // Cabecera: emisor | letra | datos del comprobante
        PdfPTable cab = new PdfPTable(new float[]{45, 10, 45});
        cab.setWidthPercentage(100);

        PdfPCell izq = new PdfPCell();
        izq.setPadding(8);
        izq.addElement(new Paragraph(EMISOR, TITULO));
        izq.addElement(new Paragraph("Condición frente al IVA: Responsable Monotributo", NORMAL));
        izq.addElement(new Paragraph("Punto de venta: " + nz(f.getPuntoVentaDescripcion()), NORMAL));
        cab.addCell(izq);

        PdfPCell centro = new PdfPCell();
        Paragraph letra = new Paragraph("C", LETRA);
        letra.setAlignment(Element.ALIGN_CENTER);
        Paragraph cod = new Paragraph("COD. 011", CHICA);
        cod.setAlignment(Element.ALIGN_CENTER);
        centro.addElement(letra);
        centro.addElement(cod);
        cab.addCell(centro);

        PdfPCell der = new PdfPCell();
        der.setPadding(8);
        der.addElement(new Paragraph(pendiente ? "COMPROBANTE" : "FACTURA", TITULO));
        der.addElement(new Paragraph("Punto de venta: " + pv + "    Comp. Nro: " + String.format("%08d", nro), NEGRITA));
        der.addElement(new Paragraph("Fecha de emisión: " + fecha(f.getFechaEmision()), NORMAL));
        der.addElement(new Paragraph("CUIT: " + cuitEmisor, NORMAL));
        cab.addCell(der);
        doc.add(cab);

        // Receptor
        PdfPTable rec = new PdfPTable(2);
        rec.setWidthPercentage(100);
        rec.setSpacingBefore(8);
        rec.addCell(celda("Apellido y Nombre / Razón Social: " + nz(f.getClienteDenominacion()), NORMAL, Element.ALIGN_LEFT));
        rec.addCell(celda("CUIT/CUIL: " + nz(f.getClienteCuitCuil()), NORMAL, Element.ALIGN_LEFT));
        rec.addCell(celda("Condición frente al IVA: " + nz(f.getCondicionIvaDenominacion()), NORMAL, Element.ALIGN_LEFT));
        rec.addCell(celda("Moneda: " + nz(f.getTipoMonedaDenominacion()), NORMAL, Element.ALIGN_LEFT));
        doc.add(rec);

        // Renglones
        PdfPTable items = new PdfPTable(new float[]{12, 43, 10, 17, 18});
        items.setWidthPercentage(100);
        items.setSpacingBefore(8);
        items.setHeaderRows(1);
        String[] titulos = {"Código", "Producto", "Cant.", "Precio unit.", "Subtotal"};
        for (int i = 0; i < titulos.length; i++) {
            PdfPCell h = celda(titulos[i], NEGRITA, i < 2 ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT);
            h.setBackgroundColor(GRIS);
            items.addCell(h);
        }
        for (FacturaDetalleDTO d : f.getDetalles()) {
            items.addCell(fila(nz(d.getArticuloCodigo()), Element.ALIGN_LEFT));
            items.addCell(fila(nz(d.getArticuloDenominacion()), Element.ALIGN_LEFT));
            items.addCell(fila(cantidad(d.getCantidad()), Element.ALIGN_RIGHT));
            items.addCell(fila(pesos(d.getPrecioUnitario(), simbolo), Element.ALIGN_RIGHT));
            items.addCell(fila(pesos(d.getImporteSubtotal(), simbolo), Element.ALIGN_RIGHT));
        }
        doc.add(items);

        // Total
        PdfPTable total = new PdfPTable(new float[]{60, 40});
        total.setWidthPercentage(100);
        total.setSpacingBefore(10);
        PdfPCell vacio = celda("", NORMAL, Element.ALIGN_LEFT);
        vacio.setBorder(Rectangle.NO_BORDER);
        total.addCell(vacio);
        total.addCell(celda("Importe total: " + pesos(f.getImporteTotal(), simbolo), TOTAL, Element.ALIGN_RIGHT));
        doc.add(total);

        // Observaciones
        if (f.getObservaciones() != null && !f.getObservaciones().isBlank()) {
            Paragraph obs = new Paragraph("Observaciones: " + f.getObservaciones(), NORMAL);
            obs.setSpacingBefore(10);
            doc.add(obs);
        }

        // Datos de ARCA
        if (f.getCae() != null) {
            PdfPTable arca = new PdfPTable(2);
            arca.setWidthPercentage(100);
            arca.setSpacingBefore(16);
            arca.addCell(celda("CAE N°: " + f.getCae(), NEGRITA, Element.ALIGN_LEFT));
            arca.addCell(celda("Fecha de Vto. de CAE: " + fecha(f.getCaeFechaVencimiento()), NEGRITA, Element.ALIGN_RIGHT));
            doc.add(arca);
        }

        doc.close();
        return salida.toByteArray();
    }

    private PdfPCell celda(String texto, Font fuente, int alineacion) {
        PdfPCell c = new PdfPCell(new Phrase(texto, fuente));
        c.setHorizontalAlignment(alineacion);
        c.setPadding(5);
        return c;
    }

    private PdfPCell fila(String texto, int alineacion) {
        PdfPCell c = celda(texto, NORMAL, alineacion);
        c.setBorder(Rectangle.BOTTOM);
        return c;
    }

    private String nz(String texto) {
        return texto == null || texto.isBlank() ? "-" : texto;
    }

    private String fecha(Date fecha) {
        return fecha == null ? "-" : new SimpleDateFormat("dd/MM/yyyy").format(fecha);
    }

    private String cantidad(double cantidad) {
        return cantidad == Math.floor(cantidad)
                ? String.valueOf((long) cantidad)
                : String.format(Locale.forLanguageTag("es-AR"), "%.2f", cantidad);
    }

    private String pesos(double importe, String simbolo) {
        DecimalFormat formato = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.forLanguageTag("es-AR")));
        return simbolo + " " + formato.format(importe);
    }
}