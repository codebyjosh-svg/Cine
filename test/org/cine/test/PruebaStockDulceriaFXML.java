package org.cine.test;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import javax.imageio.ImageIO;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import org.cine.dao.impl.UsuarioDAOImpl;
import org.cine.model.ClienteVinculo;
import org.cine.model.LineaVentaProducto;
import org.cine.model.Producto;
import org.cine.model.Usuario;
import org.cine.service.SesionContext;
import org.cine.system.Principal;
import org.cine.util.Conexion;

/** Prueba real de FXML, botones, carrito, factura y acceso por rol. Genera capturas. */
public final class PruebaStockDulceriaFXML extends Application {
    private static volatile Throwable fallo;
    private Stage stage;
    private int categoria, cliente, producto, venta, comprobaciones;
    private final String marca = "UI_US33_" + System.nanoTime();

    @Override
    public void start(Stage escenario) throws Exception {
        stage = escenario;
        new Principal().start(stage);
        Platform.setImplicitExit(false);
        Thread hilo = new Thread(() -> {
            try { probar(); }
            catch (Throwable ex) { fallo = ex; ex.printStackTrace(); }
            finally {
                try { limpiar(); } catch (Exception ex) { fallo = ex; ex.printStackTrace(); }
                Platform.runLater(() -> { SesionContext.cerrarSesion(); stage.close(); Platform.exit(); });
            }
        }, "prueba-us33-fxml");
        hilo.setDaemon(true);
        hilo.start();
    }

