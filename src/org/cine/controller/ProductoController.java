package org.cine.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import org.cine.dao.CategoriaProductoDAO;
import org.cine.dao.ProductoDAO;
import org.cine.dao.impl.CategoriaProductoDAOImpl;
import org.cine.dao.impl.ProductoDAOImpl;
import org.cine.model.CategoriaProducto;
import org.cine.model.Producto;
import org.cine.system.Principal;

public class ProductoController {

    @FXML private ComboBox<CategoriaProducto> cboCategoriaProducto;
    @FXML private TextField txtNombreProducto;
    @FXML private TextField txtPrecioProducto;
    @FXML private TextField txtStockProducto;
    @FXML private TextField txtStockMinimoProducto;
    @FXML private TextArea txtDescripcionProducto;
    @FXML private Label lblEstadoProducto;

    @FXML private TableView<Producto> tbProductos;
    @FXML private TableColumn<Producto, Integer> colIdProducto;
    @FXML private TableColumn<Producto, String> colNombreCategoria;
    @FXML private TableColumn<Producto, String> colNombreProducto;
    @FXML private TableColumn<Producto, BigDecimal> colPrecioProducto;
    @FXML private TableColumn<Producto, Integer> colStockProducto;
    @FXML private TableColumn<Producto, Integer> colStockMinimoProducto;
    @FXML private TableColumn<Producto, String> colEstadoProducto;

    private final CategoriaProductoDAO categoriaDAO =
            new CategoriaProductoDAOImpl();
    private final ProductoDAO productoDAO = new ProductoDAOImpl();
    private Producto productoSeleccionado;

    @FXML
    private void initialize() {
        configurarTabla();
        cargarCategoriasActivas();
        cargarProductos();
    }

    private void configurarTabla() {
        colIdProducto.setCellValueFactory(
                new PropertyValueFactory<>("idProducto"));
        colNombreCategoria.setCellValueFactory(
                new PropertyValueFactory<>("nombreCategoria"));
        colNombreProducto.setCellValueFactory(
                new PropertyValueFactory<>("nombreProducto"));
        colPrecioProducto.setCellValueFactory(
                new PropertyValueFactory<>("precio"));
        colStockProducto.setCellValueFactory(
                new PropertyValueFactory<>("stock"));
        colStockMinimoProducto.setCellValueFactory(
                new PropertyValueFactory<>("stockMinimo"));
        colEstadoProducto.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        datos.getValue().getEstado() == 1
                                ? "Activo" : "Inactivo"));
    }

    private void cargarCategoriasActivas() {
        List<CategoriaProducto> categorias = categoriaDAO.listarTodos();
        List<CategoriaProducto> activas = new ArrayList<>();
        for (CategoriaProducto categoria : categorias) {
            if (categoria.getEstado() == 1) {
                activas.add(categoria);
            }
        }
        cboCategoriaProducto.setItems(
                FXCollections.observableArrayList(activas));

        String error = categoriaDAO.getUltimoError();
        if (error != null && !error.trim().isEmpty()) {
            mostrarMensaje(Alert.AlertType.ERROR,
                    "No se pudieron cargar las categorías activas.\n" + error);
        }
    }

    @FXML
    private void cargarProductos() {
        List<Producto> productos = productoDAO.listarTodos();
        tbProductos.setItems(FXCollections.observableArrayList(productos));

        String error = productoDAO.getUltimoError();
        if (error != null && !error.trim().isEmpty()) {
            mostrarMensaje(Alert.AlertType.ERROR,
                    "No se pudieron cargar los productos.\n" + error);
        }
    }

    @FXML
    private void seleccionarProducto() {
        productoSeleccionado = tbProductos.getSelectionModel()
                .getSelectedItem();
        if (productoSeleccionado == null) {
            return;
        }

        txtNombreProducto.setText(productoSeleccionado.getNombreProducto());
        txtPrecioProducto.setText(
                productoSeleccionado.getPrecio() == null
                        ? "" : productoSeleccionado.getPrecio().toPlainString());
        txtStockProducto.setText(
                String.valueOf(productoSeleccionado.getStock()));
        txtStockMinimoProducto.setText(
                String.valueOf(productoSeleccionado.getStockMinimo()));
        txtDescripcionProducto.setText(
                productoSeleccionado.getDescripcion() == null
                        ? "" : productoSeleccionado.getDescripcion());
        lblEstadoProducto.setText(
                productoSeleccionado.getEstado() == 1
                        ? "Activo" : "Inactivo");

        cboCategoriaProducto.getSelectionModel().clearSelection();
        for (CategoriaProducto categoria : cboCategoriaProducto.getItems()) {
            if (categoria.getIdCategoriaProducto()
                    == productoSeleccionado.getIdCategoriaProducto()) {
                cboCategoriaProducto.setValue(categoria);
                break;
            }
        }
    }

    @FXML
    private void limpiarFormulario() {
        productoSeleccionado = null;
        tbProductos.getSelectionModel().clearSelection();
        cboCategoriaProducto.getSelectionModel().clearSelection();
        txtNombreProducto.clear();
        txtPrecioProducto.clear();
        txtStockProducto.clear();
        txtStockMinimoProducto.clear();
        txtDescripcionProducto.clear();
        lblEstadoProducto.setText("Nuevo - se guardará como Activo");
    }

    @FXML private void guardarProducto() { mostrarPendiente(); }
    @FXML private void editarProducto() { mostrarPendiente(); }
    @FXML private void eliminarProducto() { mostrarPendiente(); }
    @FXML private void activarProducto() { mostrarPendiente(); }
    @FXML private void desactivarProducto() { mostrarPendiente(); }

    @FXML
    private void regresarDashboard() throws IOException {
        Principal.mostrarDashboardSegunRol();
    }

    private void mostrarPendiente() {
        mostrarMensaje(Alert.AlertType.INFORMATION,
                "Las operaciones de guardar, editar y cambiar estado "
                + "se implementan en la siguiente tarea.");
    }

    private void mostrarMensaje(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle("Productos de dulcería");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
