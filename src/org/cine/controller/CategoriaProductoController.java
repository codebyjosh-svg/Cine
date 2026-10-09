package org.cine.controller;

import java.io.IOException;
import java.util.List;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import org.cine.dao.CategoriaProductoDAO;
import org.cine.dao.impl.CategoriaProductoDAOImpl;
import org.cine.model.CategoriaProducto;
import org.cine.system.Principal;

public class CategoriaProductoController {

    @FXML private TextField txtNombreCategoria;
    @FXML private TextArea txtDescripcionCategoria;
    @FXML private Label lblEstadoCategoria;

    @FXML private TableView<CategoriaProducto> tbCategorias;
    @FXML private TableColumn<CategoriaProducto, Integer> colIdCategoria;
    @FXML private TableColumn<CategoriaProducto, String> colNombreCategoria;
    @FXML private TableColumn<CategoriaProducto, String> colDescripcionCategoria;
    @FXML private TableColumn<CategoriaProducto, String> colEstadoCategoria;

    private final CategoriaProductoDAO categoriaDAO =
            new CategoriaProductoDAOImpl();
    private CategoriaProducto categoriaSeleccionada;

    @FXML
    private void initialize() {
        configurarTabla();
        cargarCategorias();
    }

    private void configurarTabla() {
        colIdCategoria.setCellValueFactory(
                new PropertyValueFactory<>("idCategoriaProducto"));
        colNombreCategoria.setCellValueFactory(
                new PropertyValueFactory<>("nombreCategoria"));
        colDescripcionCategoria.setCellValueFactory(
                new PropertyValueFactory<>("descripcion"));
        colEstadoCategoria.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        datos.getValue().getEstado() == 1
                                ? "Activa" : "Inactiva"));
    }

    @FXML
    private void cargarCategorias() {
        List<CategoriaProducto> categorias = categoriaDAO.listarTodos();
        tbCategorias.setItems(
                FXCollections.observableArrayList(categorias));

        String error = categoriaDAO.getUltimoError();
        if (error != null && !error.trim().isEmpty()) {
            mostrarMensaje(Alert.AlertType.ERROR,
                    "No se pudieron cargar las categorías.\n" + error);
        }
    }

    @FXML
    private void seleccionarCategoria() {
        categoriaSeleccionada = tbCategorias.getSelectionModel()
                .getSelectedItem();
        if (categoriaSeleccionada == null) {
            return;
        }

        txtNombreCategoria.setText(
                categoriaSeleccionada.getNombreCategoria());
        txtDescripcionCategoria.setText(
                categoriaSeleccionada.getDescripcion() == null
                        ? "" : categoriaSeleccionada.getDescripcion());
        lblEstadoCategoria.setText(
                categoriaSeleccionada.getEstado() == 1
                        ? "Activa" : "Inactiva");
    }

    @FXML
    private void limpiarFormulario() {
        categoriaSeleccionada = null;
        tbCategorias.getSelectionModel().clearSelection();
        txtNombreCategoria.clear();
        txtDescripcionCategoria.clear();
        lblEstadoCategoria.setText("Nueva - se guardará como Activa");
    }

    @FXML private void guardarCategoria() { mostrarPendiente(); }
    @FXML private void editarCategoria() { mostrarPendiente(); }
    @FXML private void eliminarCategoria() { mostrarPendiente(); }
    @FXML private void activarCategoria() { mostrarPendiente(); }
    @FXML private void desactivarCategoria() { mostrarPendiente(); }

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
        alerta.setTitle("Categorías de productos");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
