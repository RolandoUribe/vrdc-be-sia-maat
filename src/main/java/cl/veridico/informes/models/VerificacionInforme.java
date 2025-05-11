package cl.veridico.informes.models;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "verificacion_informe")
public class VerificacionInforme {

    @Id
    @Column(name = "id_consulta", nullable = false, updatable = false)
    private UUID idConsulta;

    @Column(name = "folio", nullable = false, unique = true)
    private String folio;

    @Column(name = "codigo_verificacion", nullable = false)
    private String codigoVerificacion;

    @Column(name = "contador_verificacion", nullable = false)
    private int contadorVerificacion = 0;

    // Getters y Setters
    public UUID getIdConsulta() {
        return idConsulta;
    }

    public void setIdConsulta(UUID idConsulta) {
        this.idConsulta = idConsulta;
    }

    public String getFolio() {
        return folio;
    }

    public void setFolio(String folio) {
        this.folio = folio;
    }

    public String getCodigoVerificacion() {
        return codigoVerificacion;
    }

    public void setCodigoVerificacion(String codigoVerificacion) {
        this.codigoVerificacion = codigoVerificacion;
    }

    public Integer getContadorVerificacion() {
        return contadorVerificacion;
    }

    public void setContadorVerificacion(Integer contadorVerificacion) {
        this.contadorVerificacion = contadorVerificacion;
    }

}