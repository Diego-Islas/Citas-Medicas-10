package com.diego.medicos.services;

import com.diego.commons.dto.medicos.MedicoRequest;
import com.diego.commons.dto.medicos.MedicoResponse;
import com.diego.commons.services.CrudService;

public interface MedicoService extends CrudService<MedicoRequest, MedicoResponse> {
    MedicoResponse obtenerMedicoPorIdSinEstado(Long id);

    void actualizarDisponibilidadMedico(Long id, Long idDisponibilidad);
}
