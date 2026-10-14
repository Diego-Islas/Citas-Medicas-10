package com.diego.pacientes.services;

import com.diego.commons.clients.CitaClient;
import com.diego.commons.dto.pacientes.PacienteRequest;
import com.diego.commons.dto.pacientes.PacienteResponse;
import com.diego.commons.enums.EstadoRegistro;
import com.diego.commons.exceptions.ConflictoException;
import com.diego.commons.exceptions.RecursoNoEncontradoException;
import com.diego.pacientes.entities.Paciente;
import com.diego.pacientes.mapper.PacienteMapper;
import com.diego.pacientes.repositories.PacienteRepository;
import com.diego.pacientes.utils.ServiceUtils;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PacienteServiceImpl implements PacienteService {
    private final PacienteRepository pacienteRepository;
    private final PacienteMapper pacienteMapper;

    private final CitaClient citaClient;

    @Override
    @Transactional(readOnly = true)
    public List<PacienteResponse> listar() {
        log.info("Listando pacientes...");
        return pacienteRepository.findAllByEstadoRegistro(EstadoRegistro.ACTIVO).stream()
                .map(pacienteMapper::entidadAResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PacienteResponse obtenerPorId(Long id) {
        return pacienteMapper.entidadAResponse(obtenerPacienteActivo(id));
    }

    @Override
    public PacienteResponse obtenerPorIdSinEstado(Long id) {
        return pacienteMapper.entidadAResponse(obtenerPaciente(id));
    }

    @Override
    public PacienteResponse registrar(PacienteRequest request) {
        validarRegistroUnico(request);

        Paciente paciente = pacienteMapper.requestAEntidad(request);

        pacienteRepository.saveAndFlush(paciente);

        log.info("Paciente creado con id: {}", paciente.getId());

        return pacienteMapper.entidadAResponse(paciente);
    }

    @Override
    public PacienteResponse actualizar(PacienteRequest request, Long id) {
        validarActualizarUnico(request, id);

        Paciente paciente = obtenerPacienteActivo(id);

        validarPacienteEliminableOActualizable(id);

        paciente.actualizar(
                request.nombre(),
                request.apellidoPaterno(),
                request.apellidoMaterno(),
                request.edad(),
                request.peso(),
                request.estatura(),
                request.email(),
                request.telefono(),
                request.direccion()
        );
        pacienteRepository.saveAndFlush(paciente);

        log.info("Paciente actualizado con id: {}", id);


        return pacienteMapper.entidadAResponse(paciente);
    }

    @Override
    public void eliminar(Long id) {
        Paciente paciente = obtenerPacienteActivo(id);

        validarPacienteEliminableOActualizable(id);

        paciente.eliminar();

        pacienteRepository.saveAndFlush(paciente);

        log.info("Paciente eliminado con id:{}", id);
    }

    private void validarPacienteEliminableOActualizable(Long id) {
        try {
            citaClient.pacienteTieneCitaActiva(id);
        } catch (FeignException.Conflict e) {
            throw new ConflictoException("El paciente con id: " + id + " tiene citas activas y no puede ser eliminado o actualizado");
        }
    }

    private Paciente obtenerPaciente(Long id) {
        return ServiceUtils.obtenerEntidadOExcepcion(
                pacienteRepository,
                id,
                Paciente.class
        );
    }

    private Paciente obtenerPacienteActivo(Long id) {
        log.info("Obteniendo paciente activo con id: {}", id);
        return pacienteRepository.findByIdAndEstadoRegistro(
                id, EstadoRegistro.ACTIVO
        ).orElseThrow(() ->
                new RecursoNoEncontradoException(
                        "Paciente no encontrado con id: " + id
                )
        );
    }

    private void validarRegistroUnico(PacienteRequest request) {
        if (pacienteRepository.existsByEmailIgnoreCaseOrTelefonoAndEstadoRegistro(
                request.email().toLowerCase(),
                request.telefono(),
                EstadoRegistro.ACTIVO
        ))
            throw new ConflictoException("Ya existe un paciente registrado con el mismo correo electrónico y número de teléfono");
    }

    private void validarActualizarUnico(PacienteRequest request, Long id) {
        if (pacienteRepository.existsByEmailIgnoreCaseOrTelefonoAndEstadoRegistroAndIdNot(
                request.email().toLowerCase(),
                request.telefono(),
                EstadoRegistro.ACTIVO,
                id
        ))
            throw new ConflictoException("Ya existe un paciente con el mismo correo electrónico y número de teléfono");
    }
}