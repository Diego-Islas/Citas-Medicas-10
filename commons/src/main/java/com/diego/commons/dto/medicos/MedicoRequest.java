package com.diego.commons.dto.medicos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(name = "MedicoRequest", description = "Request para crear o actualizar un medico")
public record   MedicoRequest(
        @Schema(description = "Nombre del medico", example = "Juan")
        @NotBlank(message = "El nombre es requerido")
        @Size(min = 1, max = 50, message = "El nombre debe tener entre 1 y 50 caracteres")
        String nombre,

        @Schema(description = "El apellido paterno del medico", example = "Perez")
        @NotBlank(message = "El apellido paterno es requerido")
        @Size(min = 1, max = 50, message = "El apellido paterno debe tener entre 1 y 50 caracteres")
        String apellidoPaterno,

        @Schema(description = "El apellido materno del medico", example = "Perez")
        @NotBlank(message = "El apellido materno es requerido")
        @Size(min = 1, max = 50, message = "El apellido materno debe tener entre 1 y 50 caracteres")
        String apellidoMaterno,

        @Schema(description = "La edad del medico", example = "20")
        @Min(value = 1, message = "La edad debe ser mayor a 0")
        @Max(value = 100, message = "La edad debe ser menor a 100 años")
        short edad,

        @Schema(description = "Email del medico", example = "laura.martinez@medical.com")
        @NotBlank(message = "El email es requerido")
        @Size(min = 5, max = 100, message = "El email debe tener entre 5 y 100 caracteres")
        @Email(message = "El email debe tener un formato valido (ejemplo@dominio.com)")
        String email,

        @Schema(description = "El telefono del paciente", example = "9191011001")
        @Pattern(regexp = "^[0-9]{10}", message = "El telefono debe tener 10 caracteres numericos")
        String telefono,

        @Schema(description = "Cedula profesional del medico. Debe contener exactamente 12 caracteres",
                example = "123456789012", minLength = 12, maxLength = 12
        )
        @NotBlank(message = "La cedula profesional es requerida")
        @Size(min = 12, max = 12, message = "La cedula profesional debe tener 12 caracteres")
        String cedulaProfesional,

        @Schema(description = "Identificador de la especialidad del medico", example = "5", minimum = "1")
        @NotNull(message = "El id de la especialidad es requerido")
        @Positive(message = "El id de la especialidad debe ser positivo")
        Long idEspecialidad
) { }