package cl.veridico.informes.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

import cl.veridico.informes.models.VerificacionInforme;

@Repository
public interface IVerificacionInformeRepository extends JpaRepository<VerificacionInforme, UUID> {
    Optional<VerificacionInforme> findByFolioAndCodigoVerificacion(String folio, String codigoVerificacion);
}
