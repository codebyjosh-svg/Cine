package org.cine.system;

import java.io.IOException;
import java.net.URL;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Screen;
import org.cine.controller.FacturaController;
import org.cine.model.Usuario;
import org.cine.service.NavegacionRol;
import org.cine.service.PermisosVenta;
import org.cine.service.SesionContext;

public class Principal extends Application {

    private static Stage stagePrincipal;

    @Override
    public void start(Stage stage) throws IOException {
        stagePrincipal = stage;
        mostrarLogin();
        stage.show();
    }

    public static void mostrarLogin() throws IOException {
        cambiarEscena(
                "/org/cine/view/Login.fxml",
                "Cine - Inicio de sesión"
        );
    }

    public static void mostrarDashboardSegunRol() throws IOException {
        if (!SesionContext.haySesionActiva()) {
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

    public static void mostrarUsuarios() throws IOException {
        Usuario actual = SesionContext.getUsuarioActual();

        if (actual == null
                || !actual.isEstado()
                || !"admin".equals(actual.getNombreRol())) {

            throw new IllegalStateException(
                    "La gestión de usuarios requiere una sesión de administrador."
            );
        }

        cambiarEscena(
                "/org/cine/view/Usuarios.fxml",
                "Cine - Usuarios"
        );
    }

    public static void mostrarPeliculas() throws IOException {
        Usuario actual = SesionContext.getUsuarioActual();

        if (actual == null
                || !actual.isEstado()
                || !"admin".equals(actual.getNombreRol())) {

            throw new IllegalStateException(
                    "La gestión de películas requiere una sesión de administrador."
            );
        }

        cambiarEscena(
                "/org/cine/view/Peliculas.fxml",
                "Cine - Películas"
        );
    }

    public static void mostrarGeneros() throws IOException {
        Usuario actual = SesionContext.getUsuarioActual();

        if (actual == null
                || !actual.isEstado()
                || !"admin".equals(actual.getNombreRol())) {

            throw new IllegalStateException(
                    "La gestión de géneros requiere una sesión de administrador."
            );
        }

        cambiarEscena(
                "/org/cine/view/Generos.fxml",
                "Cine - Géneros"
        );
    }

    public static void cerrarSesion() throws IOException {
        SesionContext.cerrarSesion();
        mostrarLogin();
    }

    public static void mostrarStockCritico() throws IOException {
        PermisosVenta.exigirStockCritico();
        cambiarEscena("/org/cine/view/StockCritico.fxml", "Cine - Stock crítico");
    }

    public static void mostrarVentas() throws IOException {
        PermisosVenta.exigirVendedor();
        cambiarEscena("/org/cine/view/Venta.fxml", "Cine - Ventas de dulcería");
    }

    public static void mostrarFactura(int idVenta) throws IOException {
        PermisosVenta.exigirVendedor();
        FXMLLoader loader = new FXMLLoader(Principal.class.getResource("/org/cine/view/Factura.fxml"));
        Parent root = loader.load();
        aplicarEscena(root, "Cine - Factura #" + idVenta, true, true);
        FacturaController controller = loader.getController();
        controller.cargarFactura(idVenta);
    }

    public static void cambiarEscena(
            String ruta,
            String titulo) throws IOException {

        URL recurso = Principal.class.getResource(ruta);

        if (recurso == null) {
            throw new IOException(
                    "No se encontró la vista: " + ruta
            );
        }

        Parent root = FXMLLoader.load(recurso);

        boolean usuarios = ruta.endsWith("/Usuarios.fxml");
        boolean peliculas = ruta.endsWith("/Peliculas.fxml");
        boolean generos = ruta.endsWith("/Generos.fxml");

        boolean operacion = ruta.endsWith("/StockCritico.fxml")
                || ruta.endsWith("/Venta.fxml") || ruta.endsWith("/Factura.fxml");
        aplicarEscena(root, titulo, usuarios || peliculas || generos || operacion, operacion);
    }

    private static void aplicarEscena(Parent root, String titulo, boolean grande, boolean operacion) {
        var pantalla = Screen.getPrimary().getVisualBounds();
        double ancho = Math.min(operacion ? 1180 : grande ? 1100 : 860, pantalla.getWidth() - 48);
        double alto = Math.min(operacion ? 720 : grande ? 700 : 540, pantalla.getHeight() - 48);
        stagePrincipal.setMinWidth(Math.min(grande ? 1050 : 860, ancho));
        stagePrincipal.setMinHeight(Math.min(grande ? 650 : 540, alto));
        stagePrincipal.setScene(new Scene(root, ancho, alto));
        stagePrincipal.setTitle(titulo);
        stagePrincipal.sizeToScene();
        stagePrincipal.centerOnScreen();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
