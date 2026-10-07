package org.cine.service;

import org.cine.model.Usuario;

public final class SesionContext {
    private static Usuario usuarioActual;

    private SesionContext() {
    }

    public static void iniciarSesion(Usuario usuario) {
        usuarioActual = usuario;
    }

    public static Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public static String getRolActual() {
        return usuarioActual == null ? null : usuarioActual.getNombreRol();
    }

    public static boolean haySesionActiva() {
        return usuarioActual != null;
    }

    public static void cerrarSesion() {
        usuarioActual = null;
    }
}
