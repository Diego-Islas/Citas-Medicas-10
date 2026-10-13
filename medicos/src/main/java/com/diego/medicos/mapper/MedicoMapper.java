package com.diego.medicos.mapper;

import com.diego.commons.dto.medicos.MedicoRequest;
import com.diego.commons.dto.medicos.MedicoResponse;
import com.diego.commons.enums.EspecialidadMedico;
import com.diego.commons.mapper.CommonMapper;
import com.diego.medicos.entities.Medico;
import org.springframework.stereotype.Component;

@Component
public class MedicoMapper implements CommonMapper<MedicoRequest, MedicoResponse, Medico> {
    @Override
    public Medico requestAEntidad(MedicoRequest request) {
        return request == null ? null
                : Medico.crear(
                request.nombre(),
                request.apellidoPaterno(),
                request.apellidoMaterno(),
                request.edad(),
                request.email(),
                request.telefono(),
                request.cedulaProfesional()
        );
    }

    public Medico requestAEntidad(
            MedicoRequest request,
            EspecialidadMedico especialidadMedico
    ) {
        Medico medico = requestAEntidad(request);

        medico.asignarEspecialidadMedico(especialidadMedico);

        return medico;
    }

    @Override
    public MedicoResponse entidadAResponse(Medico medico) {
        return medico == null ? null
                : new MedicoResponse(
                        medico.getId(),
                        medico.getNombre(),
                        medico.getApellidoPaterno(),
                        medico.getApellidoMaterno(),
                        medico.getEdad(),
                        medico.getEmail(),
                        medico.getTelefono(),
                        medico.getCedulaProfesional(),
                        medico.getEspecialidadMedico().getDescripcion(),
                        medico.getDisponibilidadMedico().getDescripcion(),
                        medico.getDisponibilidadMedico().getCodigo()
        );
    }
}
