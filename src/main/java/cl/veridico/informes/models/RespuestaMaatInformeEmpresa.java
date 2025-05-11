package cl.veridico.informes.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
public class RespuestaMaatInformeEmpresa {
    @JsonProperty("Estado")
    private String estado;

    @JsonProperty("Mensaje")
    private String mensaje;

    @JsonProperty("Id_informe")
    private int idInforme;

    @JsonProperty("Datos")
    public DatosEmpresa datosEmpresa;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
class DatosEmpresa {
    @JsonProperty("IdentificacionJuridica")
    public IdentificacionJuridica idJuridica;

    @JsonProperty("ScoreMaat")
    public String scoreMaat;

    @JsonProperty("TimbrajeAutorizado")
    @JsonDeserialize(using = ListaOErrorDeserializer.class)
    private ListaOError<TimbrajeAutorizado> timbrajesAutorizados;

    @JsonProperty("ActividadesEconomicas")
    @JsonDeserialize(using = ListaOErrorDeserializer.class)
    private ListaOError<ActividadEconomica> actividadesEconomicas;

    @JsonProperty("DireccionesRegistradas")
    @JsonDeserialize(using = ListaOErrorDeserializer.class)
    private ListaOError<DireccionRegistrada> direccionesRegistradas;

    @JsonProperty("SociedadesEmpresariales")
    @JsonDeserialize(using = ListaOErrorDeserializer.class)
    private ListaOError<SociedadEmpresarial> sociedadesEmpresariales;

    @JsonProperty("SociedadesPersonas")
    @JsonDeserialize(using = ListaOErrorDeserializer.class)
    private ListaOError<SociedadPersona> sociedadesPersonas;

    @JsonProperty("ResumenTotalesMorosidades")
    private ResumenTotalMorosidad resumenTotalesMorosidades;

    @JsonProperty("InfraccionesPrevisional")
    @JsonDeserialize(using = ListaOErrorDeserializer.class)
    private ListaOError<DeudaPrevisional> deudasPrevisionales;

    @JsonProperty("InfraccionesLaborales")
    @JsonDeserialize(using = ListaOErrorDeserializer.class)
    private ListaOError<DeudaLaboral> deudasLaborales;

    @JsonProperty("Protestos")
    @JsonDeserialize(using = ListaOErrorDeserializer.class)
    private ListaOError<Protesto> protestos;

    @JsonProperty("MorosidadInfocom")
    @JsonDeserialize(using = ListaOErrorDeserializer.class)
    private ListaOError<MorosidadInfocom> morosidadesInfocom;

    @JsonProperty("MorosidadVigente")
    @JsonDeserialize(using = ListaOErrorDeserializer.class)
    private ListaOError<MorosidadVigente> morosidadesVigentes;

    @JsonProperty("BoletinMaat")
    @JsonDeserialize(using = ListaOErrorDeserializer.class)
    private ListaOError<BoletinMaat> boletinesMaat;

    @JsonProperty("AnotacionesCorredores")
    @JsonDeserialize(using = ListaOErrorDeserializer.class)
    private ListaOError<AnotacionCorredor> anotacionesCorredores;

    @JsonProperty("ResumenConsolidado")
    private ResumenConsolidado resumenConsolidado;

    @JsonProperty("Quiebras")
    @JsonDeserialize(using = ListaOErrorDeserializer.class)
    private ListaOError<Quiebra> quiebras;

    @JsonProperty("Vehiculos")
    @JsonDeserialize(using = ListaOErrorDeserializer.class)
    private ListaOError<Vehiculo> vehiculos;

    @JsonProperty("BienesRaices")
    @JsonDeserialize(using = ListaOErrorDeserializer.class)
    private ListaOError<BienRaiz> bienesRaices;

    @JsonProperty("Exportaciones")
    @JsonDeserialize(using = ListaOErrorDeserializer.class)
    private ListaOError<Exportacion> exportaciones;

    @JsonProperty("Importaciones")
    @JsonDeserialize(using = ListaOErrorDeserializer.class)
    private ListaOError<Importacion> importaciones;

