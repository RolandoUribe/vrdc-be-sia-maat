package cl.veridico.informes.utils;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.font.PdfFontFactory.EmbeddingStrategy;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.BorderRadius;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.itextpdf.layout.properties.HorizontalAlignment;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import java.awt.image.BufferedImage;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.jfree.chart.JFreeChart;

import java.util.AbstractMap.SimpleEntry;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.jfree.chart.plot.dial.*;

import java.awt.*;

public class PdfUtils {

    // Seteo color grisSuave
    public static Color grisSuave = new DeviceRgb(220, 220, 220); // RGB más claro

    public static JFreeChart crearTacometro(int score) {
        DialPlot plot = new DialPlot();

        // Escala
        StandardDialScale scale = new StandardDialScale(0, 950, 180, -180, 50, 4);
        scale.setTickRadius(0.88);
        scale.setTickLabelOffset(0.15);
        scale.setTickLabelFont(new Font("Dialog", Font.PLAIN, 12));
        plot.addScale(0, scale);

        // Rangos
        plot.addLayer(crearRango(0, 200, new java.awt.Color(255, 0, 0))); // Crítico
        plot.addLayer(crearRango(200, 350, new java.awt.Color(255, 100, 0))); // Muy alto
        plot.addLayer(crearRango(350, 500, new java.awt.Color(255, 165, 0))); // Alto
        plot.addLayer(crearRango(500, 700, new java.awt.Color(255, 255, 0))); // Moderado-Alto
        plot.addLayer(crearRango(700, 800, new java.awt.Color(173, 255, 47))); // Moderado
        plot.addLayer(crearRango(800, 950, new java.awt.Color(0, 128, 0))); // Bajo

        // Aguja
        DialPointer.Pointer pointer = new DialPointer.Pointer();
        plot.addPointer(pointer);
        plot.setDataset(0, new org.jfree.data.general.DefaultValueDataset(score));

        // Fondo
        DialBackground background = new DialBackground(java.awt.Color.WHITE);
        plot.setBackground(background);

        return new JFreeChart(plot);
    }

    private static StandardDialRange crearRango(double min, double max, Paint color) {
        StandardDialRange range = new StandardDialRange();
        range.setLowerBound(min);
        range.setUpperBound(max);
        range.setPaint(color);
        range.setInnerRadius(0.52);
        range.setOuterRadius(0.55);
        return range;
    }

    // Seteo font del documento pdf
    public static PdfFont setFontPDF(InputStream fontStream) {
        try {
            PdfFont font = PdfFontFactory.createFont(
                    fontStream.readAllBytes(),
                    "Identity-H",
                    EmbeddingStrategy.PREFER_EMBEDDED);
            return font;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // para tablas del tipo n filas etiqueta - valor
    public static void agregarTablaDatos(Document document, String titulo, List<SimpleEntry<String, String>> datos) {
        if (datos == null || datos.isEmpty()) {
            return;
        }

        // Agrega el título de la sección
        document.add(estiloTituloSeccionInforme(titulo));

        // Crear la tabla con 2 columnas
        Color grisSuave = new DeviceRgb(220, 220, 220); // RGB más claro
        Table tabla = new Table(2)
                .setWidth(UnitValue.createPercentValue(100))
                .setBorder(new SolidBorder(grisSuave, 1));
        ;

        // Alternar colores en las filas
        boolean esFilaPar = false;

        for (SimpleEntry<String, String> entrada : datos) {
            tabla.addCell(estiloCeldasDatos(entrada.getKey(), esFilaPar).setTextAlignment(TextAlignment.LEFT));

            tabla.addCell(estiloCeldasDatos(entrada.getValue(), esFilaPar).setTextAlignment(null)
                    .setTextAlignment(TextAlignment.LEFT));
            esFilaPar = !esFilaPar;
        }

        document.add(tabla);
        document.add(new Paragraph("\n"));
    }

    // da el formato a las celdas de los informes
    public static Cell estiloCeldasDatos(String valorDato, boolean isGray) {
        // Definir un gris más suave
        Color grisSuave = new DeviceRgb(220, 220, 220); // RGB más claro

        Color backgroundColor = isGray ? grisSuave : ColorConstants.WHITE;

        Cell cellDatos = new Cell().add(new Paragraph(valorDato))
                .setBackgroundColor(backgroundColor)
                .setBorder(Border.NO_BORDER)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER);

        return cellDatos;
    }

    public static Cell estiloCeldasDatos(Cell celda, boolean isGray) {
        // Definir un gris más suave
        Color grisSuave = new DeviceRgb(220, 220, 220); // RGB más claro

        Color backgroundColor = isGray ? grisSuave : ColorConstants.WHITE;

        celda.setBackgroundColor(backgroundColor)
                .setBorder(Border.NO_BORDER)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER);

        return celda;
    }

