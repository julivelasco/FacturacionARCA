package com.example.tp4spring.arca;

import com.example.tp4spring.exception.ArcaException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ArcaFacturacionService {

    public static final int FACTURA_C = 11;
    private static final String NS = "http://ar.gov.afip.dif.FEV1/";

    // Lo que hay que mandarle a ARCA de una factura
    public record SolicitudCae(int puntoVenta, int tipoComprobante, int docTipo, long docNro,
                               int condicionIvaReceptor, String moneda, double importeTotal) {}

    // Lo que ARCA contesta
    public record ResultadoCae(boolean aprobado, String resultado, Long numeroComprobante,
                               String cae, LocalDate vencimientoCae, String mensaje) {}

    private final ArcaAutenticacionService autenticacion;

    @Value("${arca.url-wsfe}")
    private String urlWsfe;
    @Value("${arca.cuit}")
    private String cuit;

    // Cuál fue el último número autorizado por ARCA para ese punto de venta y tipo
    public long ultimoAutorizado(int puntoVenta, int tipoComprobante) {
        String cuerpo = "<ar:FECompUltimoAutorizado>" + auth() +
                "<ar:PtoVta>" + puntoVenta + "</ar:PtoVta>" +
                "<ar:CbteTipo>" + tipoComprobante + "</ar:CbteTipo>" +
                "</ar:FECompUltimoAutorizado>";
        String respuesta = enviar("FECompUltimoAutorizado", cuerpo);
        revisarErrores(respuesta);
        return Long.parseLong(SoapCliente.extraer(respuesta, "CbteNro"));
    }

    public ResultadoCae solicitarCae(SolicitudCae s) {
        long numero = ultimoAutorizado(s.puntoVenta(), s.tipoComprobante()) + 1;
        String total = String.format(Locale.US, "%.2f", s.importeTotal());
        String fecha = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);   // yyyyMMdd

        String cuerpo = "<ar:FECAESolicitar>" + auth() +
                "<ar:FeCAEReq>" +
                "<ar:FeCabReq>" +
                "<ar:CantReg>1</ar:CantReg>" +
                "<ar:PtoVta>" + s.puntoVenta() + "</ar:PtoVta>" +
                "<ar:CbteTipo>" + s.tipoComprobante() + "</ar:CbteTipo>" +
                "</ar:FeCabReq>" +
                "<ar:FeDetReq><ar:FECAEDetRequest>" +
                "<ar:Concepto>1</ar:Concepto>" +                            // 1 = productos
                "<ar:DocTipo>" + s.docTipo() + "</ar:DocTipo>" +
                "<ar:DocNro>" + s.docNro() + "</ar:DocNro>" +
                "<ar:CbteDesde>" + numero + "</ar:CbteDesde>" +
                "<ar:CbteHasta>" + numero + "</ar:CbteHasta>" +
                "<ar:CbteFch>" + fecha + "</ar:CbteFch>" +
                "<ar:ImpTotal>" + total + "</ar:ImpTotal>" +
                "<ar:ImpTotConc>0</ar:ImpTotConc>" +
                "<ar:ImpNeto>" + total + "</ar:ImpNeto>" +                  // Factura C: neto = total
                "<ar:ImpOpEx>0</ar:ImpOpEx>" +
                "<ar:ImpTrib>0</ar:ImpTrib>" +
                "<ar:ImpIVA>0</ar:ImpIVA>" +                                // Factura C: sin IVA discriminado
                "<ar:MonId>" + s.moneda() + "</ar:MonId>" +
                "<ar:MonCotiz>1</ar:MonCotiz>" +
                "<ar:CondicionIVAReceptorId>" + s.condicionIvaReceptor() + "</ar:CondicionIVAReceptorId>" +
                "</ar:FECAEDetRequest></ar:FeDetReq>" +
                "</ar:FeCAEReq>" +
                "</ar:FECAESolicitar>";

        String respuesta = enviar("FECAESolicitar", cuerpo);
        revisarErrores(respuesta);

        String resultado = SoapCliente.extraer(respuesta, "Resultado");
        if ("A".equals(resultado)) {   // A = aprobada
            String cae = SoapCliente.extraer(respuesta, "CAE");
            LocalDate vencimiento = LocalDate.parse(
                    SoapCliente.extraer(respuesta, "CAEFchVto"), DateTimeFormatter.BASIC_ISO_DATE);
            return new ResultadoCae(true, resultado, numero, cae, vencimiento, null);
        }
        String motivo = respuesta.contains("<Obs>")
                ? SoapCliente.extraer(respuesta, "Msg")
                : "ARCA rechazó la factura sin indicar el motivo";
        return new ResultadoCae(false, resultado, null, null, null, motivo);
    }

    // El pase (token y sign) + el CUIT, que van en todos los pedidos
    private String auth() {
        ArcaAutenticacionService.TicketAcceso ticket = autenticacion.obtenerTicket();
        return "<ar:Auth>" +
                "<ar:Token>" + ticket.token() + "</ar:Token>" +
                "<ar:Sign>" + ticket.sign() + "</ar:Sign>" +
                "<ar:Cuit>" + cuit + "</ar:Cuit>" +
                "</ar:Auth>";
    }

    private String enviar(String metodo, String cuerpo) {
        String sobre = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
                "xmlns:ar=\"" + NS + "\">" +
                "<soapenv:Header/><soapenv:Body>" + cuerpo + "</soapenv:Body></soapenv:Envelope>";
        return SoapCliente.enviar(urlWsfe, sobre, NS + metodo);
    }

    private void revisarErrores(String respuesta) {
        if (respuesta.contains("<Err>")) {
            String codigo = SoapCliente.extraer(respuesta, "Code");
            String mensaje = SoapCliente.extraer(respuesta, "Msg");
            throw new ArcaException("ARCA devolvió el error " + codigo + ": " + mensaje);
        }
    }
}