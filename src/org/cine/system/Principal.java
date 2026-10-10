package org.cine.system;

import java.io.IOException;
import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.cine.model.Usuario;
import org.cine.service.NavegacionRol;
import org.cine.service.SesionContext;

public class Principal extends Application {

    private static Stage stagePrincipal;

    @Override
    public void start(Stage stage) throws IOException {
        stagePrincipal = stage;
        mostrarLogin();
        stage.show();
    }

    // =========================================================
    // LOGIN
    // =========================================================
    public static void mostrarLogin() throws IOException {
        cambiarEscena(
                "/org/cine/view/Login.fxml",
                "Cine - Inicio de sesión"
        );
    }

    // =========================================================
    // DASHBOARD SEGÚN ROL
    // =========================================================
    public static void mostrarDashboardSegunRol() throws IOException {

        if (!SesionContext.haySesionActiva()
                || !SesionContext.getUsuarioActual().isEstado()) {
            mostrarLogin();
            return;
        }

        String ruta = NavegacionRol.vistaPorRol(
                SesionContext.getRolActual()
        );

        cambiarEscena(
                ruta,
                "Cine - " + SesionContext.getRolActual()
        );
    }

    // =========================================================
    // USUARIOS
    // =========================================================
    public static void mostrarUsuarios() throws IOException {

        Usuario actual = SesionContext.getUsuarioActual();

        if (actual == null
                || !actual.isEstado()
                || !"admin".equalsIgnoreCase(actual.getNombreRol())) {

            throw new IllegalStateException(
                    "La gestión de usuarios requiere una sesión de administrador."
            );
        }

        cambiarEscena(
                "/org/cine/view/Usuarios.fxml",
                "Cine - Usuarios"
        );
    }

    // =========================================================
    // PELÍCULAS
    // =========================================================
    public static void mostrarPeliculas() throws IOException {

        Usuario actual = SesionContext.getUsuarioActual();

        if (actual == null
                || !actual.isEstado()
                || !"admin".equalsIgnoreCase(actual.getNombreRol())) {

            throw new IllegalStateException(
                    "La gestión de películas requiere una sesión de administrador."
            );
        }

        cambiarEscena(
                "/org/cine/view/Peliculas.fxml",
                "Cine - Películas"
        );
    }

    // =========================================================
    // GÉNEROS
    // =========================================================
    public static void mostrarGeneros() throws IOException {

        Usuario actual = SesionContext.getUsuarioActual();

        if (actual == null
                || !actual.isEstado()
                || !"admin".equalsIgnoreCase(actual.getNombreRol())) {

            throw new IllegalStateException(
                    "La gestión de géneros requiere una sesión de administrador."
            );
        }

        cambiarEscena(
                "/org/cine/view/Generos.fxml",
                "Cine - Géneros"
        );
    }

    // =========================================================
    // CLIENTES
    // =========================================================
    /**
     * US-1.4 - Gestión de clientes.
     */
    public static void mostrarClientes() throws IOException {

        Usuario actual = SesionContext.getUsuarioActual();

        if (actual == null
                || !actual.isEstado()
                || !("admin".equalsIgnoreCase(actual.getNombreRol())
                || "taquillero".equalsIgnoreCase(actual.getNombreRol()))) {

            throw new IllegalStateException(
                    "La gestión de clientes requiere una sesión de administrador o taquillero."
            );
        }

        cambiarEscena(
                "/org/cine/view/Clientes.fxml",
                "Cine - Clientes"
        );
    }

    // =========================================================
    // SALAS Y BUTACAS - US-2.1
    // =========================================================
    /**
     * US-2.1 - Gestión de salas y butacas.
     *
     * Solo un administrador puede acceder al módulo de gestión de salas y
     * butacas.
     */
    public static void mostrarSalas() throws IOException {

        Usuario actual = SesionContext.getUsuarioActual();

        if (actual == null
                || !actual.isEstado()
                || !"admin".equalsIgnoreCase(actual.getNombreRol())) {

            throw new IllegalStateException(
                    "La gestión de salas requiere una sesión de administrador."
            );
        }

        cambiarEscena(
                "/org/cine/view/Salas.fxml",
                "Cine - Salas y butacas"
        );
    }

