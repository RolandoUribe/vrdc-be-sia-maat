package cl.veridico.informes.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.Optional;
import java.util.UUID;

import cl.veridico.informes.models.VerificacionInforme;
import cl.veridico.informes.repositories.IVerificacionInformeRepository;;

@Service
public class VerificacionInformeService {

    private final IVerificacionInformeRepository repository;

    @PersistenceContext
    private EntityManager entityManager;  // Para ejecutar consultas nativas

    public VerificacionInformeService(IVerificacionInformeRepository repository) {
        this.repository = repository;
    }

    // Método para obtener el siguiente número de la secuencia
    private long obtenerSiguienteFolio() {
        return (long) entityManager.createNativeQuery("SELECT nextval('folio_seq')").getSingleResult();
    }

    // Método para insertar un nuevo registro
    @Transactional
    public VerificacionInforme registrarInforme(UUID idConsulta) {
        VerificacionInforme informe = new VerificacionInforme();
        informe.setIdConsulta(idConsulta);

        // Obtener el siguiente valor de la secuencia desde la BD
        long secuencia = obtenerSiguienteFolio();

        // Generar folio en formato "YYYY-XXXXX"
        String folio = Year.now().getValue() + "-" + secuencia;
        informe.setFolio(folio);

        // Extraer primer segmento del UUID y convertirlo a mayúsculas
        String codigoVerificacion = idConsulta.toString().split("-")[0].toUpperCase();
        informe.setCodigoVerificacion(codigoVerificacion);

        // Guardar en base de datos
        return repository.save(informe);
    }

    // Método para verificar un informe
    @Transactional
    public Optional<VerificacionInforme> verificarInforme(String folio, String codigoVerificacion) {
        Optional<VerificacionInforme> informeOpt = repository.findByFolioAndCodigoVerificacion(folio, codigoVerificacion);

        // Si se encuentra el informe, incrementar contador de verificación
        informeOpt.ifPresent(informe -> {
            informe.setContadorVerificacion(informe.getContadorVerificacion() + 1);
            repository.save(informe);
        });

        return informeOpt;
    }
}
