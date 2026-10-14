
package com.diego.citas.entity;

import com.diego.citas.enums.EstadoCita;
import com.diego.commons.enums.EstadoRegistro;
import com.diego.commons.exceptions.ConflictoException;
import com.diego.commons.exceptions.DatoInvalidoException;
import com.diego.commons.utils.StringCustomUtils;
import com.diego.commons.utils.ValoresNumericosUtils;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "CITAS")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class Cita {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CITA", nullable = false)
    private Long id;

    @Column(name = "ID_PACIENTE", nullable = false)
    private Long idPaciente;

    @Column(name = "ID_MEDICO", nullable = false)
    private Long idMedico;

    @Column(name = "FECHA_CITA", nullable = false)
    private LocalDateTime fechaCita;

    @Column(name = "SINTOMAS", nullable = false)
    private String sintomas;

    @Column(name = "ESTADO_CITA", nullable = false)
    @Enumerated(EnumType.STRING)
    private EstadoCita estadoCita;

    @Column(name = "ESTADO_REGISTRO", nullable = false)
    @Enumerated(EnumType.STRING)
    private EstadoRegistro estadoRegistro;

    private void validarNoEliminada() {
        if (this.estadoRegistro.equals(EstadoRegistro.ELIMINADO))
            throw new ConflictoException("La cita con id " + this.id + " ya ha sido eliminada");
    }

    private static void validarId(Long id, String campo) {
        ValoresNumericosUtils.validarPositivo(id, "El id del " + campo + " es requerido y debe ser positivo");
    }

    private static void validarFecha(LocalDateTime fechaCita) {
        if (fechaCita == null || !fechaCita.isAfter(LocalDateTime.now()))
            throw new DatoInvalidoException("La fecha de la cita es requerida y debe ser posterior a la fecha actual");
    }

    public static void validarDatos(
            Long idPaciente,
            Long idMedico,
            LocalDateTime fechaCita,
            String sintomas
    ) {
        validarId(idPaciente, "paciente");
        validarId(idMedico, "medico");

        validarFecha(fechaCita);

        StringCustomUtils.validarTamanio(
                sintomas, 20, 500,
                "Los síntomas de la cita son requeridos y deben tener entre 20 y 500 caracteres"
        );
    }

    public void validarEliminacionPermitida() {
        validarNoEliminada();
        if (!this.estadoCita.isEliminable())
            throw new ConflictoException("La cita con id " + this.id + " no puede ser eliminada porque su estado es " + this.estadoCita.getDescripcion());
    }

    public void validarActualizacionPermitida() {
        validarNoEliminada();
        if (!this.estadoCita.isActualizable())
            throw new ConflictoException("La cita con id " + this.id + " no puede ser actualizada porque su estado es " + this.estadoCita.getDescripcion());
    }

    public void actualizar(
            Long idPaciente,
            Long idMedico,
            LocalDateTime fechaCita,
            String sintomas
    ) {
        validarActualizacionPermitida();
        validarDatos(
                idPaciente,
                idMedico,
                fechaCita,
                sintomas.trim()
        );

        this.idPaciente = idPaciente;
        this.idMedico = idMedico;
        this.fechaCita = fechaCita;
        this.sintomas = sintomas.trim();
    }

    public void actualizarEstadoCita(EstadoCita nuevoEstadoCita) {
        validarActualizacionPermitida();

        if (nuevoEstadoCita == null)
            throw new DatoInvalidoException("El nuevo estado de la cita es requerido");

        if (!estadoCita.puedeCambiarA(nuevoEstadoCita))
            throw new ConflictoException("El estado de la cita con id " + this.id + " no puede cambiar de " + this.estadoCita.getDescripcion() + " a " + nuevoEstadoCita.getDescripcion());

        this.estadoCita = nuevoEstadoCita;
    }

    public void eliminar() {
        validarEliminacionPermitida();
        this.estadoRegistro = EstadoRegistro.ELIMINADO;
    }

    public static Cita crear(
            Long idPaciente,
            Long idMedico,
            LocalDateTime fechaCita,
            String sintomas
    ) {
        validarDatos(
                idPaciente,
                idMedico,
                fechaCita,
                sintomas.trim()
        );

        return Cita.builder()
                .idPaciente(idPaciente)
                .idMedico(idMedico)
                .fechaCita(fechaCita)
                .sintomas(sintomas.trim())
                .estadoCita(EstadoCita.PENDIENTE)
                .estadoRegistro(EstadoRegistro.ACTIVO)
                .build();
    }
}