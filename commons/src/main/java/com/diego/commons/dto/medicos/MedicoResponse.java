package com.diego.commons.dto.medicos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos del medico")
public record MedicoResponse(
        @Schema(description = "Identificador del medico", example = "1", minimum = "1")
        Long id,

        @Schema(description = "Nombre del medico", example = "Juan")
        String nombre,

        @Schema(description = "El apellido paterno del medico", example = "Perez")
        String apellidoPaterno,

        @Schema(description = "El apellido materno del medico", example = "Perez")
        String apellidoMaterno,

        @Schema(description = "La edad del medico", example = "20")
        short edad,

        @Schema(description = "Email del medico", example = "laura.martinez@medical.com")
        String email,

        @Schema(description = "El telefono del medico", example = "9191011001")
        String telefono,

        @Schema(description = "Cedula profesional del medico. Debe contener exactamente 12 caracteres",
                example = "123456789012", minLength = 12, maxLength = 12
        )
        String cedulaProfecional,

        @Schema(description = "La especialidad del medico", example = "9191011001")
        String especialidad,

        @Schema(description = "La disponibilidad del medico", example = "9191011001")
        String disponibilidad,

        @Schema(description = "Identificador de la disponibilidad del medico", example = "5", minimum = "1")
        Long idDisponibilidad
) { }