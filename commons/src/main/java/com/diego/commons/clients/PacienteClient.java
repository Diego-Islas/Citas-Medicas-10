package com.diego.commons.clients;

import com.diego.commons.dto.pacientes.PacienteResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "pacientes")
public interface PacienteClient {
    @GetMapping("/id-paciente/{id}")
    ResponseEntity<PacienteResponse> obtenerPorIdSinEstado(@PathVariable Long id);

    @GetMapping("/{id}")
    ResponseEntity<PacienteResponse> obtenerPacientePorIdActivo(@PathVariable Long id);
}