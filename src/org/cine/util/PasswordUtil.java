package org.cine.util;

public final class PasswordUtil {
    private PasswordUtil() {
    }

    public static String hash(String contrasena) {
        return SecurityUtil.hashPassword(contrasena);
    }

    public static boolean verificar(String contrasena, String hash) {
        return SecurityUtil.verifyPassword(contrasena, hash);
    }
}
