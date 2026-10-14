package com.diego.commons.dto.pacientes;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Datos para agregar un paciente")
public record PacienteRequest(
        @Schema(description = "Nombre del paciente", example = "Juan")
        @NotBlank(message = "El nombre es requerido")
        @Size(min = 1, max = 50, message = "El nombre debe tener entre 1 y 50 caracteres")
        String nombre,

        @Schema(description = "El apellido paterno del paciente", example = "Perez")
        @NotBlank(message = "El apellido paterno es requerido")
        @Size(min = 1, max = 50, message = "El apellido paterno debe tener entre 1 y 50 caracteres")
        String apellidoPaterno,

        @Schema(description = "El apellido materno del paciente", example = "Perez")
        @NotBlank(message = "El apellido materno es requerido")
        @Size(min = 1, max = 50, message = "El apellido materno debe tener entre 1 y 50 caracteres")
        String apellidoMaterno,

        @Schema(description = "La edad del paciente", example = "20")
        @Min(value = 1, message = "La edad debe ser mayor a 0")
        @Max(value = 100, message = "La edad debe ser menor a 100 años")
        short edad,

        @Schema(description = "Peso del paciente", example = "70.5")
        @NotNull(message = "El peso es requerida")
        @DecimalMin(value = "0.1", message = "El peso debe ser mayor a 0.1")
        @DecimalMax(value = "200", message = "El peso debe ser menor a 200")
        Double peso,

        @Schema(description = "Estatura del paciente", example = "1.8")
        @NotNull(message = "La estatura es requerida")
        @DecimalMin(value = "1", message = "La estatura debe ser mayor a 1")
        @DecimalMax(value = "2", message = "La estatura debe ser menor a 2")
        Double estatura,

        @Schema(description = "Email del paciente", example = "laura.martinez@medical.com")
        @NotBlank(message = "El email es requerido")
        @Size(min = 1, max = 100, message = "El email debe tener entre 5 y 100 caracteres")
        @Email(message = "El email debe tener un formato valido (ejemplo@dominio.com)")
        String email,

        @Schema(description = "El teléfono del paciente", example = "9191011001")
        @Pattern(regexp = "^[0-9]{10}", message = "El teléfono debe tener 10 caracteres numéricos")
        String telefono,

        @Schema(description = "Dirección del paciente", example = "5A Oriente Norte num 123, Colonia San Pedro, C.P 12345, Puebla, Pue.")
        @NotBlank(message = "La dirección es requerida")
        @Size(min = 20, max = 150, message = "La dirección debe tener entre 20 y 150 caracteres")
        String direccion
) { }