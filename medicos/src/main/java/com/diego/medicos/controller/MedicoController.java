package com.diego.medicos.controller;

import com.diego.commons.controller.CrudController;
import com.diego.commons.docs.ProblemaDoc;
import com.diego.commons.dto.medicos.MedicoRequest;
import com.diego.commons.dto.medicos.MedicoResponse;
import com.diego.medicos.services.MedicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "API Medicos", description = "Operaciones relacionadas con los medicos")
public class MedicoController extends CrudController<MedicoRequest, MedicoResponse, MedicoService> {
    public MedicoController(MedicoService service) {
        super(service);
    }

    @GetMapping("/id-medico/{id}")
    @Operation(summary = "Obtener medico por id sin estado", description = "Obtiene un medico por su id sin importar su estado de registro")
    @ApiResponse(responseCode = "200", description = "Medico encontrado")
    @ApiResponse(responseCode = "404", description = "Medico no existe",
        content = @Content(mediaType = "application/problem+json",
            schema = @Schema(
                    implementation = ProblemaDoc.class
            )
        )
    )
    public ResponseEntity<MedicoResponse> obtenerMedicoPorIdSinEstado(
            @PathVariable @Positive(message = "El id debe ser positivo") Long id
    ) {
        return ResponseEntity.ok(service.obtenerMedicoPorIdSinEstado(id));
    }

    // Feing no soporta el metodo PATCH, por lo que se utiliza PUT para actualizar la disponibilidad del medico
    @PutMapping("/{idMedico}/disponibilidad/{idDisponibilidad}")
    @Operation(summary = "Actualizar disponibilidad del medico", description = "Actualiza la disponibilidad de un medico por su id")
    @ApiResponse(responseCode = "204", description = "Disponibilidad del medico actualizada")
    @ApiResponse(responseCode = "409", description = "La disponibilidad del medico no puede ser actualizada por regla de negocio",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(
                            implementation = ProblemaDoc.class
                    )
            )
    )
    public ResponseEntity<Void> actualizarDisponibilidadMedico(
            @PathVariable @Positive(message = "El idMedico debe ser positivo") Long idMedico,
            @PathVariable @Positive(message = "El idDisponibilidad debe ser positivo") Long idDisponibilidad
    ) {
        service.actualizarDisponibilidadMedico(idMedico, idDisponibilidad);
        return ResponseEntity.noContent().build();
    }
}
