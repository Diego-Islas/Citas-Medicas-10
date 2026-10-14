package com.diego.commons.enums;

import com.diego.commons.exceptions.DatoInvalidoException;
import com.diego.commons.utils.StringCustomUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum EstadoRegistro {
    ACTIVO("Activo"),
    ELIMINADO("Eliminado");

    private final String descripcion;

    public static EstadoRegistro obtenerPorDescripcion(String descripcion) {
        String descripcionNormalizada = StringCustomUtils.normalizarTexto(descripcion);
        for (EstadoRegistro estadoRegistro : values()) {
            if (StringCustomUtils.normalizarTexto(
                    estadoRegistro.descripcion
            ).equals(descripcionNormalizada))
                return estadoRegistro;
        }

        throw new DatoInvalidoException("No existe un estado de registro con descripción: " + descripcion);
    }

}