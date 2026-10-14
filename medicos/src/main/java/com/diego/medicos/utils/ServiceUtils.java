package com.diego.medicos.utils;

import com.diego.commons.exceptions.RecursoNoEncontradoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;

@Slf4j
public class ServiceUtils {
    private ServiceUtils() {
        /* This utility class should not be instantiated */
    }

    public static <E, ID> E obtenerEntidadOExcepcion(
            JpaRepository<E, ID> repository,
            ID id,
            Class<E> eClass
    ) {
        String nombreEntidad = eClass.getSimpleName();

        log.info("Buscando {} con id: {}", nombreEntidad, id);

        return repository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                nombreEntidad +
                                        " no encontrado con id: " +
                                        id
                        )
                );
    }
}