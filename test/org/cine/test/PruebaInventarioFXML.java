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
import javafx.geometry.Bounds;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import org.cine.dao.impl.MovimientoInventarioDAOImpl;
import org.cine.dao.impl.UsuarioDAOImpl;
import org.cine.model.MovimientoInventario;
import org.cine.model.Producto;
import org.cine.model.Usuario;
import org.cine.service.SesionContext;
import org.cine.system.Principal;

/** Flujo real con controles JavaFX, persistencia y capturas de T3.2.12. */
public class PruebaInventarioFXML extends Application {
    private static volatile Throwable fallo;
    private final MovimientoInventarioDAOImpl dao = new MovimientoInventarioDAOImpl();
    private Stage escenario;
    private int comprobaciones;

    @Override
    public void start(Stage stage) throws Exception {
        escenario = stage;
        new Principal().start(stage);
        Platform.setImplicitExit(false);
        Thread pruebas = new Thread(() -> {
            try { probar(); }
            catch (Throwable ex) { fallo = ex; ex.printStackTrace(); }
            finally {
                Platform.runLater(() -> {
                    SesionContext.cerrarSesion();
                    stage.close();
                    Platform.exit();
                });
            }
        }, "pruebas-inventario");
        pruebas.setDaemon(true);
        pruebas.start();
    }

    private <T> T fx(Callable<T> accion) throws Exception {
        FutureTask<T> tarea = new FutureTask<>(accion);
        Platform.runLater(tarea);
        return tarea.get(20, TimeUnit.SECONDS);
    }

    private void comprobar(boolean valor, String mensaje) {
        if (!valor) throw new AssertionError(mensaje);
        System.out.println("OK " + (++comprobaciones) + " · " + mensaje);
    }

    private void pulsar(String id) throws Exception {
        fx(() -> { ((Button) escenario.getScene().lookup("#" + id)).fire(); return null; });
    }

    private String mensaje() throws Exception {
        return fx(() -> ((Label) escenario.getScene().lookup("#lblMensaje")).getText());
    }

    @SuppressWarnings("unchecked")
    private void formulario(int producto, String tipo, String cantidad, String observacion) throws Exception {
        fx(() -> {
            ComboBox<Producto> productos = (ComboBox<Producto>) escenario.getScene().lookup("#cmbProducto");
            productos.setValue(productos.getItems().stream().filter(p -> p.getIdProducto() == producto).findFirst().orElseThrow());
            ((ComboBox<String>) escenario.getScene().lookup("#cmbTipo")).setValue(tipo);
            ((TextField) escenario.getScene().lookup("#txtCantidad")).setText(cantidad);
            ((TextArea) escenario.getScene().lookup("#txtObservacion")).setText(observacion);
            return null;
        });
    }

    @SuppressWarnings("unchecked")
    private void seleccionar(int id) throws Exception {
        fx(() -> {
            TableView<MovimientoInventario> tabla = (TableView<MovimientoInventario>) escenario.getScene().lookup("#tblMovimientos");
            MovimientoInventario fila = tabla.getItems().stream().filter(m -> m.getIdMovimiento() == id).findFirst().orElseThrow();
            tabla.getSelectionModel().select(fila);
            tabla.scrollTo(fila);
            return null;
        });
    }

    private MovimientoInventario movimiento(int producto, String observacion) throws Exception {
        return dao.listarHistorial().stream().filter(m -> m.getIdProducto() == producto
                && observacion.equals(m.getObservacion())).findFirst().orElseThrow();
    }

    private int stock(int producto) throws Exception {
        return dao.buscarProducto(producto).orElseThrow().getStock();
    }

    private long cantidadMovimientos(int producto) throws Exception {
        return dao.listarHistorial().stream().filter(m -> m.getIdProducto() == producto).count();
    }

    private void capturar(String nombre) throws Exception {
        WritableImage imagen = fx(() -> {
            escenario.getScene().getRoot().applyCss();
            escenario.getScene().getRoot().layout();
            return escenario.getScene().snapshot(null);
        });
        BufferedImage archivo = new BufferedImage((int) imagen.getWidth(), (int) imagen.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < archivo.getHeight(); y++) {
            for (int x = 0; x < archivo.getWidth(); x++) archivo.setRGB(x, y, imagen.getPixelReader().getArgb(x, y));
        }
        Path destino = Path.of("test/evidencia/us_3_2", nombre);
        Files.createDirectories(destino.getParent());
        ImageIO.write(archivo, "png", destino.toFile());
    }