    // da formato a los títulos de las secciones de los informes
    public static Paragraph estiloTituloSeccionInforme(String tituloSeccionInforme) {
        Paragraph titulo = new Paragraph(tituloSeccionInforme)
                .setFontSize(10)
                .setBold()
                .setFontColor(ColorConstants.WHITE)
                .setBackgroundColor(new DeviceRgb(22, 25, 78))
                .setTextAlignment(TextAlignment.LEFT)
                .setPadding(5);

        return titulo;
    }

    // Método genérico para agregar tablas dinámicas sin formatear los montos
    public static <T> void agregarSeccionTablaSinDatos(Document document,
            String titulo) {
        Color grisSuave = new DeviceRgb(220, 220, 220); // RGB más claro
        document.add(PdfUtils.estiloTituloSeccionInforme(titulo));
        Table table = new Table(1)
                .setWidth(UnitValue.createPercentValue(100))
                .setBorder(new SolidBorder(grisSuave, 1));
        Cell cell = new Cell(1, 1).add(new Paragraph("No existe información para este rut"));
        table.addCell(cell);
        document.add(table);
        document.add(new Paragraph("\n"));

    }

    // Método genérico para agregar tablas dinámicas sin formatear los montos
    public static <T> void agregarSeccionTabla(Document document,
            String titulo, List<T> lista,
            String[] listaColumnas,
            float[] anchoColumnas) {

        Color grisSuave = new DeviceRgb(220, 220, 220); // RGB más claro
        document.add(PdfUtils.estiloTituloSeccionInforme(titulo));

        // Definir la tabla con n columnas y ancho total del documento
        Table table = new Table(anchoColumnas)
                .setWidth(UnitValue.createPercentValue(100))
                .setBorder(new SolidBorder(grisSuave, 1));

        // se despliegan los títulos de las columnas
        for (String item : listaColumnas) {
            Cell header = new Cell().add(new Paragraph(item).setBold());
            header.setBackgroundColor(ColorConstants.WHITE);
            header.setTextAlignment(TextAlignment.CENTER);
            header.setFontSize(10);
            header.setBorder(Border.NO_BORDER);

            // Border solidRoundedBorder = new SolidBorder(1.5f);

            // Agregar encabezados a la tabla
            table.addCell(header);
        }

        if (lista != null && !lista.isEmpty()) {
            // Agregar datos dinámicamente
            boolean isGray = true;
            for (T item : lista) {

                isGray = !isGray; // Alternar color

                for (var field : item.getClass().getDeclaredFields()) {
                    field.setAccessible(true);
                    try {
                        Object value = field.get(item);

                        table.addCell(PdfUtils.estiloCeldasDatos(value.toString(), isGray));

                    } catch (IllegalAccessException e) {
                        table.addCell("-");
                    }
                }
            }
        } else {
            Cell cell = new Cell(1, anchoColumnas.length).add(new Paragraph("No existe información para este rut"));
            table.addCell(PdfUtils.estiloCeldasDatos(cell, true));
        }

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    // Método genérico para agregar tablas dinámicas sin formatear los montos
    public static <T> void agregarSeccionTablaConFormato(Document document,
            String titulo, List<T> lista,
            String[] listaColumnas,
            float[] anchoColumnas,
            String[] listaColumnasFormato) {

        Color grisSuave = new DeviceRgb(220, 220, 220); // RGB más claro

        document.add(PdfUtils.estiloTituloSeccionInforme(titulo));

        // Definir la tabla con n columnas y ancho total del documento
        Table table = new Table(anchoColumnas)
                .setWidth(UnitValue.createPercentValue(100))
                .setBorder(new SolidBorder(grisSuave, 1));

        // se despliegan los títulos de las columnas
        for (String item : listaColumnas) {
            Cell header = new Cell().add(new Paragraph(item).setBold());
            header.setBackgroundColor(ColorConstants.WHITE);
            header.setTextAlignment(TextAlignment.CENTER);
            header.setFontSize(10);
            header.setBorder(Border.NO_BORDER);
            // header.setBorder(new SolidBorder(0));

            // Agregar encabezados a la tabla
            table.addCell(header);
        }

        if (lista != null && !lista.isEmpty()) {
            // Agregar datos dinámicamente
            boolean isGray = true;

            for (T item : lista) {

                isGray = !isGray; // Alternar color
                int i = 0; // identifica la columna para formato

                for (var field : item.getClass().getDeclaredFields()) {
                    field.setAccessible(true);
                    try {
                        Object value = field.get(item);

                        if (listaColumnasFormato[i].equals("moneda$")) {
                            table.addCell(
                                    PdfUtils.estiloCeldasDatos(UtilesVarios.formatCurrency(value.toString()), isGray));
                        }

                        if (listaColumnasFormato[i].equals("capital")) {
                            table.addCell(
                                    PdfUtils.estiloCeldasDatos(UtilesVarios.capitalizarPalabras(value.toString()),
                                            isGray));
                        }

                        if (listaColumnasFormato[i].equals("")) {
                            table.addCell(PdfUtils.estiloCeldasDatos(value.toString(), isGray));
                        }
                        i++;
                    } catch (Exception e) {
                        table.addCell("-");
                    }
                }
            }
        } else {
            Cell cell = new Cell(1, anchoColumnas.length).add(new Paragraph("No existe información para este rut"));
            table.addCell(PdfUtils.estiloCeldasDatos(cell, true));
        }

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    // Agregar logo a pdf
    public static void agregarLogo(Document document) {
        try {
            // Ruta de la imagen
            String rutaImagen = "src/main/resources/logo_veridico_02.png";

            // Cargar la imagen
            ImageData imageData = ImageDataFactory.create(rutaImagen);
            Image logo = new Image(imageData);

            // Ajustar tamaño si es necesario
            logo.scaleToFit(150, 100); // Ancho x Alto en puntos

            // Posicionar en la esquina superior izquierda
            logo.setFixedPosition(30, document.getPdfDocument().getDefaultPageSize().getHeight() - 60);

            // Agregar la imagen al documento
            document.add(logo);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static PdfFont obtenerFuente(String fuente) {
        try {
            return PdfFontFactory.createRegisteredFont(fuente);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void agregarTitulo(Document document, String titulo,
            String... subtitulos) {
        // Título principal
        document.add(new Paragraph(titulo)
                .setBold()
                .setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER)
                .setBold()
                .setMarginTop(20)
                .setMarginBottom(0)
                .setMultipliedLeading(0.9f));

        // Subtítulos
        for (int i = 0; i < subtitulos.length; i++) {
            boolean ultimo = (i == subtitulos.length - 1);
            document.add(new Paragraph(subtitulos[i])
                    .setBold()
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(0)
                    .setMarginBottom(ultimo ? 20 : 0) // solo el último subtítulo deja un poco de espacio
                    .setMultipliedLeading(0.9f));
        }
    }

    public static ImageData generateQrCode(String text, int width, int height) throws Exception {
        BitMatrix matrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height);
        BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(matrix);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpeg", baos);
        return ImageDataFactory.create(baos.toByteArray());
    }

    /*
     * public static void agregarCircunferenciasInformativas(PdfDocument pdfDoc,
     * PdfFont font,
     * float startX,
     * float y,
     * float diameter,
     * String[] textos,
     * Color[] colores) {
     * 
     * PdfPage page = pdfDoc.getFirstPage();
     * PdfCanvas pdfCanvas = new PdfCanvas(page);
     * float spacing = 20;
     * float currentX = startX;
     * 
     * for (int i = 0; i < textos.length; i++) {
     * // Dibujar circunferencia sin relleno, solo borde
     * pdfCanvas.setLineWidth(2f);
     * pdfCanvas.setStrokeColor(colores[i]);
     * pdfCanvas.circle(currentX + diameter / 2, y + diameter / 2, diameter / 2);
     * pdfCanvas.stroke();
     * 
     * // Texto centrado dentro de la circunferencia
     * Rectangle rect = new Rectangle(currentX, y, diameter, diameter);
     * Canvas canvas = new Canvas(pdfCanvas, pdfDoc, rect);
     * canvas.add(new Paragraph(textos[i])
     * .setFont(font)
     * .setFontSize(9)
     * .setFontColor(colores[i]) // mismo color del borde
     * .setTextAlignment(TextAlignment.CENTER)
     * .setVerticalAlignment(VerticalAlignment.MIDDLE));
     * 
     * currentX += diameter + spacing;
     * }
     * }
     */

    public static void agregarQR(String urlServiciosVeridico, String urlVerificacionDocumento,
            Document document, String folio, String codigoVerificacion, PdfFont font) throws Exception {

        // Imagen QR
        String qrCodeUrl = urlServiciosVeridico +
                urlVerificacionDocumento +
                "?folio=" + folio +
                "&codigo_verificacion=" + codigoVerificacion;
        ImageData qrCode = PdfUtils.generateQrCode(qrCodeUrl, 100, 100);
        Image qrImage = new Image(qrCode).scaleToFit(100, 100);
        qrImage.setFixedPosition(475, 725); // 📌 QR en la esquina superior derecha
        // qrImage.setFixedPosition(50, 50); // 📌 QR en la esquina inferior izquierda
        document.add(qrImage);

        // Texto debajo del QR
        String textoLinea1 = "Folio: " + folio;
        String textoLinea2 = "Código de verificación: " + codigoVerificacion;

        Paragraph textoBajoQR = new Paragraph(textoLinea1 + "\n" + textoLinea2)
                .setFont(font) // Usa tu fuente Montserrat
                .setFontSize(6)
                .setTextAlignment(TextAlignment.CENTER)
                .setFixedPosition(425, 710, 200); // 📌 Misma X que el QR, Y debajo, ancho igual

        document.add(textoBajoQR);
    }

    // Entrega la fecga en formato dd/MM/yyyy HH:mm:ss
    public static void agregarFooter(Document document, PdfFont font, String fecha) throws Exception {
        // Formatear la fecha
        LocalDateTime fechaHora = LocalDateTime.parse(fecha); // si viene como 2025-04-15T12:45:24.868885
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        String fechaFormateada = fechaHora.format(formatter);

        // Texto a mostrar
        String textoLinea1 = "Fecha/Hora Informe: " + fechaFormateada;

        Paragraph textoBajoQR = new Paragraph(textoLinea1 + "\n")
                .setFont(font)
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER);

        document.add(textoBajoQR);
    }

    public static void agregarLeyenda(Document document, PdfFont font) {
        float circleSize = 10f;

        List<Map.Entry<DeviceRgb, String>> leyendaItems = List.of(
                Map.entry(new DeviceRgb(255, 0, 0), "Alerta de información"),
                Map.entry(new DeviceRgb(0, 128, 0), "Acredita información"),
                Map.entry(new DeviceRgb(211, 211, 211), "Sin información"));

        Table tablaLeyenda = new Table(UnitValue.createPercentArray(leyendaItems.size()))
                .useAllAvailableWidth()
                .setMarginTop(10);

        for (Map.Entry<DeviceRgb, String> item : leyendaItems) {
            DeviceRgb color = item.getKey();
            String texto = item.getValue();

            // Tabla interna para alinear círculo y texto horizontalmente
            Table innerTable = new Table(2);
            innerTable.setWidth(UnitValue.createPercentValue(100));
            innerTable.setBorder(Border.NO_BORDER);

            // Celda con círculo
            Cell cellCirculo = new Cell().add(
                    new Div()
                            .setWidth(circleSize)
                            .setHeight(circleSize)
                            .setBackgroundColor(color)
                            .setBorderRadius(new BorderRadius(UnitValue.createPointValue(circleSize / 2))));
            cellCirculo.setBorder(Border.NO_BORDER);
            cellCirculo.setPaddingRight(5);
            cellCirculo.setVerticalAlignment(VerticalAlignment.MIDDLE);

            // Celda con texto
            Cell cellTexto = new Cell().add(
                    new Paragraph(texto)
                            .setFont(font)
                            .setFontSize(9)
                            .setMargin(0)
                            .setPadding(0)
                            .setTextAlignment(TextAlignment.LEFT));
            cellTexto.setBorder(Border.NO_BORDER);
            cellTexto.setVerticalAlignment(VerticalAlignment.MIDDLE);

            innerTable.addCell(cellCirculo);
            innerTable.addCell(cellTexto);

            // Agrega innerTable como contenido de la celda principal
            Cell celdaLeyenda = new Cell()
                    .add(innerTable)
                    .setBorder(Border.NO_BORDER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE);

            tablaLeyenda.addCell(celdaLeyenda);
        }

        document.add(tablaLeyenda);
    }

    public static void dibujarCirculosConTexto(Document document, PdfDocument pdfDoc, List<String> textos,
            List<String> colores) {
        float tamanoCirculo = 60f;
        // float espacioEntre = 10f;

        // Tabla de 6 columnas, una por cada círculo
        Table tabla = new Table(textos.size());
        tabla.setWidth(UnitValue.createPercentValue(100));
        tabla.setMarginBottom(20);

        for (int i = 0; i < textos.size(); i++) {
            String texto = textos.get(i);
            String color = colores.get(i);

            // Color RGB del círculo
            Color fillColor = switch (color.toLowerCase()) {
                case "rojo" -> new DeviceRgb(255, 0, 0);
                case "verde" -> new DeviceRgb(0, 128, 0);
                case "gris" -> new DeviceRgb(112, 111, 111);
                default -> new DeviceRgb(211, 211, 211);
            };

            // Crear un lienzo cuadrado para simular el círculo
            Cell cell = new Cell()
                    .setWidth(tamanoCirculo)
                    .setHeight(tamanoCirculo)
                    .setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setPadding(0)
                    .setMargin(0);

            // Creamos un párrafo con el texto
            Paragraph textoCentrado = new Paragraph(texto)
                    .setFontSize(8)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMultipliedLeading(0.9f);

            if (color.equalsIgnoreCase("rojo")) {
                textoCentrado.setFontColor(ColorConstants.WHITE);
            }
            if (color.equalsIgnoreCase("verde")) {
                textoCentrado.setFontColor(ColorConstants.YELLOW);
            }
            if (color.equalsIgnoreCase("gris")) {
                textoCentrado.setFontColor(ColorConstants.WHITE);
            }

            // Creamos un canvas para el fondo
            Div contenedor = new Div()
                    .setWidth(tamanoCirculo)
                    .setHeight(tamanoCirculo)
                    .setBackgroundColor(fillColor)
                    .setBorderRadius(new BorderRadius(tamanoCirculo / 2)) // círculo perfecto
                    .setTextAlignment(TextAlignment.CENTER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .add(textoCentrado);

            cell.add(contenedor);
            tabla.addCell(cell);
        }

        document.add(tabla);
    }

    public static void dibujarCirculosConTextoBorde(Document document, PdfDocument pdfDoc, List<String> textos,
            List<String> colores) {
        float tamanoCirculo = 59f;

        Table tabla = new Table(textos.size());
        tabla.setWidth(UnitValue.createPercentValue(100));
        tabla.setMarginBottom(20);

        for (int i = 0; i < textos.size(); i++) {
            String texto = textos.get(i);
            String color = colores.get(i);

            // Color RGB del borde del círculo
            Color borderColor = switch (color.toLowerCase()) {
                case "rojo" -> new DeviceRgb(255, 0, 0);
                case "verde" -> new DeviceRgb(0, 128, 0);
                case "gris" -> new DeviceRgb(112, 111, 111);
                default -> new DeviceRgb(211, 211, 211);
            };

            Cell cell = new Cell()
                    .setWidth(tamanoCirculo)
                    .setHeight(tamanoCirculo)
                    .setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setPadding(0)
                    .setMargin(0);

            Paragraph textoCentrado = new Paragraph(texto)
                    .setFontSize(8)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMultipliedLeading(0.9f)
                    .setFontColor(ColorConstants.BLACK); // fondo blanco → texto negro

            Div contenedor = new Div()
                    .setWidth(tamanoCirculo)
                    .setHeight(tamanoCirculo)
                    .setBackgroundColor(ColorConstants.WHITE)
                    .setBorder(new SolidBorder(borderColor, 2))
                    .setBorderRadius(new BorderRadius(tamanoCirculo / 2))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .add(textoCentrado);

            cell.add(contenedor);
            tabla.addCell(cell);
        }

        document.add(tabla);
    }

    // Evaluar el color del semáforo, glosa y riesgo
    public static void agregarResumenScore(Document document, int score, String riesgoCrediticio, String glosaScore,
            PdfFont font) {
        /*
         * if (score >= 0 && score <= 200) {
         * colorSemaforo = new DeviceRgb(255, 0, 0); // rojo
         * } else if (score <= 350) {
         * colorSemaforo = new DeviceRgb(255, 0, 0); // rojo
         * } else if (score <= 700) {
         * colorSemaforo = new DeviceRgb(255, 165, 0); // naranjo
         * } else if (score <= 800) {
         * colorSemaforo = new DeviceRgb(255, 165, 0); // naranjo
         * } else if (score <= 950) {
         * colorSemaforo = new DeviceRgb(0, 128, 0); // verde
         * } else {
         * colorSemaforo = new DeviceRgb(112, 111, 111); // gris
         * }
         */

        document.add(new Paragraph("\n"));
        document.add(estiloTituloSeccionInforme("SCORE DE RIESGO"));

        Table tabla = new Table(UnitValue.createPercentArray(new float[] { 1, 2, 3 }))
                .useAllAvailableWidth()
                .setMarginTop(5)
                .setMarginBottom(20)
                .setBorder(new SolidBorder(grisSuave, 1));

        Div semaforo = construirSemaforo(score);
        Cell celdaSemaforo = new Cell()
                .add(semaforo)
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setTextAlignment(TextAlignment.CENTER);
        tabla.addCell(celdaSemaforo);

        // 2. Columna valor + riesgo
        Paragraph parrafoScore = new Paragraph(String.valueOf(score))
                .setFontSize(18)
                .setBold()
                .setFont(font)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5);

        Paragraph parrafoRiesgo = new Paragraph(riesgoCrediticio)
                .setFontSize(10)
                .setFont(font)
                .setTextAlignment(TextAlignment.CENTER);

        Cell celdaScore = new Cell()
                .add(parrafoScore)
                .add(parrafoRiesgo)
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);
        tabla.addCell(celdaScore);

        // 3. Columna glosa del score
        Paragraph parrafoGlosa = new Paragraph(glosaScore)
                .setFontSize(10)
                .setFont(font)
                .setTextAlignment(TextAlignment.LEFT)
                .setMultipliedLeading(1.2f);

        Cell celdaGlosa = new Cell().add(parrafoGlosa)
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);
        tabla.addCell(celdaGlosa);

        document.add(tabla);
    }

    private static Div construirSemaforo(int score) {
        DeviceRgb colorActivo = score <= 350 ? new DeviceRgb(255, 0, 0)
                : score <= 700 ? new DeviceRgb(255, 165, 0) : new DeviceRgb(0, 128, 0);

        Div semaforo = new Div()
                .setWidth(20)
                .setHeight(70)
                .setBackgroundColor(new DeviceRgb(30, 30, 30)) // fondo del "semáforo"
                .setBorderRadius(new BorderRadius(5))
                .setPaddingTop(3)
                .setPaddingBottom(3)
                .setTextAlignment(TextAlignment.CENTER);

        float radio = 15;

        // 3 círculos: rojo, naranja, verde
        Color[] colores = {
                new DeviceRgb(255, 0, 0),
                new DeviceRgb(255, 165, 0),
                new DeviceRgb(0, 128, 0)
        };

        for (Color color : colores) {
            Color colorFinal = color.equals(colorActivo) ? color : new DeviceRgb(100, 100, 100); // apaga los demás

            Div luz = new Div()
                    .setWidth(radio)
                    .setHeight(radio)
                    .setBackgroundColor(colorFinal)
                    .setBorderRadius(new BorderRadius(radio / 2))
                    .setMarginBottom(5);

            semaforo.add(luz);
        }

        return semaforo;
    }

    // entrega la información gráfica del resumen de la información
    public static void dibujarImagenesIndicadores(Document document, List<String> nombresIndicadores,
            List<Boolean> indicadores) {
        float anchoImagen = 60f;
        float altoImagen = 60f;

        Table tabla = new Table(UnitValue.createPercentArray(nombresIndicadores.size()))
                .useAllAvailableWidth()
                .setMarginBottom(20)
                .setBorder(new SolidBorder(grisSuave, 0));

        for (int i = 0; i < nombresIndicadores.size(); i++) {
            String nombreBase = nombresIndicadores.get(i);
            boolean valor = indicadores.get(i);

            String color = valor ? "rojo" : "verde"; // o "gris", según lógica

            if (nombreBase.equals("vehiculos") || nombreBase.equals("socios")) {
                color = (valor) ? "verde" : "gris";
            }
            if (nombreBase.equals("otros") || nombreBase.equals("quiebras")) {
                color = (valor) ? "rojo" : "gris";
            }

            String nombreArchivo = "static/" + nombreBase + "_" + color + ".png";
            InputStream imageStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(nombreArchivo);
            if (imageStream == null) {
                throw new RuntimeException("No se encontró la imagen: " + nombreArchivo);
            }

            ImageData imageData = null;
            try {
                imageData = ImageDataFactory.create(imageStream.readAllBytes());
            } catch (Exception e) {
                e.printStackTrace(); // o lanza una excepción personalizada si lo prefieres
            }

            Image image = new Image(imageData)
                    .scaleToFit(anchoImagen, altoImagen)
                    .setHorizontalAlignment(HorizontalAlignment.CENTER);

            Div contenedor = new Div()
                    .add(image).setTextAlignment(TextAlignment.CENTER);

            Cell cell = new Cell()
                    .setBorder(Border.NO_BORDER)
                    .add(contenedor);

            tabla.addCell(cell);
        }

        document.add(tabla);
    }

    public static void dibujarImagenesIndicadoresConGlosas(Document document, List<String> nombresIndicadores,
            List<Boolean> indicadores) {
        float anchoImagen = 55f;
        float altoImagen = 55f;

        Table tabla = new Table(UnitValue.createPercentArray(nombresIndicadores.size()))
                .useAllAvailableWidth()
                .setMarginBottom(20)
                .setBorder(new SolidBorder(grisSuave, 0));

        for (int i = 0; i < nombresIndicadores.size(); i++) {
            String nombreBase = nombresIndicadores.get(i);
            String nombreArchivo = "static/";
            String nombreGlosa = "";
            boolean valor = indicadores.get(i);

            if (nombreBase.equals("protymor")) {
                nombreArchivo = nombreArchivo + (valor ? "alertaroja" : "informacionpositiva") + ".png";
                nombreGlosa = "Protestos y Morosidades";
            }
            if (nombreBase.equals("boletin")) {
                nombreArchivo = nombreArchivo + (valor ? "alertaroja" : "informacionpositiva") + ".png";
                nombreGlosa = "Boletín Laboral";
            }
            if (nombreBase.equals("vehiculos")) {
                nombreArchivo = nombreArchivo + (valor ? "informacionpositiva" : "blanco") + ".png";
                nombreGlosa = "Vehículos Bienes Raíces";
            }
            if (nombreBase.equals("socios")) {
                nombreArchivo = nombreArchivo + (valor ? "informacionpositiva" : "blanco") + ".png";
                nombreGlosa = "Socios y Sociedades";
            }
            if (nombreBase.equals("quiebras")) {
                nombreArchivo = nombreArchivo + (valor ? "informacionpositiva" : "blanco") + ".png";
                nombreGlosa = "Quiebras";
            }
            if (nombreBase.equals("otros")) {
                nombreArchivo = nombreArchivo + (valor ? "informacionpositiva" : "blanco") + ".png";
                nombreGlosa = "Otros Indicadores";
            }
            if (nombreBase.equals("blanco")) {
                nombreArchivo = nombreArchivo + (valor ? "blanco" : "blanco") + ".png";
                nombreGlosa = " ";
            }

            InputStream imageStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(nombreArchivo);
            if (imageStream == null) {
                throw new RuntimeException("No se encontró la imagen: " + nombreArchivo);
            }

            ImageData imageData = null;
            try {
                imageData = ImageDataFactory.create(imageStream.readAllBytes());
            } catch (Exception e) {
                e.printStackTrace(); // o lanza una excepción personalizada si lo prefieres
            }

            Image image = new Image(imageData)
                    .scaleToFit(anchoImagen, altoImagen)
                    .setHorizontalAlignment(HorizontalAlignment.CENTER);

            Paragraph etiqueta = new Paragraph(nombreGlosa)
                    .setFontSize(8)
                    .setTextAlignment(TextAlignment.CENTER);

            Div contenedor = new Div()
                    .add(image)
                    .add(etiqueta)
                    .setTextAlignment(TextAlignment.CENTER);

            Cell cell = new Cell()
                    .setBorder(Border.NO_BORDER)
                    .add(contenedor);

            tabla.addCell(cell);
        }

        document.add(tabla);
    }

    // entrega la información gráfica del puntaje Score de Maat
    public static void dibujarImagenesScore(Document document, List<String> nombresScore,
            List<Boolean> indicadores) {
        float anchoImagen = 60f;
        float altoImagen = 60f;

        document.add(new Paragraph("\n"));
        document.add(estiloTituloSeccionInforme("SCORE DE RIESGO"));

        Table tabla = new Table(UnitValue.createPercentArray(nombresScore.size()))
                .useAllAvailableWidth()
                .setMarginBottom(20)
                .setBorder(new SolidBorder(grisSuave, 1));

        for (int i = 0; i < nombresScore.size(); i++) {
            String nombreScore = nombresScore.get(i);
            boolean valor = indicadores.get(i);

            String prefijo = valor ? "on_" : "off_";

            String nombreArchivo = "static/" + prefijo + nombreScore + ".png";

            InputStream imageStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(nombreArchivo);
            if (imageStream == null) {
                throw new RuntimeException("No se encontró la imagen: " + nombreArchivo);
            }

            ImageData imageData = null;
            try {
                imageData = ImageDataFactory.create(imageStream.readAllBytes());
            } catch (Exception e) {
                e.printStackTrace(); // o lanza una excepción personalizada si lo prefieres
            }

            Image image = new Image(imageData)
                    .scaleToFit(anchoImagen, altoImagen)
                    .setHorizontalAlignment(HorizontalAlignment.CENTER);

            Div contenedor = new Div()
                    .add(image)
                    .setTextAlignment(TextAlignment.CENTER);

            Cell cell = new Cell()
                    .setBorder(Border.NO_BORDER)
                    .add(contenedor);

            tabla.addCell(cell);
        }

        document.add(tabla);
    }

}
