package com.diego.medicos.entities;

import com.diego.commons.enums.DisponibilidadMedico;
import com.diego.commons.enums.EspecialidadMedico;
import com.diego.commons.enums.EstadoRegistro;
import com.diego.commons.exceptions.ConflictoException;
import com.diego.commons.exceptions.DatoInvalidoException;
import com.diego.commons.utils.StringCustomUtils;
import com.diego.commons.utils.ValoresNumericosUtils;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "MEDICOS")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
public class Medico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_MEDICO ")
    private Long id;

    @Column(name = "NOMBRE", length = 50, nullable = false)
    private String nombre;

    @Column(name = "APELLIDO_PATERNO", length = 50, nullable = false)
    private String apellidoPaterno;

    @Column(name = "APELLIDO_MATERNO", length = 50, nullable = false)
    private String apellidoMaterno;

    @Column(name = "EDAD", nullable = false)
    private short edad;

    @Column(name = "EMAIL", length = 100, nullable = false)
    private String email;

    @Column(name = "TELEFONO", length = 10, nullable = false)
    private String telefono;

    @Column(name = "CEDULA_PROFESIONAL", length = 12, nullable = false)
    private String cedulaProfesional;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESPECIALIDAD", length = 10, nullable = false)
    private EspecialidadMedico especialidadMedico;

    @Enumerated(EnumType.STRING)
    @Column(name = "DISPONIBILIDAD", length = 10, nullable = false)
    private DisponibilidadMedico disponibilidadMedico;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO_REGISTRO", length = 10, nullable = false)
    private EstadoRegistro estadoRegistro;

    public static void validarDatos(
            String nombre,
            String apellidoPaterno,
            String apellidoMaterno,
            short edad,
            String email,
            String telefono,
            String cedulaProfesional
    ) {
        StringCustomUtils.validarTamanio(
                nombre, 1, 50,
                "El nombre es requerido y debe tener entre 5 y 50 caracteres"
        );
        StringCustomUtils.validarTamanio(
                apellidoPaterno, 1, 50,
                "El apellido paterno es requerido y debe tener entre 5 y 50 caracteres"
        );
        StringCustomUtils.validarTamanio(
                apellidoMaterno, 1, 50,
                "El apellido materno es requerido y debe tener entre 5 y 50 caracteres"
        );
        ValoresNumericosUtils.validarPositivo(
                edad,
                "La edad es requerida y debe ser positiva"
        );
        ValoresNumericosUtils.validarRango(
                edad, 18, 100,
                "La edad es requerida y debe estar entre 18 y 100 años"
        );
        StringCustomUtils.validarTamanio(
                email, 5, 100,
                "El email es requerido y debe contener entre 5 y 100 caracteres"
        );
        StringCustomUtils.validarTextoNumerico(
                telefono,
                "El telefonio es requerido y debe contener solo numeros"
        );
        StringCustomUtils.validarTamanio(
                cedulaProfesional, 12, 12,
                "La cedula profesional es requerida y debe contener exactamente 12 caracteres"
        );
    }

    public boolean cambiosEnDatos(
            String nombre,
            String apellidoPaterno,
            String apellidoMaterno,
            short edad,
            String email,
            String telefono,
            String cedulaProfesional,
            EspecialidadMedico especialidadMedico
    ) {
        validarDatos(
                nombre.trim(),
                apellidoPaterno.trim(),
                apellidoMaterno.trim(),
                edad,
                email.trim().toLowerCase(),
                telefono.trim(),
                cedulaProfesional.trim()
        );

        return !this.nombre.equals(nombre.trim()) ||
                !this.apellidoPaterno.equals(apellidoMaterno.trim()) ||
                !this.apellidoMaterno.equals(apellidoMaterno.trim()) ||
                this.edad != edad ||
                !this.telefono.equals(telefono.trim()) ||
                !this.cedulaProfesional.equals(cedulaProfesional.trim()) ||
                !this.especialidadMedico.equals(especialidadMedico);
    }

    public String obtenerNombreCompleto() {
        return this.nombre = this.nombre + " " + this.apellidoPaterno + " " + this.apellidoMaterno;
    }

    public void actualizar(
            String nombre,
            String apellidoPaterno,
            String apellidoMaterno,
            short edad,
            String email,
            String telefono,
            String cedulaProfesional,
            EspecialidadMedico especialidadMedico
    ) {
        validaNoEliminado();

        validarDatos(
                nombre.trim(),
                apellidoPaterno.trim(),
                apellidoMaterno.trim(),
                edad,
                email.trim().toLowerCase(),
                telefono.trim(),
                cedulaProfesional.trim()
        );

        asignarEspecialidadMedico(especialidadMedico);

        this.nombre = nombre.trim();
        this.apellidoPaterno = apellidoPaterno.trim();
        this.apellidoMaterno = apellidoMaterno.trim();
        this.edad = edad;
        this.email = email.toLowerCase().trim();
        this.telefono = telefono.trim();
        this.cedulaProfesional = cedulaProfesional.trim();
    }

    private void validaNoEliminado() {
        if (this.estadoRegistro.equals(EstadoRegistro.ELIMINADO))
            throw new ConflictoException("El medico ya se encuentra eliminado");
    }

    public void eliminar() {
        validaNoEliminado();

        this.estadoRegistro = EstadoRegistro.ELIMINADO;
    }

    public void asignarEspecialidadMedico(EspecialidadMedico especialidadMedico) {
        if (especialidadMedico == null)
            throw new DatoInvalidoException("La especialidad del medico no puede ser nula");

        this.especialidadMedico = especialidadMedico;
    }

    public void asignarDisponibilidadMedico(DisponibilidadMedico disponibilidadMedico) {
        if (disponibilidadMedico == null)
            throw new DatoInvalidoException("La disponibilidad del medico no puede ser nula");

        this.disponibilidadMedico = disponibilidadMedico;
    }

    public static Medico crear(
            String nombre,
            String apellidoPaterno,
            String apellidoMaterno,
            short edad,
            String email,
            String telefono,
            String cedulaProfesional
    ) {
        validarDatos(
                nombre.trim(),
                apellidoPaterno.trim(),
                apellidoMaterno.trim(),
                edad,
                email.trim().toLowerCase(),
                telefono.trim(),
                cedulaProfesional.trim()
        );
        return Medico.builder()
                .nombre(nombre.trim())
                .apellidoPaterno(apellidoPaterno.trim())
                .apellidoMaterno(apellidoMaterno.trim())
                .edad(edad)
                .email(email.trim().toLowerCase())
                .telefono(telefono.trim())
                .cedulaProfesional(cedulaProfesional.trim())
                .disponibilidadMedico(DisponibilidadMedico.DISPONIBLE)
                .estadoRegistro(EstadoRegistro.ACTIVO)
                .build();

    }
}