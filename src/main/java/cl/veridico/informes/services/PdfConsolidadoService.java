package cl.veridico.informes.services;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import cl.veridico.informes.services.RespuestaInformeConsolidado.ConsultaResponse;
import cl.veridico.informes.utils.PdfUtils;
import cl.veridico.informes.utils.UtilesVarios;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class PdfConsolidadoService {

    @Value("${vrdc.fe.servicios.url}")
    private String urlServiciosVeridico;

    private final String URL_VERIFICACION_DOCUMENTO = "/verifica-informe/paso1";

    public byte[] generatePdfFromConsulta(ConsultaResponse consulta, String folio, String codigoVerificacion)
            throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        try {

            InputStream fontStream = getClass().getResourceAsStream("/fonts/Montserrat-Regular.ttf");
            PdfFont font = PdfUtils.setFontPDF(fontStream);
            document.setFont(font);

            // logo
            PdfUtils.agregarLogo(document);

            // QR
            PdfUtils.agregarQR(urlServiciosVeridico, URL_VERIFICACION_DOCUMENTO, document, folio,
                    codigoVerificacion,
                    font);

            // ✅ Título principal
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.GERMANY);
            DecimalFormat df = new DecimalFormat("###,###,###", symbols);
            String runFormateado = df.format(consulta.getResultado().getDatosPersonales().getRut()) + "-" +
                    consulta.getResultado().getDatosPersonales().getDigito();
            PdfUtils.agregarTitulo(document,
                    "Informe Verídico Consolidado",
                    consulta.getResultado().getDatosPersonales().getNombre(),
                    runFormateado);

            document.add(new Paragraph("\n"));

            // Datos para generar el header de resumen gráfico de la información
            int cantidadDeudasLaborales = consulta.getResultado().getDeudasLaborales().size();
            int cantidadDeudasPrevisionales = consulta.getResultado().getDeudasPrevisionales().size();

            int cantidadDeudasComercio = consulta.getResultado().getDeudasComercio().size();
            int cantidadProtestos = consulta.getResultado().getProtestos().size();
            int cantidadInfocom = consulta.getResultado().getInfocom().size();

            boolean alertaRojaDeudasPrevisionales = (cantidadDeudasPrevisionales > 0 || cantidadDeudasLaborales > 0
                    ? true
                    : false);
            boolean alertaRojaMorososProtestos = (cantidadDeudasComercio > 0 ||
                    cantidadProtestos > 0 ||
                    cantidadInfocom > 0 ? true : false);

            List<String> nombresHeader = List.of(
                    "protymor",
                    "boletin",
                    "blanco",
                    "blanco",
                    "blanco",
                    "blanco");
            List<Boolean> valoresHeader = List.of(
                    alertaRojaMorososProtestos,
                    alertaRojaDeudasPrevisionales,
                    true,
                    true,
                    true,
                    true);

            PdfUtils.dibujarImagenesIndicadoresConGlosas(document, nombresHeader, valoresHeader);
            // fin Header de resumen del informe

            // Muestra de Score de Riesgo
            int score = consulta.getResultado().getDatosScore().getPuntaje();

            List<String> nombresScore = List.of(
                    "950A",
                    "800B",
                    "700B",
                    "500C",
                    "350D",
                    "200E");
            List<Boolean> valoresScore = List.of(
                    (score >= 950 ? true : false),
                    (score >= 800 && score < 950 ? true : false),
                    (score >= 700 && score < 800 ? true : false),
                    (score >= 500 && score < 700 ? true : false),
                    (score >= 350 && score < 500 ? true : false),
                    (score >= 0 && score < 350 ? true : false));

            PdfUtils.dibujarImagenesScore(document, nombresScore, valoresScore);

            // Deudas Previsionales
            if (consulta.getResultado() != null
                    && consulta.getResultado().getDeudasPrevisionales() != null) {

                List<RespuestaInformeConsolidado.ConsultaResponse.Resultado.DeudaPrevisional> listaPrevisionales = consulta
                        .getResultado().getDeudasPrevisionales();

                for (RespuestaInformeConsolidado.ConsultaResponse.Resultado.DeudaPrevisional item : listaPrevisionales) {
                    String acreedor = item.getAcreedor();
                    if (acreedor == null)
                        acreedor = "";

                    List<SimpleEntry<String, String>> datoDP = new ArrayList<>(List.of(
                            new SimpleEntry<>("Acreedor", acreedor),
                            new SimpleEntry<>("Monto en $",
                                    UtilesVarios.formatCurrency(
                                            item.getMontoPesos())),
                            new SimpleEntry<>("Monto en UTM",
                                    df.format(item.getMontoUTM()))));
                    PdfUtils.agregarTablaDatos(document, "DEUDAS PREVISIONALES", datoDP);

                    // Trabajador Afectado
                    if (item.getTrabajadoresAfectados() != null) {
                        String[] titTAfectado = new String[] { "Rut Trabajador", "DV", "Nombre",
                                "Período Deuda",
                                "Monto Deuda $", "Monto Deuda UTM", "Número Boletín",
                                "Motivo" };
                        float[] anchoColumnasTAfectado = new float[] { 100, 100, 100, 100, 100,
                                100, 100, 100 };
                        String[] formatoTAfectado = new String[] { "", "", "capital", "",
                                "moneda$", "", "",
                                "capital" };
                        PdfUtils.agregarSeccionTablaConFormato(document, "Trabajador Afectado",
                                item.getTrabajadoresAfectados(), titTAfectado,
                                anchoColumnasTAfectado,
                                formatoTAfectado);
                    }
                }
            }

            // Deudas Laborales
            if (consulta.getResultado() != null && consulta.getResultado().getDeudasLaborales() != null) {
                String[] titLab = new String[] { "Fecha Boletín", "Número Boletín", "Página Boletín",
                        "Monto Deuda",
                        "Acreedor", "Motivo" };
                float[] anchoColumnasLab = new float[] { 100, 100, 100, 100, 100, 100 };
                String[] formatoInflab = new String[] { "", "", "", "moneda$", "capital", "capital" };
                PdfUtils.agregarSeccionTablaConFormato(document, "INFRACCIONES LABORALES",
                        consulta.getResultado().getDeudasLaborales(),
                        titLab, anchoColumnasLab, formatoInflab);
            }

            // Protestos
            if (consulta.getResultado() != null && consulta.getResultado().getProtestos() != null) {
                String[] titPrt = new String[] { "Fecha", "Glosa", "Tipo Documento", "Monto Protesto",
                        "Número Operación", "Glosa Documento" };
                float[] anchoColumnasPrt = new float[] { 100, 100, 100, 100, 100, 100 };
                String[] formatoPrt = new String[] { "", "", "", "moneda$", "", "" };
                PdfUtils.agregarSeccionTablaConFormato(document, "PROTESTOS",
                        consulta.getResultado().getProtestos(),
                        titPrt,
                        anchoColumnasPrt, formatoPrt);
            }

            // Infocom
            if (consulta.getResultado() != null && consulta.getResultado().getInfocom() != null) {
                String[] titInfocom = new String[] { "Fecha", "Tipo", "Monto", "Emisor" };
                float[] anchoColumnasInfocom = new float[] { 100, 100, 100, 100 };
                String[] formatoInfocom = new String[] { "", "", "moneda$", "" };
                PdfUtils.agregarSeccionTablaConFormato(document, "INFOCOM",
                        consulta.getResultado().getInfocom(),
                        titInfocom, anchoColumnasInfocom, formatoInfocom);
            }

            // Deudas Comercio
            if (consulta.getResultado() != null && consulta.getResultado().getDeudasComercio() != null) {
                String[] titDC = new String[] { "Fecha Vencimiento", "Tipo Documento", "Monto",
                        "Emisor",
                        "Fecha Publicación", "Mercado" };
                float[] anchoColumnasDC = new float[] { 100, 100, 100, 100, 100, 100 };
                String[] formatoDC = new String[] { "", "capital", "moneda$", "capital", "",
                        "capital" };
                PdfUtils.agregarSeccionTablaConFormato(document, "DEUDAS COMERCIO",
                        consulta.getResultado().getDeudasComercio(), titDC,
                        anchoColumnasDC, formatoDC);
            }

            PdfUtils.agregarFooter(document, font, consulta.getTimestamp());

            // ✅ Cerrar documento
            document.close();
            return baos.toByteArray();
        } catch (Exception ex) {
            return null;

        }
    }

}
