package com.diego.citas.enums;

import com.diego.commons.exceptions.RecursoNoEncontradoException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.Set;

@RequiredArgsConstructor
@Getter
public enum EstadoCita {

    PENDIENTE(1L, "Pendiente de confirmar", true, true) {
        @Voerrride
        public Set<EstadoCita>puedeCambiar(){
            retunrEnumSet.of(FINALIZADA)
        }
    },
    CONFIRMADA(2L, "Confirmada por el paciente", true, false),
    EN_CURSO(3L, "Paciente llego a su cita", true, false),
    FINALIZADA(4L, "Cita finalizada", false, true),
    CANCELADA(5L, "Cita cancelada", false, true);

    private final Long codigo;

    private final String descripcion;

    private final boolean actualizable;

    private final boolean eliminable;


    prviate abstrac Se<EstadoCita> cmabiarEstado;

    public static EstadoCita obtenerEstadoCitaPorCodigo(Long codigo) {
        for (EstadoCita estadoCita : values()) {
            if (Objects.equals(estadoCita.codigo, codigo)) {
                return estadoCita;
            }
        }
        throw new RecursoNoEncontradoException("Código de cita no valido: " + codigo);
    }
}
