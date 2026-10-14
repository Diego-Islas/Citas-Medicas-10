
package com.diego.citas.mapper;

import com.diego.citas.dto.CitaRequest;
import com.diego.citas.dto.CitaResponse;
import com.diego.citas.entity.Cita;
import com.diego.commons.dto.medicos.DatosMedico;
import com.diego.commons.dto.medicos.MedicoResponse;
import com.diego.commons.dto.pacientes.DatosPaciente;
import com.diego.commons.dto.pacientes.PacienteResponse;
import com.diego.commons.mapper.CommonMapper;
import org.springframework.stereotype.Component;

@Component
public class CitaMapper implements CommonMapper<CitaRequest, CitaResponse, Cita> {
    @Override
    public Cita requestAEntidad(CitaRequest request) {
        return request == null ? null
                : Cita.crear(
                request.idPaciente(),
                request.idMedico(),
                request.fechaCita(),
                request.sintomas()
        );
    }

    @Override
    public CitaResponse entidadAResponse(Cita cita) {
        return cita == null ? null
                : new CitaResponse(
                cita.getId(),
                null,
                null,
                cita.getFechaCita(),
                cita.getSintomas(),
                cita.getEstadoCita().getDescripcion()
        );
    }

    public CitaResponse entidadAResponse(Cita cita, PacienteResponse pacienteResponse, MedicoResponse medicoResponse) {
        return cita == null ? null
                : new CitaResponse(
                cita.getId(),
                pacienteResponseADatosPaciente(pacienteResponse),
                medicoResponseADatosMedico(medicoResponse),
                cita.getFechaCita(),
                cita.getSintomas(),
                cita.getEstadoCita().getDescripcion()
        );
    }

    private DatosPaciente pacienteResponseADatosPaciente(PacienteResponse pacienteResponse) {
        return pacienteResponse == null ? null
                : new DatosPaciente(
                pacienteResponse.nombre(),
                pacienteResponse.numExpediente(),
                pacienteResponse.edad() + " años",
                pacienteResponse.peso() + " kg.",
                pacienteResponse.estatura() + " m.",
                String.join(" ",
                        Math.round(pacienteResponse.imc() * 100.0) / 100.0 + "",
                        clasificacionIMC(pacienteResponse.imc())
                ),
                pacienteResponse.telefono()
        );
    }

    private String clasificacionIMC(Double imc) {
        if (imc < 18.5)
            return "Bajo peso";
        if (imc < 25)
            return "Peso normal";
        if (imc < 30)
            return "Sobrepeso";
        if (imc < 35)
            return "Obesidad grado I";
        if (imc < 40)
            return "Obesidad grado II";

        return "Obesidad grado III";
    }

    private DatosMedico medicoResponseADatosMedico(MedicoResponse medicoResponse) {
        return medicoResponse == null ? null
                : new DatosMedico(
                medicoResponse.nombre(),
                medicoResponse.cedulaProfesional(),
                medicoResponse.especialidad()
        );
    }
}