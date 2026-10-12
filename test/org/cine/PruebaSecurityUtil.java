package org.cine;

import org.cine.util.SecurityUtil;

public class PruebaSecurityUtil {
    public static void main(String[] args) {
        String hash = SecurityUtil.hashPassword("PruebaCinema2026!");
        exigir(SecurityUtil.verifyPassword("PruebaCinema2026!", hash), "La contraseña correcta debe validar.");
        exigir(!SecurityUtil.verifyPassword("Incorrecta", hash), "La contraseña incorrecta no debe validar.");
        System.out.println("[OK] SecurityUtil valida PBKDF2 correctamente.");
    }

    private static void exigir(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new AssertionError(mensaje);
        }
    }
}
