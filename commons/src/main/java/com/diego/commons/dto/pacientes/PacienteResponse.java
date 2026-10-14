package com.diego.commons.dto.pacientes;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos de un paciente")
public record PacienteResponse(
        @Schema(description = "Identificador del paciente", example = "1")
        Long id,

        @Schema(description = "Nombre del paciente", example = "Juan")
        String nombre,

        @Schema(description = "La edad del paciente", example = "20")
        short edad,

        @Schema(description = "Peso del paciente", example = "70.5")
        Double peso,

        @Schema(description = "Estatura del paciente", example = "1.8")
        Double estatura,

        @Schema(description = "Indice de masa corporal del paciente", example = "20.5")
        Double imc,

        @Schema(description = "Email del paciente", example = "laura.martinez@medical.com")
        String email,

        @Schema(description = "El telefono del paciente", example = "9191011001")
        String telefono,

        @Schema(description = "Direccion del paciente", example = "5A Oriente Norte num 123, Colonia San Pedro, C.P 12345, Puebla, Pue.")
        String direccion,

        @Schema(description = "Numero de expediente del paciente", example = "12345678901234567890")
        String numExpediente
) {
}