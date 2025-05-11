package cl.veridico.informes.services;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;

import cl.veridico.informes.models.RespuestaInformeEmpresa.ConsultaResponseEmpresa;
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
public class PdfEmpresaService {

    @Value("${vrdc.fe.servicios.url}")
    private String urlServiciosVeridico;

    private final String URL_VERIFICACION_DOCUMENTO = "/verifica-informe/paso1";

    public byte[] generatePdfFromConsulta(ConsultaResponseEmpresa consulta, String folio, String codigoVerificacion)
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

            String runFormateado = df.format(consulta.getResultado().getIdJuridica().getRut()) + "-" +
                    consulta.getResultado().getIdJuridica().getDigito();

            PdfUtils.agregarTitulo(document,
                    "Informe Verídico Empresa",
                    UtilesVarios.capitalizarPalabras(
                            consulta.getResultado().getIdJuridica().getRazonSocial()),
                    runFormateado);

            if (consulta != null) {

                List<String> textos = List.of("Protestos y\nMorosidades\nSí", "Boletín\nLaboral\nNo",
                        "Vehículos\nBienes\nRaíces", "Socios y\nSociedades\nNo", "Quiebras\nNo",
                        "Otros\nIndicadores\nNo");
                List<String> colores = List.of("rojo", "gris", "verde", "gris", "gris", "gris");

                // PdfUtils.dibujarCirculosConTexto(document, pdfDoc, textos, colores);
                PdfUtils.dibujarCirculosConTextoBorde(document, pdfDoc, textos, colores);
                PdfUtils.agregarLeyenda(document, font); // 👈 Aquí agregas la leyenda justo después

                // Información Corporativa
                if (consulta.getResultado() != null
                        && consulta.getResultado().getIdJuridica() != null) {
                    cl.veridico.informes.models.RespuestaInformeEmpresa.ConsultaResponseEmpresa.Resultado.IdentificacionJuridica informacionCorporativa = consulta
                            .getResultado().getIdJuridica();

                    if (informacionCorporativa != null) {

                        List<SimpleEntry<String, String>> datosInformacionCorporativa = new ArrayList<>(
                                List.of(
                                        new SimpleEntry<>("Rut",
                                                df.format(informacionCorporativa
                                                        .getRut())
                                                        + "-"
                                                        + informacionCorporativa
                                                                .getDigito()),
                                        new SimpleEntry<>("Razón Social",
                                                UtilesVarios.capitalizarPalabras(
                                                        informacionCorporativa
                                                                .getRazonSocial())),
                                        new SimpleEntry<>(
                                                "Fecha Inicio Actividades",
                                                informacionCorporativa
                                                        .getFechaInicioActividades()),
                                        new SimpleEntry<>("Tamaño Empresa",
                                                UtilesVarios.capitalizarPalabras(
                                                        informacionCorporativa
                                                                .getSegmentoEmpresa())),
                                        new SimpleEntry<>("Tamaño Empresa",
                                                informacionCorporativa
                                                        .getNumeroEmpleados()),
                                        new SimpleEntry<>("Actividad Principal",
                                                UtilesVarios.capitalizarPalabras(
                                                        informacionCorporativa
                                                                .getActividadPrincipal()))));

                        // Faltan datos
                        PdfUtils.agregarTablaDatos(document, "INFORMACION CORPORATIVA",
                                datosInformacionCorporativa);

                        List<SimpleEntry<String, String>> datosResumenTributario = new ArrayList<>(
                                List.of(
                                        new SimpleEntry<>("Formulario N°29",
                                                informacionCorporativa
                                                        .getFormulario29()),
                                        new SimpleEntry<>("Formulario N°22",
                                                informacionCorporativa
                                                        .getFormulario22()),
                                        new SimpleEntry<>("Formulario N°50",
                                                informacionCorporativa
                                                        .getFormulario50()),
                                        new SimpleEntry<>("Formulario Pro Pyme",
                                                informacionCorporativa
                                                        .getRegimenPYME()),
                                        new SimpleEntry<>(
                                                "Declaración Moneda Extranjera",
                                                informacionCorporativa
                                                        .getMonedaExtranjera())));

                        PdfUtils.agregarTablaDatos(document, "RESUMEN TRIBUTARIO",
                                datosResumenTributario);

                        List<SimpleEntry<String, String>> datosRegimenTributarioEspecial = new ArrayList<>(
                                List.of(
                                        new SimpleEntry<>("",
                                                UtilesVarios.capitalizarPalabras(
                                                        informacionCorporativa
                                                                .getRegimenTributarioEspecial()))));

                        PdfUtils.agregarTablaDatos(document, "REGIMEN TRIBUTARIO ESPECIAL",
                                datosRegimenTributarioEspecial);
                    }

                }

                // Resumen Morosidades
                cl.veridico.informes.models.RespuestaInformeEmpresa.ConsultaResponseEmpresa.Resultado.ResumenTotalMorosidad resumenMorosidades = consulta
                        .getResultado().getResumenTotalesMorosidades();

                if (consulta.getResultado() != null && resumenMorosidades != null) {

                    List<SimpleEntry<String, String>> datosResumenMorosidades = List.of(
                            new SimpleEntry<>("Total Protestos",
                                    UtilesVarios.formatCurrency(resumenMorosidades
                                            .getMontoTotalProtestos())),
                            // new SimpleEntry<>("Total Morosidad
                            // Vigente",UtilesVarios.formatCurrency(resumenMorosidades.get)),
                            new SimpleEntry<>("Total Infocom",
                                    UtilesVarios.formatCurrency(resumenMorosidades
                                            .getMontoTotalInfocom())),
                            new SimpleEntry<>("Total Morosidad Previsional",
                                    UtilesVarios.formatCurrency(resumenMorosidades
                                            .getMontoTotalDeudasPrevisionales())),
                            new SimpleEntry<>("Total Morosidad Laboral",
                                    UtilesVarios.formatCurrency(resumenMorosidades
                                            .getMontoTotalDeudasLaborales())),
                            new SimpleEntry<>("Total Morosidad Comercio",
                                    UtilesVarios.formatCurrency(resumenMorosidades
                                            .getMontoTotalDeudasComercio())));

                    PdfUtils.agregarTablaDatos(document, "RESUMEN MOROSIDADES",
                            datosResumenMorosidades);
                }

                // Documentos Autorizados / Timbrajes Autorizados
                String[] titDocAut = new String[] { "Tipo Documento", "Fecha Autorización" };
                float[] anchoColumnasDocAut = new float[] { 100, 100 };
                String[] formatoDocAut = new String[] { "capital", "" };
                PdfUtils.agregarSeccionTablaConFormato(document, "DOCUMENTOS AUTORIZADOS",
                        consulta.getResultado().getTimbrajesAutorizados(), titDocAut,
                        anchoColumnasDocAut, formatoDocAut);

                // Actividades Económicas
                String[] titActEco = new String[] { "Código", "Glosa", "Fecha Inicio", "Fecha Término",
                        "Afecto IVA", "Categoría" };
                float[] anchoColumnasActEco = new float[] { 100, 100, 100, 100, 100, 100 };
                String[] formatoActEco = new String[] { "", "capital", "", "", "", "" };
                PdfUtils.agregarSeccionTablaConFormato(document, "ACTIVIDADES ECONÓMICAS",
                        consulta.getResultado().getActividadesEconomicas(), titActEco,
                        anchoColumnasActEco, formatoActEco);

                // Direccciones Registradas
                String[] titDirReg = new String[] { "Tipo", "Calle", "Nro", "Bloque", "Departamento",
                        "Villa/Población", "Ciudad", "Comuna", "Región" };
                float[] anchoColumnasDirReg = new float[] { 100, 100, 100, 100, 100, 100, 100, 100,
                        100 };
                String[] formatoDirReg = new String[] { "", "capital", "", "", "", "capital", "capital",
                        "capital", "capital" };
                PdfUtils.agregarSeccionTablaConFormato(document, "DIRECCIONES REGISTRADAS",
                        consulta.getResultado().getDireccionesRegistradas(), titDirReg,
                        anchoColumnasDirReg, formatoDirReg);

                // Sociedades Empresariales
                String[] titSocEmp = new String[] { "Rut", "DV", "Razón Social", "Fecha Relación" };
                float[] anchoColumnasSocEmp = new float[] { 100, 100, 100, 100 };
                String[] formatoSocEmp = new String[] { "", "", "capital", "" };
                PdfUtils.agregarSeccionTablaConFormato(document, "SOCIEDADES",
                        consulta.getResultado().getSociedadesEmpresariales(), titSocEmp,
                        anchoColumnasSocEmp, formatoSocEmp);

                // acá debería ir el registro de proveedores del estado

                // Infracciones Previsionales
                if (consulta.getResultado() != null
                        && consulta.getResultado().getDeudasPrevisionales() != null) {

                    List<cl.veridico.informes.models.RespuestaInformeEmpresa.ConsultaResponseEmpresa.Resultado.DeudaPrevisional> listaPrevisionales = consulta
                            .getResultado().getDeudasPrevisionales();

                    for (cl.veridico.informes.models.RespuestaInformeEmpresa.ConsultaResponseEmpresa.Resultado.DeudaPrevisional item : listaPrevisionales) {
                        String acreedor = item.getAcreedor();
                        if (acreedor == null)
                            acreedor = "";

                        List<SimpleEntry<String, String>> datoDP = new ArrayList<>(List.of(
                                new SimpleEntry<>("Acreedor", acreedor),
                                new SimpleEntry<>("Monto en $",
                                        UtilesVarios.formatCurrency(
                                                item.getMontoPesos())),
                                new SimpleEntry<>("Monto en UTM",
                                        Integer.toString(item.getMontoUTM()))));
                        PdfUtils.agregarTablaDatos(document, "DEUDAS PREVISIONALES", datoDP);

                        // Trabajador Afectado
                        if (item.getTrabajadoresAfectados() != null) {
                            String[] titTAfectado = new String[] { "Rut Trabajador", "DV",
                                    "Nombre", "Período Deuda",
                                    "Monto Deuda $", "Monto Deuda UTM",
                                    "Número Boletín", "Motivo" };
                            float[] anchoColumnasTAfectado = new float[] { 100, 100, 100,
                                    100, 100, 100, 100, 100 };
                            PdfUtils.agregarSeccionTabla(document, "Trabajador Afectado",
                                    item.getTrabajadoresAfectados(),
                                    titTAfectado, anchoColumnasTAfectado);
                        }
                    }
                }

                // Infracciones Laborales
                String[] titInfLab = new String[] { "Fecha Boletín", "Número Boletín", "Página",
                        "Monto Deuda", "Acreedor", "Motivo" };
                float[] anchoColumnasInflab = new float[] { 100, 100, 100, 100, 100, 100 };
                String[] formatoInflab = new String[] { "", "", "",
                        "moneda$", "", "capital" };
                PdfUtils.agregarSeccionTablaConFormato(document, "INFRACCIONES LABORALES",
                        consulta.getResultado().getDeudasLaborales(),
                        titInfLab,
                        anchoColumnasInflab, formatoInflab);

                // Protestos
                String[] titPrt = new String[] { "Fecha", "Glosa", "Tipo Documento", "Monto Protesto",
                        "Número Operación", "Glosa Documento" };
                float[] anchoColumnasPrt = new float[] { 100, 100, 100, 100, 100, 100 };
                String[] formatoPrt = new String[] { "", "", "", "moneda$", "", "" };
                PdfUtils.agregarSeccionTablaConFormato(document, "PROTESTOS",
                        consulta.getResultado().getProtestos(), titPrt, anchoColumnasPrt,
                        formatoPrt);

                // Infocom
                String[] titInfocom = new String[] { "Fecha", "Tipo", "Monto", "Emisor" };
                float[] anchoColumnasInfocom = new float[] { 100, 100, 100, 100 };
                String[] formatoInfocom = new String[] { "", "", "moneda$", "capital" };
                PdfUtils.agregarSeccionTablaConFormato(document, "INFOCOM",
                        consulta.getResultado().getMorosidadesInfocom(), titInfocom,
                        anchoColumnasInfocom, formatoInfocom);

                // Morosidad Vigente
                String[] titMorVig = new String[] { "Fecha", "Tipo", "Monto", "Acreedor" };
                float[] anchoColumnasMorVig = new float[] { 100, 100, 100, 100 };
                String[] formatoMorVig = new String[] { "", "", "moneda$", "capital" };
                PdfUtils.agregarSeccionTablaConFormato(document, "MOROSIDAD VIGENTE",
                        consulta.getResultado().getMorosidadesVigentes(), titMorVig,
                        anchoColumnasMorVig, formatoMorVig);

                // Boletin Electrónico Maat Vigente
                String[] titBolEle = new String[] { "Fecha", "Tipo", "Monto", "Librador",
                        "Fecha Publicación", "Mercado" };
                float[] anchoColumnasBoleEle = new float[] { 100, 100, 100, 100, 100, 100 };
                String[] formatoBolEle = new String[] { "", "", "moneda$",
                        "capital", "", "capital" };
                PdfUtils.agregarSeccionTablaConFormato(document, "BOLETÍN ELECTRÓNICO",
                        consulta.getResultado().getBoletinesMaat(), titBolEle,
                        anchoColumnasBoleEle, formatoBolEle);

                // Anotaciones Corredores
                String[] titAnCorr = new String[] { "Número Referencia", "Fecha Anotación", "Anotación",
                        "Corredor" };
                float[] anchoColumnasAnCorr = new float[] { 100, 100, 100, 100 };
                PdfUtils.agregarSeccionTabla(document, "ANOTACIONES CORREDORES",
                        consulta.getResultado().getAnotacionesCorredores(), titAnCorr,
                        anchoColumnasAnCorr);

                // Resumen Consolidado
                if (consulta.getResultado() != null
                        && consulta.getResultado().getResumenConsolidado() != null) {

                    List<SimpleEntry<String, String>> datosResumenEmpresarial = List.of(
                            new SimpleEntry<>("Eventos Quiebra",
                                    String.valueOf(consulta.getResultado()
                                            .getResumenConsolidado()
                                            .getEventosQuiebras())),
                            new SimpleEntry<>("Bancos Relacionados",
                                    String.valueOf(consulta.getResultado()
                                            .getResumenConsolidado()
                                            .getBancosRelacionados())),
                            new SimpleEntry<>("Órdedes No Pago",
                                    String.valueOf(consulta.getResultado()
                                            .getResumenConsolidado()
                                            .getOrdenNoPago())),
                            new SimpleEntry<>("Vehículos",
                                    Integer.toString(consulta.getResultado()
                                            .getResumenConsolidado()
                                            .getVehiculos())),
                            new SimpleEntry<>("Bienes Raíces",
                                    String.valueOf(consulta.getResultado()
                                            .getResumenConsolidado()
                                            .getBienesRaices())),
                            new SimpleEntry<>("Exportaciones",
                                    String.valueOf(consulta.getResultado()
                                            .getResumenConsolidado()
                                            .getExportaciones())),
                            new SimpleEntry<>("Importaciones",
                                    String.valueOf(consulta.getResultado()
                                            .getResumenConsolidado()
                                            .getImportaciones())),
                            new SimpleEntry<>("Infracciones Laborales",
                                    String.valueOf(consulta.getResultado()
                                            .getResumenConsolidado()
                                            .getInfraccionesLaborales())),
                            new SimpleEntry<>("Infracciones Previsionales",
                                    String.valueOf(consulta.getResultado()
                                            .getResumenConsolidado()
                                            .getInfraccionesPrevisionales())),
                            new SimpleEntry<>("Prendas",
                                    String.valueOf(consulta.getResultado()
                                            .getResumenConsolidado()
                                            .getPrendas())),
                            new SimpleEntry<>("Registro Sociedades",
                                    String.valueOf(consulta.getResultado()
                                            .getResumenConsolidado()
                                            .getSociedades())),
                            new SimpleEntry<>("Registro Socios",
                                    String.valueOf(consulta.getResultado()
                                            .getResumenConsolidado()
                                            .getSocios())));

                    PdfUtils.agregarTablaDatos(document, "RESUMEN CONSOLIDADO",
                            datosResumenEmpresarial);
                }

                // Quiebras
                String[] titQui = new String[] { "Rol", "Procedimiento", "Deudor", "Veedor",
                        "Publicación", "Tribunal", "Fecha" };
                float[] anchoColumnasQui = new float[] { 100, 100, 100, 100, 100, 100, 100 };
                PdfUtils.agregarSeccionTabla(document, "QUIEBRAS",
                        consulta.getResultado().getQuiebras(), titQui, anchoColumnasQui);

                // ONP
                /*
                 * String[] titONP = new String[] { "Banco", "Cuenta Cliente", "Número Cheque",
                 * "Número Serie", "Sucursal", "Fecha", "Motivo", "Fuente" };
                 * float[] anchoColumnasONP = new float[] { 100, 100, 100, 100, 100, 100, 100,
                 * 100 };
                 * PdfUtils.agregarSeccionTabla(document, "ÓRDENES DE NO PAGO",
                 * consulta.getResultado().getOrdenNoPago(),titONP,anchoColumnasONP);
                 */

                // Vehículos
                String[] titVeh = new String[] { "Tipo Vehículo", "Marca", "Patente", "Modelo", "Año",
                        "Fecha" };
                float[] anchoColumnasVeh = new float[] { 100, 100, 100, 100, 100, 50 };
                PdfUtils.agregarSeccionTabla(document, "VEHÍCULOS",
                        consulta.getResultado().getVehiculos(), titVeh, anchoColumnasVeh);

                // Bienes Raíces
                String[] titBR = new String[] { "Rol", "Comuna", "Dirección", "Tipo", "Destino",
                        "Avalúo" };
                float[] anchoColumnasBR = new float[] { 100, 100, 100, 100, 100, 100 };
                String[] formatoBR = new String[] { "", "", "", "", "", "moneda$" };
                PdfUtils.agregarSeccionTablaConFormato(document, "BIENES RAÍCES",
                        consulta.getResultado().getBienesRaices(), titBR, anchoColumnasBR,
                        formatoBR);

                // Exportaciones
                String[] titExp = new String[] { "Año", "FOB", "CIF", "Número", "Ene", "Feb", "Mar",
                        "Abr", "May", "Jun",
                        "Jul", "Ago", "Sep", "Oct", "Nov", "Dic" };
                float[] anchoColumnasExp = new float[] { 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50,
                        50, 50, 50, 50 };
                PdfUtils.agregarSeccionTabla(document, "EXPORTACIONES",
                        consulta.getResultado().getExportaciones(), titExp,
                        anchoColumnasExp);

                // Importaciones
                String[] titImp = new String[] { "Año", "FOB", "CIF", "Número", "Ene", "Feb", "Mar",
                        "Abr", "May", "Jun",
                        "Jul", "Ago", "Sep", "Oct", "Nov", "Dic" };
                float[] anchoColumnasImp = new float[] { 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50,
                        50, 50, 50, 50 };
                PdfUtils.agregarSeccionTabla(document, "IMPORTACIONES",
                        consulta.getResultado().getImportaciones(), titImp,
                        anchoColumnasImp);

                // Prendas
                String[] titPre = new String[] { "Acreedor", "Fecha Publicación", "Cuerpo", "Página",
                        "Extracto" };
                float[] anchoColumnasPre = new float[] { 100, 100, 100, 100, 100 };
                PdfUtils.agregarSeccionTabla(document, "Prendas", consulta.getResultado().getPrendas(),
                        titPre,
                        anchoColumnasPre);

                        //OJO , FALTA HABILITAR NUEVAMENTE
                // Socios y Sociedades Personas
                /*if (consulta.getResultado() != null
                        && consulta.getResultado().getSociedadesPersonas() != null) {
                    List<cl.veridico.informes.models.RespuestaInformeEmpresa.ConsultaResponseEmpresa.Resultado.SociedadPersona.Sociedad> listaSyS = consulta
                            .getResultado().getSociedadesPersonas().getSociedades();

                    for (cl.veridico.informes.models.RespuestaInformeEmpresa.ConsultaResponseEmpresa.Resultado.SociedadPersona.Sociedad item : listaSyS) {

                        List<SimpleEntry<String, String>> datoSociedades = new ArrayList<>(
                                List.of(
                                        new SimpleEntry<>("Fecha",
                                                item.getFecha()),
                                        new SimpleEntry<>("Contenido",
                                                UtilesVarios.capitalizarPalabras(
                                                        item.getContenido())),
                                        new SimpleEntry<>("Nombre",
                                                UtilesVarios.capitalizarPalabras(
                                                        item.getNombre())),
                                        new SimpleEntry<>("Rut", df
                                                .format(item.getRut())
                                                + "-"
                                                + item.getDigito()),
                                        new SimpleEntry<>("Edición",
                                                item.getEdicion()),
                                        new SimpleEntry<>("Cuerpo",
                                                item.getCuerpo()),
                                        new SimpleEntry<>("Página",
                                                item.getPagina()),
                                        new SimpleEntry<>("Extracto",
                                                item.getExtracto())));
                        PdfUtils.agregarTablaDatos(document, "SOCIOS Y SOCIEDADES",
                                datoSociedades);

                        // Socios
                        if (item.getSocios() != null) {
                            String[] titSocios = new String[] { "Rut Socio",
                                    "Nombre Socio", "Tipo", "Aporte" };
                            float[] anchoColumnasSocios = new float[] { 100, 100, 100,
                                    100 };
                            String[] formatoSocios = new String[] { "", "capital", "",
                                    "moneda$" };
                            PdfUtils.agregarSeccionTablaConFormato(document, "Socios",
                                    item.getSocios(), titSocios,
                                    anchoColumnasSocios, formatoSocios);
                        }
                    }

                }
*/
                // Consultas al Rut
                String[] titCaR = new String[] { "Fecha Consulta", "Institución" };
                float[] anchoColumnasCaR = new float[] { 200, 300 };
                PdfUtils.agregarSeccionTabla(document, "CONSULTAS AL RUT",
                        consulta.getResultado().getConsultasRut(), titCaR, anchoColumnasCaR);

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
