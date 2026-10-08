package org.cine.system;

import java.io.IOException;
import java.net.URL;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.cine.service.NavegacionRol;
import org.cine.service.SesionContext;

public class Principal extends Application {
    private static Stage stagePrincipal;

    @Override
    public void start(Stage stage) throws IOException {
        stagePrincipal = stage;
        stage.setTitle("Cinema");
        mostrarLogin();
        stage.show();
    }

    public static void mostrarLogin() throws IOException {
        cambiarEscena("/org/cine/view/Login.fxml", "Cinema - Inicio de sesión");
    }

    public static void mostrarDashboardSegunRol() throws IOException {
        if (!SesionContext.haySesionActiva()) {
            mostrarLogin();
            return;
        }
        String ruta = NavegacionRol.vistaPorRol(SesionContext.getRolActual());
        cambiarEscena(ruta, "Cinema - " + SesionContext.getRolActual());
    }

    public static void cerrarSesion() throws IOException {
        SesionContext.cerrarSesion();
        mostrarLogin();
    }

    public static void mostrarProgramacion() throws IOException {
        if (!SesionContext.haySesionActiva()
                || !SesionContext.getUsuarioActual().isEstado()
                || !"admin".equals(SesionContext.getRolActual())) {
            throw new IllegalStateException("La programación de funciones requiere una sesión de administrador.");
        }
        cambiarEscena("/org/cine/view/Programacion.fxml", "Cinema - Programación de funciones");
    }

    public static void cambiarEscena(String ruta, String titulo) throws IOException {
        URL recurso = Principal.class.getResource(ruta);
        if (recurso == null) {
            throw new IOException("No se encontró la vista: " + ruta);
        }
        Parent root = FXMLLoader.load(recurso);
        Scene scene = new Scene(root);
        stagePrincipal.setScene(scene);
        stagePrincipal.setTitle(titulo);
        stagePrincipal.sizeToScene();
        stagePrincipal.centerOnScreen();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
