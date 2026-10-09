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
        validarAdministrador("usuarios");

        cambiarEscena(
                "/org/cine/view/Usuarios.fxml",
                "Cine - Usuarios"
        );
    }

    public static void mostrarPeliculas() throws IOException {
        validarAdministrador("películas");

        cambiarEscena(
                "/org/cine/view/Peliculas.fxml",
                "Cine - Películas"
        );
    }

    public static void mostrarGeneros() throws IOException {
        validarAdministrador("géneros");

        cambiarEscena(
                "/org/cine/view/Generos.fxml",
                "Cine - Géneros"
        );
    }

    public static void mostrarClientes() throws IOException {
        validarAdministrador("clientes");

        cambiarEscena(
                "/org/cine/view/Clientes.fxml",
                "Cine - Clientes"
        );
    }

    public static void mostrarProductos() throws IOException {
        validarAdministrador("productos");

        cambiarEscena(
                "/org/cine/view/Productos.fxml",
                "Cine - Productos de dulcería"
        );
    }

    public static void mostrarCategoriasProducto() throws IOException {
        validarAdministrador("categorías de productos");

        cambiarEscena(
                "/org/cine/view/CategoriasProducto.fxml",
                "Cine - Categorías de productos"
        );
    }

    private static void validarAdministrador(String modulo) {
        Usuario actual = SesionContext.getUsuarioActual();

        if (actual == null
                || !actual.isEstado()
                || !"admin".equalsIgnoreCase(actual.getNombreRol())) {

            throw new IllegalStateException(
                    "La gestión de " + modulo
                    + " requiere una sesión de administrador."
            );
        }
    }

    public static void cerrarSesion() throws IOException {
        SesionContext.cerrarSesion();
        mostrarLogin();
    }

    public static void cambiarEscena(
            String ruta,
            String titulo) throws IOException {

        if (stagePrincipal == null) {
            throw new IllegalStateException(
                    "La ventana principal todavía no está inicializada."
            );
        }

        if (ruta == null || ruta.isBlank()) {
            throw new IOException(
                    "No se indicó la ruta de la vista."
            );
        }

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
        boolean productos = ruta.endsWith("/Productos.fxml");
        boolean categoriasProducto =
                ruta.endsWith("/CategoriasProducto.fxml");

        boolean moduloGrande =
                usuarios
                || peliculas
                || generos
                || clientes
                || productos
                || categoriasProducto;

        double ancho = productos
                ? 1150
                : moduloGrande ? 1100 : 860;

        double alto = moduloGrande ? 700 : 540;

        Scene scene = new Scene(root, ancho, alto);

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