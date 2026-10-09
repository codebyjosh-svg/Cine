package org.cine.service;

public final class NavegacionRol {
    private NavegacionRol() {
    }

    public static String vistaPorRol(String rol) {
        if (rol == null) {
            return "/org/cine/view/Login.fxml";
        }
        return switch (rol.trim().toLowerCase()) {
            case "admin" -> "/org/cine/view/DashboardAdmin.fxml";
            case "taquillero" -> "/org/cine/view/DashboardTaquillero.fxml";
            default -> throw new IllegalArgumentException("Rol no reconocido: " + rol);
        };
    }
}
