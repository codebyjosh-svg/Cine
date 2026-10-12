package org.cine.test;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import javax.imageio.ImageIO;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.cine.dao.impl.UsuarioDAOImpl;
import org.cine.model.Rol;
import org.cine.model.Usuario;
import org.cine.service.SesionContext;

public class PruebaUsuariosFXML extends Application {
    private static volatile Throwable fallo;
    private Scene escena;
    private Stage escenario;
    private final UsuarioDAOImpl dao = new UsuarioDAOImpl();
    private String username;
    private int idCreado;
    private int comprobaciones;

    @Override
    public void start(Stage escenario) throws Exception {
        this.escenario = escenario;
        SesionContext.iniciarSesion(dao.autenticar("admin.cine", "AdminCine2026!"));
        FXMLLoader cargador = new FXMLLoader(getClass().getResource("/org/cine/view/Usuarios.fxml"));
        Parent raiz = cargador.load();
        escena = new Scene(raiz, 1100, 700);
        escena.getStylesheets().add(getClass().getResource("/org/cine/view/style/usuarios.css").toExternalForm());
        escenario.setScene(escena);
        escenario.show();
        Platform.setImplicitExit(false);
        Timeline dialogos = new Timeline(new KeyFrame(javafx.util.Duration.millis(120), evento -> {
            for (Window ventana : java.util.List.copyOf(Window.getWindows())) {
                if (ventana != escenario && ventana.isShowing() && ventana.getScene().getRoot() instanceof DialogPane panel) {
                    if (panel.lookupButton(ButtonType.OK) instanceof Button boton) { boton.fire(); }
                }
            }
        }));
        dialogos.setCycleCount(Timeline.INDEFINITE);
        dialogos.play();
        Thread pruebas = new Thread(() -> {
            try {
                probar();
            } catch (Throwable ex) {
                fallo = ex;
                ex.printStackTrace();
            } finally {
                try {
                    if (idCreado > 0 && dao.buscarPorId(idCreado).isPresent()) { dao.eliminar(idCreado); }
                } catch (Exception ex) { fallo = ex; }
                Platform.runLater(() -> {
                    dialogos.stop();
                    SesionContext.cerrarSesion();
                    escenario.close();
                    Platform.exit();
                });
            }
        }, "pruebas-interfaz");
        pruebas.setDaemon(true);
        pruebas.start();
    }

    private <T> T fx(Callable<T> operacion) throws Exception {
        FutureTask<T> tarea = new FutureTask<>(operacion);
        Platform.runLater(tarea);
        return tarea.get(20, TimeUnit.SECONDS);
    }

    private void comprobar(boolean condicion, String mensaje) {
        if (!condicion) { throw new AssertionError(mensaje); }
        System.out.println("OK " + (++comprobaciones) + " · " + mensaje);
    }

    private void esperar(BooleanSupplier condicion, String mensaje) throws Exception {
        long limite = System.nanoTime() + Duration.ofSeconds(20).toNanos();
        while (System.nanoTime() < limite) {
            if (condicion.getAsBoolean()) { return; }
            Thread.sleep(80);
        }
        throw new AssertionError("Tiempo agotado: " + mensaje);
    }

    private String texto(String id) throws Exception {
        return fx(() -> ((Label) escena.lookup("#" + id)).getText());
    }

    private boolean textoContiene(String id, String esperado) {
        try { return texto(id).contains(esperado); }
        catch (Exception ex) { throw new IllegalStateException(ex); }
    }

    private void pulsar(String id) throws Exception {
        fx(() -> {
            ((Button) escena.lookup("#" + id)).fire();
            return null;
        });
    }

    private void campo(String id, String valor) throws Exception {
        fx(() -> {
            ((TextField) escena.lookup("#" + id)).setText(valor);
            return null;
        });
    }

    @SuppressWarnings("unchecked")
    private void seleccionar() throws Exception {
        fx(() -> {
            TableView<Usuario> tabla = (TableView<Usuario>) escena.lookup("#tblUsuarios");
            tabla.getItems().stream().filter(u -> u.getIdUsuario() == idCreado).findFirst()
                    .ifPresent(u -> tabla.getSelectionModel().select(u));
            return null;
        });
    }

    private boolean estadoBD(boolean esperado) {
        try { return dao.buscarPorId(idCreado).map(u -> u.isEstado() == esperado).orElse(false); }
        catch (Exception ex) { throw new IllegalStateException(ex); }
    }

    private void captura(String nombre) throws Exception {
        Path destino = Path.of(System.getProperty("cine.evidencia.dir", "evidencia"), nombre);
        Files.createDirectories(destino.getParent());
        WritableImage imagen = fx(() -> {
            escena.getRoot().applyCss();
            escena.getRoot().layout();
            return escena.snapshot(null);
        });
        int ancho = (int) imagen.getWidth();
        int alto = (int) imagen.getHeight();
        int[] pixeles = new int[ancho * alto];
        imagen.getPixelReader().getPixels(0, 0, ancho, alto,
                javafx.scene.image.PixelFormat.getIntArgbInstance(), pixeles, 0, ancho);
        BufferedImage png = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        png.setRGB(0, 0, ancho, alto, pixeles, 0, ancho);
        ImageIO.write(png, "png", destino.toFile());
    }

