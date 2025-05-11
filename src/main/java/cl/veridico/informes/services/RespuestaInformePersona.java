package cl.veridico.informes.services;

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
import cl.veridico.informes.utils.RutInfo;
import cl.veridico.informes.utils.UtilesVarios;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class RespuestaInformePersona {

    public static ConsultaResponsePersona generarRespuestaError(String idConsulta,
            String codigoRetorno) {
        String glosaRetorno = "";

        if (codigoRetorno == "100") {
            glosaRetorno = "Error en la ejecucion del servicio";
        } else if (codigoRetorno == "101") {
            glosaRetorno = "Error en la ejecucion del servicio - parametros incorrectos";
        }

        return ConsultaResponsePersona.builder()
                .idConsulta(idConsulta)
                .codigoRetorno(codigoRetorno)
                .glosaRetorno(glosaRetorno)
                .resultado(new ConsultaResponsePersona.Resultado()) // No incluye el objeto resultado en
                                                                    // este caso
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .build();
    }

    public static ConsultaResponsePersona generarRespuestaOK(String idConsulta,
            String codigoRetorno,
            String json) {

        try {
            String glosaRetorno = "Ejecucion Exitosa Servicio";

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            RespuestaMaatInformeFull respuestaMaat = objectMapper.readValue(json, RespuestaMaatInformeFull.class);

            RutInfo rutInfo = UtilesVarios.validarRetornarRut(respuestaMaat.getDatos().getDatosPersonales().getRut());

            int rut = rutInfo.getRut();
            String digito = rutInfo.getDigitoVerificador();
            String extracto = respuestaMaat.getDatos().getExtracto();

            System.out.println("RespuestaInformePersona->generarRespuestaOK->Rut: [" + rut + "], Digito: [" + digito
                    + "], Extracto: [" + extracto + "]");

            // Mapeo de datos personales
            DatosFull.DatosPersonales datosPersonalesMaat = respuestaMaat.getDatos().getDatosPersonales();

            System.out.println(
                    "RespuestaInformePersona->generarRespuestaOK->DatosPersonales: [" + datosPersonalesMaat + "]");

            ConsultaResponsePersona.Resultado.DatosPersonales datosPersonales = null;
            if (datosPersonalesMaat == null) {
                return ConsultaResponsePersona.builder()
                        .idConsulta(idConsulta)
                        .codigoRetorno(codigoRetorno)
                        .glosaRetorno(glosaRetorno)
                        .resultado(new ConsultaResponsePersona.Resultado()) // No incluye el objeto
                                                                            // resultado en
                                                                            // este caso
                        .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                        .build();
            } else {
                datosPersonales = ConsultaResponsePersona.Resultado.DatosPersonales
                        .builder()
                        .rut(rut)
                        .digito(digito)
                        .nombre(datosPersonalesMaat.getNombre())
                        .tipoPersona(datosPersonalesMaat.getTipoPersona().trim())
                        .persona(ConsultaResponsePersona.Resultado.DatosPersonales.Persona.builder()
                                .edad(datosPersonalesMaat.getPersona().getEdad())
                                .sexo(datosPersonalesMaat.getPersona().getSexo() != null
                                        ? datosPersonalesMaat.getPersona().getSexo()
                                        : "")
                                .estadoCivil(datosPersonalesMaat.getPersona().getEstadoCivil() != null
                                        ? datosPersonalesMaat.getPersona().getEstadoCivil()
                                        : "")
                                .nacionalidad(datosPersonalesMaat.getPersona().getNacionalidad() != null
                                        ? datosPersonalesMaat.getPersona().getNacionalidad()
                                        : "")
                                .build())
                        .empresa(ConsultaResponsePersona.Resultado.DatosPersonales.Empresa.builder()
                                .giros(datosPersonalesMaat.getEmpresa().getGiro().getData().stream()
                                        .map(g -> ConsultaResponsePersona.Resultado.DatosPersonales.Empresa.Giro
                                                .builder()
                                                .giro(g.getGiros().trim())
                                                .build())
                                        .collect(Collectors.toList())) // Convertir a
                                                                       // lista
                                /*
                                 * .productos(ConsultaResponsePersona.Resultado.DatosPersonales.Empresa.
                                 * Productos
                                 * .builder()
                                 * .producto(datosPersonalesMaat.getEmpresa()
                                 * .getProductos().getProducto())
                                 * .build())
                                 * .build())
                                 */
                                .productos(ConsultaResponsePersona.Resultado.DatosPersonales.Empresa.Productos
                                        .builder()
                                        .producto(datosPersonalesMaat.getEmpresa().getProductos().getData()
                                                .stream()
                                                .map(p -> p.getProducto())
                                                .collect(Collectors.joining(", "))) // Combine products into a single
                                                                                    // string
                                        .build())
                                .build())
                        .ultimaDireccionInformada(datosPersonalesMaat.getUltimaDireccionInformada())
                        .cantidadDirecciones(datosPersonalesMaat.getCantidadDirecciones())
                        .build();
            }

            // Mapeo Resumen Montos
            /*
             * DatosFull.ResumenMontos resumenOriginal = ListaOError
             * .obtenerPrimero(respuestaMaat.getDatos().getResumenMontos());
             * 
             * ConsultaResponsePersona.Resultado.ResumenMontos resumenMontos =
             * resumenOriginal != null
             * ? ConsultaResponsePersona.Resultado.ResumenMontos.builder()
             * .montoTotalProtestos(UtilesVarios.convertirMonto(resumenOriginal.
             * getMontoTotalProtestos()))
             * .montoTotalInfocom(UtilesVarios.convertirMonto(resumenOriginal.
             * getMontoTotalInfocom()))
             * .montoTotalDeudasPrevisionales(
             * UtilesVarios.convertirMonto(resumenOriginal.getMontoTotalDeudasPrevisionales(
             * )))
             * .montoTotalDeudasLaborales(
             * UtilesVarios.convertirMonto(resumenOriginal.getMontoTotalDeudasLaborales()))
             * .montoTotalDeudasComercio(
             * UtilesVarios.convertirMonto(resumenOriginal.getMontoTotalDeudasComercio()))
             * .build()
             * : null;
             */

            ConsultaResponsePersona.Resultado.ResumenMontos resumenMontos = ConsultaResponsePersona.Resultado.ResumenMontos
                    .builder()
                    .montoTotalProtestos(UtilesVarios.convertirMonto(respuestaMaat.getDatos()
                            .getResumenMontos().getMontoTotalProtestos()))
                    .montoTotalInfocom(UtilesVarios.convertirMonto(respuestaMaat.getDatos()
                            .getResumenMontos().getMontoTotalInfocom()))
                    .montoTotalDeudasPrevisionales(UtilesVarios
                            .convertirMonto(respuestaMaat.getDatos().getResumenMontos()
                                    .getMontoTotalDeudasPrevisionales()))
                    .montoTotalDeudasLaborales(UtilesVarios.convertirMonto(respuestaMaat.getDatos()
                            .getResumenMontos().getMontoTotalDeudasLaborales()))
                    .montoTotalDeudasComercio(UtilesVarios.convertirMonto(respuestaMaat.getDatos()
                            .getResumenMontos().getMontoTotalDeudasComercio()))
                    .build();

            // Mapeo de Direcciones
            List<ConsultaResponsePersona.Resultado.Direccion> direcciones = respuestaMaat.getDatos()
                    .getDirecciones().getData()
                    .stream()
                    .map(direc -> ConsultaResponsePersona.Resultado.Direccion.builder()
                            .tipo(direc.getTipo())
                            .direccion(direc.getDireccion())
                            .fechaInformacion(UtilesVarios
                                    .convertirFechaISO(direc.getFechaInformacion()))
                            .build())
                    .toList();

            // Mapeo de Morosidades
            List<ConsultaResponsePersona.Resultado.Morosidad> morosidades = respuestaMaat.getDatos()
                    .getMorosidades().getData()
                    .stream()
                    .map(mor -> ConsultaResponsePersona.Resultado.Morosidad.builder()
                            .fechaVencimiento(UtilesVarios
                                    .convertirFechaISO(mor.getFechaVencimiento()))
                            .tipoCredito(mor.getTipoCredito().trim())
                            .monto(UtilesVarios.convertirMonto(mor.getMonto()))
                            .acreedor(mor.getAcreedor())
                            .build())
                    .toList();

            // Mapeo de deudas previsionales
            List<ConsultaResponsePersona.Resultado.DeudaPrevisional> deudasPrevisionales = respuestaMaat
                    .getDatos()
                    .getDeudasPrevisionales().getData()
                    .stream()
                    .map(deuda -> ConsultaResponsePersona.Resultado.DeudaPrevisional.builder()
                            .acreedor(deuda.getAcreedor().trim())
                            .montoPesos(UtilesVarios.convertirMonto(deuda.getMontoPesos()))
                            .montoUTM(deuda.getMontoUTM())
                            .trabajadoresAfectados(deuda.getTrabajadoresAfectados().stream()
                                    .map(trab -> ConsultaResponsePersona.Resultado.DeudaPrevisional.TrabajadorAfectado
                                            .builder()
                                            .rutTrabajador(
                                                    UtilesVarios.validarRetornarRut(
                                                            trab.getRutTrabajador())
                                                            .getRut())
                                            .digitoTrabajador(UtilesVarios
                                                    .validarRetornarRut(
                                                            trab.getRutTrabajador())
                                                    .getDigitoVerificador())
                                            .nombreTrabajador(trab
                                                    .getNombreTrabajador())
                                            .periodoDeuda(UtilesVarios
                                                    .convertirFechaISO(
                                                            trab.getPeriodoDeuda()))
                                            .montoDeudaPesos(UtilesVarios
                                                    .convertirMonto(trab
                                                            .getMontoDeudaPesos()))
                                            .montoDeudaUTM(UtilesVarios
                                                    .convertirMonto(trab
                                                            .getMontoDeudaUTM()))
                                            .numeroBoletin(trab
                                                    .getNumeroBoletin())
                                            .motivo(trab.getMotivo())
                                            .build())
                                    .collect(Collectors.toList()))
                            .build())
                    .collect(Collectors.toList());

            // Mapeo de deudas laborales
            List<ConsultaResponsePersona.Resultado.DeudaLaboral> deudasLaborales = respuestaMaat.getDatos()
                    .getDeudasLaborales().getData()
                    .stream()
                    .map(deuda -> ConsultaResponsePersona.Resultado.DeudaLaboral.builder()
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
            List<ConsultaResponsePersona.Resultado.Protesto> protestos = respuestaMaat.getDatos()
                    .getProtestos().getData()
                    .stream()
                    .map(prot -> ConsultaResponsePersona.Resultado.Protesto.builder()
                            .fecha(UtilesVarios.convertirFechaISO(prot.getFecha()))
                            .glosa(prot.getGlosa())
                            .tipoDocumento(prot.getTipoDocumento())
                            .montoProtesto(UtilesVarios
                                    .convertirMonto(prot.getMontoProtesto()))
                            .numeroOperacion(prot.getNumeroOperacion())
                            .glosaDocumento(prot.getGlosaDocumento())
                            .build())
                    .toList();

            // Mapeo de Infocom
            List<ConsultaResponsePersona.Resultado.Infocom> infocom = respuestaMaat.getDatos().getInfocom().getData()
                    .stream()
                    .map(info -> ConsultaResponsePersona.Resultado.Infocom.builder()
                            .fecha(UtilesVarios.convertirFechaISO(info.getFecha()))
                            .tipo(info.getTipo())
                            .monto(UtilesVarios.convertirMonto(info.getMonto()))
                            .emisor(info.getEmisor())
                            .build())
                    .toList();

            // Mapeo de deudas de comercio
            List<ConsultaResponsePersona.Resultado.DeudaComercio> deudasComercio = respuestaMaat.getDatos()
                    .getDeudasComercio().getData()
                    .stream()
                    .map(deuda -> ConsultaResponsePersona.Resultado.DeudaComercio.builder()
                            .fechaVencimiento(UtilesVarios
                                    .convertirFechaISO(deuda.getFechaVencimiento()))
                            .tipoDocumento(deuda.getTipoDocumento())
                            .monto(UtilesVarios.convertirMonto(deuda.getMonto()))
                            .emisor(deuda.getEmisor())
                            .fechaPublicacion(UtilesVarios
                                    .convertirFechaISO(deuda.getFechaPublicacion()))
                            .mercado(deuda.getMercado())
                            .build())
                    .toList();

            // Mapeo Resumen Empresarial
            /*
             * DatosFull.ResumenEmpresarial resumenEmpresarialOriginal = ListaOError
             * .obtenerPrimero(respuestaMaat.getDatos().getResumenEmpresarial());
             * ConsultaResponsePersona.Resultado.ResumenEmpresarial resumenEmpresarial =
             * resumenEmpresarialOriginal != null
             * ? ConsultaResponsePersona.Resultado.ResumenEmpresarial.builder()
             * .eventosQuiebras(resumenEmpresarialOriginal.getEventosQuiebras())
             * .bancosRelacionados(resumenEmpresarialOriginal.getBancosRelacionados())
             * .ordenNoPago(resumenEmpresarialOriginal.getOrdenNoPago())
             * .vehiculos(resumenEmpresarialOriginal.getVehiculos())
             * .bienesRaices(resumenEmpresarialOriginal.getBienesRaices())
             * .exportaciones(resumenEmpresarialOriginal.getExportaciones())
             * .importaciones(resumenEmpresarialOriginal.getImportaciones())
             * .infraccionesLaborales(resumenEmpresarialOriginal.getInfraccionesLaborales())
             * .infraccionesPrevisionales(resumenEmpresarialOriginal.
             * getInfraccionesPrevisionales())
             * .prendas(resumenEmpresarialOriginal.getPrendas())
             * .anotacionesTributarias(resumenEmpresarialOriginal.getAnotacionesTributarias(
             * ))
             * .sociedades(resumenEmpresarialOriginal.getSocieades()) // ¡ojo con el typo en
             * el nombre!
             * .socios(resumenEmpresarialOriginal.getSocios())
             * .build()
             * : null;
             */

            ConsultaResponsePersona.Resultado.ResumenEmpresarial resumenEmpresarial = ConsultaResponsePersona.Resultado.ResumenEmpresarial
                    .builder()
                    .eventosQuiebras(respuestaMaat.getDatos().getResumenEmpresarial()
                            .getEventosQuiebras())
                    .bancosRelacionados(respuestaMaat.getDatos().getResumenEmpresarial().getBancosRelacionados()!=null?respuestaMaat.getDatos().getResumenEmpresarial().getBancosRelacionados():"") 
                    .ordenNoPago(respuestaMaat.getDatos().getResumenEmpresarial().getOrdenNoPago())
                    .vehiculos(respuestaMaat.getDatos().getResumenEmpresarial().getVehiculos())
                    .bienesRaices(respuestaMaat.getDatos().getResumenEmpresarial()
                            .getBienesRaices())
                    .exportaciones(respuestaMaat.getDatos().getResumenEmpresarial()
                            .getExportaciones())
                    .importaciones(respuestaMaat.getDatos().getResumenEmpresarial()
                            .getImportaciones())
                    .infraccionesLaborales(respuestaMaat.getDatos().getResumenEmpresarial()
                            .getInfraccionesLaborales())
                    .infraccionesPrevisionales(respuestaMaat.getDatos().getResumenEmpresarial()
                            .getInfraccionesPrevisionales())
                    .prendas(respuestaMaat.getDatos().getResumenEmpresarial().getPrendas())
                    .anotacionesTributarias(respuestaMaat.getDatos().getResumenEmpresarial()
                            .getAnotacionesTributarias())
                    .sociedades(respuestaMaat.getDatos().getResumenEmpresarial().getSocieades())
                    .socios(respuestaMaat.getDatos().getResumenEmpresarial().getSocios())
                    .build();

            // Mapeo de Quiebras
            List<ConsultaResponsePersona.Resultado.Quiebra> quiebras = respuestaMaat.getDatos()
                    .getQuiebras().getData()
                    .stream()
                    .map(quie -> ConsultaResponsePersona.Resultado.Quiebra.builder()
                            .resolucion(quie.getResolucion())
                            .fechaResolucion(UtilesVarios
                                    .convertirFechaISO(quie.getFechaResolucion()))
                            .fechaPublicacion(UtilesVarios
                                    .convertirFechaISO(quie.getFechaPublicacion()))
                            .pagina(quie.getPagina())
                            .extracto(quie.getExtracto())
                            .build())
                    .toList();

            // Mapeo de ONP
            List<ConsultaResponsePersona.Resultado.OrdenNoPago> ordenNoPago = respuestaMaat.getDatos()
                    .getOrdenNoPago().getData()
                    .stream()
                    .map(onp -> ConsultaResponsePersona.Resultado.OrdenNoPago.builder()
                            .banco(onp.getBanco())
                            .cuentaCliente(onp.getCuentaCliente())
                            .numeroCheque(onp.getNumeroCheque())
                            .numeroSerie(onp.getNumeroSerie())
                            .sucursal(onp.getSucursal())
                            .fecha(UtilesVarios.convertirFechaISO(onp.getFecha()))
                            .motivo(onp.getMotivo())
                            .fuente(onp.getFuente())
                            .build())
                    .toList();

            // Mapeo de Vehiculos
            List<ConsultaResponsePersona.Resultado.Vehiculo> vehiculos = respuestaMaat.getDatos()
                    .getVehiculos().getData()
                    .stream()
                    .map(veh -> ConsultaResponsePersona.Resultado.Vehiculo.builder()
                            .tipo(veh.getTipo())
                            .marca(veh.getMarca())
                            .patente(veh.getPatente())
                            .modelo(veh.getModelo())
                            .agno(veh.getAgno())
                            .avaluo(UtilesVarios.convertirMonto(veh.getAvaluo()))
                            .build())
                    .toList();

            // Mapeo de Bienes Raices
            List<ConsultaResponsePersona.Resultado.BienRaiz> bienesRaices = respuestaMaat.getDatos()
                    .getBienesRaices().getData()
                    .stream()
                    .map(braiz -> ConsultaResponsePersona.Resultado.BienRaiz.builder()
                            .rol(braiz.getRol())
                            .comuna(braiz.getComuna().trim())
                            .direccion(braiz.getDireccion())
                            .tipo(braiz.getTipo())
                            .destino(braiz.getDestino().trim())
                            .avaluo(UtilesVarios.convertirMonto(braiz.getAvaluo()))
                            .build())
                    .toList();

            // Mapeo de Exportaciones
            List<ConsultaResponsePersona.Resultado.Exportacion> exportaciones = respuestaMaat.getDatos()
                    .getExportaciones().getData()
                    .stream()
                    .map(expor -> ConsultaResponsePersona.Resultado.Exportacion.builder()
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
            List<ConsultaResponsePersona.Resultado.Importacion> importaciones = respuestaMaat.getDatos()
                    .getImportaciones().getData()
                    .stream()
                    .map(impor -> ConsultaResponsePersona.Resultado.Importacion.builder()
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
            List<ConsultaResponsePersona.Resultado.Prenda> prendas = respuestaMaat.getDatos().getPrendas().getData()
                    .stream()
                    .map(prenda -> ConsultaResponsePersona.Resultado.Prenda.builder()
                            .acreedor(prenda.getAcreedor())
                            .fechaPublicacion(UtilesVarios.convertirFechaISO(
                                    prenda.getFechaPublicacion()))
                            .cuerpo(prenda.getCuerpo())
                            .pagina(prenda.getPagina())
                            .extracto(prenda.getExtracto())
                            .build())
                    .toList();

            // Mapeo de Prendas
            List<ConsultaResponsePersona.Resultado.Anotacion> anotaciones = respuestaMaat.getDatos()
                    .getAnotaciones().getData()
                    .stream()
                    .map(anota -> ConsultaResponsePersona.Resultado.Anotacion.builder()
                            .fecha(UtilesVarios.convertirFechaISO(anota.getFecha()))
                            .motivo(anota.getMotivo())
                            .build())
                    .toList();

            // Mapeo de Socios y Sociedades
            /*
             * DatosFull.SocioSociedades socioSociedadesOriginal = ListaOError
             * .obtenerPrimero(respuestaMaat.getDatos().getSocioSociedades());
             * 
             * ConsultaResponsePersona.Resultado.SocioSociedades sociosSociedades =
             * socioSociedadesOriginal != null
             * ? ConsultaResponsePersona.Resultado.SocioSociedades.builder()
             * .sociedades(
             * socioSociedadesOriginal.getSociedades().stream()
             * .map(sociedad -> ConsultaResponsePersona.Resultado.SocioSociedades.Sociedad
             * .builder()
             * .fecha(UtilesVarios.convertirFechaISO(sociedad.getFecha()))
             * .contenido(sociedad.getContenido())
             * .nombre(sociedad.getNombre())
             * .rut(UtilesVarios.validarRetornarRut(sociedad.getRut()).getRut())
             * .digito(UtilesVarios.validarRetornarRut(sociedad.getRut())
             * .getDigitoVerificador())
             * .edicion(sociedad.getEdicion())
             * .cuerpo(sociedad.getCuerpo())
             * .pagina(sociedad.getPagina())
             * .extracto(sociedad.getExtracto())
             * .socios(
             * sociedad.getSocios().stream()
             * .map(socio ->
             * ConsultaResponsePersona.Resultado.SocioSociedades.Sociedad.Socio
             * .builder()
             * .rutSocio(UtilesVarios
             * .validarRetornarRut(
             * socio.getRutSocio())
             * .getRut())
             * .digitoSocio(UtilesVarios
             * .validarRetornarRut(
             * socio.getRutSocio())
             * .getDigitoVerificador())
             * .nombreSocio(socio.getNombreSocio())
             * .tipo(socio.getTipo())
             * .aporte(socio.getAporte())
             * .build())
             * .collect(Collectors.toList()))
             * .build())
             * .collect(Collectors.toList()))
             * .build()
             * : null;
             */

            // Paso 1: Obtener los datos de forma segura con Optional
            DatosFull datos = respuestaMaat.getDatos();
            ListaOError<DatosFull.SocioSociedades> listaOriginal = datos != null ? datos.getSocioSociedades() : null;

            List<ConsultaResponsePersona.Resultado.SocioSociedades> sociosSociedades = new ArrayList<>();

            if (listaOriginal != null && listaOriginal.getData() != null) {
                for (DatosFull.SocioSociedades sociedad : listaOriginal.getData()) {

                    // Convertir los socios internos
                    ListaOError<DatosFull.SocioSociedades.Socio> listaSocios = sociedad.getSocios();
                    List<RespuestaInformePersona.ConsultaResponsePersona.Resultado.SocioSociedades.Socio> socios = new ArrayList<>();

                    if (listaSocios != null && listaSocios.getData() != null) {
                        socios = listaSocios.getData().stream()
                                .map(socio -> ConsultaResponsePersona.Resultado.SocioSociedades.Socio.builder()
                                        .rutSocio(UtilesVarios.validarRetornarRut(socio.getRutSocio()).getRut())
                                        .digitoSocio(UtilesVarios.validarRetornarRut(socio.getRutSocio())
                                                .getDigitoVerificador())
                                        .nombreSocio(socio.getNombreSocio())
                                        .tipo(socio.getTipo())
                                        .aporte(socio.getAporte())
                                        .build())
                                .collect(Collectors.toList());
                    }

                    ConsultaResponsePersona.Resultado.SocioSociedades nuevaSociedad = ConsultaResponsePersona.Resultado.SocioSociedades
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

                    sociosSociedades.add(nuevaSociedad);
                }
            }

            /*
             * ConsultaResponsePersona.Resultado.SocioSociedades sociosSociedades =
             * ConsultaResponsePersona.Resultado.SocioSociedades
             * .builder()
             * .sociedades(respuestaMaat.getDatos().getSocioSociedades().getSociedades()
             * .stream()
             * .map(sociedad -> ConsultaResponsePersona.Resultado.SocioSociedades.Sociedad
             * .builder()
             * .fecha(UtilesVarios.convertirFechaISO(sociedad.getFecha()))
             * .contenido(sociedad.getContenido())
             * .nombre(sociedad.getNombre())
             * .rut(UtilesVarios.validarRetornarRut(sociedad.getRut()).getRut())
             * .digito(UtilesVarios.validarRetornarRut(sociedad.getRut()).
             * getDigitoVerificador())
             * .edicion(sociedad.getEdicion())
             * .cuerpo(sociedad.getCuerpo())
             * .pagina(sociedad.getPagina())
             * .extracto(sociedad.getExtracto())
             * 
             * .socios(sociedad.getSocios().stream()
             * .map(socio ->
             * ConsultaResponsePersona.Resultado.SocioSociedades.Sociedad.Socio
             * .builder()
             * .rutSocio(UtilesVarios.validarRetornarRut(socio.getRutSocio()).getRut())
             * .digitoSocio(UtilesVarios.validarRetornarRut(socio.getRutSocio()).
             * getDigitoVerificador())
             * .nombreSocio(socio.getNombreSocio())
             * .tipo(socio.getTipo())
             * .aporte(socio.getAporte())
             * .build())
             * .collect(Collectors.toList()))
             * 
             * .build())
             * .collect(Collectors.toList()) // Convertir la lista de sociedades
             * )
             * .build();
             */

            // Mapeo de Prendas
            List<ConsultaResponsePersona.Resultado.ConsultaRut> consultasAlRut = respuestaMaat.getDatos()
                    .getConsultasRut().getData()
                    .stream()
                    .map(car -> ConsultaResponsePersona.Resultado.ConsultaRut.builder()
                            .fecha(UtilesVarios.convertirFechaISO(car.getFecha()))
                            .institucion(car.getInstitucion())
                            .build())
                    .toList();

            // Mapeo de datos Score
            int score = Integer.parseInt(respuestaMaat.getDatos().getScoreMaat());
            String[] resultado = UtilesVarios.obtenerRiesgoYGlosaPorScore(score);

            String glosaScore = resultado[0];
            String riesgoCrediticio = resultado[1];

            ConsultaResponsePersona.Resultado.Score datosScore = ConsultaResponsePersona.Resultado.Score.builder()
                    .puntaje(score)
                    .riesgoCrediticio(riesgoCrediticio)
                    .glosa(glosaScore)
                    .build();

            return ConsultaResponsePersona.builder()
                    .idConsulta(idConsulta)
                    .codigoRetorno(codigoRetorno)
                    .glosaRetorno(glosaRetorno)
                    .resultado(ConsultaResponsePersona.Resultado.builder()
                            .datosPersonales(datosPersonales)
                            .datosScore(datosScore)
                            .resumenMontos(resumenMontos)
                            .direcciones(direcciones)
                            .morosidades(morosidades)
                            .deudasPrevisionales(deudasPrevisionales)
                            .deudasLaborales(deudasLaborales)
                            .protestos(protestos)
                            .infocom(infocom)
                            .deudasComercio(deudasComercio)
                            .resumenEmpresarial(resumenEmpresarial)
                            .quiebras(quiebras)
                            .ordenNoPago(ordenNoPago)
                            .vehiculos(vehiculos)
                            .bienesRaices(bienesRaices)
                            .exportaciones(exportaciones)
                            .importaciones(importaciones)
                            .prendas(prendas)
                            .anotaciones(anotaciones)
                            .sociosSociedades(sociosSociedades)
                            .extracto(extracto)
                            .consultasAlRut(consultasAlRut)
                            .build())
                    .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return ConsultaResponsePersona.builder()
                    .idConsulta(idConsulta)
                    .codigoRetorno("100")
                    .glosaRetorno("Error en la ejecución del servicio")
                    .resultado(new ConsultaResponsePersona.Resultado()) // No incluye el objeto
                                                                        // resultado en este caso
                    .build();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_EMPTY) // Permite que resultado aparezca como {}
    public static class ConsultaResponsePersona {
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
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class Resultado {
            @JsonProperty("datosPersona")
            private DatosPersonales datosPersonales;

            @JsonProperty("score")
            private Score datosScore;

            @JsonProperty("resumenMontos")
            private ResumenMontos resumenMontos;

            @JsonProperty("direccciones")
            private List<Direccion> direcciones;

            @JsonProperty("morosidades")
            private List<Morosidad> morosidades;

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

            @JsonProperty("resumenEmpresarial")
            private ResumenEmpresarial resumenEmpresarial;

            @JsonProperty("quiebras")
            private List<Quiebra> quiebras;

            @JsonProperty("ordenesNoPago")
            private List<OrdenNoPago> ordenNoPago;

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

            @JsonProperty("anotaciones")
            private List<Anotacion> anotaciones;

            @JsonProperty("SociosSociedades")
            private List<SocioSociedades> sociosSociedades;

            @JsonProperty("extracto")
            private String extracto;

            @JsonProperty("consultasAlRut")
            private List<ConsultaRut> consultasAlRut;

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class DatosPersonales {
                @JsonProperty("rut")
                private int rut;

                @JsonProperty("digito")
                private String digito;

                @JsonProperty("nombre")
                private String nombre;

                @JsonProperty("tipoPersona")
                private String tipoPersona;

                @JsonProperty("persona")
                private Persona persona;

                @JsonProperty("empresa")
                private Empresa empresa;

                @JsonProperty("ultimaDireccionInformada")
                private String ultimaDireccionInformada;

                @JsonProperty("cantidadDirecciones")
                private String cantidadDirecciones;

                @Data
                @NoArgsConstructor
                @AllArgsConstructor
                @Builder
                @JsonIgnoreProperties(ignoreUnknown = true)
                public static class Persona {
                    @JsonProperty("edad")
                    private String edad;

                    @JsonProperty("sexo")
                    private String sexo;

                    @JsonProperty("estadoCivil")
                    private String estadoCivil;

                    @JsonProperty("nacionalidad")
                    private String nacionalidad;
                }

                @Data
                @NoArgsConstructor
                @AllArgsConstructor
                @Builder
                @JsonIgnoreProperties(ignoreUnknown = true)
                public static class Empresa {
                    @JsonProperty("giros")
                    private List<Giro> giros;

                    @JsonProperty("productos")
                    private Productos productos;

                    @Data
                    @NoArgsConstructor
                    @AllArgsConstructor
                    @Builder
                    @JsonIgnoreProperties(ignoreUnknown = true)
                    public static class Giro {
                        @JsonProperty("giro")
                        private String giro;
                    }

                    @Data
                    @NoArgsConstructor
                    @AllArgsConstructor
                    @Builder
                    @JsonIgnoreProperties(ignoreUnknown = true)
                    public static class Productos {
                        @JsonProperty("producto")
                        private String producto;
                    }
                }

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
            public static class ResumenMontos {
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
            public static class Direccion {
                @JsonProperty("tipo")
                private String tipo;

                @JsonProperty("direccion")
                private String direccion;

                @JsonProperty("fechaInformacion")
                private String fechaInformacion;
            }

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Morosidad {
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
            public static class Infocom {
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
            }

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class ResumenEmpresarial {
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
                @JsonProperty("banco")
                private String banco;

                @JsonProperty("cuentaCliente")
                private String cuentaCliente;

                @JsonProperty("numeroCheque")
                private String numeroCheque;

                @JsonProperty("numeroSerie")
                private String numeroSerie;

                @JsonProperty("sucursal")
                private String sucursal;

                @JsonProperty("fecha")
                private String fecha;

                @JsonProperty("motivo")
                private String motivo;

                @JsonProperty("fuente")
                private String fuente;
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

                @JsonProperty("agno")
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
                @JsonProperty("agno")
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
                @JsonProperty("agno")
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
            public static class Anotacion {

                @JsonProperty("fecha")
                private String fecha;

                @JsonProperty("motivo")
                private String motivo;

            }

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder
            @JsonIgnoreProperties(ignoreUnknown = true)
            static class SocioSociedades {

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
            public static class ConsultaRut {
                @JsonProperty("fecha")
                private String fecha;

                @JsonProperty("institucion")
                private String institucion;
            }
        }
    }
}