    @SuppressWarnings("unchecked")
    private void probar() throws Exception {
        categoria = insertar("INSERT INTO categorias_producto(nombre_categoria) VALUES (?)", marca);
        cliente = insertar("INSERT INTO clientes(nombre_cliente,apellido_cliente,correo_electronico) VALUES ('Ana','López',?)", marca + "@example.com");
        producto = insertar("INSERT INTO productos(nombre_producto,precio,stock,stock_minimo,id_categoria_producto) VALUES (?,10.25,7,7,?)",
                "Palomitas de prueba " + marca, categoria);
        comprobar("admin".equals(new UsuarioDAOImpl().autenticar("admin.cine", "AdminCine2026!").getNombreRol()),
                "Login autentica correctamente después de la actualización SQL");
        Usuario taquillero = new UsuarioDAOImpl().buscarPorUsername("taquilla.cine").orElseThrow();
        fx(() -> { SesionContext.iniciarSesion(taquillero); Principal.mostrarDashboardSegunRol(); return null; });
        comprobar(fx(() -> stage.getScene().lookup("#btnVentas") != null), "Dashboard del taquillero permite abrir ventas");
        pulsar("btnStockCritico");
        esperar(() -> listo("btnActualizar"), "carga stock");
        fx(() -> { ((TextField)nodo("txtFiltro")).setText(marca); return null; });
        comprobar(fx(() -> ((TableView<?>)nodo("tblStockCritico")).getItems().size() == 1),
                "Filtro muestra el producto con stock igual al mínimo");
        comprobar(fx(() -> !contieneScroll(stage.getScene().getRoot())), "Stock crítico usa scroll solo dentro de la tabla");
        capturar("01_stock_critico.png");
        fx(() -> { ((ComboBox<String>)nodo("cmbNivel")).setValue("Sin existencias"); return null; });
        comprobar(fx(() -> ((TableView<?>)nodo("tblStockCritico")).getItems().isEmpty()), "Filtro de agotados se aplica a la tabla");
        pulsar("btnVolver");
        pulsar("btnVentas");
        esperar(() -> listo("btnActualizar"), "carga ventas");
        fx(() -> {
            TableView<Producto> tabla = (TableView<Producto>)nodo("tblProductos");
            tabla.getItems().stream().filter(p -> p.getIdProducto() == producto).findFirst()
                    .ifPresent(p -> tabla.getSelectionModel().select(p));
            return null;
        });
        pulsar("btnAgregar");
        comprobar(fx(() -> texto("lblMensaje").contains("Seleccione un cliente")), "Agregar sin cliente muestra validación");
        fx(() -> {
            ComboBox<ClienteVinculo> combo = (ComboBox<ClienteVinculo>)nodo("cmbCliente");
            combo.getItems().stream().filter(c -> c.getIdCliente() == cliente).findFirst().ifPresent(combo::setValue);
            return null;
        });
        for (int i = 1; i <= 3; i++) {
            int esperada = i;
            pulsar("btnAgregar");
            esperar(() -> cantidad() == esperada && listo("btnActualizar"), "agregado " + i);
        }
        venta = fx(() -> Integer.parseInt(((TextField)nodo("txtIdVenta")).getText()));
        comprobar(fx(() -> ((TableView<?>)nodo("tblCarrito")).getItems().size() == 1) && cantidad() == 3,
                "Tres clics de 1x conservan una línea de cantidad 3");
        comprobar(fx(() -> "Q 30.75".equals(texto("lblSubtotalProductos")) && "Q 30.75".equals(texto("lblTotal"))),
                "La vista actualiza subtotal y total después de cada agregado");
        comprobar(fx(() -> !contieneScroll(stage.getScene().getRoot())), "Ventas no desplaza la escena principal");
        capturar("02_venta_dulceria.png");
        fx(() -> {
            ((TableView<?>)nodo("tblCarrito")).getSelectionModel().selectFirst();
            ((Spinner<Integer>)nodo("spnCantidadCarrito")).getValueFactory().setValue(9);
            return null;
        });
        pulsar("btnCantidad");
        esperar(() -> listo("btnActualizar") && textoSeguro("lblMensaje").toLowerCase().contains("stock insuficiente"), "cantidad insuficiente");
        comprobar(cantidad() == 3, "Edición con stock insuficiente conserva la cantidad del carrito");
        fx(() -> { ((Spinner<Integer>)nodo("spnCantidad")).getValueFactory().setValue(5); return null; });
        pulsar("btnAgregar");
        comprobar(fx(() -> texto("lblMensaje").contains("Stock insuficiente")) && cantidad() == 3,
                "La vista rechaza un agregado acumulado superior al stock");
        fx(() -> { ((Spinner<Integer>)nodo("spnCantidad")).getValueFactory().setValue(1); return null; });
        pulsar("btnConfirmar");
        esperar(() -> existe("tblFactura") && listo("btnImprimir"), "factura");
        comprobar(fx(() -> ((TableView<?>)nodo("tblFactura")).getItems().size() == 1
                && "Q 30.75".equals(texto("lblTotal"))), "La confirmación abre factura con una sola línea y total correcto");
        comprobar(fx(() -> texto("lblTaquillero").equals(taquillero.getNombreCompleto())),
                "La factura muestra el nombre real del vendedor");
        comprobar(fx(() -> !contieneScroll(stage.getScene().getRoot())), "Factura no desplaza la escena principal");
        capturar("03_factura_dulceria.png");
        pulsar("btnVolver");
        comprobar(fx(() -> stage.getScene().lookup("#btnVentas") != null), "Volver desde factura regresa al dashboard correcto");
        Usuario bodega = new UsuarioDAOImpl().buscarPorUsername("bodega.cine").orElseThrow();
        fx(() -> { SesionContext.iniciarSesion(bodega); Principal.mostrarDashboardSegunRol(); return null; });
        comprobar(fx(() -> stage.getScene().lookup("#btnStockCritico") != null), "Bodega tiene acceso al listado de stock crítico");
        comprobar(fx(() -> {
            try { Principal.mostrarVentas(); return false; } catch (IllegalStateException esperado) { return true; }
        }), "Bodega no puede entrar a ventas");
        Usuario usuarioCliente = new UsuarioDAOImpl().buscarPorUsername("cliente.cine").orElseThrow();
        fx(() -> { SesionContext.iniciarSesion(usuarioCliente); Principal.mostrarDashboardSegunRol(); return null; });
        comprobar(fx(() -> {
            try { Principal.mostrarStockCritico(); return false; } catch (IllegalStateException esperado) { return true; }
        }), "Cliente no puede entrar a stock crítico");
        Usuario admin = new UsuarioDAOImpl().buscarPorUsername("admin.cine").orElseThrow();
        fx(() -> { SesionContext.iniciarSesion(admin); Principal.mostrarDashboardSegunRol(); return null; });
        comprobar(fx(() -> stage.getScene().lookup("#btnStockCritico") != null
                && stage.getScene().lookup("#btnVentas") != null), "Administrador tiene los dos accesos integrados");
        System.out.println("RESULTADO: " + comprobaciones + " comprobaciones FXML correctas y 3 capturas.");
    }

