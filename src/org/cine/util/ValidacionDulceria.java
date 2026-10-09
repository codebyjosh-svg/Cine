package org.cine.util;

import java.math.BigDecimal;

public final class ValidacionDulceria {

    private ValidacionDulceria() {
    }

    public static String nombre(String valor) {
        String texto = valor == null ? "" : valor.trim();

        if (texto.isEmpty() || texto.length() > 100) {
            throw new IllegalArgumentException(
                    "El nombre es obligatorio y admite hasta 100 caracteres."
            );
        }

        return texto;
    }

    public static String descripcion(String valor) {
        String texto = valor == null ? "" : valor.trim();

        if (texto.length() > 255) {
            throw new IllegalArgumentException(
                    "La descripción admite hasta 255 caracteres."
            );
        }

        return texto;
    }

    public static int entero(String valor, String campo) {
        try {
            int numero = Integer.parseInt(valor.trim());

            if (numero < 0) {
                throw new NumberFormatException();
            }

            return numero;

        } catch (RuntimeException ex) {
            throw new IllegalArgumentException(
                    campo + " debe ser un entero mayor o igual a cero."
            );
        }
    }

    public static BigDecimal precio(String valor) {
        try {
            BigDecimal numero = new BigDecimal(valor.trim());

            if (numero.signum() < 0
                    || numero.compareTo(
                            new BigDecimal("99999999.99")) > 0
                    || numero.stripTrailingZeros().scale() > 2) {

                throw new NumberFormatException();
            }

            return numero.setScale(2);

        } catch (RuntimeException ex) {
            throw new IllegalArgumentException(
                    "El precio debe estar entre 0 y 99999999.99 "
                    + "y tener como máximo dos decimales."
            );
        }
    }
}