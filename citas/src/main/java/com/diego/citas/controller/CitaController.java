package com.diego.citas.controller;

import com.diego.citas.dto.CitaRequest;
import com.diego.citas.dto.CitaResponse;
import com.diego.citas.service.CitaService;
import com.diego.commons.controller.CrudController;
import com.diego.commons.docs.ProblemaDoc;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "API Citas", description = "Operaciones relacionadas con las citas")
public class CitaController extends CrudController<CitaRequest, CitaResponse, CitaService> {
    public CitaController(CitaService service) {
        super(service);
    }

    @PatchMapping("/{idCita}/estado/{idEstado}")
    @ApiResponse(responseCode = "204", description = "Estado de la cita actualizado")
    @ApiResponse(responseCode = "409", description = "El estado de la cita no puede ser actualizado por regla de negocio",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(
                            implementation = ProblemaDoc.class
                    )
            )

    )
    public ResponseEntity<Void> actualizarEstadoCita(
            @PathVariable @Positive(message = "El idCita debe ser positivo") Long idCita,
            @PathVariable @Positive(message = "El idEstado debe ser positivo") Long idEstado
    ) {
        service.actualizarEstadoCita(idCita, idEstado);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/paciente-cita/{idPaciente}")
    @ApiResponse(responseCode = "204", description = "El paciente no tiene citas activas")
    @ApiResponse(responseCode = "409", description = "El paciente tiene una cita activa y no puede agendar otra",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(
                            implementation = ProblemaDoc.class
                    )
            )

    )
    public ResponseEntity<Void> pacienteTieneCitaActiva(
            @PathVariable @Positive(message = "El idPaciente debe ser positivo") Long idPaciente
    ) {
        service.pacienteConCitaActiva(idPaciente);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/medico-cita/{idMedico}")
    @ApiResponse(responseCode = "204", description = "El medico no tiene citas activas")
    @ApiResponse(responseCode = "409", description = "El medico tiene una cita activa y no puede agendar otra",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(
                            implementation = ProblemaDoc.class
                    )
            )

    )
    public ResponseEntity<Void> medicoTieneCitaActiva(
            @PathVariable @Positive(message = "El idMedico debe ser positivo") Long idMedico
    ) {
        service.medicoConCitaActiva(idMedico);
        return ResponseEntity.noContent().build();
    }
}