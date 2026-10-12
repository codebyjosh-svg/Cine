package org.cine.test;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import org.cine.dao.impl.UsuarioDAOImpl;
import org.cine.model.Rol;
import org.cine.model.Usuario;
import org.cine.service.SesionContext;
import org.cine.system.Principal;

public class PruebaIntegracionCine extends Application {
    private static volatile Throwable fallo;
    private Stage escenario;
    private int comprobaciones;

    @Override
    public void start(Stage stage) throws Exception {
        escenario = stage;
        new Principal().start(stage);
        Platform.setImplicitExit(false);
        Thread pruebas = new Thread(() -> {
            try {
                probar();
            } catch (Throwable ex) {
                fallo = ex;
                ex.printStackTrace();
            } finally {
                Platform.runLater(() -> {
                    SesionContext.cerrarSesion();
                    stage.close();
                    Platform.exit();
                });
            }
        }, "pruebas-integracion-cine");
        pruebas.setDaemon(true);
        pruebas.start();
    }

    private <T> T fx(Callable<T> operacion) throws Exception {
        FutureTask<T> tarea = new FutureTask<>(operacion);
        Platform.runLater(tarea);
        return tarea.get(20, TimeUnit.SECONDS);
    }

    private void comprobar(boolean resultado, String mensaje) {
        if (!resultado) {
            throw new AssertionError(mensaje);
        }
        System.out.println("OK " + (++comprobaciones) + " - " + mensaje);
    }

    private void pulsar(String id) throws Exception {
        fx(() -> {
            ((Button) escenario.getScene().lookup("#" + id)).fire();
            return null;
        });
    }

    private void login(String username, String clave) throws Exception {
        fx(() -> {
            ((TextField) escenario.getScene().lookup("#txtUsername")).setText(username);
            ((TextField) escenario.getScene().lookup("#txtPassword")).setText(clave);
            ((Button) escenario.getScene().lookup("#btnIngresar")).fire();
            return null;
        });
    }

    private void capturar(String nombre) throws Exception {
        WritableImage imagen = fx(() -> escenario.getScene().snapshot(null));
        int ancho = (int) imagen.getWidth();
        int alto = (int) imagen.getHeight();
        BufferedImage png = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                png.setRGB(x, y, imagen.getPixelReader().getArgb(x, y));
            }
        }
        Path carpeta = Path.of(System.getProperty("cine.evidencia.dir", "test/evidencia"));
        Files.createDirectories(carpeta);
        ImageIO.write(png, "png", carpeta.resolve(nombre).toFile());
    }

    private boolean accesoUsuariosBloqueado() throws Exception {
        return fx(() -> {
            try {
                Principal.mostrarUsuarios();
                return false;
            } catch (IllegalStateException esperado) {
                return true;
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void probar() throws Exception {
        comprobar(fx(() -> escenario.getScene().lookup("#txtUsername") != null),
                "Principal abre la pantalla de login");
        capturar("cine_login.png");
        comprobar(accesoUsuariosBloqueado(), "Usuarios requiere una sesión de administrador");
        login("admin.cine", "incorrecta");
        comprobar(fx(() -> !SesionContext.haySesionActiva()
                && ((Label) escenario.getScene().lookup("#lblMensaje")).getText().contains("incorrectos")),
                "Login incorrecto no abre una sesión");
        login("inactivo.cine", "BodegaCine2026!");
        comprobar(fx(() -> !SesionContext.haySesionActiva()
                && ((Label) escenario.getScene().lookup("#lblMensaje")).getText().contains("inactivo")),
                "La pantalla de login rechaza la cuenta inactiva");

        login("admin.cine", "AdminCine2026!");
        comprobar(fx(() -> "admin".equals(SesionContext.getRolActual())
                && escenario.getScene().lookup("#btnUsuarios") != null),
                "El login del administrador abre su dashboard con Usuarios");
        capturar("cine_dashboard_admin.png");
        pulsar("btnUsuarios");
        comprobar(fx(() -> escenario.getScene().lookup("#tblUsuarios") != null),
                "El botón Usuarios cambia a la pantalla de gestión");
        comprobar(fx(() -> ((TableView<Usuario>) escenario.getScene().lookup("#tblUsuarios")).getItems().size() == 5),
                "La gestión carga las cinco cuentas de los SQL");
        comprobar(fx(() -> "admin".equals(SesionContext.getRolActual())),
                "La sesión se conserva al abrir Usuarios");
        capturar("cine_usuarios_integrados.png");

        fx(() -> {
            TableView<Usuario> tabla = (TableView<Usuario>) escenario.getScene().lookup("#tblUsuarios");
            for (Usuario usuario : tabla.getItems()) {
                if (usuario.getIdUsuario() == 1) {
                    tabla.getSelectionModel().select(usuario);
                    break;
                }
            }
            ((Button) escenario.getScene().lookup("#btnEditar")).fire();
            ComboBox<Rol> roles = (ComboBox<Rol>) escenario.getScene().lookup("#cmbRol");
            for (Rol rol : roles.getItems()) {
                if ("bodega".equals(rol.getNombreRol())) {
                    roles.setValue(rol);
                    break;
                }
            }
            ((Button) escenario.getScene().lookup("#btnGuardar")).fire();
            return null;
        });
        comprobar(fx(() -> ((Label) escenario.getScene().lookup("#lblError")).getText().contains("propio rol")),
                "El administrador no puede cambiar su propio rol desde el formulario");
        comprobar(new UsuarioDAOImpl().buscarPorId(1).orElseThrow().getIdRol() == 1,
                "El rechazo conserva el rol del administrador en MySQL");
        pulsar("btnVolver");
        comprobar(fx(() -> escenario.getScene().lookup("#btnUsuarios") != null
                && "admin".equals(SesionContext.getRolActual())),
                "Volver regresa al dashboard y conserva la sesión");
        pulsar("btnCerrarSesion");
        comprobar(fx(() -> escenario.getScene().lookup("#txtUsername") != null
                && !SesionContext.haySesionActiva()),
                "Cerrar sesión regresa al login y limpia la sesión");

        String[][] cuentas = {
            {"taquilla.cine", "TaquillaCine2026!", "taquillero"},
            {"bodega.cine", "BodegaCine2026!", "bodega"},
            {"cliente.cine", "ClienteCine2026!", "cliente"}
        };
        for (String[] cuenta : cuentas) {
            login(cuenta[0], cuenta[1]);
            comprobar(fx(() -> cuenta[2].equals(SesionContext.getRolActual())
                    && escenario.getScene().lookup("#btnCerrarSesion") != null
                    && escenario.getScene().lookup("#btnUsuarios") == null),
                    "El rol " + cuenta[2] + " abre su dashboard sin gestión de usuarios");
            comprobar(accesoUsuariosBloqueado(), "Usuarios bloquea el acceso del rol " + cuenta[2]);
            pulsar("btnCerrarSesion");
            comprobar(fx(() -> !SesionContext.haySesionActiva()
                    && escenario.getScene().lookup("#txtUsername") != null),
                    "Logout correcto del rol " + cuenta[2]);
        }
        System.out.println("RESULTADO: " + comprobaciones + " comprobaciones de integración correctas.");
    }

    public static void main(String[] args) {
        if (!Boolean.getBoolean("cine.pruebas.bd")) {
            throw new IllegalStateException("Usa -Dcine.pruebas.bd=true con una base de pruebas.");
        }
        launch(args);
        if (fallo != null) {
            throw new AssertionError("Fallaron las pruebas de integración", fallo);
        }
    }
}
