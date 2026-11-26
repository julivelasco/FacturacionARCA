package com.example.tp4spring.arca;

import com.example.tp4spring.exception.ArcaException;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

@Service
public class ArcaAutenticacionService {

    // El "pase" que da ARCA: token + sign, y hasta cuándo vale
    public record TicketAcceso(String token, String sign, OffsetDateTime vencimiento) {}

    @Value("${arca.clave-privada}")
    private String rutaClavePrivada;
    @Value("${arca.certificado}")
    private String rutaCertificado;
    @Value("${arca.ticket}")
    private String rutaTicket;
    @Value("${arca.url-wsaa}")
    private String urlWsaa;

    private TicketAcceso ticket;

    public synchronized TicketAcceso obtenerTicket() {
        if (ticket == null) {
            ticket = leerTicketGuardado();
        }
        // Si no hay pase, o vence en menos de 10 minutos, pedimos uno nuevo
        if (ticket == null || ticket.vencimiento().isBefore(OffsetDateTime.now().plusMinutes(10))) {
            ticket = pedirTicketNuevo();
            guardarTicket(ticket);
        }
        return ticket;
    }

    private TicketAcceso pedirTicketNuevo() {
        try {
            if (!Files.exists(Path.of(rutaClavePrivada)) || !Files.exists(Path.of(rutaCertificado))) {
                throw new ArcaException("ARCA no está configurado en esta computadora: faltan el certificado y la clave privada. " +
                        "Los datos y las demás pantallas funcionan normalmente.");
            }
            if (Security.getProvider("BC") == null) {
                Security.addProvider(new BouncyCastleProvider());
            }
            PrivateKey clavePrivada = cargarClavePrivada();
            X509Certificate certificado = cargarCertificado();

            String pedido = armarPedidoDeAcceso();
            String pedidoFirmado = firmar(pedido, clavePrivada, certificado);

            String sobre = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
                    "xmlns:wsaa=\"http://wsaa.view.sua.dvadac.desein.afip.gov\">" +
                    "<soapenv:Header/><soapenv:Body><wsaa:loginCms><wsaa:in0>" + pedidoFirmado +
                    "</wsaa:in0></wsaa:loginCms></soapenv:Body></soapenv:Envelope>";

            String respuesta = SoapCliente.enviar(urlWsaa, sobre, "");

            // El pase viene como XML "escondido" adentro del XML: lo destapamos
            String xml = respuesta.replace("&lt;", "<").replace("&gt;", ">");
            String token = SoapCliente.extraer(xml, "token");
            String sign = SoapCliente.extraer(xml, "sign");
            OffsetDateTime vencimiento = OffsetDateTime.parse(SoapCliente.extraer(xml, "expirationTime"));

            return new TicketAcceso(token, sign, vencimiento);
        } catch (ArcaException e) {
            throw e;
        } catch (Exception e) {
            throw new ArcaException("No se pudo iniciar sesión en ARCA: " + e.getMessage(), e);
        }
    }

    // El "quiero entrar al servicio wsfe", válido por unos minutos
    private String armarPedidoDeAcceso() {
        ZonedDateTime ahora = ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        DateTimeFormatter formato = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<loginTicketRequest version=\"1.0\">" +
                "<header>" +
                "<uniqueId>" + (System.currentTimeMillis() / 1000) + "</uniqueId>" +
                "<generationTime>" + ahora.minusMinutes(5).format(formato) + "</generationTime>" +
                "<expirationTime>" + ahora.plusMinutes(10).format(formato) + "</expirationTime>" +
                "</header>" +
                "<service>wsfe</service>" +
                "</loginTicketRequest>";
    }

    // Firma el pedido con la clave privada y le adjunta el certificado
    private String firmar(String pedido, PrivateKey clavePrivada, X509Certificate certificado) throws Exception {
        CMSSignedDataGenerator generador = new CMSSignedDataGenerator();
        ContentSigner firmante = new JcaContentSignerBuilder("SHA256withRSA").setProvider("BC").build(clavePrivada);
        generador.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(
                new JcaDigestCalculatorProviderBuilder().setProvider("BC").build()).build(firmante, certificado));
        generador.addCertificates(new JcaCertStore(Collections.singletonList(certificado)));
        CMSSignedData firmado = generador.generate(
                new CMSProcessableByteArray(pedido.getBytes(StandardCharsets.UTF_8)), true);
        return Base64.getEncoder().encodeToString(firmado.getEncoded());
    }

    private PrivateKey cargarClavePrivada() throws Exception {
        String pem = Files.readString(Path.of(rutaClavePrivada));
        String base64 = pem.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] bytes = Base64.getDecoder().decode(base64);
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(bytes));
    }

    private X509Certificate cargarCertificado() throws Exception {
        try (FileInputStream archivo = new FileInputStream(rutaCertificado)) {
            return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(archivo);
        }
    }

    private void guardarTicket(TicketAcceso ticketNuevo) {
        try {
            Files.write(Path.of(rutaTicket),
                    List.of(ticketNuevo.token(), ticketNuevo.sign(), ticketNuevo.vencimiento().toString()));
        } catch (IOException e) {
            // No es grave: solo se pierde el pase guardado
        }
    }

    private TicketAcceso leerTicketGuardado() {
        try {
            Path archivo = Path.of(rutaTicket);
            if (!Files.exists(archivo)) {
                return null;
            }
            List<String> lineas = Files.readAllLines(archivo);
            return new TicketAcceso(lineas.get(0), lineas.get(1), OffsetDateTime.parse(lineas.get(2)));
        } catch (Exception e) {
            return null;
        }
    }
}