    @JsonProperty("Prendas")
    @JsonDeserialize(using = ListaOErrorDeserializer.class)
    private ListaOError<Prenda> prendas;

    @JsonProperty("Extracto")
    private String extracto;

    @JsonProperty("ConsultasAlRut")
    @JsonDeserialize(using = ListaOErrorDeserializer.class)
    private ListaOError<ConsultaRut> consultasAlRut;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class IdentificacionJuridica {
        @JsonProperty("Rut")
        private int rut;

        @JsonProperty("Dv")
        private String digito;

        @JsonProperty("Rsocial")
        private String razonSocial;

        @JsonProperty("FechaIni")
        private String fechaInicioActividades;

        @JsonProperty("ActPrincipal")
        private String actividadPrincipal;

        @JsonProperty("SegEmpresa")
        private String segmentoEmpresa;

        @JsonProperty("NumEmpleados")
        private String numeroEmpleados;

        @JsonProperty("RegTributarioEsp")
        private String regimenTributarioEspecial;

        @JsonProperty("F29")
        private String formulario29;

        @JsonProperty("F22")
        private String formulario22;

        @JsonProperty("F50")
        private String formulario50;

        @JsonProperty("FproPyme")
        private String regimenPYME;

        @JsonProperty("FmonedaExt")
        private String monedaExtranjera;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TimbrajeAutorizado {
        @JsonProperty("DocAutorizado")
        private String documentoAutorizado;

        @JsonProperty("FecAutorizacion")
        private String fechaAutorizacion;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ActividadEconomica {
        @JsonProperty("CodActeco")
        private String codigoActividadEconomica;

        @JsonProperty("ActEconomica")
        private String glosaActividadEconomica;

        @JsonProperty("FecInicio")
        private String fechaInicio;

        @JsonProperty("FecTermino")
        private String fechaTermino;

        @JsonProperty("AfectoIva")
        private String afectoIva;

        @JsonProperty("CategoriaActeco")
        private String categoriaActividadEconomica;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DireccionRegistrada {
        @JsonProperty("TipoDireccion")
        private String tipoDireccion;

        @JsonProperty("Calle")
        private String calle;

        @JsonProperty("Numero")
        private String numero;

        @JsonProperty("Bloque")
        private String bloque;

        @JsonProperty("Depto")
        private String departamento;

        @JsonProperty("VillaPoblacion")
        private String villaPoblacion;

        @JsonProperty("Ciudad")
        private String ciudad;

        @JsonProperty("Comuna")
        private String comuna;

        @JsonProperty("Region")
        private String region;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SociedadEmpresarial {
        @JsonProperty("RutSocio")
        private String rutSocio;

        @JsonProperty("RazonSocial")
        private String razonSocial;

        @JsonProperty("FecRelacion")
        private String fechaRelacion;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class SociedadPersona {

        @JsonProperty("Fecha")
        private String fecha;

        @JsonProperty("Contenido")
        private String contenido;

        @JsonProperty("Nombre")
        private String nombre;

        @JsonProperty("Rut")
        private String rut;

        @JsonProperty("Edicion")
        private String edicion;

        @JsonProperty("Cuerpo")
        private String cuerpo;

        @JsonProperty("Pagina")
        private String pagina;

        @JsonProperty("Extracto")
        private String extracto;

        @JsonProperty("Socios")
        @JsonDeserialize(using = ListaOErrorDeserializer.class)
        private ListaOError<Socio> socios;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Socio {
            @JsonProperty("Rut_socio")
            private String rutSocio;

            @JsonProperty("Nombre_socio")
            private String nombreSocio;

            @JsonProperty("Tipo")
            private String tipo;

            @JsonProperty("Aporte")
            private int aporte;
        }

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ResumenTotalMorosidad {
        @JsonProperty("MontoTotalProtestos")
        private String montoTotalProtestos;

        @JsonProperty("MontoTotalInfocom")
        private String montoTotalInfocom;

        @JsonProperty("MontoTotalDeudasPrevisionales")
        private String montoTotalDeudasPrevisionales;

        @JsonProperty("MontoTotalDeudasLaborales")
        private String montoTotalDeudasLaborales;

        @JsonProperty("MontoTotalDeudasComercio")
        private String montoTotalDeudasComercio;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DeudaPrevisional {
        @JsonProperty("Acreedor")
        private String acreedor;

        @JsonProperty("Monto_Pesos")
        private String montoPesos;

        @JsonProperty("Monto_UTM")
        private int montoUTM;

        @JsonProperty("Trabajadores_Afectados")
        private List<TrabajadorAfectado> trabajadoresAfectados;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class TrabajadorAfectado {
            @JsonProperty("Rut_trabajador")
            private String rutTrabajador;

            @JsonProperty("Nombre_trabajador")
            private String nombreTrabajador;

            @JsonProperty("Periodo_deuda")
            private String periodoDeuda;

            @JsonProperty("Monto_deuda_pesos")
            private String montoDeudaPesos;

            @JsonProperty("Monto_deuda_UTM")
            private String montoDeudaUTM;

            @JsonProperty("Numero_boletin")
            private String numeroBoletin;

            @JsonProperty("Motivo")
            private String motivo;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DeudaLaboral {
        @JsonProperty("Fecha_Boletin")
        private String fechaBoletin;

        @JsonProperty("Numero_Boletin")
        private String numeroBoletin;

        @JsonProperty("Pagina_Boletin")
        private String paginaBoletin;

        @JsonProperty("Monto_Deuda")
        private String montoDeuda;

        @JsonProperty("Acreedor")
        private String acreedor;

        @JsonProperty("Motivo")
        private String motivo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Protesto {
        @JsonProperty("Fecha")
        private String fecha;

        @JsonProperty("Glosa")
        private String glosa;

        @JsonProperty("Tipo_Documento")
        private String tipoDocumento;

        @JsonProperty("Monto_protesto")
        private String montoProtesto;

        @JsonProperty("Numero_operacion")
        private String numeroOperacion;

        @JsonProperty("Glosa_documento")
        private String glosaDocumento;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MorosidadInfocom {
        @JsonProperty("Fecha_vencimiento")
        private String fecha;

        @JsonProperty("Tipo")
        private String tipo;

        @JsonProperty("Monto")
        private String monto;

        @JsonProperty("Emisor")
        private String emisor;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MorosidadVigente {
        @JsonProperty("Fecha_Vencimiento")
        private String fechaVencimiento;

        @JsonProperty("Tipo_credito")
        private String tipoCredito;

        @JsonProperty("Monto")
        private String monto;

        @JsonProperty("Acreedor")
        private String acreedor;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BoletinMaat {
        @JsonProperty("Fecha_vencimiento")
        private String fechaVencimiento;

        @JsonProperty("Tipo_documento")
        private String tipoCredito;

        @JsonProperty("Monto")
        private String monto;

        @JsonProperty("Acreedor")
        private String acreedor;

        @JsonProperty("Fecha_publicacion")
        private String fechaPublicacion;

        @JsonProperty("Mercado")
        private String mercado;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AnotacionCorredor {
        @JsonProperty("Corredor")
        private String corredor;

        @JsonProperty("Direccion")
        private String direccion;

        @JsonProperty("Anotacion")
        private String anotacion;

        @JsonProperty("Tipo")
        private String tipo;

        @JsonProperty("Fecha")
        private String fecha;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ResumenConsolidado {
        @JsonProperty("Eventos_quiebras")
        private String eventosQuiebras;

        @JsonProperty("Bancos_relacionados")
        private String bancosRelacionados;

        @JsonProperty("Orden_no_pago")
        private int ordenNoPago;

        @JsonProperty("Vehiculos")
        private int vehiculos;

        @JsonProperty("Bienes_raices")
        private int bienesRaices;

        @JsonProperty("Exportaciones")
        private int exportaciones;

        @JsonProperty("Importaciones")
        private int importaciones;

        @JsonProperty("Infracciones_laborales")
        private int infraccionesLaborales;

        @JsonProperty("Infracciones_provisionales")
        private int infraccionesPrevisionales;

        @JsonProperty("Prendas")
        private int prendas;

        @JsonProperty("Anotaciones_tributarias")
        private int anotacionesTributarias;

        @JsonProperty("Socieades")
        private int socieades;

        @JsonProperty("Socios")
        private int socios;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Quiebra {
        @JsonProperty("Rol")
        private String rol;

        @JsonProperty("Procedimiento")
        private String procedimiento;

        @JsonProperty("Deudor")
        private String deudor;

        @JsonProperty("Veedor")
        private String veedor;

        @JsonProperty("Publicacion")
        private String publicacion;

        @JsonProperty("Tribunal")
        private String tribunal;

        @JsonProperty("Fecha")
        private String fecha;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Vehiculo {
        @JsonProperty("Tipo")
        private String tipo;

        @JsonProperty("Marca")
        private String marca;

        @JsonProperty("Patente")
        private String patente;

        @JsonProperty("Modelo")
        private String modelo;

        @JsonProperty("Agno")
        private String agno;

        @JsonProperty("Avaluo")
        private String avaluo;  }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BienRaiz {
        @JsonProperty("Rol")
        private String rol;

        @JsonProperty("Comuna")
        private String comuna;

        @JsonProperty("Direccion")
        private String direccion;

        @JsonProperty("Tipo")
        private String tipo;

        @JsonProperty("Destino")
        private String destino;

        @JsonProperty("Avaluo")
        private String avaluo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Exportacion {
        @JsonProperty("Agno")
        private String agno;

        @JsonProperty("Fob")
        private String fob;

        @JsonProperty("Cif")
        private String cif;

        @JsonProperty("Mumero")
        private String numero;

        @JsonProperty("Enero")
        private String enero;

        @JsonProperty("Febrero")
        private String febrero;

        @JsonProperty("Marzo")
        private String marzo;

        @JsonProperty("Abril")
        private String abril;

        @JsonProperty("Mayo")
        private String mayo;

        @JsonProperty("Junio")
        private String junio;

        @JsonProperty("Julio")
        private String julio;

        @JsonProperty("Agosto")
        private String agosto;

        @JsonProperty("Septiembre")
        private String septiembre;

        @JsonProperty("Octubre")
        private String octubre;

        @JsonProperty("Noviembre")
        private String noviembre;

        @JsonProperty("Diciembre")
        private String diciembre;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Importacion {
        @JsonProperty("Agno")
        private String agno;

        @JsonProperty("Fob")
        private String fob;

        @JsonProperty("Cif")
        private String cif;

        @JsonProperty("Mumero")
        private String numero;

        @JsonProperty("Enero")
        private String enero;

        @JsonProperty("Febrero")
        private String febrero;

        @JsonProperty("Marzo")
        private String marzo;

        @JsonProperty("Abril")
        private String abril;

        @JsonProperty("Mayo")
        private String mayo;

        @JsonProperty("Junio")
        private String junio;

        @JsonProperty("Julio")
        private String julio;

        @JsonProperty("Agosto")
        private String agosto;

        @JsonProperty("Septiembre")
        private String septiembre;

        @JsonProperty("Octubre")
        private String octubre;

        @JsonProperty("Noviembre")
        private String noviembre;

        @JsonProperty("Diciembre")
        private String diciembre;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Prenda {
        @JsonProperty("Acreedor")
        private String acreedor;

        @JsonProperty("Fecha_publicacion")
        private String fechaPublicacion;

        @JsonProperty("Cuerpo")
        private String cuerpo;

        @JsonProperty("Pagina")
        private String pagina;

        @JsonProperty("Extracto")
        private String extracto;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ConsultaRut {
        @JsonProperty("Fecha")
        private String fecha;

        @JsonProperty("Institucion")
        private String institucion;
    }

}
