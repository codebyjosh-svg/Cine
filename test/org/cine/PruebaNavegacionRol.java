package org.cine;

import org.cine.service.NavegacionRol;

public class PruebaNavegacionRol {
    public static void main(String[] args) {
        exigir(NavegacionRol.vistaPorRol("admin").endsWith("DashboardAdmin.fxml"), "Admin incorrecto");
        exigir(NavegacionRol.vistaPorRol("taquillero").endsWith("DashboardTaquillero.fxml"), "Taquillero incorrecto");
        exigir(NavegacionRol.vistaPorRol("bodega").endsWith("DashboardBodega.fxml"), "Bodega incorrecto");
        exigir(NavegacionRol.vistaPorRol("cliente").endsWith("DashboardCliente.fxml"), "Cliente incorrecto");
        System.out.println("[OK] Navegación por rol correcta.");
    }

    private static void exigir(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new AssertionError(mensaje);
        }
    }
}
