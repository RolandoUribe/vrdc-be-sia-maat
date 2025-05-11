package cl.veridico.informes.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

public class UtilesVarios {

    public static String generateUUId() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    // convierte un string en log para formatear la salida de un valor monetario
    // cuando se le envía un log
    public static String formatCurrency(BigDecimal amount) {
        NumberFormat formatter = NumberFormat.getInstance(Locale.forLanguageTag("es-CL"));
        return "$ " + formatter.format(amount);
    }

    // convierte un string en log para formatear la salida de un valor monetario
    // cuando se le envía un string
    public static String formatCurrency(String strAmount) {
        long amount = 0;
        try {
            amount = Long.valueOf(strAmount);

            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.GERMANY);
            DecimalFormat df = new DecimalFormat("###,###,###", symbols);
            String formattedAmount = df.format(amount);
            return "$ " + formattedAmount;
        } catch (Exception e) {
            return "$ 0";
        }
    }

    /**
     * Valida un RUT chileno.
     * 
     * @param rut El RUT completo (incluyendo dígito verificador) en formato
     *            "XXXXXXXX-D".
     *            El rut puede venir como en los siguientes ejemplos:
     *            "12.345.678-5", "12345678-5", "9876543-K", "9876543-0",
     *            "123456789"
     * @return true si el RUT es válido, false en caso contrario.
     */
    public static boolean validarRut(String rut) {
        // Eliminar puntos y espacios, y convertir a mayúsculas
        rut = rut.replace(".", "").replace("-", "").toUpperCase();

        // Verificar que tenga al menos un dígito y un dígito verificador
        if (!rut.matches("\\d{7,8}[0-9K]")) {
            return false;
        }

        // Separar el número del dígito verificador
        String numero = rut.substring(0, rut.length() - 1);
        char dvIngresado = rut.charAt(rut.length() - 1);

        // Calcular el dígito verificador
        char dvCalculado = calcularDigitoVerificador(numero);

        // Comparar el dígito ingresado con el calculado
        return dvIngresado == dvCalculado;
    }

    public static String convertirFechaISO(String fecha) {

        if (fecha == null) {
            return "";
        }

        //System.out.println("Fecha original: [" + fecha + "]");

        fecha = fecha.replace('/', '-');
        // Definir los posibles formatos de entrada
        DateTimeFormatter formatoDiaMesAnio = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        DateTimeFormatter formatoDiaMesAnioSlash = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter formatoAnioMesDia = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter formatoAnioMes = DateTimeFormatter.ofPattern("yyyy-MM");
        DateTimeFormatter formatoMesAnio = DateTimeFormatter.ofPattern("MM-yyyy");
        DateTimeFormatter formatoAnioDiaMes = DateTimeFormatter.ofPattern("yyyy-dd-MM");
        DateTimeFormatter formatoAnioMesDiaSinSep = DateTimeFormatter.ofPattern("yyyyMMdd");

        try {
            // Intentar parsear como "dd-MM-yyyy"
            return LocalDate.parse(fecha, formatoDiaMesAnio).toString(); // Retorna yyyy-MM-dd
        } catch (DateTimeParseException ignored) {
        }

        try {
            // Intentar parsear como "dd/MM/yyyy"
            return LocalDate.parse(fecha, formatoDiaMesAnioSlash).toString(); // Retorna yyyy-MM-dd
        } catch (DateTimeParseException ignored) {
        }

        try {
            // Intentar parsear como "yyyy-MM-dd"
            return LocalDate.parse(fecha, formatoAnioMesDia).toString(); // Retorna yyyy-MM-dd
        } catch (DateTimeParseException ignored) {
        }

        try {
            // Intentar parsear como "yyyy-MM" (solo año y mes, asumiendo día 01)
            return YearMonth.parse(fecha, formatoAnioMes).atDay(1).toString().substring(0, 7); // Retorna yyyy-MM
        } catch (DateTimeParseException ignored) {
        }

        try {
            // Intentar parsear como "MM-yyyy" (solo mes y año, asumiendo día 01)
            return YearMonth.parse(fecha, formatoMesAnio).atDay(1).toString().substring(0, 7); // MM-yyyy → yyyy-MM
        } catch (DateTimeParseException ignored) {
        }

        try {
            // Intentar parsear como "M-yyyy" (solo mes (un digito) y año, asumiendo día 01)
            String fecha_01 = fecha;
            if (fecha.length() == 6)
                fecha_01 = "0" + fecha;
            return YearMonth.parse(fecha_01, formatoMesAnio).atDay(1).toString().substring(0, 7); // M-yyyy → yyyy-MM
        } catch (DateTimeParseException ignored) {
        }

        try {
            // Intentar parsear como "yyyy-dd-MM"
            return LocalDate.parse(fecha, formatoAnioDiaMes).toString(); // Retorna yyyy-MM-dd
        } catch (DateTimeParseException ignored) {
        }

        try {
            // Intentar parsear como "yyyyMMdd"
            return LocalDate.parse(fecha, formatoAnioMesDiaSinSep).toString(); // Retorna yyyyMMdd
        } catch (DateTimeParseException ignored) {
        }
        // Si ninguno funciona, retornar null o lanzar una excepción
        // throw new IllegalArgumentException("Formato de fecha no reconocido: " +
        // fecha);
        return fecha;
    }

    public static RutInfo validarRetornarRut(String rut) {

        if (rut == null || rut.isEmpty()) {
            System.out.println("Rut [" + rut + "] no válido");
            return new RutInfo(0, "", false);
        }
        
        // Eliminar puntos y espacios, y convertir a mayúsculas
        rut = rut.trim().replace(".", "").replace("-", "").toUpperCase();

        // Verificar que tenga al menos un dígito y un dígito verificador
        if (!rut.matches("\\d{7,8}[0-9K]")) {
            System.out.println("Rut [" + rut + "] no válido");
            return new RutInfo(0, "", false);
        }

        // Separar el número del dígito verificador
        String numero = rut.substring(0, rut.length() - 1);
        char dvIngresado = rut.charAt(rut.length() - 1);

        // Calcular el dígito verificador
        char dvCalculado = calcularDigitoVerificador(numero);

        // Comparar el dígito ingresado con el calculado
        boolean esValido = dvIngresado == dvCalculado;
        int run = Integer.parseInt(numero);
        String dv = String.valueOf(dvCalculado);
        return new RutInfo(run, dv, esValido);
    }

    /**
     * Calcula el dígito verificador para un RUT chileno.
     * 
     * @param numero El número del RUT (sin el dígito verificador).
     * @return El dígito verificador como un carácter ('0'-'9' o 'K').
     */
    private static char calcularDigitoVerificador(String numero) {
        int suma = 0;
        int multiplicador = 2;

        // Recorrer los dígitos de derecha a izquierda
        for (int i = numero.length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(numero.charAt(i)) * multiplicador;
            multiplicador = (multiplicador == 7) ? 2 : multiplicador + 1;
        }

        // Calcular el resto y el dígito verificador
        int resto = 11 - (suma % 11);
        if (resto == 11) {
            return '0';
        } else if (resto == 10) {
            return 'K';
        } else {
            return (char) (resto + '0');
        }
    }

    public static String generateId() {
        // Obtener la fecha y hora actual
        LocalDateTime now = LocalDateTime.now();

        // Formatear AAAAMMDDhhmmssSSS
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        String timestamp = now.format(formatter);

        // Generar un número aleatorio de 0000 a 9999
        Random random = new Random();
        int randomNumber = random.nextInt(10000); // Número entre 0 y 9999

        // Formatear para que siempre tenga 4 dígitos (rellenar con ceros si es
        // necesario)
        String randomPart = String.format("%04d", randomNumber);

        // Concatenar todo y retornar el ID
        return timestamp + randomPart;
    }

    // Método para "pretty print" del JSON
    public static String prettyPrintJson(String json) {
        if (json == null || json.isEmpty()) {
            return "No body";
        }

        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Object jsonObject = mapper.readValue(json, Object.class);

            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
        } catch (Exception e) {
            return "Invalid JSON: " + json;
        }
    }

    public static String getRespuestaErrorVeridico(String idConsulta,
            String codigoRetorno,
            String glosaRetorno) {

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"));

        String retornoError = "{\n" +
                "  \"idConsulta\": \"" + idConsulta + "\",\n" +
                "  \"codigoRetorno\": \"" + codigoRetorno + "\",\n" +
                "  \"glosaRetorno\": \"" + glosaRetorno + "\",\n" +
                "  \"resultado\": {}" + ",\n" +
                "  \"timestamp\": \"" + timestamp + "\"\n" +
                "}";

        return retornoError;
    }

    // Función para convertir el monto (String con puntos) a BigDecimal
    public static BigDecimal convertirMonto(String monto) {
        if (monto == null || monto.isEmpty()) {
            return BigDecimal.ZERO;
        }
        String montoLimpio = monto.replace(".", ""); // Elimina puntos de miles
        return new BigDecimal(montoLimpio);
    }


        // Método para capitalizar la primera letra de cada palabra en un texto
    // Ejemplo: "hola mundo" -> "Hola Mundo"
    public static String capitalizarPalabras(String texto) {
        if (texto == null || texto.isEmpty()) {
            return texto;
        }
    
        String[] palabras = texto.toLowerCase().split("\\s+");
        StringBuilder resultado = new StringBuilder();
    
        for (String palabra : palabras) {
            if (!palabra.isEmpty()) {
                resultado.append(Character.toUpperCase(palabra.charAt(0)))
                         .append(palabra.substring(1))
                         .append(" ");
            }
        }
    
        return resultado.toString().trim();
    }

    // Método para obtener el riesgo crediticio y la glosa del score
    // según la tabla proporcionada
    // El score debe estar entre 0 y 950
    public static String[] obtenerRiesgoYGlosaPorScore(int score) {
        String glosaScore;
        String riesgoCrediticio;
    
        if (score >= 0 && score <= 200) {
            riesgoCrediticio = "Riesgo Crítico";
            glosaScore = "Montos Impagos Superior a $5.000.000";
        } else if (score > 200 && score <= 350) {
            riesgoCrediticio = "Riesgo Muy Alto";
            glosaScore = "Montos Impagos Entre $2.500.001 y $5.000.000";
        } else if (score > 350 && score <= 500) {
            riesgoCrediticio = "Alto Riesgo";
            glosaScore = "Montos Impagos Entre $1.000.001 y $2.500.000";
        } else if (score > 500 && score <= 700) {
            riesgoCrediticio = "Riesgo Moderado a Alto";
            glosaScore = "Montos Impagos Entre $500.001 y $1.000.000";
        } else if (score > 700 && score <= 800) {
            riesgoCrediticio = "Riesgo Moderado";
            glosaScore = "Montos Impagos Entre $1 y $500.000";
        } else if (score > 800 && score <= 950) {
            riesgoCrediticio = "Bajo Riesgo";
            glosaScore = "Montos Impagos $0 (No se registran deudas)";
        } else {
            riesgoCrediticio = "Sin Score";
            glosaScore = "Sin Score";
        }
    
        return new String[]{glosaScore, riesgoCrediticio};
    }
}
