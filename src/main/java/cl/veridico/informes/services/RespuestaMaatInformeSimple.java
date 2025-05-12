package cl.veridico.informes.services;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue.Builder;

import cl.veridico.informes.utils.ListaOError;
import cl.veridico.informes.utils.ListaOErrorDeserializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class RespuestaMaatInformeSimple {
    @JsonProperty("Estado")
    private int Estado;

    @JsonProperty("Mensaje")
    private String Mensaje;

    @JsonProperty("Id_informe")
    private int Id_informe;

    @JsonProperty("Datos")
    private Datos Datos;

    // Getters y Setters
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
class Datos {
    @JsonProperty("Datos_Personales")
    private DatosPersonales datosPersonales;

    @JsonProperty("ScoreMaat")
    private String ScoreMaat;

    @JsonProperty("Deudas_previsionales")
    @JsonDeserialize(using = ListaOErrorDeserializer.class)
    private ListaOError<DeudaPrevisional> deudasPrevisionales;
    // private List<DeudaPrevisional> deudasPrevisionales;

    @JsonProperty("Deudas_Laborales")
    @JsonDeserialize(using = ListaOErrorDeserializer.class)
    private ListaOError<DeudaLaboral> deudasLaborales;
    // private List<DeudaLaboral> deudasLaborales;

    @JsonProperty("Protestos")
    @JsonDeserialize(using = ListaOErrorDeserializer.class)
    private ListaOError<Protesto> protestos;
    // private List<Protesto> protestos;

    // @JsonProperty("Infocom")
    @JsonProperty("Infocom")
    @JsonDeserialize(using = ListaOErrorDeserializer.class)
    private ListaOError<Infocom> infocom;
    // private List<Infocom> infocom;

    @JsonProperty("Deudas_Comercio")
    @JsonDeserialize(using = ListaOErrorDeserializer.class)
    private ListaOError<DeudaComercio> deudasComercio;
    // private List<DeudaComercio> deudasComercio;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class DatosPersonales {
        @JsonProperty("Rut")
        private String Rut;

        @JsonProperty("Nombre")
        private String Nombre;
        // Getters y Setters
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class DeudaPrevisional {
        @JsonProperty("Acreedor")
        private String Acreedor;

        @JsonProperty("Monto_Pesos")
        private String Monto_Pesos;

        @JsonProperty("Monto_UTM")
        private int Monto_UTM;

        @JsonProperty("Trabajadores_Afectados")
        private List<TrabajadorAfectado> Trabajadores_Afectados;

        // Getters y Setters

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        @JsonIgnoreProperties(ignoreUnknown = true)
        static class TrabajadorAfectado {
            @JsonProperty("Rut_trabajador")
            private String Rut_trabajador;

            @JsonProperty("Nombre_trabajador")
            private String Nombre_trabajador;

            @JsonProperty("Periodo_deuda")
            private String Periodo_deuda;

            @JsonProperty("Monto_deuda_pesos")
            private String Monto_deuda_pesos;

            @JsonProperty("Monto_deuda_UTM")
            private String Monto_deuda_UTM;

            @JsonProperty("Numero_boletin")
            private String Numero_boletin;

            @JsonProperty("Motivo")
            private String Motivo;

            // Getters y Setters
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class DeudaLaboral {
        @JsonProperty("Fecha_Boletin")
        private String Fecha_Boletin;

        @JsonProperty("Numero_Boletin")
        private String Numero_Boletin;

        @JsonProperty("Pagina_Boletin")
        private String Pagina_Boletin;

        @JsonProperty("Monto_Deuda")
        private String Monto_Deuda;

        @JsonProperty("Acreedor")
        private String Acreedor;

        @JsonProperty("Motivo")
        private String Motivo;

        // Getters y Setters
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Protesto {
        @JsonProperty("Fecha")
        private String Fecha;

        @JsonProperty("Glosa")
        private String Glosa;

        @JsonProperty("Tipo_Documento")
        private String Tipo_Documento;

        @JsonProperty("Monto_protesto")
        private String Monto_protesto;

        @JsonProperty("Numero_operacion")
        private String Numero_operacion;

        @JsonProperty("Glosa_documento")
        private String Glosa_documento;

        // Getters y Setters
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Infocom {
        @JsonProperty("Fecha")
        private String Fecha;

        @JsonProperty("tipo")
        private String tipo;

        @JsonProperty("Monto")
        private String Monto;

        @JsonProperty("Emisor")
        private String Emisor;

        // Getters y Setters
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class DeudaComercio {
        @JsonProperty("Fecha_vencimiento")
        private String Fecha_vencimiento;

        @JsonProperty("Tipo_documento")
        private String Tipo_documento;

        @JsonProperty("Monto")
        private String Monto;

        @JsonProperty("Emisor")
        private String Emisor;

        @JsonProperty("Fecha_publicacion")
        private String Fecha_publicacion;

        @JsonProperty("Mercado")
        private String Mercado;

        // Getters y Setters
    }

    // Getters y Setters
}
