package org.cine.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.cine.dao.MovimientoInventarioDAO;
import org.cine.dao.impl.MovimientoInventarioDAOImpl;
import org.cine.model.MovimientoInventario;
import org.cine.model.Producto;
import org.cine.model.Usuario;
import org.cine.service.SesionContext;
import org.cine.system.Principal;

/** Entradas, salidas e historial de confitería de US-3.2. */
public class InventarioController {
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm:ss");

    @FXML private TableView<MovimientoInventario> tblMovimientos;
    @FXML private TableColumn<MovimientoInventario, Number> colId;
    @FXML private TableColumn<MovimientoInventario, String> colProducto;
    @FXML private TableColumn<MovimientoInventario, String> colUsuario;
    @FXML private TableColumn<MovimientoInventario, String> colTipo;
    @FXML private TableColumn<MovimientoInventario, Number> colCantidad;
    @FXML private TableColumn<MovimientoInventario, String> colFecha;
    @FXML private TableColumn<MovimientoInventario, String> colObservacion;
    @FXML private ComboBox<Producto> cmbProducto;
    @FXML private ComboBox<String> cmbTipo;
    @FXML private ComboBox<String> cmbFiltroTipo;
    @FXML private TextField txtCantidad;
    @FXML private TextArea txtObservacion;
    @FXML private TextField txtBusqueda;
    @FXML private Label lblUsuarioActual;
    @FXML private Label lblStockActual;
    @FXML private Label lblStockResultante;
    @FXML private Label lblResumen;
    @FXML private Label lblDetalle;
    @FXML private Label lblMensaje;
    @FXML private Button btnRegistrar;

    private final MovimientoInventarioDAO dao = new MovimientoInventarioDAOImpl();
    private final ObservableList<MovimientoInventario> movimientos = FXCollections.observableArrayList();
    private final FilteredList<MovimientoInventario> filtrados = new FilteredList<>(movimientos, m -> true);
    private boolean datosDisponibles;