    // =========================================================
    // VENTA DE BOLETOS - US-2.4
    // =========================================================
    public static void mostrarVenta() throws IOException {

        String rol = SesionContext.getRolActual();

        if (rol == null
                || !("admin".equalsIgnoreCase(rol)
                || "taquillero".equalsIgnoreCase(rol))) {

            throw new IllegalStateException(
                    "No autorizado para ventas."
            );
        }

        cambiarEscena(
                "/org/cine/view/Venta.fxml",
                "Cinema - Venta de boletos"
        );
    }

    // =========================================================
// US-2.3 → US-2.4
// VENTA CON FUNCIÓN PRESELECCIONADA
// =========================================================
    public static void mostrarVenta(int idFuncion) throws IOException {

        String rol = SesionContext.getRolActual();

        if (rol == null
                || !("admin".equalsIgnoreCase(rol)
                || "taquillero".equalsIgnoreCase(rol))) {

            throw new IllegalStateException(
                    "No autorizado para ventas."
            );
        }

        FXMLLoader loader = new FXMLLoader(
                Principal.class.getResource(
                        "/org/cine/view/Venta.fxml"
                )
        );

        Parent root = loader.load();

        org.cine.controller.VentaController controller
                = loader.getController();

        // Pasar la función seleccionada desde Cartelera
        controller.seleccionarFuncion(idFuncion);

        Scene scene = new Scene(
                root,
                1100,
                700
        );

        stagePrincipal.setScene(scene);

        stagePrincipal.setTitle(
                "Cinema - Venta de boletos"
        );

        stagePrincipal.setMinWidth(1000);
        stagePrincipal.setMinHeight(650);

        stagePrincipal.sizeToScene();
        stagePrincipal.centerOnScreen();
    }

    // =========================================================
    // FACTURA - US-2.4
    // =========================================================
    public static void mostrarFactura(int id) throws IOException {

        String rol = SesionContext.getRolActual();

        if (rol == null
                || !("admin".equalsIgnoreCase(rol)
                || "taquillero".equalsIgnoreCase(rol))) {

            throw new IllegalStateException(
                    "No autorizado para facturas."
            );
        }

        FXMLLoader loader = new FXMLLoader(
                Principal.class.getResource(
                        "/org/cine/view/Factura.fxml"
                )
        );

        Parent root = loader.load();

        stagePrincipal.setScene(
                new Scene(root, 1050, 700)
        );

        stagePrincipal.setTitle(
                "Cinema - Factura"
        );

        ((org.cine.controller.FacturaController) loader.getController()).mostrar(id);

        stagePrincipal.centerOnScreen();
    }

    // =========================================================
    // PROGRAMACIÓN - US-2.2
    // =========================================================
    /**
     * US-2.2 - Programación de funciones.
     *
     * Solo un administrador puede acceder al módulo de programación de
     * funciones.
     */
    public static void mostrarProgramacion() throws IOException {

        Usuario actual = SesionContext.getUsuarioActual();

        if (actual == null
                || !actual.isEstado()
                || !"admin".equalsIgnoreCase(actual.getNombreRol())) {

            throw new IllegalStateException(
                    "La programación de funciones requiere una sesión de administrador."
            );
        }

        cambiarEscena(
                "/org/cine/view/Programacion.fxml",
                "Cine - Programación de funciones"
        );
    }

    // ==========================================
    // CARTELERA - US-2.3
    // ==========================================
    public static void mostrarCartelera() throws IOException {

        Usuario actual = SesionContext.getUsuarioActual();

        if (actual == null || !actual.isEstado()) {
            throw new IllegalStateException(
                    "Debes iniciar sesión para consultar la cartelera."
            );
        }

        String rol = actual.getNombreRol();

        if (rol == null
                || !("admin".equalsIgnoreCase(rol)
                || "taquillero".equalsIgnoreCase(rol)
                || "cliente".equalsIgnoreCase(rol))) {

            throw new IllegalStateException(
                    "Tu rol no tiene permiso para consultar la cartelera."
            );
        }

        cambiarEscena(
                "/org/cine/view/Cartelera.fxml",
                "Cinema - Cartelera"
        );
    }

    // ==========================================
    // CONFITERÍA - SPRINT 3
    // ==========================================
    public static void mostrarConfiteria() throws IOException {

        verificarRoles("admin", "taquillero", "bodega");

        cambiarEscena(
                "/org/cine/view/Confiteria.fxml",
                "Cinema - Confitería"
        );
    }

