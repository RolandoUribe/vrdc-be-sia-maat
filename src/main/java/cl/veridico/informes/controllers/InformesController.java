package cl.veridico.informes.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import cl.veridico.informes.dtos.TransaccionCliente;
import cl.veridico.informes.models.RespuestaInformeEmpresa;
import cl.veridico.informes.models.VerificacionInforme;
import cl.veridico.informes.models.RespuestaInformeEmpresa.ConsultaResponseEmpresa;
import cl.veridico.informes.services.InformeMaatClient;
import cl.veridico.informes.services.PdfConsolidadoService;
import cl.veridico.informes.services.PdfEmpresaService;
import cl.veridico.informes.services.PdfPersonaService;
import cl.veridico.informes.services.RegistroServicioService;
import cl.veridico.informes.services.RespuestaInformeConsolidado;
import cl.veridico.informes.services.RespuestaInformePersona;
import cl.veridico.informes.services.RespuestaInformeConsolidado.ConsultaResponse;
import cl.veridico.informes.services.RespuestaInformePersona.ConsultaResponsePersona;
import cl.veridico.informes.services.StorageService;
import cl.veridico.informes.services.VerificacionInformeService;
import cl.veridico.informes.utils.UtilesVarios;

@RestController
@RequestMapping("/v1")
public class InformesController {

    @Autowired
    private InformeMaatClient informeMaatClient;

    @Autowired
    private StorageService storageService;

    @Autowired
    private VerificacionInformeService verificaInformeService;

    @Autowired
    private RegistroServicioService registroServicioService;

    @Autowired
    private PdfConsolidadoService pdfConsolidadoService;

    @Value("${gcp.storage.bucket-name}")
    private String bucketName;

    @Value("${pdf.storage.path}")
    private String pdfStoragePath; // Ruta parametrizada desde application.properties

