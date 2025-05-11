package cl.veridico.informes.services;

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
public class RespuestaMaatInformeFull {
    @JsonProperty("Estado")
    private String estado;

    @JsonProperty("Mensaje")
    private String mensaje;

    @JsonProperty("Id_informe")
    private int idInforme;

    @JsonProperty("Datos")
    private DatosFull datos;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
class DatosFull {
    @JsonProperty("Datos_Personales")
    private DatosPersonales datosPersonales;
    
    @JsonProperty("ScoreMaat")
    private String ScoreMaat;

    @JsonProperty("Resumen_Montos")
    private ResumenMontos resumenMontos;

    @JsonProperty("Direcciones")
    @JsonDeserialize(using = ListaOErrorDeserializer.class)
    private ListaOError<Direccion> direcciones;

    @JsonProperty("Morosidades")
    @JsonDeserialize(using = ListaOErrorDeserializer.class)
    private ListaOError<Morosidad> morosidades;

    @JsonProperty("Deudas_previsionales")
    @JsonDeserialize(using = ListaOErrorDeserializer.class)
    private ListaOError<DeudaPrevisional> deudasPrevisionales;

    @JsonProperty("Deudas_Laborales")
    @JsonDeserialize(using = ListaOErrorDeserializer.class)
    private ListaOError<DeudaLaboral> deudasLaborales;

    @JsonProperty("Protestos")
    @JsonDeserialize(using = ListaOErrorDeserializer.class)
    private ListaOError<Protesto> protestos;

    @JsonProperty("Infocom")
    @JsonDeserialize(using = ListaOErrorDeserializer.class)
    private ListaOError<Infocom> infocom;

    @JsonProperty("Deudas_Comercio")
    @JsonDeserialize(using = ListaOErrorDeserializer.class)
    private ListaOError<DeudaComercio> deudasComercio;

    @JsonProperty("Resumen_empresarial")
    private ResumenEmpresarial resumenEmpresarial;

    @JsonProperty("Quiebras")
    @JsonDeserialize(using = ListaOErrorDeserializer.class)
    private ListaOError<Quiebra> quiebras;

    @JsonProperty("Orden_no_pago")
    @JsonDeserialize(using = ListaOErrorDeserializer.class)
    private ListaOError<OrdenNoPago> ordenNoPago;

    @JsonProperty("Vehiculos")
    @JsonDeserialize(using = ListaOErrorDeserializer.class)
    private ListaOError<Vehiculo> vehiculos;

    @JsonProperty("Bienes_Raices")
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

    @JsonProperty("Anotaciones")
    @JsonDeserialize(using = ListaOErrorDeserializer.class)
    private ListaOError<Anotacion> anotaciones;

    @JsonProperty("Socio_Sociedades")
    @JsonDeserialize(using = ListaOErrorDeserializer.class)
    private ListaOError<SocioSociedades> socioSociedades;

    @JsonProperty("Extracto")
    private String extracto;

    @JsonProperty("Consultas_Rut")
    @JsonDeserialize(using = ListaOErrorDeserializer.class)
    private ListaOError<ConsultaRut> consultasRut;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class DatosPersonales {
        @JsonProperty("Rut")
        private String rut;

        @JsonProperty("Nombre")
        private String nombre;

        @JsonProperty("Tipo_persona")
        private String tipoPersona;

        @JsonProperty("Persona")
        private Persona persona;

        @JsonProperty("Empresa")
        private Empresa empresa;

        @JsonProperty("Ultima_direccion_informada")
        private String ultimaDireccionInformada;

        @JsonProperty("Cantidad_direcciones")
        private String cantidadDirecciones;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Persona {
            @JsonProperty("Edad")
            private String edad;
    
            @JsonProperty("sexo")
            private String sexo;
    
            @JsonProperty("Estado_civil")
            private String estadoCivil;
    
            @JsonProperty("Nacionalidad")
            private String nacionalidad;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Empresa {
            @JsonProperty("Giro")
            @JsonDeserialize(using = ListaOErrorDeserializer.class)
            private ListaOError<Giro> giro;
            //private List<Giro> giro;
    
            @JsonProperty("Productos")
            @JsonDeserialize(using = ListaOErrorDeserializer.class)
            private ListaOError<Productos> productos;
            //private Productos productos;

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Giro {
                @JsonProperty("Giros")
                private String giros;
            }

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Productos {
                @JsonProperty("Producto")
                private String producto;
            }
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ResumenMontos {
        @JsonProperty("Monto_total_protestos")
        private String montoTotalProtestos;

        @JsonProperty("Monto_total_infocom")
        private String montoTotalInfocom;

        @JsonProperty("Monto_total_deudas_previsionales")
        private String montoTotalDeudasPrevisionales;

        @JsonProperty("Monto_total_deudas_laborales")
        private String montoTotalDeudasLaborales;

        @JsonProperty("Monto_total_deudas_comercio")
        private String montoTotalDeudasComercio;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Direccion {
        @JsonProperty("Tipo")
        private String tipo;

        @JsonProperty("Direccion")
        private String direccion;

        @JsonProperty("Fecha_informacion")
        private String fechaInformacion;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Morosidad {
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
    public static class Infocom {
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
    public static class DeudaComercio {
        @JsonProperty("Fecha_vencimiento")
        private String fechaVencimiento;

        @JsonProperty("Tipo_documento")
        private String tipoDocumento;

        @JsonProperty("Monto")
        private String monto;

        @JsonProperty("Emisor")
        private String emisor;

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
    public static class ResumenEmpresarial {
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
        @JsonProperty("Resolucion")
        private String resolucion;

        @JsonProperty("Fecha_resolucion")
        private String fechaResolucion;

        @JsonProperty("Fecha_publicacion")
        private String fechaPublicacion;

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
    public static class OrdenNoPago {
        @JsonProperty("Banco")
        private String banco;

        @JsonProperty("Cuenta_cliente")
        private String cuentaCliente;

        @JsonProperty("Numero_cheque")
        private String numeroCheque;

        @JsonProperty("Numero_serie")
        private String numeroSerie;

        @JsonProperty("Sucursal")
        private String sucursal;

        @JsonProperty("Fecha")
        private String fecha;

        @JsonProperty("Motoivo")
        private String motivo;

        @JsonProperty("Fuente")
        private String fuente;
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
        private String avaluo;
    }

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
    public static class Anotacion {
        @JsonProperty("Fecha")
        private String fecha;

        @JsonProperty("Motivo")
        private String motivo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class SocioSociedades {

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
    public static class ConsultaRut {
        @JsonProperty("Fecha")
        private String fecha;

        @JsonProperty("Institucion")
        private String institucion;
    }
}