    @SuppressWarnings("unchecked")
    private void probar() throws Exception {
        try (DatosInventarioPrueba d = new DatosInventarioPrueba()) {
            comprobar(fx(() -> {
                try { Principal.mostrarInventario(); return false; }
                catch (IllegalStateException ex) { return true; }
            }), "Inventario bloqueado sin sesión");
            UsuarioDAOImpl usuarios = new UsuarioDAOImpl();
            SesionContext.iniciarSesion(usuarios.autenticar("taquilla.cine", "TaquillaCine2026!"));
            comprobar(fx(() -> {
                try { Principal.mostrarInventario(); return false; }
                catch (IllegalStateException ex) { return true; }
            }), "Inventario bloqueado para taquillero");
            Usuario bodega = usuarios.autenticar("bodega.cine", "BodegaCine2026!");
            SesionContext.iniciarSesion(bodega);
            fx(() -> { Principal.mostrarDashboardSegunRol(); return null; });
            pulsar("btnInventario");
            comprobar(fx(() -> escenario.getScene().lookup("#tblMovimientos") != null),
                    "El dashboard de Bodega abre Inventario.fxml");
            comprobar(fx(() -> {
                escenario.getScene().getRoot().applyCss();
                escenario.getScene().getRoot().layout();
                for (String id : new String[]{"btnRegistrar", "btnLimpiar", "btnActualizar", "btnVolver"}) {
                    Button boton = (Button) escenario.getScene().lookup("#" + id);
                    Bounds limites = boton.localToScene(boton.getBoundsInLocal());
                    if (limites.getMinX() < 0 || limites.getMinY() < 0
                            || limites.getMaxX() > escenario.getScene().getWidth()
                            || limites.getMaxY() > escenario.getScene().getHeight()) return false;
                }
                return true;
            }), "Todos los botones quedan dentro de la escena");
            comprobar(fx(() -> ((ComboBox<Producto>) escenario.getScene().lookup("#cmbProducto")).getItems().stream()
                    .noneMatch(p -> p.getIdProducto() == d.productoInactivo)), "El formulario excluye productos inactivos");
            pulsar("btnRegistrar");
            comprobar(mensaje().contains("Selecciona un producto"), "Registrar exige elegir un producto");
            formulario(d.producto, "Entrada", "5", "Recepción desde pantalla");
            comprobar(fx(() -> ((Label) escenario.getScene().lookup("#lblStockActual")).getText().equals("10")),
                    "Seleccionar el producto muestra el stock actual");
            pulsar("btnRegistrar");
            comprobar(stock(d.producto) == 15 && mensaje().contains("registrada correctamente"),
                    "Registrar entrada aumenta el stock y actualiza la pantalla");
            MovimientoInventario entrada = movimiento(d.producto, "Recepción desde pantalla");
            seleccionar(entrada.getIdMovimiento());
            comprobar(fx(() -> {
                TableView<MovimientoInventario> tabla = (TableView<MovimientoInventario>) escenario.getScene().lookup("#tblMovimientos");
                TableColumn<MovimientoInventario, String> usuario = (TableColumn<MovimientoInventario, String>) tabla.getColumns().get(2);
                TableColumn<MovimientoInventario, String> fecha = (TableColumn<MovimientoInventario, String>) tabla.getColumns().get(5);
                MovimientoInventario seleccionada = tabla.getSelectionModel().getSelectedItem();
                return entrada.getIdUsuario() == d.bodega && usuario.getCellData(seleccionada).equals(bodega.getNombreCompleto())
                        && !fecha.getCellData(seleccionada).isBlank()
                        && ((Label) escenario.getScene().lookup("#lblDetalle")).getText().contains("Recepción desde pantalla");
            }), "La tabla muestra el usuario de sesión, fecha, tipo y observación del movimiento");
            capturar("entrada_inventario.png");
            formulario(d.producto, "Salida", "3", "Salida desde pantalla");
            pulsar("btnRegistrar");
            comprobar(stock(d.producto) == 12 && mensaje().contains("Salida de 3"), "Registrar salida disminuye el stock y conserva el historial");
            seleccionar(movimiento(d.producto, "Salida desde pantalla").getIdMovimiento());
            capturar("salida_inventario.png");
            formulario(d.producto, "Entrada", "2.5", "Decimal inválido");
            pulsar("btnRegistrar");
            comprobar(mensaje().contains("número entero"), "Cantidad decimal rechazada en el formulario");
            formulario(d.producto, "Entrada", "0", "Cantidad cero");
            pulsar("btnRegistrar");
            comprobar(mensaje().contains("mayor que cero"), "Cantidad cero rechazada en el formulario");
            formulario(d.producto, "Entrada", "2147483648", "Fuera de rango");
            pulsar("btnRegistrar");
            comprobar(mensaje().contains("máximo permitido"), "Cantidad fuera del rango entero rechazada");
            formulario(d.producto, "Entrada", "1", "  ");
            pulsar("btnRegistrar");
            comprobar(mensaje().contains("observación"), "Una observación vacía no permite registrar");
            formulario(d.producto, "Entrada", "1", "x".repeat(201));
            pulsar("btnRegistrar");
            comprobar(mensaje().contains("200 caracteres"), "Una observación demasiado larga no permite registrar");
            formulario(d.producto, "Salida", "13", "Salida sin existencias suficientes");
            pulsar("btnRegistrar");
            comprobar(mensaje().contains("Stock insuficiente") && stock(d.producto) == 12 && cantidadMovimientos(d.producto) == 2,
                    "Una salida insuficiente muestra el motivo y no modifica stock ni historial");
            capturar("stock_insuficiente.png");
            dao.registrarSalida(d.producto, d.admin, 5, "Auditoría externa de prueba");
            formulario(d.producto, "Salida", "10", "Stock visible desactualizado");
            pulsar("btnRegistrar");
            comprobar(mensaje().contains("hay 7 unidades") && stock(d.producto) == 7 && cantidadMovimientos(d.producto) == 3,
                    "Antes de una salida se relee el stock cambiado por otro operador");
            pulsar("btnActualizar");
            comprobar(fx(() -> ((TableView<MovimientoInventario>) escenario.getScene().lookup("#tblMovimientos")).getItems().stream()
                    .anyMatch(m -> m.getIdProducto() == d.producto && m.getIdUsuario() == d.admin)),
                    "Actualizar incorpora al historial los movimientos de otro usuario");
            fx(() -> { ((ComboBox<String>) escenario.getScene().lookup("#cmbFiltroTipo")).setValue("Salidas"); return null; });
            comprobar(fx(() -> {
                TableView<MovimientoInventario> tabla = (TableView<MovimientoInventario>) escenario.getScene().lookup("#tblMovimientos");
                return !tabla.getItems().isEmpty() && tabla.getItems().stream().allMatch(m -> "salida".equals(m.getTipoMovimiento()));
            }), "El filtro Salidas muestra únicamente movimientos de salida");
            fx(() -> { ((TextField) escenario.getScene().lookup("#txtBusqueda")).setText("Auditoría externa de prueba"); return null; });
            comprobar(fx(() -> ((TableView<?>) escenario.getScene().lookup("#tblMovimientos")).getItems().size() == 1),
                    "La búsqueda encuentra la observación del movimiento");
            seleccionar(movimiento(d.producto, "Auditoría externa de prueba").getIdMovimiento());
            comprobar(fx(() -> ((Label) escenario.getScene().lookup("#lblDetalle")).getText().contains("Auditoría externa de prueba")),
                    "Seleccionar una fila muestra su observación completa");
            fx(() -> {
                ((ComboBox<String>) escenario.getScene().lookup("#cmbFiltroTipo")).setValue("Todos");
                ((TextField) escenario.getScene().lookup("#txtBusqueda")).setText("Palomitas prueba " + d.sufijo);
                ((TextField) escenario.getScene().lookup("#txtCantidad")).clear();
                ((TextArea) escenario.getScene().lookup("#txtObservacion")).clear();
                return null;
            });
            seleccionar(movimiento(d.producto, "Auditoría externa de prueba").getIdMovimiento());
            capturar("historial_inventario.png");
            pulsar("btnLimpiar");
            comprobar(fx(() -> ((ComboBox<?>) escenario.getScene().lookup("#cmbProducto")).getValue() == null
                    && ((TextField) escenario.getScene().lookup("#txtCantidad")).getText().isEmpty()
                    && ((TextArea) escenario.getScene().lookup("#txtObservacion")).getText().isEmpty()),
                    "Limpiar prepara un formulario nuevo");
            pulsar("btnVolver");
            comprobar(fx(() -> escenario.getScene().lookup("#btnInventario") != null
                    && "bodega".equals(SesionContext.getRolActual())), "Volver conserva la sesión y regresa a Bodega");
            SesionContext.iniciarSesion(usuarios.autenticar("admin.cine", "AdminCine2026!"));
            fx(() -> { Principal.mostrarDashboardSegunRol(); return null; });
            pulsar("btnInventario");
            comprobar(fx(() -> escenario.getScene().lookup("#tblMovimientos") != null), "El administrador también puede abrir Inventario");
            SesionContext.iniciarSesion(usuarios.autenticar("cliente.cine", "ClienteCine2026!"));
            fx(() -> { Principal.mostrarDashboardSegunRol(); return null; });
            comprobar(fx(() -> {
                if (escenario.getScene().lookup("#btnInventario") != null) return false;
                try { Principal.mostrarInventario(); return false; }
                catch (IllegalStateException ex) { return true; }
            }), "El cliente no ve el botón y no puede abrir el inventario");
        }
        System.out.println("RESULTADO: " + comprobaciones + " comprobaciones de interfaz correctas y 4 capturas generadas.");
    }

    public static void main(String[] args) {
        if (!Boolean.getBoolean("cine.pruebas.bd")) throw new IllegalStateException("Usa una base de pruebas y -Dcine.pruebas.bd=true.");
        launch(args);
        if (fallo != null) throw new AssertionError("Falló la prueba de inventario", fallo);
    }
}
