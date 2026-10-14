
package com.diego.citas.repository;

import com.diego.citas.entity.Cita;
import com.diego.citas.enums.EstadoCita;
import com.diego.commons.enums.EstadoRegistro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {
    List<Cita> findByEstadoRegistro(EstadoRegistro estadoRegistro);

    boolean existsByIdPacienteAndEstadoRegistroAndEstadoCitaIn(
            Long idPaciente,
            EstadoRegistro estadoRegistro,
            List<EstadoCita> estadosCita
    );

    boolean existsByIdMedicoAndEstadoRegistroAndEstadoCitaIn(
            Long idMedico,
            EstadoRegistro estadoRegistro,
            List<EstadoCita> estadosCita
    );
}