    @FXML
    private void initialize() {
        Usuario usuario = exigirOperador();
        lblUsuarioActual.setText(usuario.getNombreCompleto() + " · " + usuario.getNombreRol());
        colId.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getIdMovimiento()));
        colProducto.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombreProducto()));
        colUsuario.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUsuario()));
        colTipo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTipoMovimiento()));
        colCantidad.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getCantidad()));
        colFecha.setCellValueFactory(d -> new SimpleStringProperty(FECHA.format(d.getValue().getFechaMovimiento())));
        colObservacion.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getObservacion()));
        colTipo.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(String tipo, boolean vacia) {
                super.updateItem(tipo, vacia);
                getStyleClass().removeAll("tipo-entrada", "tipo-salida", "tipo-venta", "tipo-devolucion");
                setText(vacia || tipo == null ? null : tituloTipo(tipo));
                if (!vacia && tipo != null) getStyleClass().add("tipo-" + tipo);
            }
        });
        tblMovimientos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tblMovimientos.setPlaceholder(new Label("No hay movimientos para los filtros seleccionados."));
        SortedList<MovimientoInventario> ordenados = new SortedList<>(filtrados);
        ordenados.comparatorProperty().bind(tblMovimientos.comparatorProperty());
        tblMovimientos.setItems(ordenados);
        tblMovimientos.getSelectionModel().selectedItemProperty().addListener((o, antes, actual) -> mostrarDetalle(actual));
        cmbTipo.setItems(FXCollections.observableArrayList("Entrada", "Salida"));
        cmbTipo.setValue("Entrada");
        cmbFiltroTipo.setItems(FXCollections.observableArrayList("Todos", "Entradas", "Salidas", "Ventas", "Devoluciones"));
        cmbFiltroTipo.setValue("Todos");
        cmbFiltroTipo.valueProperty().addListener((o, antes, actual) -> filtrar());
        txtBusqueda.textProperty().addListener((o, antes, actual) -> filtrar());
        cmbProducto.valueProperty().addListener((o, antes, actual) -> actualizarStock());
        cmbTipo.valueProperty().addListener((o, antes, actual) -> actualizarStock());
        txtCantidad.textProperty().addListener((o, antes, actual) -> actualizarStock());
        mostrarDetalle(null);
        actualizarStock();
        try {
            cargarDatos();
            mensaje(cmbProducto.getItems().isEmpty() ? "No hay productos activos para registrar movimientos."
                    : "Selecciona un producto y registra una entrada o salida.", false);
        } catch (SQLException ex) {
            bloquearRegistro();
            mensaje(explicarSql(ex), true);
        } catch (IllegalStateException ex) {
            bloquearRegistro();
            mensaje("No se pudo cargar la configuración de conexión. Revisa db.properties.", true);
        }
    }

    private Usuario exigirOperador() {
        Usuario actual = SesionContext.getUsuarioActual();
        if (actual == null || !actual.isEstado() || actual.getIdUsuario() <= 0
                || !("admin".equals(actual.getNombreRol()) || "bodega".equals(actual.getNombreRol()))) {
            throw new IllegalStateException("El inventario requiere una sesión activa de administrador o bodega.");
        }
        return actual;
    }

    private void cargarDatos() throws SQLException {
        List<Producto> productos = dao.listarProductosActivos();
        List<MovimientoInventario> historial = dao.listarHistorial();
        Producto anterior = cmbProducto.getValue();
        cmbProducto.setItems(FXCollections.observableArrayList(productos));
        cmbProducto.setValue(anterior == null ? null : productos.stream()
                .filter(p -> p.getIdProducto() == anterior.getIdProducto()).findFirst().orElse(null));
        movimientos.setAll(historial);
        datosDisponibles = true;
        btnRegistrar.setDisable(productos.isEmpty());
        filtrar();
        actualizarStock();
    }

    @FXML
    private void registrarMovimiento() {
        try {
            Usuario usuario = exigirOperador();
            if (!datosDisponibles) throw new IllegalArgumentException("Pulsa Actualizar para cargar los datos antes de registrar.");
            Producto elegido = cmbProducto.getValue();
            if (elegido == null) throw new IllegalArgumentException("Selecciona un producto activo.");
            String tipo = leerTipo();
            int cantidad = leerCantidad();
            String observacion = txtObservacion.getText() == null ? "" : txtObservacion.getText().strip();
            if (observacion.isBlank()) throw new IllegalArgumentException("Escribe una observación para el movimiento.");
            if (observacion.codePointCount(0, observacion.length()) > 200) {
                throw new IllegalArgumentException("La observación puede tener hasta 200 caracteres.");
            }
            Producto actual = dao.buscarProducto(elegido.getIdProducto())
                    .orElseThrow(() -> new IllegalArgumentException("El producto ya no existe. Pulsa Actualizar."));
            if (actual.getEstado() != 1) throw new IllegalArgumentException("El producto está inactivo. Pulsa Actualizar.");
            elegido.setStock(actual.getStock());
            actualizarStock();
            if ("salida".equals(tipo) && cantidad > actual.getStock()) {
                throw new IllegalArgumentException("Stock insuficiente: hay " + actual.getStock()
                        + " unidades y solicitaste " + cantidad + ".");
            }
            if ("entrada".equals(tipo) && (long) actual.getStock() + cantidad > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("La entrada supera el máximo de unidades permitido para el stock.");
            }
            dao.registrar(new MovimientoInventario(actual.getIdProducto(), usuario.getIdUsuario(),
                    tipo, cantidad, observacion));
            // El registro ya se confirmó: limpiar evita repetirlo si falla la recarga.
            txtCantidad.clear();
            txtObservacion.clear();
            try {
                cargarDatos();
                String stock = cmbProducto.getValue() == null ? "" : " Stock actual: " + cmbProducto.getValue().getStock() + ".";
                mensaje(tituloTipo(tipo) + " de " + cantidad + " unidades registrada correctamente." + stock, false);
            } catch (SQLException ex) {
                bloquearRegistro();
                mensaje("El movimiento quedó registrado. " + explicarSql(ex) + " Pulsa Actualizar.", true);
            }
        } catch (SQLException ex) {
            mensaje(explicarSql(ex), true);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            mensaje(ex.getMessage(), true);
        }
    }

    @FXML
    private void actualizarLista() {
        try {
            exigirOperador();
            cargarDatos();
            mensaje(cmbProducto.getItems().isEmpty() ? "No hay productos activos para registrar movimientos."
                    : "Productos, stock e historial actualizados.", false);
        } catch (SQLException ex) {
            bloquearRegistro();
            mensaje(explicarSql(ex), true);
        } catch (IllegalStateException ex) {
            bloquearRegistro();
            mensaje(ex.getMessage(), true);
        }
    }

    @FXML
    private void limpiarFormulario() {
        cmbProducto.getSelectionModel().clearSelection();
        cmbTipo.setValue("Entrada");
        txtCantidad.clear();
        txtObservacion.clear();
        mensaje("Formulario listo para un nuevo movimiento.", false);
    }

    @FXML
    private void volver() throws IOException {
        Principal.mostrarDashboardSegunRol();
    }

    private int leerCantidad() {
        String texto = txtCantidad.getText() == null ? "" : txtCantidad.getText().strip();
        if (!texto.matches("[0-9]+")) throw new IllegalArgumentException("La cantidad debe ser un número entero mayor que cero.");
        try {
            int cantidad = Integer.parseInt(texto);
            if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser un número entero mayor que cero.");
            return cantidad;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("La cantidad supera el máximo permitido de 2147483647 unidades.");
        }
    }

    private String leerTipo() {
        if (!"Entrada".equals(cmbTipo.getValue()) && !"Salida".equals(cmbTipo.getValue())) {
            throw new IllegalArgumentException("Selecciona Entrada o Salida.");
        }
        return cmbTipo.getValue().toLowerCase(Locale.ROOT);
    }

    private void actualizarStock() {
        Producto producto = cmbProducto.getValue();
        lblStockActual.setText(producto == null ? "—" : Integer.toString(producto.getStock()));
        lblStockResultante.getStyleClass().removeAll("stock-ok", "stock-error");
        if (producto == null) {
            lblStockResultante.setText("Selecciona un producto para consultar su stock.");
            return;
        }
        try {
            int cantidad = leerCantidad();
            long resultado = (long) producto.getStock() + ("entrada".equals(leerTipo()) ? cantidad : -cantidad);
            boolean invalido = resultado < 0 || resultado > Integer.MAX_VALUE;
            lblStockResultante.setText(resultado < 0 ? "Stock insuficiente para esta salida."
                    : resultado > Integer.MAX_VALUE ? "La entrada supera el stock máximo permitido."
                    : "Stock después: " + resultado + " unidades.");
            lblStockResultante.getStyleClass().add(invalido ? "stock-error" : "stock-ok");
        } catch (IllegalArgumentException ex) {
            lblStockResultante.setText("Escribe una cantidad positiva para calcular el stock después.");
        }
    }

    private void filtrar() {
        String texto = txtBusqueda.getText() == null ? "" : txtBusqueda.getText().strip().toLowerCase(Locale.ROOT);
        String tipo = switch (cmbFiltroTipo.getValue() == null ? "Todos" : cmbFiltroTipo.getValue()) {
            case "Entradas" -> "entrada";
            case "Salidas" -> "salida";
            case "Ventas" -> "venta";
            case "Devoluciones" -> "devolucion";
            default -> "";
        };
        filtrados.setPredicate(m -> (tipo.isEmpty() || tipo.equals(m.getTipoMovimiento()))
                && (texto.isEmpty() || (m.getIdMovimiento() + " " + m.getNombreProducto() + " "
                        + m.getUsuario() + " " + m.getObservacion()).toLowerCase(Locale.ROOT).contains(texto)));
        lblResumen.setText(filtrados.size() + " de " + movimientos.size() + " movimientos · Historial conservado");
    }

    private void mostrarDetalle(MovimientoInventario movimiento) {
        lblDetalle.setText(movimiento == null ? "Selecciona una fila para leer su observación completa."
                : "#" + movimiento.getIdMovimiento() + " · " + tituloTipo(movimiento.getTipoMovimiento()) + " · "
                        + movimiento.getUsuario() + " · " + FECHA.format(movimiento.getFechaMovimiento())
                        + " · " + movimiento.getObservacion());
    }

    private void bloquearRegistro() {
        datosDisponibles = false;
        btnRegistrar.setDisable(true);
    }

    private static String tituloTipo(String tipo) {
        return "devolucion".equals(tipo) ? "Devolución" : tipo.substring(0, 1).toUpperCase(Locale.ROOT) + tipo.substring(1);
    }

    private String explicarSql(SQLException ex) {
        String estado = ex.getSQLState() == null ? "" : ex.getSQLState();
        if ("45000".equals(estado)) return ex.getMessage();
        if (ex.getErrorCode() == 1305) return "Falta un procedimiento de inventario en la base. La integración SQL está pendiente.";
        if (ex.getErrorCode() == 1045) return "MySQL rechazó el usuario o la contraseña. Revisa db.properties.";
        if (ex.getErrorCode() == 1049) return "La base configurada no existe. Revisa db.properties.";
        if (estado.startsWith("08")) return "No se pudo conectar con MySQL. Revisa el servidor y la dirección en db.properties.";
        if (ex.getErrorCode() == 1146 || ex.getErrorCode() == 1054) return "La estructura de inventario no coincide con la aplicación. Revisa la integración SQL pendiente.";
        if ("22003".equals(estado) || ex.getErrorCode() == 1264) return "El movimiento supera el máximo de unidades permitido para el stock.";
        if (ex.getErrorCode() == 1406) return "La observación supera la longitud permitida.";
        if ("40001".equals(estado) || ex.getErrorCode() == 1205) return "Otra operación está usando estos datos. Actualiza el historial e inténtalo nuevamente.";
        if (estado.startsWith("23")) return "No se pudo registrar por una restricción de la base. Revisa el producto, la cantidad y la observación.";
        return "No se pudo completar la operación de inventario en MySQL. Código: " + ex.getErrorCode() + ".";
    }

    private void mensaje(String texto, boolean error) {
        lblMensaje.setText(texto);
        lblMensaje.getStyleClass().removeAll("status-ok", "status-error");
        lblMensaje.getStyleClass().add(error ? "status-error" : "status-ok");
    }
}
