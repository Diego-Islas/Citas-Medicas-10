package com.diego.commons.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "citas")
public interface CitaClient {

    @GetMapping("/paciente-cita/{idPaciente}")
    ResponseEntity<Void> pacienteTieneCitaActiva(@PathVariable Long idPaciente);

    @GetMapping("/medico-cita/{idMedico}")
    ResponseEntity<Void> medicoTieneCitaActiva(@PathVariable Long idMedico);
}