package org.cine.service;

import org.cine.model.Usuario;
import org.cine.model.VentaResumen;

/** Controles de rol y propiedad de venta usados antes de acceder al módulo. */
public final class PermisosVenta {
    private PermisosVenta() { }

    public static Usuario exigirVendedor() {
        Usuario usuario = SesionContext.getUsuarioActual();
        if (usuario == null || !usuario.isEstado()
                || !("admin".equals(usuario.getNombreRol())
                || "taquillero".equals(usuario.getNombreRol()))) {
            throw new IllegalStateException("Ventas requiere un administrador o taquillero activo.");
        }
        return usuario;
    }

    public static void exigirAcceso(VentaResumen venta) {
        Usuario usuario = exigirVendedor();
        if (!"admin".equals(usuario.getNombreRol()) && venta.idUsuario() != usuario.getIdUsuario()) {
            throw new IllegalStateException("Solo puede consultar y modificar sus propias ventas.");
        }
    }

    public static void exigirStockCritico() {
        Usuario usuario = SesionContext.getUsuarioActual();
        if (usuario == null || !usuario.isEstado()
                || !("admin".equals(usuario.getNombreRol())
                || "bodega".equals(usuario.getNombreRol())
                || "taquillero".equals(usuario.getNombreRol()))) {
            throw new IllegalStateException("La consulta de stock requiere un rol de personal activo.");
        }
    }
}