    // ==========================================
    // CATEGORÍAS DE PRODUCTOS
    // ==========================================
    public static void mostrarCategoriasProducto() throws IOException {

        verificarRoles("admin", "bodega");

        cambiarEscena(
                "/org/cine/view/CategoriasProducto.fxml",
                "Cinema - Categorías de productos"
        );
    }

    // ==========================================
    // INVENTARIO
    // ==========================================
    public static void mostrarInventario() throws IOException {

        verificarRoles("admin", "bodega");

        cambiarEscena(
                "/org/cine/view/Inventario.fxml",
                "Cinema - Inventario"
        );
    }

    // ==========================================
    // STOCK CRÍTICO
    // ==========================================
    public static void mostrarStockCritico() throws IOException {

        verificarRoles("admin", "bodega");

        cambiarEscena(
                "/org/cine/view/StockCritico.fxml",
                "Cinema - Stock crítico"
        );
    }

    // ==========================================
    // REPORTES
    // ==========================================
    public static void mostrarReportes() throws IOException {

        verificarRoles("admin");

        cambiarEscena(
                "/org/cine/view/Reportes.fxml",
                "Cinema - Reportes"
        );
    }

    // ==========================================
    // VERIFICAR PERMISOS
    // ==========================================
    private static void verificarRoles(String... rolesPermitidos) {

        Usuario usuario = SesionContext.getUsuarioActual();

        if (usuario == null || !usuario.isEstado()) {
            throw new IllegalStateException(
                    "Debes iniciar sesión para acceder a este módulo."
            );
        }

        String rolActual = usuario.getNombreRol();

        if (rolActual == null) {
            throw new IllegalStateException(
                    "El usuario no tiene un rol asignado."
            );
        }

        for (String permitido : rolesPermitidos) {

            if (permitido.equalsIgnoreCase(rolActual.trim())) {
                return;
            }
        }

        throw new IllegalStateException(
                "No tienes permiso para acceder a este módulo."
        );
    }

    // =========================================================
    // CERRAR SESIÓN
    // =========================================================
    public static void cerrarSesion() throws IOException {

        SesionContext.cerrarSesion();
        mostrarLogin();
    }

    // =========================================================
    // CAMBIO DE ESCENA
    // =========================================================
    public static void cambiarEscena(
            String ruta,
            String titulo) throws IOException {

        URL recurso = Principal.class.getResource(ruta);

        if (recurso == null) {
            throw new IOException(
                    "No se encontró la vista: " + ruta
            );
        }

        FXMLLoader loader = new FXMLLoader(recurso);
        Parent root = loader.load();

        boolean usuarios
                = ruta.endsWith("/Usuarios.fxml");

        boolean peliculas
                = ruta.endsWith("/Peliculas.fxml");

        boolean generos
                = ruta.endsWith("/Generos.fxml");

        boolean clientes
                = ruta.endsWith("/Clientes.fxml");

        boolean salas
                = ruta.endsWith("/Salas.fxml");

        boolean venta
                = ruta.endsWith("/Venta.fxml");

        boolean programacion
                = ruta.endsWith("/Programacion.fxml");

        boolean reportes = ruta.endsWith("/Reportes.fxml");

        boolean cartelera
                = ruta.endsWith("/Cartelera.fxml");

        boolean moduloGrande
                = usuarios
                || peliculas
                || generos
                || clientes
                || salas
                || venta
                || programacion
                || cartelera
                || reportes;

        double ancho = moduloGrande ? 1100 : 860;
        double alto = moduloGrande ? 700 : 540;

        Scene scene = new Scene(
                root,
                ancho,
                alto
        );

        stagePrincipal.setMinWidth(
                moduloGrande ? 1000 : 860
        );

        stagePrincipal.setMinHeight(
                moduloGrande ? 650 : 540
        );

        stagePrincipal.setScene(scene);
        stagePrincipal.setTitle(titulo);
        stagePrincipal.sizeToScene();
        stagePrincipal.centerOnScreen();
    }

    // =========================================================
    // MAIN
    // =========================================================
    public static void main(String[] args) {
        launch(args);
    }
}
