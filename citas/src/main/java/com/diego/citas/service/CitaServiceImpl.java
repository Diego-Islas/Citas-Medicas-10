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

    private static final List<EstadoCita> ESTADOS_ACTIVOS_CITA = List.of(
            EstadoCita.PENDIENTE,
            EstadoCita.CONFIRMADA,
            EstadoCita.EN_CURSO
    );

    @Override
    @Transactional(readOnly = true)
    public List<CitaResponse> listar() {
        log.info("Listando citas activas");

        return citaRepository.findByEstadoRegistro(EstadoRegistro.ACTIVO).stream()
                .map(cita -> citaMapper.entidadAResponse(
                        cita,
                        obtenerPaciente(cita.getIdPaciente()),
                        obtenerMedico(cita.getIdMedico())
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CitaResponse obtenerPorId(Long id) {
        Cita cita = obtenerCita(id);

        return citaMapper.entidadAResponse(
                cita,
                obtenerPaciente(cita.getIdPaciente()),
                obtenerMedico(cita.getIdMedico())
        );
    }

    @Override
    public CitaResponse registrar(CitaRequest request) {
        log.info(
                "Registrando cita para el paciente ID: {} y médico ID: {}",
                request.idPaciente(),
                request.idMedico()
        );

        MedicoResponse medico = obtenerMedicoActivo(request.idMedico());

        validarMedicoDisponible(medico);

        PacienteResponse paciente = obtenerPacienteActivo(request.idPaciente());

        validarPacienteNoTengaCitasActivas(paciente.id());

        Cita cita = citaMapper.requestAEntidad(request);

        citaRepository.saveAndFlush(cita);

        cambiarDisponibilidadMedicoSegunEstadoCita(
                medico.id(),
                cita.getEstadoCita()
        );

        log.info("Cita registrada con ID: {}", cita.getId());

        return citaMapper.entidadAResponse(
                cita,
                paciente,
                medico
        );
    }

    @Override
    public CitaResponse actualizar(CitaRequest request, Long id) {
        Cita cita = obtenerCita(id);

        if (cita.getEstadoCita() == EstadoCita.EN_CURSO) {
            throw new ConflictoException(
                    "La cita con ID " + id + " está en curso y no puede actualizarse"
            );
        }

        Long idMedicoAnterior = cita.getIdMedico();

        boolean cambioMedico = !idMedicoAnterior.equals(request.idMedico());
        boolean cambioPaciente = !cita.getIdPaciente().equals(request.idPaciente());

        MedicoResponse medico = cambioMedico
                ? obtenerMedicoActivo(request.idMedico())
                : obtenerMedico(idMedicoAnterior);

        if (cambioMedico) {
            validarMedicoDisponible(medico);
        }

        PacienteResponse paciente = obtenerPacienteActivo(request.idPaciente());

        if (cambioPaciente) {
            validarPacienteNoTengaCitasActivas(paciente.id());
        }

        log.info("Actualizando cita con ID: {}", id);

        cita.actualizar(
                request.idPaciente(),
                request.idMedico(),
                request.fechaCita(),
                request.sintomas()
        );

        citaRepository.saveAndFlush(cita);

        if (cambioMedico && ESTADOS_ACTIVOS_CITA.contains(cita.getEstadoCita())) {
            actualizarDisponibilidadMedico(
                    idMedicoAnterior,
                    DisponibilidadMedico.DISPONIBLE.getCodigo()
            );

            cambiarDisponibilidadMedicoSegunEstadoCita(
                    medico.id(),
                    cita.getEstadoCita()
            );
        }

        log.info("Cita actualizada con ID: {}", id);

        return citaMapper.entidadAResponse(
                cita,
                paciente,
                medico
        );
    }

    @Override
    public void actualizarEstadoCita(Long id, Long idEstado) {
        Cita cita = obtenerCita(id);

        log.info(
                "Actualizando estado de la cita con ID: {} al estado ID: {}",
                id,
                idEstado
        );

        cita.actualizarEstadoCita(
                EstadoCita.obtenerEstadoCitaPorCodigo(idEstado)
        );

        citaRepository.saveAndFlush(cita);

        cambiarDisponibilidadMedicoSegunEstadoCita(
                cita.getIdMedico(),
                cita.getEstadoCita()
        );

        log.info("Estado de la cita con ID: {} actualizado", id);
    }

    @Override
    public void eliminar(Long id) {
        Cita cita = obtenerCita(id);

        log.info("Eliminando cita con ID: {}", id);

        cita.eliminar();

        citaRepository.saveAndFlush(cita);

        if (cita.getEstadoCita() == EstadoCita.PENDIENTE) {
            actualizarDisponibilidadMedico(
                    cita.getIdMedico(),
                    DisponibilidadMedico.DISPONIBLE.getCodigo()
            );
        }

        log.info("Cita con ID: {} eliminada", id);
    }

    @Override
    public void pacienteTieneCitaActiva(Long idPaciente) {
        validarPacienteNoTengaCitasActivas(idPaciente);
    }

    @Override
    public void medicoTieneCitaActiva(Long idMedico) {
        log.info("Validando citas activas del médico con ID: {}", idMedico);

        if (citaRepository.existsByIdMedicoAndEstadoRegistroAndEstadoCitaIn(
                idMedico,
                EstadoRegistro.ACTIVO,
                ESTADOS_ACTIVOS_CITA
        )) {
            throw new ConflictoException(
                    "El médico con ID " + idMedico + " tiene citas activas"
            );
        }
    }

    private void validarPacienteNoTengaCitasActivas(Long idPaciente) {
        log.info("Validando citas activas del paciente con ID: {}", idPaciente);

        if (citaRepository.existsByIdPacienteAndEstadoRegistroAndEstadoCitaIn(
                idPaciente,
                EstadoRegistro.ACTIVO,
                ESTADOS_ACTIVOS_CITA
        )) {
            throw new ConflictoException(
                    "El paciente con ID " + idPaciente + " ya tiene una cita activa"
            );
        }
    }

    private Cita obtenerCita(Long id) {
        return ServiceUtils.obtenerEntidadOExcepcion(
                citaRepository,
                id,
                Cita.class
        );
    }

    private MedicoResponse obtenerMedicoActivo(Long id) {
        log.info("Consultando médico activo con ID: {}", id);

        return medicoClient.obtenerMedicoPorId(id).getBody();
    }

    private MedicoResponse obtenerMedico(Long id) {
        log.info("Consultando médico con ID: {}", id);

        return medicoClient.obtenerMedicoPorIdSinEstado(id).getBody();
    }

    private PacienteResponse obtenerPacienteActivo(Long id) {
        log.info("Consultando paciente activo con ID: {}", id);

        return pacienteClient.obtenerPacientePorIdActivo(id).getBody();
    }

    private PacienteResponse obtenerPaciente(Long id) {
        log.info("Consultando paciente con ID: {}", id);

        return pacienteClient.obtenerPorIdSinEstado(id).getBody();
    }

    private void validarMedicoDisponible(MedicoResponse medico) {
        log.info("Validando disponibilidad del médico con ID: {}", medico.id());

        if (!DisponibilidadMedico.DISPONIBLE.getCodigo()
                .equals(medico.idDisponibilidad())) {
            throw new ConflictoException(
                    "El médico con ID " + medico.id() + " no está disponible"
            );
        }
    }

    private void actualizarDisponibilidadMedico(
            Long idMedico,
            Long idDisponibilidad
    ) {
        log.info(
                "Actualizando disponibilidad del médico con ID: {}",
                idMedico
        );

        medicoClient.actualizarDisponibilidadMedicoPorCita(
                idMedico,
                idDisponibilidad
        );

        log.info(
                "Disponibilidad del médico con ID: {} actualizada",
                idMedico
        );
    }

    private void cambiarDisponibilidadMedicoSegunEstadoCita(
            Long idMedico,
            EstadoCita estadoCita
    ) {
        switch (estadoCita) {
            case PENDIENTE, CONFIRMADA -> actualizarDisponibilidadMedico(
                    idMedico,
                    DisponibilidadMedico.NO_DISPONIBLE.getCodigo()
            );

            case EN_CURSO -> actualizarDisponibilidadMedico(
                    idMedico,
                    DisponibilidadMedico.EN_CONSULTA.getCodigo()
            );

            case FINALIZADA, CANCELADA -> actualizarDisponibilidadMedico(
                    idMedico,
                    DisponibilidadMedico.DISPONIBLE.getCodigo()
            );
        }
    }
}