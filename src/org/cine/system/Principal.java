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

    /**
     * US-1.4 - Gestión de clientes.
     */
    public static void mostrarClientes() throws IOException {

        Usuario actual = SesionContext.getUsuarioActual();

        if (actual == null
                || !actual.isEstado()
                || !"admin".equalsIgnoreCase(actual.getNombreRol())) {

            throw new IllegalStateException(
                    "La gestión de clientes requiere una sesión de administrador."
            );
        }

        cambiarEscena(
                "/org/cine/view/Clientes.fxml",
                "Cine - Clientes"
        );
    }

    public static void mostrarVenta() throws IOException {
        String rol=SesionContext.getRolActual();
        if(rol==null||!("admin".equalsIgnoreCase(rol)||"taquillero".equalsIgnoreCase(rol)))throw new IllegalStateException("No autorizado para ventas");
        cambiarEscena("/org/cine/view/Venta.fxml","Cinema - Venta de boletos");
    }
    public static void mostrarFactura(int id) throws IOException {
        String rol=SesionContext.getRolActual();
        if(rol==null||!("admin".equalsIgnoreCase(rol)||"taquillero".equalsIgnoreCase(rol)))throw new IllegalStateException("No autorizado para facturas");
        FXMLLoader l=new FXMLLoader(Principal.class.getResource("/org/cine/view/Factura.fxml"));Parent root=l.load();
        stagePrincipal.setScene(new Scene(root,1050,700));stagePrincipal.setTitle("Cinema - Factura");
        ((org.cine.controller.FacturaController)l.getController()).mostrar(id);
        stagePrincipal.centerOnScreen();
    }
    public static void cerrarSesion() throws IOException {
        SesionContext.cerrarSesion();
        mostrarLogin();
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

        FXMLLoader loader = new FXMLLoader(recurso);
        Parent root = loader.load();

        boolean usuarios = ruta.endsWith("/Usuarios.fxml");
        boolean peliculas = ruta.endsWith("/Peliculas.fxml");
        boolean generos = ruta.endsWith("/Generos.fxml");
        boolean clientes = ruta.endsWith("/Clientes.fxml");
        boolean venta = ruta.endsWith("/Venta.fxml");

        boolean moduloGrande =
                usuarios
                || peliculas
                || generos
                || clientes || venta;

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

    public static void main(String[] args) {
        launch(args);
    }
}