    @GetMapping("/informe-veridico-consolidado/run/{run}")
    public ResponseEntity<?> informeConsolidado(
            @PathVariable String run,
            @RequestHeader(value = "x-apigateway-api-userinfo", required = true) String userInfo) {

        StopWatch stopWatch = new StopWatch();
        // Se inicia cronómetro
        stopWatch.start();

        String digito = run.substring(run.length() - 1);
        run = run.substring(0, run.length() - 1);

        String uuidConsulta = UtilesVarios.generateUUId();
        ConsultaResponse respuestaError = null;

        try {

            // Decodificar el JSON del header
            String decoded = new String(Base64.getDecoder().decode(userInfo));
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(decoded);
            String preferredUsername = jsonNode.has("preferred_username") ? jsonNode.get("preferred_username").asText()
                    : "sin_usuario";
            Boolean esClienteAPI = preferredUsername.substring(0, 15).equals("service-account");

            if (validaParametros(run, digito, userInfo) == false) {
                respuestaError = RespuestaInformeConsolidado.generarRespuestaError(uuidConsulta, "101");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuestaError);
            }

            // respuesta json desde maat que viene como string
            String respuestaMaat = informeMaatClient.obtenerInforme(run, digito, userInfo, uuidConsulta, "consolidado");

            // System.out.println("InformesController - informeConsolidado - Respuesta Maat
            // 2: [" + respuestaMaat + "]");

            if (respuestaMaat == null || respuestaMaat.isEmpty()) {
                respuestaError = RespuestaInformeConsolidado.generarRespuestaError(uuidConsulta, "100");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(respuestaError);
            }

            // Se detiene cronómetro
            stopWatch.stop();

            // Registro consumo cliente
            TransaccionCliente transaccionCliente = new TransaccionCliente();
            transaccionCliente.setIdConsulta(UUID.fromString(uuidConsulta));
            transaccionCliente.setIdServicio(3);
            transaccionCliente.setParametrosConsulta(run + digito);
            transaccionCliente.setStatusHttp(200);
            transaccionCliente.setCodigoRetorno("0");
            transaccionCliente.setTiempoRespuesta(((int) stopWatch.getTotalTimeMillis()));
            transaccionCliente.setFechaHoraConsulta(LocalDateTime.now());
            registroServicioService.registrarLogTransaccionCliente(transaccionCliente, userInfo);

            // Se convierte al JSON en formato Verídico
            ConsultaResponse respuesta = RespuestaInformeConsolidado.generarRespuestaOK(uuidConsulta, "0",
                    respuestaMaat);

            // Registra folio para verificar informe a posterior
            if (!esClienteAPI) {
                VerificacionInforme verificacionInforme = verificaInformeService
                        .registrarInforme(UUID.fromString(uuidConsulta));
                // System.out.println("folio:" + verificacionInforme.getFolio());
                // System.out.println("código verificación:" +
                // verificacionInforme.getCodigoVerificacion());

                // Genera informe PDF

                byte[] pdfBytes = pdfConsolidadoService.generatePdfFromConsulta(
                        respuesta,
                        verificacionInforme.getFolio(),
                        verificacionInforme.getCodigoVerificacion());

                // Guardar el PDF en el bucket
                String fileName = uuidConsulta + ".pdf";
                storageService.uploadPdfBytes(pdfBytes, bucketName, fileName);

                Map<String, Object> data = new HashMap<>();
                data.put("codigoRetorno", respuesta.getCodigoRetorno());
                data.put("glosaRetorno", respuesta.getGlosaRetorno());
                data.put("idConsulta", respuesta.getIdConsulta());
                data.put("resultado", null);
                data.put("timestamp", respuesta.getTimestamp());
                return ResponseEntity.ok(data);

            } else {
                return ResponseEntity.ok(respuesta);
            }

        } catch (Exception e) {
            e.printStackTrace();
            respuestaError = RespuestaInformeConsolidado.generarRespuestaError(uuidConsulta, "100");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(respuestaError);
        }
    }

    @GetMapping("/informe-veridico-personas/run/{run}")
    public ResponseEntity<?> informeVeridicoPersona(
            @PathVariable String run,
            @RequestHeader(value = "x-apigateway-api-userinfo", required = true) String userInfo) {

        StopWatch stopWatch = new StopWatch();
        // Se inicia cronómetro
        stopWatch.start();

        String digito = run.substring(run.length() - 1);
        run = run.substring(0, run.length() - 1);

        String uuidConsulta = UtilesVarios.generateUUId();
        ConsultaResponse respuestaError = null;

        try {

            // Decodificar el JSON del header
            String decoded = new String(Base64.getDecoder().decode(userInfo));
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(decoded);
            String preferredUsername = jsonNode.has("preferred_username") ? jsonNode.get("preferred_username").asText()
                    : "sin_usuario";
            Boolean esClienteAPI = preferredUsername.substring(0, 15).equals("service-account");

            if (validaParametros(run, digito, userInfo) == false) {
                respuestaError = RespuestaInformeConsolidado.generarRespuestaError(uuidConsulta, "101");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuestaError);
            }

            // respuesta json desde maat que viene como string
            String respuestaMaat = informeMaatClient.obtenerInforme(run, digito, userInfo, uuidConsulta, "persona");
            if (respuestaMaat == null || respuestaMaat.isEmpty()) {
                respuestaError = RespuestaInformeConsolidado.generarRespuestaError(uuidConsulta, "100");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(respuestaError);
            }

            // Se detiene cronómetro
            stopWatch.stop();

            // Registro consumo cliente
            TransaccionCliente transaccionCliente = new TransaccionCliente();
            transaccionCliente.setIdConsulta(UUID.fromString(uuidConsulta));
            transaccionCliente.setIdServicio(3);
            transaccionCliente.setParametrosConsulta(run + digito);
            transaccionCliente.setStatusHttp(200);
            transaccionCliente.setCodigoRetorno("0");
            transaccionCliente.setTiempoRespuesta(((int) stopWatch.getTotalTimeMillis()));
            transaccionCliente.setFechaHoraConsulta(LocalDateTime.now());
            registroServicioService.registrarLogTransaccionCliente(transaccionCliente, userInfo);

            // Se convierte al JSON en formato Verídico
            ConsultaResponsePersona respuesta = RespuestaInformePersona.generarRespuestaOK(uuidConsulta, "0",
                    respuestaMaat);

            // Registra folio para verificar informe a posterior
            if (!esClienteAPI) {
                // Registra folio para verificar informe a posterior
                VerificacionInforme verificacionInforme = verificaInformeService
                        .registrarInforme(UUID.fromString(uuidConsulta));
                // System.out.println("folio:" + verificacionInforme.getFolio());
                // System.out.println("código verificación:" +
                // verificacionInforme.getCodigoVerificacion());

                // Genera informe PDF
                PdfPersonaService pdfService = new PdfPersonaService();
                byte[] pdfBytes = pdfService.generatePdfFromConsulta(respuesta,
                        verificacionInforme.getFolio(),
                        verificacionInforme.getCodigoVerificacion());

                // Guardar el PDF en el bucket
                String fileName = uuidConsulta + ".pdf";
                storageService.uploadPdfBytes(pdfBytes, bucketName, fileName);

                Map<String, Object> data = new HashMap<>();
                data.put("codigoRetorno", respuesta.getCodigoRetorno());
                data.put("glosaRetorno", respuesta.getGlosaRetorno());
                data.put("idConsulta", respuesta.getIdConsulta());
                data.put("resultado", null);
                data.put("timestamp", respuesta.getTimestamp());
                return ResponseEntity.ok(data);

            } else {
                return ResponseEntity.ok(respuesta);
            }

        } catch (Exception e) {
            e.printStackTrace();
            respuestaError = RespuestaInformeConsolidado.generarRespuestaError(uuidConsulta, "100");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(respuestaError);
        }
    }

    @GetMapping("/informe-veridico-empresarial/run/{run}")
    public ResponseEntity<?> informeVeridicoEmpresa(
            @PathVariable String run,
            @RequestHeader(value = "x-apigateway-api-userinfo", required = true) String userInfo) {

        StopWatch stopWatch = new StopWatch();
        // Se inicia cronómetro
        stopWatch.start();

        String digito = run.substring(run.length() - 1);
        run = run.substring(0, run.length() - 1);

        String uuidConsulta = UtilesVarios.generateUUId();
        ConsultaResponse respuestaError = null;

        try {
            // Decodificar el JSON del header
            String decoded = new String(Base64.getDecoder().decode(userInfo));

            System.out.println("InformesController - informeVeridicoEmpresa - userInfo: [" + decoded + "]");
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(decoded);
            String preferredUsername = jsonNode.has("preferred_username") ? jsonNode.get("preferred_username").asText()
                    : "sin_usuario";
            Boolean esClienteAPI = preferredUsername.substring(0, 15).equals("service-account");

            if (validaParametros(run, digito, userInfo) == false) {
                respuestaError = RespuestaInformeConsolidado.generarRespuestaError(uuidConsulta, "101");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuestaError);
            }

            // respuesta json desde maat que viene como string
            String respuestaMaat = informeMaatClient.obtenerInforme(run, digito, userInfo, uuidConsulta, "empresa");
            if (respuestaMaat == null || respuestaMaat.isEmpty()) {
                respuestaError = RespuestaInformeConsolidado.generarRespuestaError(uuidConsulta, "100");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(respuestaError);
            }

            // Se detiene cronómetro
            stopWatch.stop();

            // Registro consumo cliente
            TransaccionCliente transaccionCliente = new TransaccionCliente();
            transaccionCliente.setIdConsulta(UUID.fromString(uuidConsulta));
            transaccionCliente.setIdServicio(3);
            transaccionCliente.setParametrosConsulta(run + digito);
            transaccionCliente.setStatusHttp(200);
            transaccionCliente.setCodigoRetorno("0");
            transaccionCliente.setTiempoRespuesta(((int) stopWatch.getTotalTimeMillis()));
            transaccionCliente.setFechaHoraConsulta(LocalDateTime.now());
            registroServicioService.registrarLogTransaccionCliente(transaccionCliente, userInfo);

            // Se convierte al JSON en formato Verídico
            ConsultaResponseEmpresa respuesta = RespuestaInformeEmpresa.generarRespuestaOK(uuidConsulta, "0",
                    respuestaMaat);

            // Registra folio para verificar informe a posterior
            if (!esClienteAPI) {
                // Registra folio para verificar informe a posterior
                VerificacionInforme verificacionInforme = verificaInformeService
                        .registrarInforme(UUID.fromString(uuidConsulta));
                // System.out.println("folio:" + verificacionInforme.getFolio());
                // System.out.println("código verificación:" +
                // verificacionInforme.getCodigoVerificacion());

                // Genera informe PDF
                PdfEmpresaService pdfService = new PdfEmpresaService();
                byte[] pdfBytes = pdfService.generatePdfFromConsulta(respuesta,
                        verificacionInforme.getFolio(),
                        verificacionInforme.getCodigoVerificacion());

                // Guardar el PDF en el bucket
                String fileName = uuidConsulta + ".pdf";
                storageService.uploadPdfBytes(pdfBytes, bucketName, fileName);

                Map<String, Object> data = new HashMap<>();
                data.put("codigoRetorno", respuesta.getCodigoRetorno());
                data.put("glosaRetorno", respuesta.getGlosaRetorno());
                data.put("idConsulta", respuesta.getIdConsulta());
                data.put("resultado", null);
                data.put("timestamp", respuesta.getTimestamp());
                return ResponseEntity.ok(data);

            } else {
                return ResponseEntity.ok(respuesta);
            }

        } catch (Exception e) {
            e.printStackTrace();
            respuestaError = RespuestaInformeConsolidado.generarRespuestaError(uuidConsulta, "100");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(respuestaError);
        }
    }

    // Obtiene el informe en pdf almacenado previamente.
    // en caso de no encontrar el pdf entrega error
    @GetMapping("/informe/uuid/{uuid}")
    public ResponseEntity<byte[]> getInformePdf(@PathVariable String uuid) {
        try {
            Path filePath = Paths.get(pdfStoragePath, uuid + ".pdf");
            File pdfFile = filePath.toFile();

            if (!pdfFile.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            byte[] pdfBytes = Files.readAllBytes(filePath);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + uuid + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    private boolean validaParametros(String run, String digito, String userInfo) {
        try {
            if (run == null || run.isEmpty() || digito == null || digito.isEmpty() || userInfo == null
                    || userInfo.isEmpty()) {
                return false;
            }

            if (UtilesVarios.validarRut(run + "-" + digito) == false) {
                return false;
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
