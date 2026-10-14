
package com.diego.citas.repository;

import com.diego.citas.entity.Cita;
import com.diego.commons.enums.EstadoRegistro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CitasRepository extends JpaRepository<Cita, Long> {

    List<Cita> findByEstadoRegistro(EstadoRegistro estadoRegistro);
}
