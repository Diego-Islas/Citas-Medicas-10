package com.diego.medicos.services;

import com.diego.commons.clients.CitaClient;
import com.diego.commons.dto.medicos.MedicoRequest;
import com.diego.commons.dto.medicos.MedicoResponse;
import com.diego.commons.enums.DisponibilidadMedico;
import com.diego.commons.enums.EspecialidadMedico;
import com.diego.commons.enums.EstadoRegistro;
import com.diego.commons.exceptions.ConflictoException;
import com.diego.commons.exceptions.RecursoNoEncontradoException;
import com.diego.medicos.entities.Medico;
import com.diego.medicos.mapper.MedicoMapper;
import com.diego.medicos.repositories.MedicoRepository;
import com.diego.medicos.utils.ServiceUtils;
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
public class MedicoServiceImpl implements MedicoService {
    private final MedicoRepository medicoRepository;
    private final MedicoMapper medicoMapper;

    private final CitaClient citaClient;

    @Override
    @Transactional(readOnly = true)
    public List<MedicoResponse> listar() {
        log.info("Listando medicos activos...");
        return medicoRepository.findByEstadoRegistro(EstadoRegistro.ACTIVO).stream()
                .map(medicoMapper::entidadAResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MedicoResponse obtenerPorId(Long id) {
        return medicoMapper.entidadAResponse(obtenerMedicoActivo(id));
    }

    @Override
    public MedicoResponse registrar(MedicoRequest request) {
        validarRegistroUnico(request);

        Medico medico = medicoMapper.requestAEntidad(
                request,
                EspecialidadMedico.obtenerEspecialidadPorCodigo(request.idEspecialidad())
        );

        medicoRepository.saveAndFlush(medico);
        log.info("Medico registrado con id: {}", medico.getId());

        return medicoMapper.entidadAResponse(medico);
    }

    @Override
    public MedicoResponse actualizar(MedicoRequest request, Long id) {
        validarCambiosUnico(request, id);

        Medico medico = obtenerMedicoActivo(id);

        validarMedicoEliminableOActualizable(id);

        EspecialidadMedico especialidadMedico = EspecialidadMedico.obtenerEspecialidadPorCodigo(
                request.idEspecialidad()
        );

        medico.actualizar(
                request.nombre(),
                request.apellidoPaterno(),
                request.apellidoMaterno(),
                request.edad(),
                request.email(),
                request.telefono(),
                request.cedulaProfesional(),
                especialidadMedico
        );

        medicoRepository.saveAndFlush(medico);
        log.info("Medico actualizado con id: {}", medico.getId());

        return medicoMapper.entidadAResponse(medico);
    }

    @Override
    public void eliminar(Long id) {
        Medico medico = obtenerMedicoActivo(id);

        validarMedicoEliminableOActualizable(id);

        log.info("Crucificando medico con id: {}", id);
        medico.eliminar();
        medicoRepository.saveAndFlush(medico);
        log.info("Medico ha sido crucificado exitosamente!!!");
    }

    @Override
    public MedicoResponse obtenerMedicoPorIdSinEstado(Long id) {
        return medicoMapper.entidadAResponse(obtenerMedico(id));
    }

    @Override
    public void actualizarDisponibilidadMedico(Long id, Long idDisponibilidad) {
        Medico medico = obtenerMedicoActivo(id);
        validarMedicoEliminableOActualizable(id);
        asignarDisponibilidad(medico, idDisponibilidad);
    }

    @Override
    public void actualizarDisponibilidadPorCita(Long id, Long idDisponibilidad) {
        // sin estado: el médico pudo ser eliminado teniendo una cita PENDIENTE
        asignarDisponibilidad(obtenerMedico(id), idDisponibilidad);
    }

    private void asignarDisponibilidad(Medico medico, Long idDisponibilidad) {
        DisponibilidadMedico anterior = medico.getDisponibilidadMedico();
        DisponibilidadMedico nueva = DisponibilidadMedico.obtenerDisponibilidadPorCodigo(idDisponibilidad);

        medico.asignarDisponibilidadMedico(nueva);
        medicoRepository.saveAndFlush(medico);

        log.info("Disponibilidad del medico con id {} actualizada de {} a {}",
                medico.getId(), anterior.getDescripcion(), nueva.getDescripcion());
    }

    private void validarMedicoEliminableOActualizable(Long id){
        try {
            citaClient.medicoTieneCitaActiva(id);
        } catch (FeignException.Conflict e){
            throw new ConflictoException("El medico con id: " + id + " tiene citas activas y no puede ser eliminado o actualizado");
        }
    }

    private Medico obtenerMedico(Long id){
        return ServiceUtils.obtenerEntidadOExcepcion(
                medicoRepository,
                id,
                Medico.class
        );
    }

    private Medico obtenerMedicoActivo(Long id){
        log.info("Obteniendo medico activo con id: {}", id);
        return medicoRepository.findByIdAndEstadoRegistro(
                id,
                EstadoRegistro.ACTIVO
        ).orElseThrow(() ->
                new RecursoNoEncontradoException("El medico con id " + id + " no existe o no se encuentra activo")
        );
    }

    private void validarRegistroUnico(MedicoRequest request){
        log.info("Validando email unico...");
        if (medicoRepository.existsByEmailIgnoreCaseAndEstadoRegistro(
                request.email(), EstadoRegistro.ACTIVO
        ))
            throw new ConflictoException("El email " + request.email() + " ya se encuentra registrado");

        log.info("Validando telefono unico...");
        if (medicoRepository.existsByTelefonoAndEstadoRegistro(
                request.telefono(), EstadoRegistro.ACTIVO
        ))
            throw new ConflictoException("El telefono " + request.telefono() + " ya se encuentra registrado");

        log.info("Validando cedula profesional unica...");
        if (medicoRepository.existsByCedulaProfesionalIgnoreCaseAndEstadoRegistro(
                request.cedulaProfesional(), EstadoRegistro.ACTIVO
        ))
            throw new ConflictoException("La cedula profesional " + request.cedulaProfesional() + " ya se encuentra registrada");
    }

    private void validarCambiosUnico(MedicoRequest request, Long id){
        log.info("Validando email unico para actualizar...");
        if (medicoRepository.existsByEmailIgnoreCaseAndEstadoRegistroAndIdNot(
                request.email(), EstadoRegistro.ACTIVO, id
        ))
            throw new ConflictoException("El email " + request.email() + " ya se encuentra registrado");

        log.info("Validando telefono unico para actualizar...");
        if (medicoRepository.existsByTelefonoAndEstadoRegistroAndIdNot(
                request.telefono(), EstadoRegistro.ACTIVO, id
        ))
            throw new ConflictoException("El telefono " + request.telefono() + " ya se encuentra registrado");

        log.info("Validando cedula profesional unica para actualizar...");
        if (medicoRepository.existsByCedulaProfesionalIgnoreCaseAndEstadoRegistroAndIdNot(
                request.cedulaProfesional(), EstadoRegistro.ACTIVO, id
        ))
            throw new ConflictoException("La cedula profesional " + request.cedulaProfesional() + " ya se encuentra registrada");
    }
}