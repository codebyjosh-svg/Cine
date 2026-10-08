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
        cambiarEscena("/org/cine/view/Login.fxml", "Cine - Inicio de sesión");
    }

    public static void mostrarDashboardSegunRol() throws IOException {
        if (!SesionContext.haySesionActiva()) {
            mostrarLogin();
            return;
        }
        String ruta = NavegacionRol.vistaPorRol(SesionContext.getRolActual());
        cambiarEscena(ruta, "Cine - " + SesionContext.getRolActual());
    }

    public static void mostrarUsuarios() throws IOException {
        Usuario actual = SesionContext.getUsuarioActual();
        if (actual == null || !actual.isEstado() || !"admin".equals(actual.getNombreRol())) {
            throw new IllegalStateException("La gestión de usuarios requiere una sesión de administrador.");
        }
        cambiarEscena("/org/cine/view/Usuarios.fxml", "Cine - Usuarios");
    }

    public static void cerrarSesion() throws IOException {
        SesionContext.cerrarSesion();
        mostrarLogin();
    }

    public static void cambiarEscena(String ruta, String titulo) throws IOException {
        URL recurso = Principal.class.getResource(ruta);
        if (recurso == null) {
            throw new IOException("No se encontró la vista: " + ruta);
        }
        Parent root = FXMLLoader.load(recurso);
        boolean usuarios = ruta.endsWith("/Usuarios.fxml");
        Scene scene = new Scene(root, usuarios ? 1100 : 860, usuarios ? 700 : 540);
        stagePrincipal.setMinWidth(usuarios ? 1000 : 860);
        stagePrincipal.setMinHeight(usuarios ? 650 : 540);
        stagePrincipal.setScene(scene);
        stagePrincipal.setTitle(titulo);
        stagePrincipal.sizeToScene();
        stagePrincipal.centerOnScreen();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
