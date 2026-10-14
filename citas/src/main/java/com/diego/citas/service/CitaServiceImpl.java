package com.diego.citas.service;

import com.diego.citas.dto.CitaRequest;
import com.diego.citas.dto.CitaResponse;
import com.diego.citas.entity.Cita;
import com.diego.citas.enums.EstadoCita;
import com.diego.citas.mapper.CitaMapper;
import com.diego.citas.repository.CitasRepository;
import com.diego.commons.dto.medicos.MedicoResponse;
import com.diego.commons.enums.DisponibilidadMedico;
import com.diego.commons.enums.EstadoRegistro;
import com.diego.commons.exceptions.ConflictoException;
import com.diego.commons.exceptions.RecursoNoEncontradoException;
import com.diego.commons.services.MedicoClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CitaServiceImpl implements CitaService {

    private final CitasRepository citaRepository;
    private final CitaMapper citaMapper;
    private final MedicoClient medicoClient;

    @Override
    @Transactional(readOnly = true)
    public List<CitaResponse> listar() {
        log.info("Listando todas las citas activas");

        return citaRepository.findByEstadoRegistro(EstadoRegistro.ACTIVO)
                .stream()
                .map(cita -> citaMapper.entidadAResponse(cita, null, obtenerMedicoSinEstadoPorId(cita.getIdMedico())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CitaResponse obtenerPorId(Long id) {
        Cita cita = obtenerCitaOException(id);

        return citaMapper.entidadAResponse(cita, null, obtenerMedicoSinEstadoPorId(cita.getIdMedico()));
    }

    @Override
    public CitaResponse registrar(CitaRequest request) {
        log.info("Registrando nueva cita");

        MedicoResponse medico = obtenerMedicoActivoPorId(request.idMedico());
        validarMedicoActivoDisponible(medico);

        Cita cita = citaMapper.requestAEntidad(request);

        citaRepository.saveAndFlush(cita);

        log.info("Cita registrada exitosamente");

        cambiarDisponibilidadMedicoSegunEstadoCita(medico.);

        return citaMapper.entidadAResponse(cita, null, medico);
    }

    @Override
    public CitaResponse actualizar(CitaRequest request, Long id) {
        log.info("Actualizando cita con id: {}", id);

        Cita cita = obtenerCitaOException(id);

        if (cita.getEstadoCita() == EstadoCita.EN_CURSO) {
            throw new ConflictoException(
                    "La cita está en curso y no se puede actualizar"
            );
        }

        MedicoResponse medico = obtenerMedicoActivoPorId(request.idMedico());

        cita.actualizar(
                request.idPaciente(),
                request.idMedico(),
                request.fechaCita(),
                request.sintomas()
        );

        cita = citaRepository.saveAndFlush(cita);

        log.info("Cita actualizada exitosamente con id: {}", id);

        cambiarDisponibilidadMedicoSegunEstadoCita(
                cita.getIdMedico(),
                cita.getEstadoCita()
        );

        return citaMapper.entidadAResponse(cita, null, medico);
    }

    @Override
    public void eliminar(Long id) {
        Cita cita = obtenerCitaOException(id);

        log.info("Eliminando cita con id: {}", id);

        cita.eliminar();

        if (cita.getEstadoCita() == EstadoCita.PENDIENTE) {
            actualizarDisponibilidadMedico(
                    cita.getIdMedico(),
                    DisponibilidadMedico.DISPONIBLE.getCodigo()
            );
        }

        log.info("Cita con id: {} ha sido marcada como eliminada", id);
    }

    @Override
    public void actualizarEstadoCita(Long idCita, Long idEstadoCita) {
        Cita cita = obtenerCitaOException(idCita);

        log.info("Actualizando estado de la cita con id: {}", idCita);

        cita.actualizarEstado(
                EstadoCita.obtenerEstadoCitaPorCodigo(idEstadoCita)
        );

        cita = citaRepository.saveAndFlush(cita);

        cambiarDisponibilidadMedicoSegunEstadoCita(
                cita.getIdMedico(),
                cita.getEstadoCita()
        );

        log.info("Estado de la cita actualizado correctamente");
    }

    private Cita obtenerCitaOException(Long id) {
        log.info("Buscando cita con id: {}", id);

        return citaRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Cita no encontrada con id: " + id
                        )
                );
    }

    private MedicoResponse obtenerMedicoActivoPorId(Long id) {
        log.info("Buscando médico activo con id: {}", id);

        return medicoClient.obtenerMedicoActivoPorId(id);
    }

    private MedicoResponse obtenerMedicoSinEstadoPorId(Long id) {
        log.info("Buscando médico sin validar estado con id: {}", id);

        return medicoClient.obtenerMedicosSinEstadoPorId(id);
    }

    private void validarMedicoActivoDisponible(MedicoResponse medico) {
        log.info("Validando si el médico activo está disponible para consulta");

        if (!DisponibilidadMedico.DISPONIBLE.getCodigo()
                .equals(medico.idDisponibilidad())) {
            throw new ConflictoException(
                    "El médico activo no está disponible para consulta"
            );
        }
    }


    private void actualizarDisponibilidadMedico(Long idMedico, Long idDisponibilidad) {
        log.info("Actualizando disponibilidad del médico con id: {}", idMedico);

        medicoClient.actualizarDisponibilidadMedico(idMedico, idDisponibilidad);

        log.info("Disponibilidad del médico actualizada exitosamente");
    }

    private void cambiarDisponibilidadMedicoSegunEstadoCita(
            Long idMedico, EstadoCita estadoCita) {

        switch (estadoCita) {
            case PENDIENTE, CONFIRMADA ->
                    actualizarDisponibilidadMedico(
                            idMedico,
                            DisponibilidadMedico.NO_DISPONIBLE.getCodigo()
                    );

            case EN_CURSO ->
                    actualizarDisponibilidadMedico(
                            idMedico,
                            DisponibilidadMedico.EN_CONSULTA.getCodigo()
                    );

            case FINALIZADA, CANCELADA ->
                    actualizarDisponibilidadMedico(
                            idMedico,
                            DisponibilidadMedico.DISPONIBLE.getCodigo()
                    );
        }
    }
}