    private Node nodo(String id) { return stage.getScene().lookup("#" + id); }
    private String texto(String id) { return ((Label)nodo(id)).getText(); }
    private String textoSeguro(String id) {
        try { return fx(() -> nodo(id) == null ? "" : texto(id)); } catch (Exception ex) { throw new IllegalStateException(ex); }
    }
    private boolean existe(String id) {
        try { return fx(() -> nodo(id) != null); } catch (Exception ex) { throw new IllegalStateException(ex); }
    }
    private boolean listo(String id) {
        try { return fx(() -> nodo(id) != null && !nodo(id).isDisable()); } catch (Exception ex) { throw new IllegalStateException(ex); }
    }
    @SuppressWarnings("unchecked") private int cantidad() {
        try {
            return fx(() -> {
                var tabla = (TableView<LineaVentaProducto>)nodo("tblCarrito");
                return tabla.getItems().isEmpty() ? 0 : tabla.getItems().get(0).getCantidad();
            });
        } catch (Exception ex) { throw new IllegalStateException(ex); }
    }
    private void pulsar(String id) throws Exception { fx(() -> { ((Button)nodo(id)).fire(); return null; }); }
    private <T> T fx(Callable<T> operacion) throws Exception {
        FutureTask<T> tarea = new FutureTask<>(operacion);
        Platform.runLater(tarea);
        return tarea.get(15, TimeUnit.SECONDS);
    }
    private void esperar(BooleanSupplier condicion, String descripcion) throws Exception {
        long limite = System.nanoTime() + TimeUnit.SECONDS.toNanos(15);
        while (System.nanoTime() < limite) { if (condicion.getAsBoolean()) { return; } Thread.sleep(50); }
        throw new AssertionError("Tiempo agotado: " + descripcion + ". " + textoSeguro("lblMensaje"));
    }
    private void comprobar(boolean condicion, String descripcion) {
        if (!condicion) { throw new AssertionError(descripcion); }
        System.out.println("OK " + (++comprobaciones) + " - " + descripcion);
    }
    private static boolean contieneScroll(Parent padre) {
        for (Node hijo : padre.getChildrenUnmodifiable()) {
            if (hijo instanceof ScrollPane) { return true; }
            if (hijo instanceof Parent p && contieneScroll(p)) { return true; }
        }
        return false;
    }
    private void capturar(String nombre) throws Exception {
        WritableImage imagen = fx(() -> stage.getScene().snapshot(null));
        BufferedImage png = new BufferedImage((int)imagen.getWidth(), (int)imagen.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < imagen.getHeight(); y++) {
            for (int x = 0; x < imagen.getWidth(); x++) { png.setRGB(x, y, imagen.getPixelReader().getArgb(x, y)); }
        }
        Path carpeta = Path.of("test/evidencia/US_3_3");
        Files.createDirectories(carpeta);
        ImageIO.write(png, "png", carpeta.resolve(nombre).toFile());
    }
    private static int insertar(String sql, Object... valores) throws Exception {
        try (Connection cn = Conexion.getInstancia().getConnection(); PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < valores.length; i++) { ps.setObject(i + 1, valores[i]); }
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); return rs.getInt(1); }
        }
    }
    private static void borrar(String sql, int id) throws Exception {
        try (Connection cn = Conexion.getInstancia().getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id); ps.executeUpdate();
        }
    }
    private void limpiar() throws Exception {
        if (venta > 0) {
            borrar("DELETE FROM movimientos_inventario WHERE id_venta=?", venta);
            borrar("DELETE FROM detalle_venta_productos WHERE id_venta=?", venta);
            borrar("DELETE FROM ventas WHERE id_venta=?", venta);
        }
        if (producto > 0) { borrar("DELETE FROM productos WHERE id_producto=?", producto); }
        if (categoria > 0) { borrar("DELETE FROM categorias_producto WHERE id_categoria_producto=?", categoria); }
        if (cliente > 0) { borrar("DELETE FROM clientes WHERE id_cliente=?", cliente); }
    }
    public static void main(String[] args) {
        launch(args);
        if (fallo != null) { throw new AssertionError("Fallaron las pruebas FXML de US-3.3", fallo); }
    }
}
