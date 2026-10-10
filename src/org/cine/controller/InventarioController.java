package org.cine.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import org.cine.dao.impl.MovimientoInventarioDAOImpl;
import org.cine.dao.impl.ProductoDAOImpl;
import org.cine.model.MovimientoInventario;
import org.cine.model.Producto;
import org.cine.model.Usuario;
import org.cine.service.SesionContext;
import org.cine.system.Principal;

public class InventarioController {
    @FXML private ComboBox<Producto> productos;
    @FXML private TextField cantidad;
    @FXML private TextField observacion;
    @FXML private ComboBox<String> tipo;
    @FXML private Label mensaje;
    @FXML private TableView<MovimientoInventario> tabla;
    @FXML private TableColumn<MovimientoInventario, String> colProducto;
    @FXML private TableColumn<MovimientoInventario, String> colTipo;
    @FXML private TableColumn<MovimientoInventario, String> colCantidad;
    @FXML private TableColumn<MovimientoInventario, String> colFecha;
    @FXML private TableColumn<MovimientoInventario, String> colUsuario;
    @FXML private TableColumn<MovimientoInventario, String> colObservacion;

    private final ProductoDAOImpl pdao = new ProductoDAOImpl();
    private final MovimientoInventarioDAOImpl dao = new MovimientoInventarioDAOImpl();

    @FXML
    private void initialize() {
        productos.setConverter(new StringConverter<>() {
            @Override public String toString(Producto p) {
                return p == null ? "" : p.nombre() + " (" + p.stock() + " disponibles)";
            }
            @Override public Producto fromString(String valor) { return null; }
        });
        tipo.setItems(FXCollections.observableArrayList("ENTRADA", "SALIDA"));
        tipo.setValue("ENTRADA");
        colProducto.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().nombreProducto()));
        colTipo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().tipo()));
        colCantidad.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().cantidad())));
        colFecha.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().fecha().toString().replace('T', ' ')));
        colUsuario.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().nombreUsuario()));
        colObservacion.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().observacion()));
        actualizar();
    }

    private Producto recargarDatos(Integer idSeleccionado) throws SQLException {
        var actualizados = pdao.listar();
        productos.setItems(FXCollections.observableArrayList(actualizados));
        Producto elegido = null;
        if (idSeleccionado != null) {
            for (Producto p : actualizados) {
                if (p.id() == idSeleccionado) {
                    elegido = p;
                    productos.setValue(p);
                    break;
                }
            }
        }
        tabla.setItems(FXCollections.observableArrayList(dao.listar()));
        return elegido;
    }

    @FXML
    private void actualizar() {
        try {
            Producto seleccionado = productos.getValue();
            recargarDatos(seleccionado == null ? null : seleccionado.id());
            mensaje.setText("Inventario e historial actualizados.");
        } catch (SQLException ex) {
            mensaje.setText("Error al actualizar: " + ex.getMessage());
        }
    }

    @FXML
    private void registrar() {
        try {
            Usuario usuario = SesionContext.getUsuarioActual();
            if (usuario == null || !usuario.isEstado()) {
                throw new IllegalStateException("Debes iniciar sesión con una cuenta activa.");
            }
            String rol = usuario.getNombreRol() == null ? ""
                    : usuario.getNombreRol().trim().toLowerCase(Locale.ROOT);
            if (!rol.equals("admin") && !rol.equals("bodega")) {
                throw new IllegalStateException("Solo administrador o bodega pueden registrar movimientos.");
            }
            Producto producto = productos.getValue();
            if (producto == null) {
                throw new IllegalArgumentException("Selecciona un producto.");
            }
            String movimiento = tipo.getValue();
            if (!"ENTRADA".equals(movimiento) && !"SALIDA".equals(movimiento)) {
                throw new IllegalArgumentException("Selecciona ENTRADA o SALIDA.");
            }
            int unidades;
            try {
                unidades = Integer.parseInt(cantidad.getText().trim());
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Ingresa una cantidad entera válida.");
            }
            if (unidades <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
            }
            String nota = observacion.getText() == null ? "" : observacion.getText().trim();
            if (nota.isEmpty()) {
                throw new IllegalArgumentException("Escribe la observación del movimiento.");
            }
            int stockAnterior = producto.stock();
            dao.registrar(producto.id(), usuario.getIdUsuario(), movimiento, unidades, nota);
            Producto recargado = recargarDatos(producto.id());
            cantidad.clear();
            observacion.clear();
            if (recargado != null) {
                mensaje.setText("Movimiento guardado. Stock: " + stockAnterior
                        + " → " + recargado.stock() + " unidades.");
            } else {
                mensaje.setText("Movimiento guardado. El producto ya no aparece en el listado.");
            }
        } catch (Exception ex) {
            mensaje.setText("Error: " + ex.getMessage());
        }
    }

    @FXML
    private void volver() throws IOException {
        Principal.mostrarDashboardSegunRol();
    }
}
