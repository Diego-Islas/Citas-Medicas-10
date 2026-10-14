package com.diego.commons.clients;

import com.diego.commons.dto.medicos.MedicoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "medicos")
public interface MedicoClient {
    @GetMapping("/id-medico/{id}")
    ResponseEntity<MedicoResponse> obtenerMedicoPorIdSinEstado(@PathVariable Long id);

    @GetMapping("/{id}")
    ResponseEntity<MedicoResponse> obtenerMedicoPorId(@PathVariable Long id);

    @PutMapping("/{idMedico}/disponibilidad/{idDisponibilidad}")
    void actualizarDisponibilidadMedico(
            @PathVariable Long idMedico,
            @PathVariable Long idDisponibilidad
    );

    @PutMapping("/interno/{idMedico}/disponibilidad/{idDisponibilidad}")
    void actualizarDisponibilidadMedicoPorCita(
            @PathVariable Long idMedico,
            @PathVariable Long idDisponibilidad
    );
}