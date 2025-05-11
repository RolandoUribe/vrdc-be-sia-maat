package cl.veridico.informes.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import cl.veridico.informes.utils.RutInfo;
import cl.veridico.informes.utils.UtilesVarios;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class RespuestaInformeConsolidado {

    public static ConsultaResponse generarRespuestaError(String idConsulta,
            String codigoRetorno) {
        String glosaRetorno = "";

        if (codigoRetorno.equals("100")) {
            glosaRetorno = "Error en la ejecucion del servicio";
        } else if (codigoRetorno.equals("101")) {
            glosaRetorno = "Error en la ejecucion del servicio - parametros incorrectos";
        }

        return ConsultaResponse.builder()
                .idConsulta(idConsulta)
                .codigoRetorno(codigoRetorno)
                .glosaRetorno(glosaRetorno)
                .resultado(new ConsultaResponse.Resultado()) // No incluye el objeto resultado en este caso
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .build();
    }

    public static ConsultaResponse generarRespuestaOK(String idConsulta,
            String codigoRetorno,
            String json) {

        try {
            String glosaRetorno = "Ejecucion Exitosa Servicio";

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            RespuestaMaatInformeSimple respuestaMaat = objectMapper.readValue(json, RespuestaMaatInformeSimple.class);

            RutInfo rutInfo = UtilesVarios.validarRetornarRut(respuestaMaat.getDatos().getDatosPersonales().getRut());

            int rut = rutInfo.getRut();
            String digito = rutInfo.getDigitoVerificador();

            // Mapeo de datos personales
            cl.veridico.informes.services.Datos.DatosPersonales datosPersonalesMaat = respuestaMaat.getDatos()
                    .getDatosPersonales();
            ConsultaResponse.Resultado.DatosPersonales datosPersonales = ConsultaResponse.Resultado.DatosPersonales
                    .builder()
                    .rut(rut)
                    .digito(digito)
                    .nombre(datosPersonalesMaat.getNombre())
                    .build();

            // Mapeo de datos Score
            int score = Integer.parseInt(respuestaMaat.getDatos().getScoreMaat());
            String[] resultado = UtilesVarios.obtenerRiesgoYGlosaPorScore(score);

            String glosaScore = resultado[0];
            String riesgoCrediticio = resultado[1];

            ConsultaResponse.Resultado.Score datosScore = ConsultaResponse.Resultado.Score.builder()
                    .puntaje(score)
                    .riesgoCrediticio(riesgoCrediticio)
                    .glosa(glosaScore)
                    .build();

            // Mapeo de deudas previsionales
            List<ConsultaResponse.Resultado.DeudaPrevisional> deudasPrevisionales = respuestaMaat.getDatos()
                    .getDeudasPrevisionales().getData()
                    .stream()
                    .map(deuda -> ConsultaResponse.Resultado.DeudaPrevisional.builder()
                            .acreedor(deuda.getAcreedor().trim())
                            .montoPesos(UtilesVarios.convertirMonto(deuda.getMonto_Pesos()))
                            .montoUTM(deuda.getMonto_UTM())
                            .trabajadoresAfectados(deuda.getTrabajadores_Afectados().stream()
                                    .map(trab -> ConsultaResponse.Resultado.DeudaPrevisional.TrabajadorAfectado
                                            .builder()
                                            .rutTrabajador(
                                                    UtilesVarios.validarRetornarRut(trab.getRut_trabajador()).getRut())
                                            .digitoTrabajador(UtilesVarios.validarRetornarRut(trab.getRut_trabajador())
                                                    .getDigitoVerificador())
                                            .nombreTrabajador(trab.getNombre_trabajador())
                                            .periodoDeuda(UtilesVarios.convertirFechaISO(trab.getPeriodo_deuda()))
                                            .montoDeudaPesos(UtilesVarios.convertirMonto(trab.getMonto_deuda_pesos()))
                                            .montoDeudaUTM(UtilesVarios.convertirMonto(trab.getMonto_deuda_UTM()))
                                            .numeroBoletin(trab.getNumero_boletin())
                                            .motivo(trab.getMotivo())
                                            .build())
                                    .collect(Collectors.toList()))
                            .build())
                    .collect(Collectors.toList());

            // Mapeo de deudas laborales
            List<ConsultaResponse.Resultado.DeudaLaboral> deudasLaborales = respuestaMaat.getDatos()
                    .getDeudasLaborales().getData()
                    .stream()
                    .map(deuda -> ConsultaResponse.Resultado.DeudaLaboral.builder()
                            .fechaBoletin(UtilesVarios.convertirFechaISO(deuda.getFecha_Boletin()))
                            .numeroBoletin(deuda.getNumero_Boletin())
                            .paginaBoletin(deuda.getPagina_Boletin())
                            .montoDeuda(UtilesVarios.convertirMonto(deuda.getMonto_Deuda()))
                            .acreedor(deuda.getAcreedor().trim())
                            .motivo(deuda.getMotivo())
                            .build())
                    .toList();

            // Mapeo de protestos
            List<ConsultaResponse.Resultado.Protesto> protestos = respuestaMaat.getDatos().getProtestos().getData()
                    .stream()
                    .map(prot -> ConsultaResponse.Resultado.Protesto.builder()
                            .fecha(UtilesVarios.convertirFechaISO(prot.getFecha()))
                            .glosa(prot.getGlosa())
                            .tipoDocumento(prot.getTipo_Documento())
                            .montoProtesto(UtilesVarios.convertirMonto(prot.getMonto_protesto()))
                            .numeroOperacion(prot.getNumero_operacion())
                            .glosaDocumento(prot.getGlosa_documento())
                            .build())
                    .toList();

            // Mapeo de Infocom
            List<ConsultaResponse.Resultado.Infocom> infocom = respuestaMaat.getDatos().getInfocom().getData()
                    .stream()
                    .map(info -> ConsultaResponse.Resultado.Infocom.builder()
                            .fecha(UtilesVarios.convertirFechaISO(info.getFecha()))
                            .tipo(info.getTipo())
                            .monto(UtilesVarios.convertirMonto(info.getMonto()))
                            .emisor(info.getEmisor())
                            .build())
                    .toList();

            // Mapeo de deudas de comercio
            List<ConsultaResponse.Resultado.DeudaComercio> deudasComercio = respuestaMaat.getDatos().getDeudasComercio()
                    .getData()
                    .stream()
                    .map(deuda -> ConsultaResponse.Resultado.DeudaComercio.builder()
                            .fechaVencimiento(UtilesVarios.convertirFechaISO(deuda.getFecha_vencimiento()))
                            .tipoDocumento(deuda.getTipo_documento())
                            .monto(UtilesVarios.convertirMonto(deuda.getMonto()))
                            .emisor(deuda.getEmisor())
                            .fechaPublicacion(UtilesVarios.convertirFechaISO(deuda.getFecha_publicacion()))
                            .mercado(deuda.getMercado())
                            .build())
                    .toList();

            return ConsultaResponse.builder()
                    .idConsulta(idConsulta)
                    .codigoRetorno(codigoRetorno)
                    .glosaRetorno(glosaRetorno)
                    .resultado(ConsultaResponse.Resultado.builder()
                            .datosPersonales(datosPersonales)
                            .datosScore(datosScore)
                            .deudasPrevisionales(deudasPrevisionales)
                            .deudasLaborales(deudasLaborales)
                            .protestos(protestos)
                            .infocom(infocom)
                            .deudasComercio(deudasComercio)
                            .build())
                    .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return ConsultaResponse.builder()
                    .idConsulta(idConsulta)
                    .codigoRetorno("100")
                    .glosaRetorno("Error en la ejecución del servicio")
                    .resultado(new ConsultaResponse.Resultado()) // No incluye el objeto resultado en este caso
                    .build();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_EMPTY) // Permite que resultado aparezca como {}
    public static class ConsultaResponse {
        private String idConsulta;
        private String codigoRetorno;
        private String glosaRetorno;
        private Resultado resultado;
        private String timestamp;

        /*
         * @JsonPropertyOrder({
         * "run",
         * "digito",
         * "numeroDocumento",
         * "numeroSerie",
         * "fechaVencimiento",
         * "indicadorBloqueo",
         * "indicadorVigencia",
         * "glosaRespuesta"
         * })
         */
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class Resultado {
            @JsonProperty("datosPersonales")
            private DatosPersonales datosPersonales;

            @JsonProperty("score")
            private Score datosScore;

            @JsonProperty("deudasPrevisionales")
            private List<DeudaPrevisional> deudasPrevisionales;

            @JsonProperty("deudasLaborales")
            private List<DeudaLaboral> deudasLaborales;

            @JsonProperty("protestos")
            private List<Protesto> protestos;

            @JsonProperty("infocom")
            private List<Infocom> infocom;

            @JsonProperty("deudasComercio")
            private List<DeudaComercio> deudasComercio;

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder
            public static class DatosPersonales {
                @JsonProperty("rut")
                private int rut;

                @JsonProperty("digito")
                private String digito;

                @JsonProperty("nombre")
                private String nombre;

                // Getters y Setters
            }

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder
            public static class Score {
                @JsonProperty("puntaje")
                private int puntaje;

                @JsonProperty("riesgoCrediticio")
                private String riesgoCrediticio;

                @JsonProperty("glosa")
                private String glosa;

                // Getters y Setters
            }

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder
            public static class DeudaPrevisional {
                @JsonProperty("acreedor")
                private String acreedor;

                @JsonProperty("montoPesos")
                private BigDecimal montoPesos;

                @JsonProperty("montoUTM")
                private int montoUTM;

                @JsonProperty("trabajadoresAfectados")
                private List<TrabajadorAfectado> trabajadoresAfectados;

                // Getters y Setters

                @Data
                @NoArgsConstructor
                @AllArgsConstructor
                @Builder
                public static class TrabajadorAfectado {
                    @JsonProperty("rutTrabajador")
                    private int rutTrabajador;

                    @JsonProperty("digitoTrabajador")
                    private String digitoTrabajador;

                    @JsonProperty("nombreTrabajador")
                    private String nombreTrabajador;

                    @JsonProperty("periodoDeuda")
                    private String periodoDeuda;

                    @JsonProperty("montoDeudaPesos")
                    private BigDecimal montoDeudaPesos;

                    @JsonProperty("montoDeudaUTM")
                    private BigDecimal montoDeudaUTM;

                    @JsonProperty("numeroBoletin")
                    private String numeroBoletin;

                    @JsonProperty("motivo")
                    private String motivo;

                    // Getters y Setters
                }
            }

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder
            public static class DeudaLaboral {
                @JsonProperty("fechaBoletin")
                private String fechaBoletin;

                @JsonProperty("numeroBoletin")
                private String numeroBoletin;

                @JsonProperty("paginaBoletin")
                private String paginaBoletin;

                @JsonProperty("montoDeuda")
                private BigDecimal montoDeuda;

                @JsonProperty("acreeedor")
                private String acreedor;

                @JsonProperty("motivo")
                private String motivo;

                // Getters y Setters
            }

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder
            public static class Protesto {
                @JsonProperty("fecha")
                private String fecha;

                @JsonProperty("glosa")
                private String glosa;

                @JsonProperty("tipoDocumento")
                private String tipoDocumento;

                @JsonProperty("montoProtesto")
                private BigDecimal montoProtesto;

                @JsonProperty("numeroOperacion")
                private String numeroOperacion;

                @JsonProperty("glosaDocumento")
                private String glosaDocumento;

                // Getters y Setters
            }

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder
            public static class Infocom {
                @JsonProperty("fecha")
                private String fecha;

                @JsonProperty("tipo")
                private String tipo;

                @JsonProperty("monto")
                private BigDecimal monto;

                @JsonProperty("emisor")
                private String emisor;

                // Getters y Setters
            }

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder
            public static class DeudaComercio {
                @JsonProperty("fechaVencimiento")
                private String fechaVencimiento;

                @JsonProperty("tipoDocumento")
                private String tipoDocumento;

                @JsonProperty("monto")
                private BigDecimal monto;

                @JsonProperty("emisor")
                private String emisor;

                @JsonProperty("fechaPublicacion")
                private String fechaPublicacion;

                @JsonProperty("mercado")
                private String mercado;

                // Getters y Setters
            }
        }
    }
}
