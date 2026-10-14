package com.diego.citas.service;

import com.diego.citas.dto.CitaRequest;
import com.diego.citas.dto.CitaResponse;
import com.diego.citas.entity.Cita;
import com.diego.citas.enums.EstadoCita;
import com.diego.citas.mapper.CitaMapper;
import com.diego.citas.repository.CitaRepository;
import com.diego.commons.clients.MedicoClient;
import com.diego.commons.dto.medicos.MedicoResponse;
import com.diego.commons.enums.DisponibilidadMedico;
import com.diego.commons.enums.EstadoRegistro;
import com.diego.commons.exceptions.ConflictoException;
import com.diego.commons.exceptions.RecursoNoEncontradoException;
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

    private final CitaRepository citaRepository;

    private final CitaMapper citaMapper;

    private final MedicoClient medicoClient;

    @Override
    @Transactional(readOnly = true)
    public List<CitaResponse> listar() {

        log.info("Listando todas las citas activas");

        return citaRepository.findByEstadoRegistro(EstadoRegistro.ACTIVO).stream()
                .map(cita -> citaMapper.entidadAResponse(
                        cita,
                        null,
                        obtenerMedicoSinEstado(cita.getIdMedico())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CitaResponse obtenerPorId(Long id) {

        Cita cita = obtenerCitaOException(id);

        return citaMapper.entidadAResponse(
                cita,
                null,
                obtenerMedicoSinEstado(cita.getIdMedico()));
    }

    @Override
    public CitaResponse registrar(CitaRequest request) {

        log.info("Registrando nueva cita...");

        MedicoResponse medico = obtenerMedicoActivo(request.idMedico());

        validarMedicoActivoDisponible(medico);

        Cita cita = citaMapper.requestAEntidad(request);

        citaRepository.saveAndFlush(cita);

        log.info("Cita registrada exitósamente");

        cambiarDisponibilidadMedicoSegunEstadoCita(
                medico.id(), cita.getEstadoCita());

        return citaMapper.entidadAResponse(
                cita,
                null,
                medico);
    }

    @Override
    public CitaResponse actualizar(CitaRequest request, Long id) {

        Cita cita = obtenerCitaOException(id);

        if (cita.getEstadoCita() == EstadoCita.EN_CURSO)
            throw new ConflictoException(
                    "La cita está en curso y no puede actualizarse");

        MedicoResponse medico = obtenerMedicoActivo(request.idMedico());

        log.info("Actualizando cita con id: {}", id);

        cita.actualizar(
                request.idPaciente(),
                request.idMedico(),
                request.fechaCita(),
                request.sintomas()
        );

        citaRepository.saveAndFlush(cita);

        log.info("Cita actualizada con id: {}", id);

        return citaMapper.entidadAResponse(
                cita,
                null,
                medico
        );
    }

    @Override
    public void actualizarEstadoCita(Long idCita, Long idEstadoCita) {

        Cita cita = obtenerCitaOException(idCita);

        log.info("Actualizando estado de la cita con id: {}", idCita);

        cita.actualizarEstadoCita(
                EstadoCita.obtenerEstadoCitaPorCodigo(idEstadoCita));

        citaRepository.saveAndFlush(cita);

        cambiarDisponibilidadMedicoSegunEstadoCita(
                cita.getIdMedico(), cita.getEstadoCita());

        log.info("Estado de la cita {} actualizado correctamente", idCita);
    }

    @Override
    public void eliminar(Long id) {

        Cita cita = obtenerCitaOException(id);

        log.info("Eliminando cita con id: {}", id);

        cita.eliminar();

        if (cita.getEstadoCita() == EstadoCita.PENDIENTE)
            actualizarDisponibilidadMedico(cita.getIdMedico(),
                    DisponibilidadMedico.DISPONIBLE.getCodigo());

        log.info("Cita con id {} ha sido marcada como eliminada", id);
    }

    private Cita obtenerCitaOException(Long id) {

        log.info("Buscando cita con id: {}", id);

        return citaRepository.findById(id).orElseThrow(() ->
                new RecursoNoEncontradoException("Cita no encontrada con id: " + id));
    }

    private MedicoResponse obtenerMedicoActivo(Long id) {

        log.info("Buscando médico activo con id {} en el servicio remoto...", id);

        return medicoClient.obtenerMedicoActivoPorId(id);
    }

    private MedicoResponse obtenerMedicoSinEstado(Long id) {

        log.info("Buscando médico sin estado con id {} en el servicio remoto...", id);

        return medicoClient.obtenerMedicoSinEstadoPorId(id);
    }

    private void validarMedicoActivoDisponible(MedicoResponse medico) {

        log.info("Validando si el médico activo está disponible...");

        if (!DisponibilidadMedico.DISPONIBLE.getCodigo().equals(medico.idDisponibilidad()))
            throw new ConflictoException("El médico activo no está disponible para consulta");
    }

    private void actualizarDisponibilidadMedico(Long idMedico, Long idDisponibilidad) {

        log.info("Actualizando disponibilidad del médico en el servicio remoto...");

        medicoClient.actualizarDisponibilidadMedico(idMedico, idDisponibilidad);

        log.info("Disponibilidad del médico actualizada en el servicio remoto");
    }

    private void cambiarDisponibilidadMedicoSegunEstadoCita(Long idMedico, EstadoCita estadoCita) {

        switch (estadoCita) {

            case PENDIENTE, CONFIRMADA ->
                    actualizarDisponibilidadMedico(idMedico, DisponibilidadMedico.NO_DISPONIBLE.getCodigo());

            case EN_CURSO -> actualizarDisponibilidadMedico(idMedico, DisponibilidadMedico.EN_CONSULTA.getCodigo());

            case FINALIZADA, CANCELADA ->
                    actualizarDisponibilidadMedico(idMedico, DisponibilidadMedico.DISPONIBLE.getCodigo());
        }
    }
}