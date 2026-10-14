
package com.diego.commons.dto.pacientes;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos de un paciente asociado a una cita")
public record DatosPaciente(

        @Schema(
                description = "Nombre completo del paciente",
                example = "María Fernanda López García"
        )
        String nombre,

        @Schema(
                description = "Número de expediente del paciente",
                example = "EXP-00125"
        )
        String numeroExpediente,

        @Schema(
                description = "Edad del paciente en años",
                example = "35"
        )
        String edad,

        @Schema(
                description = "Peso del paciente en kilogramos",
                example = "68.5"
        )
        String peso,

        @Schema(
                description = "Estatura del paciente en metros",
                example = "1.70"
        )
        String estatura,

        @Schema(
                description = "Índice de masa corporal del paciente",
                example = "23.53"
        )
        String imc,

        @Schema(
                description = "Número telefónico del paciente",
                example = "7711234567"
        )
        String telefono
) {
}
