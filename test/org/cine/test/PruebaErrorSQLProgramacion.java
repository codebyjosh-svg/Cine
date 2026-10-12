package org.cine.test;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.cine.model.Usuario;
import org.cine.service.SesionContext;
import org.cine.system.Principal;

/** Ejecutar con credenciales de BD inválidas, únicamente en una copia de pruebas. */
public final class PruebaErrorSQLProgramacion {
    private static volatile Throwable fallo;

    private static void comprobar(boolean valor, String mensaje) {
        if (!valor) throw new AssertionError(mensaje);
        System.out.println("OK · " + mensaje);
    }

    public static final class VistaDePrueba extends Application {
        @Override
        public void start(Stage stage) {
            try {
                new Principal().start(stage);
                Usuario admin = new Usuario();
                admin.setEstado(true);
                admin.setNombreRol("admin");
                SesionContext.iniciarSesion(admin);
                Principal.mostrarProgramacion();
                Label mensaje = (Label) stage.getScene().lookup("#lblMensaje");
                comprobar(mensaje.getText().contains("rechazó el usuario o la contraseña"),
                        "La pantalla muestra una explicación de credenciales SQL inválidas");
                comprobar(((Button) stage.getScene().lookup("#btnGuardar")).isDisabled(),
                        "Guardar está deshabilitado cuando los datos no pudieron cargarse");
                ((Button) stage.getScene().lookup("#btnActualizar")).fire();
                comprobar(mensaje.getStyleClass().contains("status-error"),
                        "Actualizar conserva el error visible y la pantalla abierta");
                ((Button) stage.getScene().lookup("#btnVolver")).fire();
                comprobar(stage.getScene().lookup("#btnFunciones") != null,
                        "Es posible volver al dashboard después de un error SQL");
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
        if (!Boolean.getBoolean("cine.pruebas.bd")) {
            throw new IllegalStateException("Usa una copia de pruebas y -Dcine.pruebas.bd=true.");
        }
        Application.launch(VistaDePrueba.class, args);
        if (fallo != null) throw new AssertionError("Falló la prueba de error SQL", fallo);
    }
}
