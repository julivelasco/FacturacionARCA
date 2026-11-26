package com.example.tp4spring.arca;

import com.example.tp4spring.exception.ArcaException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class SoapCliente {

    private SoapCliente() {}

    // Manda un sobre SOAP (XML) por POST y devuelve la respuesta (XML)
    public static String enviar(String url, String sobreXml, String soapAction) {
        try {
            HttpURLConnection conexion = (HttpURLConnection) new URL(url).openConnection();
            conexion.setRequestMethod("POST");
            conexion.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
            conexion.setRequestProperty("SOAPAction", soapAction);
            conexion.setConnectTimeout(15000);
            conexion.setReadTimeout(30000);
            conexion.setDoOutput(true);

            try (OutputStream salida = conexion.getOutputStream()) {
                salida.write(sobreXml.getBytes(StandardCharsets.UTF_8));
            }

            int codigo = conexion.getResponseCode();
            InputStream entrada = codigo < 400 ? conexion.getInputStream() : conexion.getErrorStream();
            String cuerpo = entrada == null ? "" : new String(entrada.readAllBytes(), StandardCharsets.UTF_8);

            if (codigo >= 400) {
                String detalle = cuerpo.contains("<faultstring>") ? extraer(cuerpo, "faultstring") : cuerpo;
                throw new ArcaException("ARCA respondió con error (" + codigo + "): " + detalle);
            }
            return cuerpo;
        } catch (IOException e) {
            throw new ArcaException("No se pudo conectar con ARCA: " + e.getMessage(), e);
        }
    }

    // Saca el texto que está entre <etiqueta> y </etiqueta>
    public static String extraer(String xml, String etiqueta) {
        int inicio = xml.indexOf("<" + etiqueta + ">");
        int fin = xml.indexOf("</" + etiqueta + ">");
        if (inicio < 0 || fin < 0) {
            throw new ArcaException("Respuesta inesperada de ARCA: falta <" + etiqueta + ">");
        }
        return xml.substring(inicio + etiqueta.length() + 2, fin);
    }
}