    @SuppressWarnings("unchecked")
    private void probar() throws Exception {
        esperar(() -> textoContiene("lblStatus", "Datos actualizados"), "Carga inicial de usuarios, roles y clientes");
        comprobar(fx(() -> ((ComboBox<Rol>) escena.lookup("#cmbRol")).getItems().size() == 4), "FXML carga los cuatro roles de MySQL");
        comprobar(fx(() -> escena.lookup("#txtContrasena") instanceof javafx.scene.control.PasswordField), "Las contraseñas usan PasswordField");
        comprobar(fx(() -> ((Button) escena.lookup("#btnEditar")).isDisabled()), "Acciones deshabilitadas sin seleccionar una fila");
        fx(() -> {
            ComboBox<Rol> combo = (ComboBox<Rol>) escena.lookup("#cmbRol");
            combo.setValue(combo.getItems().stream().filter(r -> "cliente".equals(r.getNombreRol())).findFirst().orElseThrow());
            return null;
        });
        comprobar(fx(() -> escena.lookup("#bloqueCliente").isVisible()), "El rol cliente muestra el selector de vínculo");
        pulsar("btnNuevo");
        captura("usuarios_pantalla.png");
        pulsar("btnGuardar");
        esperar(() -> textoContiene("lblError", "nombre"), "Validación de formulario vacío");
        comprobar(true, "Guardar desde la pantalla muestra campos obligatorios");
        username = "prueba.ui." + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        campo("txtNombre", "Diego");
        campo("txtApellido", "Interfaz");
        campo("txtUsername", username);
        campo("txtCorreo", "correo.@example.com");
        fx(() -> {
            ComboBox<Rol> combo = (ComboBox<Rol>) escena.lookup("#cmbRol");
            combo.setValue(combo.getItems().stream().filter(r -> "taquillero".equals(r.getNombreRol())).findFirst().orElseThrow());
            return null;
        });
        campo("txtContrasena", "InterfazDiego2026!");
        campo("txtConfirmacion", "InterfazDiego2026!");
        pulsar("btnGuardar");
        esperar(() -> textoContiene("lblError", "correo válido"), "Validación de correo");
        comprobar(true, "La pantalla rechaza un punto antes de @");
        captura("usuarios_validacion.png");
        campo("txtCorreo", username + "@example.com");
        pulsar("btnGuardar");
        esperar(() -> {
            try {
                idCreado = dao.listar().stream().filter(u -> username.equals(u.getUsername())).mapToInt(Usuario::getIdUsuario).findFirst().orElse(0);
                return idCreado > 0 && textoContiene("lblStatus", "Datos actualizados");
            } catch (Exception ex) { throw new IllegalStateException(ex); }
        }, "Guardar usuario desde JavaFX");
        comprobar(true, "Botón Guardar crea una cuenta real y actualiza la tabla");
        campo("txtBusqueda", username);
        comprobar(fx(() -> ((TableView<Usuario>) escena.lookup("#tblUsuarios")).getItems().size() == 1), "Buscador filtra la tabla");
        seleccionar();
        pulsar("btnEditar");
        comprobar(texto("lblModo").contains(Integer.toString(idCreado)), "Botón Editar carga la fila en el formulario");
        campo("txtNombre", "Diego actualizado");
        pulsar("btnGuardar");
        esperar(() -> {
            try { return dao.buscarPorId(idCreado).orElseThrow().getNombreUsuario().equals("Diego actualizado")
                    && textoContiene("lblStatus", "Datos actualizados"); }
            catch (Exception ex) { throw new IllegalStateException(ex); }
        }, "Guardar edición desde JavaFX");
        comprobar(true, "Guardar cambios persiste la edición");
        seleccionar();
        pulsar("btnEstado");
        esperar(() -> estadoBD(false) && textoContiene("lblStatus", "Datos actualizados"), "Desactivar desde JavaFX");
        comprobar(true, "Botón Desactivar cambia el estado real");
        captura("usuarios_inactivo.png");
        seleccionar();
        pulsar("btnEstado");
        esperar(() -> estadoBD(true) && textoContiene("lblStatus", "Datos actualizados"), "Activar desde JavaFX");
        comprobar(true, "Botón Activar restaura la cuenta");
        seleccionar();
        pulsar("btnEliminar");
        esperar(() -> {
            try { return dao.buscarPorId(idCreado).isEmpty() && textoContiene("lblStatus", "Datos actualizados"); }
            catch (Exception ex) { throw new IllegalStateException(ex); }
        }, "Eliminar desde JavaFX");
        comprobar(true, "Botón Eliminar borra la cuenta de prueba y actualiza la tabla");
        idCreado = 0;
        campo("txtBusqueda", "");
        fx(() -> {
            escena.getWindow().setWidth(1000);
            escena.getWindow().setHeight(650);
            return null;
        });
        Thread.sleep(250);
        captura("usuarios_tamano_minimo.png");
        comprobar(fx(() -> {
            javafx.scene.Node guardar = escena.lookup("#btnGuardar");
            javafx.geometry.Bounds posicion = guardar.localToScene(guardar.getBoundsInLocal());
            return posicion.getMinY() >= 0 && posicion.getMaxY() <= escena.getHeight()
                    && posicion.getMinX() >= 0 && posicion.getMaxX() <= escena.getWidth();
        }), "Guardar permanece visible al tamaño mínimo");
        System.out.println("RESULTADO: " + comprobaciones + " comprobaciones de interfaz correctas.");
    }

    public static void main(String[] args) {
        if (!Boolean.getBoolean("cine.pruebas.bd")) {
            throw new IllegalStateException("Configura una BD de pruebas y agrega -Dcine.pruebas.bd=true para ejecutar.");
        }
        launch(args);
        if (fallo != null) { throw new AssertionError("Falló la prueba de JavaFX.", fallo); }
    }
}
