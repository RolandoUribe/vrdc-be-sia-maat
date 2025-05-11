package cl.veridico.informes.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import cl.veridico.informes.utils.ListaOError;
import cl.veridico.informes.utils.UtilesVarios;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class RespuestaInformeEmpresa {

    public static ConsultaResponseEmpresa generarRespuestaSinResultado(String idConsulta,
            String codigoRetorno) {
        String glosaRetorno = "";

        if (codigoRetorno.equals("100")) {
            glosaRetorno = "Error en la ejecucion del servicio";
        } else if (codigoRetorno.equals("101")) {
            glosaRetorno = "Error en la ejecucion del servicio - parametros incorrectos";
        } else if (codigoRetorno.equals("000")) {
            glosaRetorno = "Ejecucion Exitosa Servicio";
        }

        return ConsultaResponseEmpresa.builder()
                .idConsulta(idConsulta)
                .codigoRetorno(codigoRetorno)
                .glosaRetorno(glosaRetorno)
                .resultado(new ConsultaResponseEmpresa.Resultado()) // No incluye el objeto resultado en
                                                                    // este caso
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .build();
    }

    public static ConsultaResponseEmpresa generarRespuestaOK(String idConsulta,
            String codigoRetorno,
            String json) {

        try {
            String glosaRetorno = "Ejecucion Exitosa Servicio";

            //System.out.println("RespuestaInformeEmpresa->generarRespuestaOK->Respuesta Maat: [" + json + "]");

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            RespuestaMaatInformeEmpresa respuestaMaat = objectMapper.readValue(
                    UtilesVarios.prettyPrintJson(json),
                    RespuestaMaatInformeEmpresa.class);

            int rut = respuestaMaat.getDatosEmpresa().getIdJuridica().getRut();
            String digito = respuestaMaat.getDatosEmpresa().getIdJuridica().getDigito();

            // Mapeo Identificacion Juridica
            // cl.veridico.informes.models.RespuestaMaatInformeEmpresa.DatosEmpresa.IdentificacionJuridica
            // datosIdJuridicaMaat = respuestaMaat
            // .getDatosEmpresa()
            // .getIdJuridica();

            DatosEmpresa.IdentificacionJuridica datosIdJuridicaMaat = respuestaMaat
                    .getDatosEmpresa()
                    .getIdJuridica();

            ConsultaResponseEmpresa.Resultado.IdentificacionJuridica datosIdJuridica = ConsultaResponseEmpresa.Resultado.IdentificacionJuridica
                    .builder()
                    .rut(rut)
                    .digito(digito)
                    .razonSocial(datosIdJuridicaMaat.getRazonSocial())
                    .fechaInicioActividades(
                            UtilesVarios.convertirFechaISO(datosIdJuridicaMaat.getFechaInicioActividades()))
                    .actividadPrincipal(datosIdJuridicaMaat.getActividadPrincipal()!=null ? datosIdJuridicaMaat.getActividadPrincipal().trim() : "")
                    .segmentoEmpresa(datosIdJuridicaMaat.getSegmentoEmpresa())
                    .numeroEmpleados(datosIdJuridicaMaat.getNumeroEmpleados())
                    .regimenTributarioEspecial(datosIdJuridicaMaat.getRegimenTributarioEspecial()!= null
                            ? datosIdJuridicaMaat.getRegimenTributarioEspecial().trim()
                            : "")
                    .formulario29(datosIdJuridicaMaat.getFormulario29())
                    .formulario22(datosIdJuridicaMaat.getFormulario22())
                    .formulario50(datosIdJuridicaMaat.getFormulario50())
                    .regimenPYME(datosIdJuridicaMaat.getRegimenPYME())
                    .monedaExtranjera(datosIdJuridicaMaat.getMonedaExtranjera())
                    .build();

            // Mapeo Trimbajes Autorizados
            List<ConsultaResponseEmpresa.Resultado.TimbrajeAutorizado> datosTimbrajesAutorizados = respuestaMaat
                    .getDatosEmpresa()
                    .getTimbrajesAutorizados().getData()
                    .stream()
                    .map(timaut -> ConsultaResponseEmpresa.Resultado.TimbrajeAutorizado.builder()
                            .documentoAutorizado(timaut.getDocumentoAutorizado())
                            .fechaAutorizacion(UtilesVarios.convertirFechaISO(timaut.getFechaAutorizacion()))
                            .build())
                    .toList();

            // Mapeo Actividades Económicas
            List<ConsultaResponseEmpresa.Resultado.ActividadEconomica> datosActividadEconomicas = respuestaMaat
                    .getDatosEmpresa()
                    .getActividadesEconomicas().getData()
                    .stream()
                    .map(acteco -> ConsultaResponseEmpresa.Resultado.ActividadEconomica.builder()
                            .codigoActividadEconomica(acteco.getCodigoActividadEconomica())
                            .glosaActividadEconomica(acteco.getGlosaActividadEconomica())
                            .fechaInicio(UtilesVarios.convertirFechaISO(acteco.getFechaInicio()))
                            .fechaTermino(UtilesVarios.convertirFechaISO(acteco.getFechaTermino()))
                            .afectoIva(acteco.getAfectoIva())
                            .categoriaActividadEconomica(acteco.getCategoriaActividadEconomica())
                            .build())
                    .toList();

            // Mapeo Direcciones Registradas
            List<ConsultaResponseEmpresa.Resultado.DireccionRegistrada> datosDireccionesRegistradas = respuestaMaat
                    .getDatosEmpresa()
                    .getDireccionesRegistradas().getData()
                    .stream()
                    .map(direc -> ConsultaResponseEmpresa.Resultado.DireccionRegistrada.builder()
                            .tipoDireccion(direc.getTipoDireccion())
                            .calle(direc.getCalle())
                            .numero(direc.getNumero())
                            .bloque(direc.getBloque())
                            .departamento(direc.getDepartamento())
                            .villaPoblacion(direc.getVillaPoblacion())
                            .ciudad(direc.getCiudad())
                            .comuna(direc.getComuna())
                            .region(direc.getRegion())
                            .build())
                    .toList();

            // Mapeo Sociedades Empresariales
            List<ConsultaResponseEmpresa.Resultado.SociedadEmpresarial> datosSociedadEmpresariales = null;
            if (respuestaMaat.getDatosEmpresa().getSociedadesEmpresariales() != null) {
                datosSociedadEmpresariales = new ArrayList<>();
                datosSociedadEmpresariales = respuestaMaat
                        .getDatosEmpresa()
                        .getSociedadesEmpresariales().getData()
                        .stream()
                        .map(socemp -> ConsultaResponseEmpresa.Resultado.SociedadEmpresarial.builder()
                                .rutSocio(UtilesVarios.validarRetornarRut(socemp.getRutSocio()).getRut())
                                .digito(UtilesVarios.validarRetornarRut(socemp.getRutSocio()).getDigitoVerificador())
                                .razonSocial(socemp.getRazonSocial() != null ? socemp.getRazonSocial().trim() : "")
                                .fechaRelacion(UtilesVarios.convertirFechaISO(socemp.getFechaRelacion()))
                                .build())
                        .toList();
            }

            // Mapeo Socios Sociedades Personas
            DatosEmpresa datos = respuestaMaat.getDatosEmpresa();
            ListaOError<DatosEmpresa.SociedadPersona> listaOriginal = datos != null ? datos.getSociedadesPersonas()
                    : null;

            List<ConsultaResponseEmpresa.Resultado.SociedadPersona> datosSociedadPersona = new ArrayList<>();

            if (listaOriginal != null && listaOriginal.getData() != null) {
                for (DatosEmpresa.SociedadPersona sociedad : listaOriginal.getData()) {

                    // Convertir los socios internos
                    ListaOError<DatosEmpresa.SociedadPersona.Socio> listaSocios = sociedad.getSocios();
                    List<RespuestaInformeEmpresa.ConsultaResponseEmpresa.Resultado.SociedadPersona.Socio> socios = new ArrayList<>();

                    if (listaSocios != null && listaSocios.getData() != null) {
                        socios = listaSocios.getData().stream()
                                .map(socio -> ConsultaResponseEmpresa.Resultado.SociedadPersona.Socio.builder()
                                        .rutSocio(UtilesVarios.validarRetornarRut(socio.getRutSocio()).getRut())
                                        .digitoSocio(UtilesVarios.validarRetornarRut(socio.getRutSocio())
                                                .getDigitoVerificador())
                                        .nombreSocio(socio.getNombreSocio())
                                        .tipo(socio.getTipo())
                                        .aporte(socio.getAporte())
                                        .build())
                                .collect(Collectors.toList());
                    }

                    ConsultaResponseEmpresa.Resultado.SociedadPersona nuevaSociedad = ConsultaResponseEmpresa.Resultado.SociedadPersona
                            .builder()
                            .fecha(UtilesVarios.convertirFechaISO(sociedad.getFecha()))
                            .contenido(sociedad.getContenido())
                            .nombre(sociedad.getNombre())
                            .rut(UtilesVarios.validarRetornarRut(sociedad.getRut()).getRut())
                            .digito(UtilesVarios.validarRetornarRut(sociedad.getRut()).getDigitoVerificador())
                            .edicion(sociedad.getEdicion())
                            .cuerpo(sociedad.getCuerpo())
                            .pagina(sociedad.getPagina())
                            .extracto(sociedad.getExtracto())
                            .socios(socios)
                            .build();

                    datosSociedadPersona.add(nuevaSociedad);
                }
            }

            // Mapeo Resumen Total Morosidades Personas
            ConsultaResponseEmpresa.Resultado.ResumenTotalMorosidad datosResumenTotalMorosidad = ConsultaResponseEmpresa.Resultado.ResumenTotalMorosidad
                    .builder()
                    .montoTotalProtestos(UtilesVarios.convertirMonto(
                            respuestaMaat.getDatosEmpresa().getResumenTotalesMorosidades().getMontoTotalProtestos()))
                    .montoTotalInfocom(UtilesVarios.convertirMonto(
                            respuestaMaat.getDatosEmpresa().getResumenTotalesMorosidades().getMontoTotalInfocom()))
                    .montoTotalDeudasPrevisionales(UtilesVarios.convertirMonto(respuestaMaat.getDatosEmpresa()
                            .getResumenTotalesMorosidades().getMontoTotalDeudasPrevisionales()))
                    .montoTotalDeudasLaborales(UtilesVarios.convertirMonto(respuestaMaat.getDatosEmpresa()
                            .getResumenTotalesMorosidades().getMontoTotalDeudasLaborales()))
                    .montoTotalDeudasComercio(UtilesVarios.convertirMonto(respuestaMaat.getDatosEmpresa()
                            .getResumenTotalesMorosidades().getMontoTotalDeudasComercio()))
                    .build();

            // Mapeo de deudas previsionales
            List<ConsultaResponseEmpresa.Resultado.DeudaPrevisional> datosInfraccionesPrevisionales = respuestaMaat
                    .getDatosEmpresa()
                    .getDeudasPrevisionales().getData()
                    .stream()
                    .map(deuda -> ConsultaResponseEmpresa.Resultado.DeudaPrevisional.builder()
                            .acreedor(deuda.getAcreedor().trim())
                            .montoPesos(UtilesVarios.convertirMonto(deuda.getMontoPesos()))
                            .montoUTM(deuda.getMontoUTM())
                            .trabajadoresAfectados(deuda.getTrabajadoresAfectados().stream()
                                    .map(trab -> ConsultaResponseEmpresa.Resultado.DeudaPrevisional.TrabajadorAfectado
                                            .builder()
                                            .rutTrabajador(trab.getRutTrabajador())
                                            .nombreTrabajador(trab.getNombreTrabajador())
                                            .periodoDeuda(UtilesVarios.convertirFechaISO(trab.getPeriodoDeuda()))
                                            .montoDeudaPesos(UtilesVarios.convertirMonto(trab.getMontoDeudaPesos()))
                                            .montoDeudaUTM(trab.getMontoDeudaUTM())
                                            .numeroBoletin(trab.getNumeroBoletin())
                                            .motivo(trab.getMotivo())
                                            .build())
                                    .collect(Collectors.toList()))
                            .build())
                    .collect(Collectors.toList());

            // Mapeo de infracciones laborales
            List<ConsultaResponseEmpresa.Resultado.DeudaLaboral> datosInfraccionesLaborales = respuestaMaat
                    .getDatosEmpresa()
                    .getDeudasLaborales().getData()
                    .stream()
                    .map(deuda -> ConsultaResponseEmpresa.Resultado.DeudaLaboral.builder()
                            .fechaBoletin(UtilesVarios
                                    .convertirFechaISO(deuda.getFechaBoletin()))
                            .numeroBoletin(deuda.getNumeroBoletin())
                            .paginaBoletin(deuda.getPaginaBoletin())
                            .montoDeuda(UtilesVarios.convertirMonto(deuda.getMontoDeuda()))
                            .acreedor(deuda.getAcreedor().trim())
                            .motivo(deuda.getMotivo())
                            .build())
                    .toList();

            // Mapeo de protestos
            List<ConsultaResponseEmpresa.Resultado.Protesto> datosProtestos = respuestaMaat.getDatosEmpresa()
                    .getProtestos().getData()
                    .stream()
                    .map(prot -> ConsultaResponseEmpresa.Resultado.Protesto.builder()
                            .fecha(UtilesVarios.convertirFechaISO(prot.getFecha()))
                            .glosa(prot.getGlosa())
                            .tipoDocumento(prot.getGlosaDocumento())
                            .montoProtesto(UtilesVarios.convertirMonto(prot.getMontoProtesto()))
                            .numeroOperacion(prot.getNumeroOperacion())
                            .glosaDocumento(prot.getGlosaDocumento())
                            .build())
                    .toList();

            // Mapeo Morosidades Infocom
            List<ConsultaResponseEmpresa.Resultado.MorosidadInfocom> datosMorosidadesInfocom = respuestaMaat
                    .getDatosEmpresa()
                    .getMorosidadesInfocom().getData()
                    .stream()
                    .map(mor -> ConsultaResponseEmpresa.Resultado.MorosidadInfocom.builder()
                            .fecha(UtilesVarios.convertirFechaISO(mor.getFecha()))
                            .tipo(mor.getTipo())
                            .monto(UtilesVarios.convertirMonto(mor.getMonto()))
                            .emisor(mor.getEmisor())
                            .build())
                    .toList();

            // Mapeo Morosidades Vigente
            List<ConsultaResponseEmpresa.Resultado.MorosidadVigente> datosMorosidadesVigentes = respuestaMaat
                    .getDatosEmpresa()
                    .getMorosidadesVigentes().getData()
                    .stream()
                    .map(morvig -> ConsultaResponseEmpresa.Resultado.MorosidadVigente.builder()
                            .fechaVencimiento(UtilesVarios.convertirFechaISO(morvig.getFechaVencimiento()))
                            .tipoCredito(morvig.getTipoCredito())
                            .monto(UtilesVarios.convertirMonto(morvig.getMonto()))
                            .acreedor(morvig.getAcreedor())
                            .build())
                    .toList();

            // Mapeo Boletin Maat
            List<ConsultaResponseEmpresa.Resultado.BoletinMaat> datosBoletinesMaat = respuestaMaat.getDatosEmpresa()
                    .getBoletinesMaat().getData()
                    .stream()
                    .map(bolmaat -> ConsultaResponseEmpresa.Resultado.BoletinMaat.builder()
                            .fechaVencimiento(UtilesVarios.convertirFechaISO(bolmaat.getFechaVencimiento()))
                            .tipoCredito(bolmaat.getTipoCredito()) // ojo
                            .monto(UtilesVarios.convertirMonto(bolmaat.getMonto()))
                            .acreedor(bolmaat.getAcreedor())
                            .fechaPublicacion(UtilesVarios.convertirFechaISO(bolmaat.getFechaPublicacion()))
                            .mercado(bolmaat.getMercado())
                            .build())
                    .toList();

            // Mapeo Anotaciones Corredores
            List<ConsultaResponseEmpresa.Resultado.AnotacionCorredor> datosAnotacionesCorredores = respuestaMaat
                    .getDatosEmpresa()
                    .getAnotacionesCorredores().getData()
                    .stream()
                    .map(anotcorr -> ConsultaResponseEmpresa.Resultado.AnotacionCorredor.builder()
                            .corredor(anotcorr.getCorredor())
                            .direccion(anotcorr.getDireccion())
                            .anotacion(anotcorr.getAnotacion())
                            .tipo(anotcorr.getTipo())
                            .fecha(UtilesVarios.convertirFechaISO(anotcorr.getFecha()))
                            .build())
                    .toList();

            // Mapeo Resumen Consolidado
            ConsultaResponseEmpresa.Resultado.ResumenConsolidado datosResumenConsolidado = ConsultaResponseEmpresa.Resultado.ResumenConsolidado
                    .builder()
                    .eventosQuiebras(respuestaMaat.getDatosEmpresa().getResumenConsolidado().getEventosQuiebras())
                    .bancosRelacionados(
                            respuestaMaat.getDatosEmpresa().getResumenConsolidado().getBancosRelacionados() != null
                                    ? respuestaMaat.getDatosEmpresa().getResumenConsolidado().getBancosRelacionados()
                                            .trim()
                                    : "")
                    .ordenNoPago(respuestaMaat.getDatosEmpresa().getResumenConsolidado().getOrdenNoPago())
                    .vehiculos(respuestaMaat.getDatosEmpresa().getResumenConsolidado().getVehiculos())
                    .bienesRaices(respuestaMaat.getDatosEmpresa().getResumenConsolidado().getBienesRaices())
                    .exportaciones(respuestaMaat.getDatosEmpresa().getResumenConsolidado().getExportaciones())
                    .importaciones(respuestaMaat.getDatosEmpresa().getResumenConsolidado().getImportaciones())
                    .infraccionesLaborales(
                            respuestaMaat.getDatosEmpresa().getResumenConsolidado().getInfraccionesLaborales())
                    .infraccionesPrevisionales(
                            respuestaMaat.getDatosEmpresa().getResumenConsolidado().getInfraccionesPrevisionales())
                    .prendas(respuestaMaat.getDatosEmpresa().getResumenConsolidado().getPrendas())
                    .anotacionesTributarias(
                            respuestaMaat.getDatosEmpresa().getResumenConsolidado().getAnotacionesTributarias())
                    .sociedades(respuestaMaat.getDatosEmpresa().getResumenConsolidado().getSocieades())
                    .socios(respuestaMaat.getDatosEmpresa().getResumenConsolidado().getSocios())
                    .build();

            // Mapeo Quiebras
            List<ConsultaResponseEmpresa.Resultado.Quiebra> datosQuiebras = respuestaMaat.getDatosEmpresa()
                    .getQuiebras().getData()
                    .stream()
                    .map(qui -> ConsultaResponseEmpresa.Resultado.Quiebra.builder()
                            .rol(qui.getRol())
                            .procedimiento(qui.getProcedimiento())
                            .deudor(qui.getDeudor())
                            .veedor(qui.getVeedor())
                            .publicacion(qui.getPublicacion())
                            .tribunal(qui.getTribunal())
                            .fecha(UtilesVarios.convertirFechaISO(qui.getFecha()))
                            .build())
                    .toList();

            // Mapeo Vehiculos
            List<ConsultaResponseEmpresa.Resultado.Vehiculo> datosVehiculos = respuestaMaat.getDatosEmpresa()
                    .getVehiculos().getData()
                    .stream()
                    .map(veh -> ConsultaResponseEmpresa.Resultado.Vehiculo.builder()
                            .tipo(veh.getTipo())
                            .marca(veh.getMarca())
                            .patente(veh.getPatente())
                            .modelo(veh.getModelo())
                            .agno(veh.getAgno())
                            .avaluo(UtilesVarios.convertirMonto(veh.getAvaluo()))
                            .build())
                    .toList();

            // Mapeo de Bienes Raices
            List<ConsultaResponseEmpresa.Resultado.BienRaiz> datosBienesRaices = respuestaMaat.getDatosEmpresa()
                    .getBienesRaices().getData()
                    .stream()
                    .map(braiz -> ConsultaResponseEmpresa.Resultado.BienRaiz.builder()
                            .rol(braiz.getRol())
                            .comuna(braiz.getComuna().trim())
                            .direccion(braiz.getDireccion())
                            .tipo(braiz.getTipo())
                            .destino(braiz.getDestino().trim())
                            .avaluo(UtilesVarios.convertirMonto(braiz.getAvaluo()))
                            .build())
                    .toList();

            // Mapeo de Exportaciones
            List<ConsultaResponseEmpresa.Resultado.Exportacion> datosExportaciones = respuestaMaat.getDatosEmpresa()
                    .getExportaciones().getData()
                    .stream()
                    .map(expor -> ConsultaResponseEmpresa.Resultado.Exportacion.builder()
                            .agno(expor.getAgno())
                            .fob(expor.getFob())
                            .cif(expor.getCif())
                            .numero(expor.getNumero())
                            .enero(expor.getEnero())
                            .febrero(expor.getFebrero())
                            .marzo(expor.getMarzo())
                            .abril(expor.getAbril())
                            .mayo(expor.getMayo())
                            .junio(expor.getJunio())
                            .julio(expor.getJulio())
                            .agosto(expor.getAgosto())
                            .septiembre(expor.getSeptiembre())
                            .octubre(expor.getOctubre())
                            .noviembre(expor.getNoviembre())
                            .diciembre(expor.getDiciembre())
                            .build())
                    .toList();

            // Mapeo de Importaciones
            List<ConsultaResponseEmpresa.Resultado.Importacion> datosImportaciones = respuestaMaat.getDatosEmpresa()
                    .getImportaciones().getData()
                    .stream()
                    .map(impor -> ConsultaResponseEmpresa.Resultado.Importacion.builder()
                            .agno(impor.getAgno())
                            .fob(impor.getFob())
                            .cif(impor.getCif())
                            .numero(impor.getNumero())
                            .enero(impor.getEnero())
                            .febrero(impor.getFebrero())
                            .marzo(impor.getMarzo())
                            .abril(impor.getAbril())
                            .mayo(impor.getMayo())
                            .junio(impor.getJunio())
                            .julio(impor.getJulio())
                            .agosto(impor.getAgosto())
                            .septiembre(impor.getSeptiembre())
                            .octubre(impor.getOctubre())
                            .noviembre(impor.getNoviembre())
                            .diciembre(impor.getDiciembre())
                            .build())
                    .toList();

            // Mapeo de Prendas
            List<ConsultaResponseEmpresa.Resultado.Prenda> datosPrendas = respuestaMaat.getDatosEmpresa().getPrendas()
                    .getData()
                    .stream()
                    .map(prenda -> ConsultaResponseEmpresa.Resultado.Prenda.builder()
                            .acreedor(prenda.getAcreedor())
                            .fechaPublicacion(UtilesVarios.convertirFechaISO(prenda.getFechaPublicacion()))
                            .cuerpo(prenda.getCuerpo())
                            .pagina(prenda.getPagina())
                            .extracto(prenda.getExtracto())
                            .build())
                    .toList();

            // Mapeo Consulta al Rut
            List<ConsultaResponseEmpresa.Resultado.ConsultaRut> datosConsultasAlRut = respuestaMaat.getDatosEmpresa()
                    .getConsultasAlRut().getData()
                    .stream()
                    .map(calr -> ConsultaResponseEmpresa.Resultado.ConsultaRut.builder()
                            .fecha(UtilesVarios.convertirFechaISO(calr.getFecha()))
                            .institucion(calr.getInstitucion())
                            .build())
                    .toList();

            // Mapeo de datos Score
            int score = Integer.parseInt(respuestaMaat.getDatosEmpresa().getScoreMaat());
            String[] resultado = UtilesVarios.obtenerRiesgoYGlosaPorScore(score);

            String glosaScore = resultado[0];
            String riesgoCrediticio = resultado[1];

            ConsultaResponseEmpresa.Resultado.Score datosScore = ConsultaResponseEmpresa.Resultado.Score.builder()
                    .puntaje(score)
                    .riesgoCrediticio(riesgoCrediticio)
                    .glosa(glosaScore)
                    .build();

            return ConsultaResponseEmpresa.builder()
                    .idConsulta(idConsulta)
                    .codigoRetorno(codigoRetorno)
                    .glosaRetorno(glosaRetorno)
                    .resultado(ConsultaResponseEmpresa.Resultado.builder()
                            .idJuridica(datosIdJuridica)
                            .datosScore(datosScore)
                            .timbrajesAutorizados(datosTimbrajesAutorizados)
                            .actividadesEconomicas(datosActividadEconomicas)
                            .direccionesRegistradas(datosDireccionesRegistradas)
                            .sociedadesEmpresariales(datosSociedadEmpresariales)
                            .sociedadesPersonas(datosSociedadPersona)
                            .resumenTotalesMorosidades(datosResumenTotalMorosidad)
                            .deudasPrevisionales(datosInfraccionesPrevisionales)
                            .deudasLaborales(datosInfraccionesLaborales)
                            .protestos(datosProtestos)
                            .morosidadesInfocom(datosMorosidadesInfocom)
                            .morosidadesVigentes(datosMorosidadesVigentes)
                            .boletinesMaat(datosBoletinesMaat)
                            .anotacionesCorredores(datosAnotacionesCorredores)
                            .resumenConsolidado(datosResumenConsolidado)
                            .quiebras(datosQuiebras)
                            .vehiculos(datosVehiculos)
                            .bienesRaices(datosBienesRaices)
                            .exportaciones(datosExportaciones)
                            .importaciones(datosImportaciones)
                            .prendas(datosPrendas)
                            .extracto(respuestaMaat.getDatosEmpresa().getExtracto())
                            .consultasRut(datosConsultasAlRut)
                            .build())
                    .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return ConsultaResponseEmpresa.builder()
                    .idConsulta(idConsulta)
                    .codigoRetorno("100")
                    .glosaRetorno("Error en la ejecución del servicio")
                    .resultado(new ConsultaResponseEmpresa.Resultado()) // No incluye el objeto
                                                                        // resultado en este caso
                    .build();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_EMPTY) // Permite que resultado aparezca como {}
    public static class ConsultaResponseEmpresa {
        @JsonProperty("idConsulta")
        private String idConsulta;

        @JsonProperty("codigoRetorno")
        private String codigoRetorno;

        @JsonProperty("glosaRetorno")
        private String glosaRetorno;

        @JsonProperty("resultado")
        private Resultado resultado;

        @JsonProperty("timestamp")
        private String timestamp;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class Resultado {
            @JsonProperty("identificacionJuridica")
            private IdentificacionJuridica idJuridica;

            @JsonProperty("score")
            private Score datosScore;

            @JsonProperty("timbrajesAutorizados")
            private List<TimbrajeAutorizado> timbrajesAutorizados;

            @JsonProperty("actividadesEconomicas")
            private List<ActividadEconomica> actividadesEconomicas;

            @JsonProperty("direccionesRegistradas")
            private List<DireccionRegistrada> direccionesRegistradas;

            @JsonProperty("sociedadesEmpresariales")
            private List<SociedadEmpresarial> sociedadesEmpresariales;

            @JsonProperty("sociedadesPersonas")
            private List<SociedadPersona> sociedadesPersonas;

            @JsonProperty("resumenTotalesMorosidades")
            private ResumenTotalMorosidad resumenTotalesMorosidades;

            @JsonProperty("deudasPrevisionales")
            private List<DeudaPrevisional> deudasPrevisionales;

            @JsonProperty("deudasLaborales")
            private List<DeudaLaboral> deudasLaborales;

            @JsonProperty("protestos")
            private List<Protesto> protestos;

            @JsonProperty("morosidadesInfocom")
            private List<MorosidadInfocom> morosidadesInfocom;

            @JsonProperty("morosidadesVigentes")
            private List<MorosidadVigente> morosidadesVigentes;

            @JsonProperty("boletinesMaat")
            private List<BoletinMaat> boletinesMaat;

            @JsonProperty("anotacionesCorredores")
            private List<AnotacionCorredor> anotacionesCorredores;

            @JsonProperty("resumenConsolidado")
            private ResumenConsolidado resumenConsolidado;

            @JsonProperty("quiebras")
            private List<Quiebra> quiebras;

            @JsonProperty("vehiculos")
            private List<Vehiculo> vehiculos;

            @JsonProperty("bienesRaices")
            private List<BienRaiz> bienesRaices;

            @JsonProperty("exportaciones")
            private List<Exportacion> exportaciones;

            @JsonProperty("importaciones")
            private List<Importacion> importaciones;

            @JsonProperty("prendas")
            private List<Prenda> prendas;

            @JsonProperty("extracto")
            private String extracto;

            @JsonProperty("consultasRut")
            private List<ConsultaRut> consultasRut;

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class IdentificacionJuridica {
                @JsonProperty("rut")
                private int rut;

                @JsonProperty("digito")
                private String digito;

                @JsonProperty("razonSocial")
                private String razonSocial;

                @JsonProperty("fechaInicioActividades")
                private String fechaInicioActividades;

                @JsonProperty("actividadPrincipal")
                private String actividadPrincipal;

                @JsonProperty("segmentoEmpresa")
                private String segmentoEmpresa;

                @JsonProperty("numeroEmpleados")
                private String numeroEmpleados;

                @JsonProperty("regimenTributarioEspecial")
                private String regimenTributarioEspecial;

                @JsonProperty("formulario29")
                private String formulario29;

                @JsonProperty("formulario22")
                private String formulario22;

                @JsonProperty("formulario50")
                private String formulario50;

                @JsonProperty("regimenPYME")
                private String regimenPYME;

                @JsonProperty("monedaExtranjera")
                private String monedaExtranjera;
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
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class TimbrajeAutorizado {
                @JsonProperty("documentoAutorizado")
                private String documentoAutorizado;

                @JsonProperty("fechaAutorizacion")
                private String fechaAutorizacion;
            }

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class ActividadEconomica {
                @JsonProperty("codigoActividadEconomica")
                private String codigoActividadEconomica;

                @JsonProperty("glosaActividadEconomica")
                private String glosaActividadEconomica;

                @JsonProperty("fechaInicio")
                private String fechaInicio;

                @JsonProperty("fechaTermino")
                private String fechaTermino;

                @JsonProperty("afectoIva")
                private String afectoIva;

                @JsonProperty("categoriaActividadEconomica")
                private String categoriaActividadEconomica;
            }

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class DireccionRegistrada {
                @JsonProperty("tipoDireccion")
                private String tipoDireccion;

                @JsonProperty("calle")
                private String calle;

                @JsonProperty("numero")
                private String numero;

                @JsonProperty("bloque")
                private String bloque;

                @JsonProperty("departamento")
                private String departamento;

                @JsonProperty("villaPoblacion")
                private String villaPoblacion;

                @JsonProperty("ciudad")
                private String ciudad;

                @JsonProperty("comuna")
                private String comuna;

                @JsonProperty("region")
                private String region;
            }

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class SociedadEmpresarial {
                @JsonProperty("rutSocio")
                private int rutSocio;

                @JsonProperty("digito")
                private String digito;

                @JsonProperty("razonSocial")
                private String razonSocial;

                @JsonProperty("fechaRelacion")
                private String fechaRelacion;
            }

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder
            @JsonIgnoreProperties(ignoreUnknown = true)
            static class SociedadPersona {

                @JsonProperty("fecha")
                private String fecha;

                @JsonProperty("contenido")
                private String contenido;

                @JsonProperty("nombre")
                private String nombre;

                @JsonProperty("rut")
                private int rut;

                @JsonProperty("digito")
                private String digito;

                @JsonProperty("edicion")
                private String edicion;

                @JsonProperty("cuerpo")
                private String cuerpo;

                @JsonProperty("pagina")
                private String pagina;

                @JsonProperty("extracto")
                private String extracto;

                @JsonProperty("socios")
                private List<Socio> socios;

                @Data
                @NoArgsConstructor
                @AllArgsConstructor
                @Builder
                @JsonIgnoreProperties(ignoreUnknown = true)
                public static class Socio {
                    @JsonProperty("rutSocio")
                    private int rutSocio;

                    @JsonProperty("digitoSocio")
                    private String digitoSocio;

                    @JsonProperty("nombreSocio")
                    private String nombreSocio;

                    @JsonProperty("tipo")
                    private String tipo;

                    @JsonProperty("aporte")
                    private int aporte;
                }

            }

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class ResumenTotalMorosidad {
                @JsonProperty("montoTotalProtestos")
                private BigDecimal montoTotalProtestos;

                @JsonProperty("montoTotalInfocom")
                private BigDecimal montoTotalInfocom;

                @JsonProperty("montoTotalDeudasPrevisionales")
                private BigDecimal montoTotalDeudasPrevisionales;

                @JsonProperty("montoTotalDeudasLaborales")
                private BigDecimal montoTotalDeudasLaborales;

                @JsonProperty("montoTotalDeudasComercio")
                private BigDecimal montoTotalDeudasComercio;
            }

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class DeudaPrevisional {
                @JsonProperty("acreedor")
                private String acreedor;

                @JsonProperty("montoPesos")
                private BigDecimal montoPesos;

                @JsonProperty("montoUTM")
                private int montoUTM;

                @JsonProperty("trabajadoresAfectados")
                private List<TrabajadorAfectado> trabajadoresAfectados;

                @Data
                @NoArgsConstructor
                @AllArgsConstructor
                @Builder
                @JsonIgnoreProperties(ignoreUnknown = true)
                public static class TrabajadorAfectado {
                    @JsonProperty("rutTrabajador")
                    private String rutTrabajador;

                    @JsonProperty("nombreTrabajador")
                    private String nombreTrabajador;

                    @JsonProperty("periodoDeuda")
                    private String periodoDeuda;

                    @JsonProperty("montoDeudaPesos")
                    private BigDecimal montoDeudaPesos;

                    @JsonProperty("montoDeudaUTM")
                    private String montoDeudaUTM;

                    @JsonProperty("numeroBoletin")
                    private String numeroBoletin;

                    @JsonProperty("motivo")
                    private String motivo;
                }
            }

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class DeudaLaboral {
                @JsonProperty("fechaBoletin")
                private String fechaBoletin;

                @JsonProperty("numeroBoletin")
                private String numeroBoletin;

                @JsonProperty("paginaBoletin")
                private String paginaBoletin;

                @JsonProperty("montoDeuda")
                private BigDecimal montoDeuda;

                @JsonProperty("acreedor")
                private String acreedor;

                @JsonProperty("motivo")
                private String motivo;
            }

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder
            @JsonIgnoreProperties(ignoreUnknown = true)
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
            }

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class MorosidadInfocom {
                @JsonProperty("fecha")
                private String fecha;

                @JsonProperty("tipo")
                private String tipo;

                @JsonProperty("monto")
                private BigDecimal monto;

                @JsonProperty("emisor")
                private String emisor;
            }

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class MorosidadVigente {
                @JsonProperty("fechaVencimiento")
                private String fechaVencimiento;

                @JsonProperty("tipoCredito")
                private String tipoCredito;

                @JsonProperty("monto")
                private BigDecimal monto;

                @JsonProperty("acreedor")
                private String acreedor;
            }

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class BoletinMaat {
                @JsonProperty("fechaVencimiento")
                private String fechaVencimiento;

                @JsonProperty("tipoCredito")
                private String tipoCredito;

                @JsonProperty("monto")
                private BigDecimal monto;

                @JsonProperty("acreedor")
                private String acreedor;

                @JsonProperty("fechaPublicacion")
                private String fechaPublicacion;

                @JsonProperty("mercado")
                private String mercado;
            }

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class AnotacionCorredor {
                @JsonProperty("corredor")
                private String corredor;

                @JsonProperty("direccion")
                private String direccion;

                @JsonProperty("anotacion")
                private String anotacion;

                @JsonProperty("tipo")
                private String tipo;

                @JsonProperty("fecha")
                private String fecha;
            }

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class ResumenConsolidado {
                @JsonProperty("eventosQuiebras")
                private String eventosQuiebras;

                @JsonProperty("bancosRelacionados")
                private String bancosRelacionados;

                @JsonProperty("ordenNoPago")
                private int ordenNoPago;

                @JsonProperty("vehiculos")
                private int vehiculos;

                @JsonProperty("bienesRaices")
                private int bienesRaices;

                @JsonProperty("exportaciones")
                private int exportaciones;

                @JsonProperty("importaciones")
                private int importaciones;

                @JsonProperty("infraccionesLaborales")
                private int infraccionesLaborales;

                @JsonProperty("infraccionesPrevisionales")
                private int infraccionesPrevisionales;

                @JsonProperty("prendas")
                private int prendas;

                @JsonProperty("anotacionesTributarias")
                private int anotacionesTributarias;

                @JsonProperty("sociedades")
                private int sociedades;

                @JsonProperty("socios")
                private int socios;
            }

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Quiebra {
                @JsonProperty("rol")
                private String rol;

                @JsonProperty("procedimiento")
                private String procedimiento;

                @JsonProperty("deudor")
                private String deudor;

                @JsonProperty("veedor")
                private String veedor;

                @JsonProperty("publicacion")
                private String publicacion;

                @JsonProperty("tribunal")
                private String tribunal;

                @JsonProperty("fecha")
                private String fecha;
            }

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Vehiculo {
                @JsonProperty("tipo")
                private String tipo;

                @JsonProperty("marca")
                private String marca;

                @JsonProperty("patente")
                private String patente;

                @JsonProperty("modelo")
                private String modelo;

                @JsonProperty("año")
                private String agno;

                @JsonProperty("avaluo")
                private BigDecimal avaluo;
            }

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class BienRaiz {
                @JsonProperty("rol")
                private String rol;

                @JsonProperty("comuna")
                private String comuna;

                @JsonProperty("direccion")
                private String direccion;

                @JsonProperty("tipo")
                private String tipo;

                @JsonProperty("destino")
                private String destino;

                @JsonProperty("avaluo")
                private BigDecimal avaluo;
            }

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Exportacion {
                @JsonProperty("Año")
                private String agno;

                @JsonProperty("fob")
                private String fob;

                @JsonProperty("cif")
                private String cif;

                @JsonProperty("numero")
                private String numero;

                @JsonProperty("enero")
                private String enero;

                @JsonProperty("febrero")
                private String febrero;

                @JsonProperty("marzo")
                private String marzo;

                @JsonProperty("abril")
                private String abril;

                @JsonProperty("mayo")
                private String mayo;

                @JsonProperty("junio")
                private String junio;

                @JsonProperty("julio")
                private String julio;

                @JsonProperty("agosto")
                private String agosto;

                @JsonProperty("septiembre")
                private String septiembre;

                @JsonProperty("octubre")
                private String octubre;

                @JsonProperty("noviembre")
                private String noviembre;

                @JsonProperty("diciembre")
                private String diciembre;
            }

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Importacion {
                @JsonProperty("Año")
                private String agno;

                @JsonProperty("fob")
                private String fob;

                @JsonProperty("cif")
                private String cif;

                @JsonProperty("numero")
                private String numero;

                @JsonProperty("enero")
                private String enero;

                @JsonProperty("febrero")
                private String febrero;

                @JsonProperty("marzo")
                private String marzo;

                @JsonProperty("abril")
                private String abril;

                @JsonProperty("mayo")
                private String mayo;

                @JsonProperty("junio")
                private String junio;

                @JsonProperty("julio")
                private String julio;

                @JsonProperty("agosto")
                private String agosto;

                @JsonProperty("septiembre")
                private String septiembre;

                @JsonProperty("octubre")
                private String octubre;

                @JsonProperty("noviembre")
                private String noviembre;

                @JsonProperty("diciembre")
                private String diciembre;
            }

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Prenda {
                @JsonProperty("acreedor")
                private String acreedor;

                @JsonProperty("fechaPublicacion")
                private String fechaPublicacion;

                @JsonProperty("cuerpo")
                private String cuerpo;

                @JsonProperty("pagina")
                private String pagina;

                @JsonProperty("extracto")
                private String extracto;
            }

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class ConsultaRut {
                @JsonProperty("fecha")
                private String fecha;

                @JsonProperty("institucion")
                private String institucion;
            }

        }

    }
}
