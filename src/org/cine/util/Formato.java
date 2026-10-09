package org.cine.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Formato monetario en quetzales sin perder precisión con double. */
public final class Formato {
    private Formato() { }

    public static String dinero(BigDecimal valor) {
        return "Q " + valor.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    public static String mensaje(Throwable error) {
        return error.getMessage() == null ? "No se pudo completar la operación." : error.getMessage();
    }
}
