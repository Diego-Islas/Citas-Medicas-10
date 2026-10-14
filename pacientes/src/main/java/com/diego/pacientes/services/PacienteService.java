package com.diego.pacientes.services;

import com.diego.commons.dto.pacientes.PacienteRequest;
import com.diego.commons.dto.pacientes.PacienteResponse;
import com.diego.commons.services.CrudService;

public interface PacienteService extends CrudService<PacienteRequest, PacienteResponse> {
    PacienteResponse obtenerPorIdSinEstado(Long id);
}
