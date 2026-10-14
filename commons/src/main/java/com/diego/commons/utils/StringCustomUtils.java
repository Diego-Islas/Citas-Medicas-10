package com.diego.commons.utils;


import com.diego.commons.exceptions.DatoInvalidoException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class StringCustomUtils {
    private StringCustomUtils() {
        /* This utility class should not be instantiated */
    }

    private static final DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static void validarNoVacio(String texto, String mensaje) {
        if (texto == null || texto.isBlank())
            throw new DatoInvalidoException(mensaje);
    }

    public static void validarTamanio(String texto, Integer min, Integer max, String mensaje) {

        validarNoVacio(texto, mensaje);

        if (texto.length() < min || texto.length() > max)
            throw new DatoInvalidoException(mensaje);
    }


    public static void validarNoVacioNoNull(String texto, String mensajeError) {
        if (texto == null || texto.isBlank())
            throw new DatoInvalidoException(mensajeError);
    }

    public static String quitarAcentos(String texto) {
        return texto.toLowerCase()
                .replace("á", "a").replace("é", "e")
                .replace("í", "i").replace("ó", "o")
                .replace("ú", "u").replace("ü", "u");
    }

    public static String localDateAString(LocalDate fecha) {
        return fecha == null ? null : fecha.format(formato);
    }

    public static String normalizarTexto(String texto){
        return texto
                .replace("á", "a")
                .replace("à", "a")
                .replace("ä", "a")
                .replace("â", "a")
                .replace("ã", "a")
                .replace("å", "a")

                .replace("é", "e")
                .replace("è", "e")
                .replace("ë", "e")
                .replace("ê", "e")

                .replace("í", "i")
                .replace("ì", "i")
                .replace("ï", "i")
                .replace("î", "i")

                .replace("ó", "o")
                .replace("ò", "o")
                .replace("ö", "o")
                .replace("ô", "o")
                .replace("õ", "o")
                .replace("ø", "o")

                .replace("ú", "u")
                .replace("ù", "u")
                .replace("ü", "u")
                .replace("û", "u")

                .replace("ñ", "n")
                .replace("ç", "c")
                .replace("ý", "y")
                .replace("ÿ", "y")

                .trim();
    }


    public static void validarTextoNumerico(String texto, String mensajeError) {
        validarNoVacioNoNull(texto, mensajeError);
        if (!texto.trim().matches("\\d+"))
            throw new DatoInvalidoException(mensajeError);
    }
}