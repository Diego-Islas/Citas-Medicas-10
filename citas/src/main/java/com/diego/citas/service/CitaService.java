package com.diego.citas.service;

import com.diego.citas.dto.CitaRequest;
import com.diego.citas.dto.CitaResponse;
import com.diego.commons.services.CrudService;

public interface CitaService extends CrudService<CitaRequest, CitaResponse> {
    void actualizarEstadoCita(Long idCita, Long idEstadoCita);
}
