package com.diego.pacientes.repositories;

import com.diego.commons.enums.EstadoRegistro;
import com.diego.pacientes.entities.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    List<Paciente> findAllByEstadoRegistro(EstadoRegistro estadoRegistro);

    Optional<Paciente> findByIdAndEstadoRegistro(Long id, EstadoRegistro estadoRegistro);

    boolean existsByEmailIgnoreCaseAndTelefonoAndEstadoRegistro(String email, String telefono, EstadoRegistro estadoRegistro);

    boolean existsByEmailIgnoreCaseAndTelefonoAndEstadoRegistroAndIdNot(String email, String telefono, EstadoRegistro estadoRegistro, Long id);
}
