package cl.veridico.informes.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

public class TransaccionCliente {
    private UUID idConsulta;
    private Integer idServicio;
    private String parametrosConsulta;
    private Integer statusHttp;
    private String codigoRetorno;
    private Integer tiempoRespuesta;
    private LocalDateTime fechaHoraConsulta;

    // Constructor vacío
    public TransaccionCliente() {
    }

    // Constructor con parámetros
    public TransaccionCliente(UUID idConsulta, Integer idServicio, 
            String parametrosConsulta, Integer statusHttp, String codigoRetorno, Integer tiempoRespuesta, LocalDateTime fechaHoraConsulta) {
        this.idConsulta = idConsulta;
        this.idServicio = idServicio;
        this.parametrosConsulta = parametrosConsulta;
        this.statusHttp = statusHttp;
        this.codigoRetorno = codigoRetorno;
        this.tiempoRespuesta = tiempoRespuesta;
        this.fechaHoraConsulta = fechaHoraConsulta;
    }

    // Getters y Setters
    public UUID getIdConsulta() {
        return idConsulta;
    }

    public void setIdConsulta(UUID idConsulta) {
        this.idConsulta = idConsulta;
    }

    public Integer getIdServicio() {
        return idServicio;
    }

    public void setIdServicio(Integer idServicio) {
        this.idServicio = idServicio;
    }

    public String getParametrosConsulta() {
        return parametrosConsulta;
    }

    public void setParametrosConsulta(String parametrosConsulta) {
        this.parametrosConsulta = parametrosConsulta;
    }

    public Integer getStatusHttp() {
        return statusHttp;
    }

    public void setStatusHttp(Integer statusHttp) {
        this.statusHttp = statusHttp;
    }

    public String getCodigoRetorno() {
        return codigoRetorno;
    }

    public void setCodigoRetorno(String codigoRetorno) {
        this.codigoRetorno = codigoRetorno;
    }

    public Integer getTiempoRespuesta() {
        return tiempoRespuesta;
    }

    public void setTiempoRespuesta(Integer tiempoRespuesta) {
        this.tiempoRespuesta = tiempoRespuesta;
    }

    public LocalDateTime getFechaHoraConsulta() {
        return fechaHoraConsulta;
    }

    public void setFechaHoraConsulta(LocalDateTime fechaHoraConsulta) {
        this.fechaHoraConsulta = fechaHoraConsulta;
    }
}