package com.diego.citas.service;

import com.diego.citas.Utils.ServiceUtils;
import com.diego.citas.dto.CitaRequest;
import com.diego.citas.dto.CitaResponse;
import com.diego.citas.entity.Cita;
import com.diego.citas.enums.EstadoCita;
import com.diego.citas.mapper.CitaMapper;
import com.diego.citas.repository.CitaRepository;
import com.diego.commons.clients.MedicoClient;
import com.diego.commons.clients.PacienteClient;
import com.diego.commons.dto.medicos.MedicoResponse;
import com.diego.commons.dto.pacientes.PacienteResponse;
import com.diego.commons.enums.DisponibilidadMedico;
import com.diego.commons.enums.EstadoRegistro;
import com.diego.commons.exceptions.ConflictoException;
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
    private final PacienteClient pacienteClient;

    @Override
    @Transactional(readOnly = true)
    public List<CitaResponse> listar() {
        log.info("Listando todas las citas activas");
        return citaRepository.findByEstadoRegistro(EstadoRegistro.ACTIVO).stream()
                .map(cita ->
                        citaMapper.entidadAResponse(
                                cita,
                                obtenerPaciente(cita.getIdPaciente()),
                                obtenerMedico(cita.getIdMedico())
                        )
                ).toList();
    }

    @Override
    public CitaResponse obtenerPorId(Long id) {
        Cita cita = obtenerCita(id);
        log.info("Obteniendo cita con id: {}", id);
        return citaMapper.entidadAResponse(
                cita,
                obtenerPacienteActivo(cita.getIdPaciente()),
                obtenerMedico(cita.getIdMedico())
        );
    }

    @Override
    public CitaResponse registrar(CitaRequest request) {
        log.info("Registrando nueva cita: {}", request);
        MedicoResponse medico = obtenerMedicoActivo(request.idMedico());

        validarMedicoDisponible(medico);

        PacienteResponse paciente = obtenerPacienteActivo(request.idPaciente());

        validarPacienteNoTengaCitasActivas(request.idPaciente());

        Cita cita = citaMapper.requestAEntidad(request);

        citaRepository.saveAndFlush(cita);

        cambiarDisponibilidadMedicoSegunEstadoCita(medico.id(), cita.getEstadoCita());
        return citaMapper.entidadAResponse(cita,
                paciente,
                medico
        );
    }

    @Override
    public CitaResponse actualizar(CitaRequest request, Long id) {
        log.info("Actualizando cita {}: {}", id, request);

        Cita cita = obtenerCita(id);
        cita.validarActualizacionPermitida();

        if (cita.getEstadoCita() == EstadoCita.EN_CURSO)
            throw new ConflictoException("No se puede actualizar la cita con id: " + id + " porque esta en curso");

        Long idMedicoAnterior = cita.getIdMedico();
        MedicoResponse medico = obtenerMedico(idMedicoAnterior);
        PacienteResponse paciente = obtenerPaciente(cita.getIdPaciente());
        boolean cambiaMedico = !idMedicoAnterior.equals(request.idMedico());

        if (cambiaMedico) {
            medico = obtenerMedicoActivo(request.idMedico());
            validarMedicoDisponible(medico);
        }

        if (!cita.getIdPaciente().equals(request.idPaciente())) {
            log.info("El paciente de la cita con id: {} ha cambiado de {} a {}", id, cita.getIdPaciente(), request.idPaciente());
            paciente = obtenerPacienteActivo(request.idPaciente());
            validarPacienteNoTengaCitasActivas(paciente.id());
        }

        cita.actualizar(request.idPaciente(), request.idMedico(), request.fechaCita(), request.sintomas());
        citaRepository.saveAndFlush(cita);

        if (cambiaMedico) {
            actualizarDisponibilidadMedico(idMedicoAnterior, DisponibilidadMedico.DISPONIBLE.getCodigo());
            actualizarDisponibilidadMedico(medico.id(), DisponibilidadMedico.NO_DISPONIBLE.getCodigo());
        }

        return citaMapper.entidadAResponse(cita, paciente, medico);
    }

    @Override
    public void actualizarEstadoCita(Long id, Long idEstado) {
        Cita cita = obtenerCita(id);

        log.info("Actualizando estado de la cita con id: {} a estado con id: {}", id, idEstado);

        cita.actualizarEstadoCita(EstadoCita.obtenerEstadoCitaPorCodigo(idEstado));

        citaRepository.saveAndFlush(cita);

        cambiarDisponibilidadMedicoSegunEstadoCita(cita.getIdMedico(), cita.getEstadoCita());

        log.info("Estado de la cita con id: {} actualizado a estado con id: {}", id, idEstado);
    }

    @Override
    public void medicoConCitaActiva(Long idMedico) {
        log.info("Validando que el medico con id: {} no tenga citas activas", idMedico);

        if (citaRepository.existsByIdMedicoAndEstadoRegistroAndEstadoCitaIn(
                idMedico,
                EstadoRegistro.ACTIVO,
                List.of(
                        EstadoCita.PENDIENTE,
                        EstadoCita.CONFIRMADA,
                        EstadoCita.EN_CURSO
                )
        ))
            throw new ConflictoException("El medico con id: " + idMedico + " ya tiene una cita activa");
    }

    @Override
    public void pacienteConCitaActiva(Long idPaciente) {
        log.info("Validando que el paciente con id: {} no tenga citas activas", idPaciente);

        if (citaRepository.existsByIdPacienteAndEstadoRegistroAndEstadoCitaIn(
                idPaciente,
                EstadoRegistro.ACTIVO,
                List.of(
                        EstadoCita.PENDIENTE,
                        EstadoCita.CONFIRMADA,
                        EstadoCita.EN_CURSO
                )
        ))
            throw new ConflictoException("El paciente con id: " + idPaciente + " ya tiene una cita activa");
    }

    @Override
    public void eliminar(Long id) {
        Cita cita = obtenerCita(id);

        log.info("Eliminando cita con id: {}", id);

        cita.eliminar();

        citaRepository.saveAndFlush(cita);

        if (cita.getEstadoCita() == EstadoCita.PENDIENTE)
            actualizarDisponibilidadMedico(
                    cita.getIdMedico(),
                    DisponibilidadMedico.DISPONIBLE.getCodigo()
            );

        log.info("Cita con id: {} eliminada", id);
    }

    private Cita obtenerCita(Long id) {
        return ServiceUtils.obtenerEntidadOExcepcion(
                citaRepository,
                id,
                Cita.class
        );
    }

    private MedicoResponse obtenerMedicoActivo(Long id) {
        log.info("Obteniendo medico activo con id: {}", id);
        return medicoClient.obtenerMedicoPorId(id).getBody();
    }

    private MedicoResponse obtenerMedico(Long id) {
        log.info("Obteniendo medico con id: {}", id);
        return medicoClient.obtenerMedicoPorIdSinEstado(id).getBody();
    }

    private PacienteResponse obtenerPacienteActivo(Long id) {
        log.info("Obteniendo paciente con id: {}", id);
        return pacienteClient.obtenerPacientePorIdActivo(id).getBody();
    }

    private PacienteResponse obtenerPaciente(Long id) {
        log.info("Obteniendo paciente sin estado con id: {}", id);
        return pacienteClient.obtenerPorIdSinEstado(id).getBody();
    }

    private void validarMedicoDisponible(MedicoResponse medico) {
        log.info("Validando que el medico con id: {} este activo y disponible", medico.id());

        if (!DisponibilidadMedico.DISPONIBLE.getCodigo().equals(medico.idDisponibilidad()))
            throw new ConflictoException("El medico con id: " + medico.id() + " no esta disponible");
    }

    private void validarPacienteNoTengaCitasActivas(Long idPaciente) {
        log.info("Validando que el paciente con id: {} este activo", idPaciente);

        if (citaRepository.existsByIdPacienteAndEstadoRegistroAndEstadoCitaIn(
                idPaciente,
                EstadoRegistro.ACTIVO,
                List.of(
                        EstadoCita.PENDIENTE,
                        EstadoCita.CONFIRMADA,
                        EstadoCita.EN_CURSO
                )
        ))
            throw new ConflictoException("El paciente con id: " + idPaciente + " ya tiene una cita activa");
    }

    private void actualizarDisponibilidadMedico(Long idMedico, Long idDisponibilidad) {
        log.info("Actualizando disponibilidad del medico con id: {} a {}", idMedico, idDisponibilidad);
        medicoClient.actualizarDisponibilidadMedicoPorCita(idMedico, idDisponibilidad);
        log.info("Disponibilidad del medico con id: {} actualizada a {}", idMedico, idDisponibilidad);
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