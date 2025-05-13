package cl.veridico.informes.services;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import cl.veridico.informes.services.RespuestaInformePersona.ConsultaResponsePersona;
import cl.veridico.informes.utils.PdfUtils;
import cl.veridico.informes.utils.UtilesVarios;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class PdfPersonaService {

    @Value("${vrdc.fe.servicios.url}")
    private String urlServiciosVeridico;

    private final String URL_VERIFICACION_DOCUMENTO = "/verifica-informe/paso1";

    public byte[] generatePdfFromConsulta(ConsultaResponsePersona consulta, String folio, String codigoVerificacion)
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

            // Agregar QR
            PdfUtils.agregarQR(urlServiciosVeridico, URL_VERIFICACION_DOCUMENTO, document, folio,
                    codigoVerificacion, font);

            // ✅ Título principal
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.GERMANY);
            DecimalFormat df = new DecimalFormat("###,###,###", symbols);

            String runFormateado = df.format(consulta.getResultado().getDatosPersonales().getRut()) + "-" +
                    consulta.getResultado().getDatosPersonales().getDigito();

            PdfUtils.agregarTitulo(document,
                    "Informe Verídico Persona",
                    UtilesVarios.capitalizarPalabras(
                            consulta.getResultado().getDatosPersonales().getNombre()),
                    runFormateado);

            document.add(new Paragraph("\n"));

            // Comienzo a analizar los datos para generar Header de resumen del informe
            BigDecimal montoTotalProtestos = consulta.getResultado().getResumenMontos().getMontoTotalProtestos();
            BigDecimal montoTotalInfocom = consulta.getResultado().getResumenMontos().getMontoTotalInfocom();
            BigDecimal montoTotalDeudasComercio = consulta.getResultado().getResumenMontos()
                    .getMontoTotalDeudasComercio();

            BigDecimal montoTotalDeudasPrevisionales = consulta.getResultado().getResumenMontos()
                    .getMontoTotalDeudasPrevisionales();
            BigDecimal montoTotalDeudasLaborales = consulta.getResultado().getResumenMontos()
                    .getMontoTotalDeudasLaborales();

            boolean alertaRojaMorososProtestos = false;
            if (montoTotalProtestos.compareTo(BigDecimal.ZERO) > 0 ||
                    montoTotalInfocom.compareTo(BigDecimal.ZERO) > 0 ||
                    montoTotalDeudasComercio.compareTo(BigDecimal.ZERO) > 0) {
                alertaRojaMorososProtestos = true;
            }
            boolean alertaRojaDeudasPrevisionales = false;
            if (montoTotalDeudasPrevisionales.compareTo(BigDecimal.ZERO) > 0 ||
                    montoTotalDeudasLaborales.compareTo(BigDecimal.ZERO) > 0) {
                alertaRojaDeudasPrevisionales = true;
            }

            long alertaQuiebras = (consulta.getResultado().getResumenEmpresarial().getEventosQuiebras().equals("No") ? 0
                    : 1);

            long alertaVehiculos = consulta.getResultado().getResumenEmpresarial().getVehiculos();
            long alertaBienesRaices = consulta.getResultado().getResumenEmpresarial().getBienesRaices();

            long alertaSociedades = consulta.getResultado().getResumenEmpresarial().getSociedades();
            long alertaSocios = consulta.getResultado().getResumenEmpresarial().getSocios();

            long alertaPrendas = consulta.getResultado().getResumenEmpresarial().getPrendas();
            long alertaATributarias = consulta.getResultado().getResumenEmpresarial()
                    .getAnotacionesTributarias();
            long alertaOrdenesNoPago = consulta.getResultado().getResumenEmpresarial().getOrdenNoPago();

            List<String> nombresHeader = List.of("protymor",
                    "boletin",
                    "vehiculos",
                    "socios",
                    "quiebras",
                    "otros");
            List<Boolean> valoresHeader = List.of(alertaRojaMorososProtestos,
                    alertaRojaDeudasPrevisionales,
                    (alertaVehiculos > 0 || alertaBienesRaices > 0 ? true : false),
                    (alertaSociedades > 0 || alertaSocios > 0 ? true : false),
                    (alertaQuiebras > 0 ? true : false),
                    (alertaATributarias > 0 || alertaPrendas > 0 || alertaOrdenesNoPago > 0 ? true : false));

            PdfUtils.dibujarImagenesIndicadoresConGlosas(document, nombresHeader, valoresHeader);
            // fin Header de resumen del informe

            // Datos Personales
            if (consulta.getResultado() != null && consulta.getResultado().getDatosPersonales() != null) {

                cl.veridico.informes.services.RespuestaInformePersona.ConsultaResponsePersona.Resultado.DatosPersonales listaAntecedentesP = consulta
                        .getResultado().getDatosPersonales();

                if (consulta.getResultado() != null && listaAntecedentesP != null) {

                    // List<SimpleEntry<String, String>> datosAntecedentesP = List.of(
                    List<SimpleEntry<String, String>> datosAntecedentesP = new ArrayList<>(List.of(
                            new SimpleEntry<>("Rut",
                                    df.format(listaAntecedentesP.getRut()) + "-"
                                            + listaAntecedentesP
                                                    .getDigito()),
                            new SimpleEntry<>("Nombre/Razón Social",
                                    UtilesVarios.capitalizarPalabras(consulta
                                            .getResultado()
                                            .getDatosPersonales()
                                            .getNombre())),
                            new SimpleEntry<>("Tipo Persona",
                                    consulta.getResultado().getDatosPersonales()
                                            .getTipoPersona()),
                            new SimpleEntry<>("Total Direcciones Registradas",
                                    consulta.getResultado().getDatosPersonales()
                                            .getCantidadDirecciones()),
                            new SimpleEntry<>("Última Dirección Registrada", consulta
                                    .getResultado()
                                    .getDatosPersonales()
                                    .getUltimaDireccionInformada())));

                    String tipoPersona = consulta.getResultado().getDatosPersonales()
                            .getTipoPersona();

                    if (tipoPersona != null && tipoPersona.equals("PN")) {
                        datosAntecedentesP.add(new SimpleEntry<>("Edad", consulta.getResultado()
                                .getDatosPersonales().getPersona().getEdad()));
                        datosAntecedentesP.add(new SimpleEntry<>("Género",
                                consulta.getResultado()
                                        .getDatosPersonales().getPersona()
                                        .getSexo()));
                        datosAntecedentesP.add(new SimpleEntry<>("Estado Civil",
                                consulta.getResultado()
                                        .getDatosPersonales().getPersona()
                                        .getEstadoCivil()));
                        datosAntecedentesP.add(new SimpleEntry<>("Nacionalidad",
                                consulta.getResultado()
                                        .getDatosPersonales().getPersona()
                                        .getNacionalidad()));
                    } else {
                        datosAntecedentesP.add(new SimpleEntry<>("Productos",
                                UtilesVarios.capitalizarPalabras(consulta.getResultado()
                                        .getDatosPersonales()
                                        .getEmpresa()
                                        .getProductos().getProducto())));

                        String glosasGiros = "";
                        if (consulta.getResultado().getDatosPersonales().getEmpresa()
                                .getGiros() != null) {
                            List<cl.veridico.informes.services.RespuestaInformePersona.ConsultaResponsePersona.Resultado.DatosPersonales.Empresa.Giro> listaGiros = consulta
                                    .getResultado().getDatosPersonales()
                                    .getEmpresa()
                                    .getGiros();

                            for (cl.veridico.informes.services.RespuestaInformePersona.ConsultaResponsePersona.Resultado.DatosPersonales.Empresa.Giro itemTA : listaGiros) {
                                glosasGiros += itemTA.getGiro();
                            }
                        }
                        datosAntecedentesP.add(
                                new SimpleEntry<>("Giros", glosasGiros.toLowerCase()));

                    }

                    PdfUtils.agregarTablaDatos(document, "ANTECEDENTES PERSONALES",
                            datosAntecedentesP);
                }

            }

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

            // String riesgoCrediticio =
            // consulta.getResultado().getDatosScore().getRiesgoCrediticio();
            // String riesgoCrediticioGlosa =
            // consulta.getResultado().getDatosScore().getGlosa();
            // PdfUtils.agregarResumenScore(document, score, riesgoCrediticio,
            // riesgoCrediticioGlosa, font);

            // Resumen Montos
            cl.veridico.informes.services.RespuestaInformePersona.ConsultaResponsePersona.Resultado.ResumenMontos resumenMontos = consulta
                    .getResultado().getResumenMontos();

            if (consulta.getResultado() != null && resumenMontos != null) {

                List<SimpleEntry<String, String>> datosResumenMontos = List.of(
                        new SimpleEntry<>("Monto Total Protestos", UtilesVarios.formatCurrency(montoTotalProtestos)),
                        new SimpleEntry<>("Monto Total Infocom", UtilesVarios.formatCurrency(montoTotalInfocom)),
                        new SimpleEntry<>("Monto Total Deudas Previsionales",
                                UtilesVarios.formatCurrency(montoTotalDeudasPrevisionales)),
                        new SimpleEntry<>("Monto Total Deudas Laborales",
                                UtilesVarios.formatCurrency(montoTotalDeudasLaborales)),
                        new SimpleEntry<>("Monto Total Deudas Comercio",
                                UtilesVarios.formatCurrency(montoTotalDeudasComercio)));

                PdfUtils.agregarTablaDatos(document, "RESUMEN MONTOS", datosResumenMontos);
            }

            // Resumen Empresarial
            if (consulta.getResultado() != null
                    && consulta.getResultado().getResumenEmpresarial() != null) {

                List<SimpleEntry<String, String>> datosResumenEmpresarial = List.of(
                        new SimpleEntry<>("Eventos Quiebra",
                                String.valueOf(consulta.getResultado()
                                        .getResumenEmpresarial()
                                        .getEventosQuiebras())),
                        new SimpleEntry<>("Órdedes No Pago",
                                String.valueOf(consulta.getResultado()
                                        .getResumenEmpresarial()
                                        .getOrdenNoPago())),
                        new SimpleEntry<>("Vehículos",
                                Integer.toString(consulta.getResultado()
                                        .getResumenEmpresarial()
                                        .getVehiculos())),
                        new SimpleEntry<>("Bienes Raíces",
                                String.valueOf(consulta.getResultado()
                                        .getResumenEmpresarial()
                                        .getBienesRaices())),
                        new SimpleEntry<>("Exportaciones",
                                String.valueOf(consulta.getResultado()
                                        .getResumenEmpresarial()
                                        .getExportaciones())),
                        new SimpleEntry<>("Importaciones",
                                String.valueOf(consulta.getResultado()
                                        .getResumenEmpresarial()
                                        .getImportaciones())),
                        new SimpleEntry<>("Infracciones Laborales",
                                String.valueOf(consulta.getResultado()
                                        .getResumenEmpresarial()
                                        .getInfraccionesLaborales())),
                        new SimpleEntry<>("Infracciones Previsionales",
                                String.valueOf(
                                        consulta.getResultado()
                                                .getResumenEmpresarial()
                                                .getInfraccionesPrevisionales())),
                        new SimpleEntry<>("Bancos Relacionados",
                                String.valueOf(consulta.getResultado()
                                        .getResumenEmpresarial()
                                        .getBancosRelacionados())),
                        new SimpleEntry<>("Prendas",
                                String.valueOf(consulta.getResultado()
                                        .getResumenEmpresarial()
                                        .getPrendas())),
                        new SimpleEntry<>("Anotaciones Tributarias",
                                String.valueOf(
                                        consulta.getResultado()
                                                .getResumenEmpresarial()
                                                .getAnotacionesTributarias())),
                        new SimpleEntry<>("Sociedades",
                                String.valueOf(consulta.getResultado()
                                        .getResumenEmpresarial()
                                        .getSociedades())),
                        new SimpleEntry<>("Socios",
                                String.valueOf(consulta.getResultado()
                                        .getResumenEmpresarial()
                                        .getSocios())));

                PdfUtils.agregarTablaDatos(document, "RESUMEN EMPRESARIAL", datosResumenEmpresarial);
            }

            // Direcciones
            String[] titDir = new String[] { "Tipo Dirección", "Dirección", "Fecha Información" };
            float[] anchoColumnasDir = new float[] { 100, 100, 100 };
            String[] formatoDir = new String[] { "capital", "capital", "" };
            PdfUtils.agregarSeccionTablaConFormato(document, "DIRECCIONES",
                    consulta.getResultado().getDirecciones(),
                    titDir,
                    anchoColumnasDir, formatoDir);

            // Morosidades
            String[] titMor = new String[] { "Fecha Vencimiento", "Tipo Crédito", "Monto", "Acreedor" };
            float[] anchoColumnasMor = new float[] { 100, 100, 100, 100 };
            String[] formatoMor = new String[] { "", "capital", "moneda$", "capital" };
            PdfUtils.agregarSeccionTablaConFormato(document, "MOROSIDADES",
                    consulta.getResultado().getMorosidades(),
                    titMor,
                    anchoColumnasMor, formatoMor);

            // Deudas Previsionales
            if (consulta.getResultado() != null
                    && consulta.getResultado().getDeudasPrevisionales() != null) {

                List<cl.veridico.informes.services.RespuestaInformePersona.ConsultaResponsePersona.Resultado.DeudaPrevisional> listaPrevisionales = consulta
                        .getResultado().getDeudasPrevisionales();

                for (cl.veridico.informes.services.RespuestaInformePersona.ConsultaResponsePersona.Resultado.DeudaPrevisional item : listaPrevisionales) {
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
                                "Período Deuda", "Monto Deuda $", "Monto Deuda UTM",
                                "Número Boletín", "Motivo" };
                        float[] anchoColumnasTAfectado = new float[] { 100, 100, 100, 100, 100,
                                100, 100, 100 };
                        String[] formatoTAfectado = new String[] { "", "", "capital", "",
                                "moneda$", "", "", "capital" };
                        PdfUtils.agregarSeccionTablaConFormato(document, "Trabajador Afectado",
                                item.getTrabajadoresAfectados(), titTAfectado,
                                anchoColumnasTAfectado, formatoTAfectado);
                    }
                }
            }

            // Deudas Laborales
            String[] titLab = new String[] { "Fecha Boletín", "Número Boletín", "Página Boletín",
                    "Monto Deuda",
                    "Acreedor",
                    "Motivo" };
            float[] anchoColumnasLab = new float[] { 100, 100, 100, 100, 100, 100 };
            String[] formatoLab = new String[] { "", "", "", "moneda$", "capital", "capital" };
            PdfUtils.agregarSeccionTablaConFormato(document, "INFRACCIONES LABORALES",
                    consulta.getResultado().getDeudasLaborales(),
                    titLab,
                    anchoColumnasLab, formatoLab);

            // Protestos
            String[] titPrt = new String[] { "Fecha", "Glosa", "Tipo Documento", "Monto Protesto",
                    "Número Operación",
                    "Glosa Documento" };
            float[] anchoColumnasPrt = new float[] { 100, 100, 100, 100, 100, 100 };
            String[] formatoPrt = new String[] { "", "capital", "capital", "moneda$", "", "capital" };
            PdfUtils.agregarSeccionTablaConFormato(document, "PROTESTOS",
                    consulta.getResultado().getProtestos(),
                    titPrt,
                    anchoColumnasPrt, formatoPrt);

            // Infocom
            String[] titInfocom = new String[] { "Fecha", "Tipo", "Monto", "Emisor" };
            float[] anchoColumnasInfocom = new float[] { 100, 100, 100, 100 };
            String[] formatoInfocom = new String[] { "", "capital", "moneda$", "capital" };
            PdfUtils.agregarSeccionTablaConFormato(document, "INFOCOM",
                    consulta.getResultado().getInfocom(),
                    titInfocom, anchoColumnasInfocom, formatoInfocom);

            // Deudas Comercio
            String[] titDC = new String[] { "Fecha Vencimiento", "Tipo Documento", "Monto", "Emisor",
                    "Fecha Publicación", "Mercado" };
            float[] anchoColumnasDC = new float[] { 100, 100, 100, 100, 100, 100 };
            String[] formatoDC = new String[] { "", "capital", "moneda$", "capital", "", "capital" };
            PdfUtils.agregarSeccionTablaConFormato(document, "DEUDAS COMERCIO",
                    consulta.getResultado().getDeudasComercio(),
                    titDC, anchoColumnasDC, formatoDC);

            // Quiebras
            String[] titQui = new String[] { "Resolución", "Fecha Resolución", "Fecha Publicación",
                    "Página",
                    "Extracto" };
            float[] anchoColumnasQui = new float[] { 100, 100, 100, 100, 100 };
            String[] formatoQui = new String[] { "", "", "", "", "capital" };
            PdfUtils.agregarSeccionTablaConFormato(document, "QUIEBRAS",
                    consulta.getResultado().getQuiebras(),
                    titQui,
                    anchoColumnasQui, formatoQui);

            // ONP
            String[] titONP = new String[] { "Banco", "Cuenta Cliente", "Número Cheque", "Número Serie",
                    "Sucursal",
                    "Fecha", "Motivo",
                    "Fuente" };
            float[] anchoColumnasONP = new float[] { 100, 100, 100, 100, 100, 100, 100, 100 };
            String[] formatoONP = new String[] { "", "", "", "", "capital", "", "capital", "capital" };
            PdfUtils.agregarSeccionTablaConFormato(document, "ÓRDENES DE NO PAGO",
                    consulta.getResultado().getOrdenNoPago(),
                    titONP,
                    anchoColumnasONP, formatoONP);

            // Vehículos
            String[] titVeh = new String[] { "Tipo Vehículo", "Marca", "Patente", "Modelo", "Año",
                    "Fecha" };
            float[] anchoColumnasVeh = new float[] { 100, 100, 100, 100, 100, 50 };
            String[] formatoVeh = new String[] { "capital", "capital", "capital", "capital", "", "" };
            PdfUtils.agregarSeccionTablaConFormato(document, "VEHÍCULOS",
                    consulta.getResultado().getVehiculos(),
                    titVeh,
                    anchoColumnasVeh, formatoVeh);

            // Bienes Raíces
            String[] titBR = new String[] { "Rol", "Comuna", "Dirección", "Tipo", "Destino", "Avalúo" };
            float[] anchoColumnasBR = new float[] { 100, 100, 100, 100, 100, 100 };
            String[] formatoBR = new String[] { "capital", "capital", "capital", "capital", "capital",
                    "moneda$" };
            PdfUtils.agregarSeccionTablaConFormato(document, "BIENES RAÍCES",
                    consulta.getResultado().getBienesRaices(),
                    titBR,
                    anchoColumnasBR, formatoBR);

            // Prendas
            String[] titPre = new String[] { "Acreedor", "Fecha Publicación", "Cuerpo", "Página",
                    "Extracto" };
            float[] anchoColumnasPre = new float[] { 100, 100, 100, 100, 100 };
            String[] formatoPre = new String[] { "capital", "", "", "", "" };
            PdfUtils.agregarSeccionTablaConFormato(document, "PRENDAS",
                    consulta.getResultado().getPrendas(), titPre,
                    anchoColumnasPre, formatoPre);

            String[] titAnotaciones = new String[] { "Fecha", "Motivo" };
            float[] anchoColumnasAnotaciones = new float[] { 200, 300 };
            String[] formatoAnotaciones = new String[] { "", "capital" };
            PdfUtils.agregarSeccionTablaConFormato(document, "ANOTACIONES TRIBUTARIAS",
                    consulta.getResultado().getConsultasAlRut(),
                    titAnotaciones, anchoColumnasAnotaciones, formatoAnotaciones);

            // Consultas al Rut
            String[] titCaR = new String[] { "Fecha Consulta", "Institución" };
            float[] anchoColumnasCaR = new float[] { 200, 300 };
            String[] formatoCaR = new String[] { "", "capital" };
            PdfUtils.agregarSeccionTablaConFormato(document, "CONSULTAS AL RUT",
                    consulta.getResultado().getConsultasAlRut(),
                    titCaR,
                    anchoColumnasCaR, formatoCaR);

            // Exportaciones
            String[] titExp = new String[] { "Año", "FOB", "CIF", "Número", "Ene", "Feb", "Mar", "Abr",
                    "May",
                    "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic" };
            float[] anchoColumnasExp = new float[] { 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50,
                    50,
                    50 };
            PdfUtils.agregarSeccionTabla(document, "EXPORTACIONES",
                    consulta.getResultado().getExportaciones(),
                    titExp, anchoColumnasExp);

            // Importaciones
            String[] titImp = new String[] { "Año", "FOB", "CIF", "Número", "Ene", "Feb", "Mar", "Abr",
                    "May",
                    "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic" };
            float[] anchoColumnasImp = new float[] { 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50,
                    50,
                    50 };
            PdfUtils.agregarSeccionTabla(document, "IMPORTACIONES",
                    consulta.getResultado().getImportaciones(),
                    titImp, anchoColumnasImp);

            // Socios y Sociedades
            if (consulta.getResultado() != null &&
                    consulta.getResultado().getSociosSociedades() != null) {
                List<RespuestaInformePersona.ConsultaResponsePersona.Resultado.SocioSociedades> listaSyS = consulta
                        .getResultado().getSociosSociedades();

                if (listaSyS != null && listaSyS.size() > 0) {
                    for (RespuestaInformePersona.ConsultaResponsePersona.Resultado.SocioSociedades item : listaSyS) {

                        List<SimpleEntry<String, String>> datoSociedades = new ArrayList<>(List.of(
                                new SimpleEntry<>("Fecha", item.getFecha()),
                                new SimpleEntry<>("Contenido",
                                        UtilesVarios.capitalizarPalabras(item.getContenido())),
                                new SimpleEntry<>("Nombre",
                                        UtilesVarios.capitalizarPalabras(item.getNombre())),
                                new SimpleEntry<>("Rut",
                                        Integer.toString(item.getRut()) + "-"
                                                + item.getDigito()),
                                new SimpleEntry<>("Edición", item.getEdicion()),
                                new SimpleEntry<>("Cuerpo", item.getCuerpo()),
                                new SimpleEntry<>("Página", item.getPagina()),
                                new SimpleEntry<>("Extracto", item.getExtracto())));
                        PdfUtils.agregarTablaDatos(document, "SOCIOS Y SOCIEDADES", datoSociedades);

                        // Socios
                        if (item.getSocios() != null) {
                            String[] titSocios = new String[] { "Rut Socio", "DV", "Nombre Socio",
                                    "Tipo",
                                    "Aporte" };
                            float[] anchoColumnasSocios = new float[] { 100, 100, 100, 100, 100 };
                            String[] formatoSocios = new String[] { "", "", "capital", "capital",
                                    "moneda$" };
                            PdfUtils.agregarSeccionTablaConFormato(document, "Socios", item.getSocios(),
                                    titSocios,
                                    anchoColumnasSocios, formatoSocios);
                        }
                    }
                } else {
                    PdfUtils.agregarSeccionTablaSinDatos(document, "SOCIOS Y SOCIEDADES");
                }

            }

            PdfUtils.agregarFooter(document, font, consulta.getTimestamp());

            // ✅ Cerrar documento
            document.close();
            return baos.toByteArray();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

}
