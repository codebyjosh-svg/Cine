package org.cine.test;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.cine.model.Usuario;
import org.cine.service.SesionContext;
import org.cine.system.Principal;

/** Ejecutar solo en una copia de pruebas configurada con contraseña SQL inválida. */
public final class PruebaErrorSQLInventario {
    private static volatile Throwable fallo;

    private static void comprobar(boolean condicion, String mensaje) {
        if (!condicion) throw new AssertionError(mensaje);
        System.out.println("OK · " + mensaje);
    }

    public static final class VistaDePrueba extends Application {
        @Override
        public void start(Stage stage) {
            try {
                new Principal().start(stage);
                Usuario admin = new Usuario();
                admin.setIdUsuario(1);
                admin.setNombreUsuario("Prueba");
                admin.setApellidoUsuario("Error SQL");
                admin.setEstado(true);
                admin.setNombreRol("admin");
                SesionContext.iniciarSesion(admin);
                Principal.mostrarInventario();
                Label mensaje = (Label) stage.getScene().lookup("#lblMensaje");
                comprobar(mensaje.getText().contains("rechazó el usuario o la contraseña"),
                        "El error de credenciales SQL aparece en la pantalla");
                comprobar(((Button) stage.getScene().lookup("#btnRegistrar")).isDisabled(),
                        "Registrar está deshabilitado cuando no pudieron cargarse los datos");
                ((Button) stage.getScene().lookup("#btnActualizar")).fire();
                comprobar(mensaje.getStyleClass().contains("status-error"),
                        "Actualizar conserva el error visible sin cerrar la pantalla");
                ((Button) stage.getScene().lookup("#btnVolver")).fire();
                comprobar(stage.getScene().lookup("#btnInventario") != null,
                        "Volver funciona después de un error de conexión");
                System.out.println("RESULTADO: 4 comprobaciones de error SQL correctas.");
            } catch (Throwable ex) {
                fallo = ex;
                ex.printStackTrace();
            } finally {
                SesionContext.cerrarSesion();
                stage.close();
                Platform.exit();
            }
        }
    }

    public static void main(String[] args) {
        if (!Boolean.getBoolean("cine.pruebas.bd")) throw new IllegalStateException("Usa una copia de pruebas y -Dcine.pruebas.bd=true.");
        Application.launch(VistaDePrueba.class, args);
        if (fallo != null) throw new AssertionError("Falló la prueba de error SQL", fallo);
    }
}
