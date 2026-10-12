package org.cine.controller;

import java.io.IOException;
import java.util.List;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
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
import org.cine.util.ValidacionDulceria;


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

        tbCategorias.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, anterior, actual) ->
                        seleccionarCategoria());

        cargarCategorias();
    }

    private void configurarTabla() {
        colIdCategoria.setCellValueFactory(
                new PropertyValueFactory<>("idCategoriaProducto")
        );

        colNombreCategoria.setCellValueFactory(
                new PropertyValueFactory<>("nombreCategoria")
        );

        colDescripcionCategoria.setCellValueFactory(
                new PropertyValueFactory<>("descripcion")
        );

        colEstadoCategoria.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        datos.getValue().getEstado() == 1
                                ? "Activa" : "Inactiva"
                )
        );
    }

    @FXML
    private void cargarCategorias() {
        List<CategoriaProducto> categorias =
                categoriaDAO.listarTodos();

        tbCategorias.setItems(
                FXCollections.observableArrayList(categorias)
        );

        String error = categoriaDAO.getUltimoError();

        if (error != null && !error.isBlank()) {
            mostrarMensaje(
                    Alert.AlertType.ERROR,
                    "No se pudieron cargar las categorías.\n" + error
            );
        }
    }

    @FXML
    private void seleccionarCategoria() {
        categoriaSeleccionada =
                tbCategorias.getSelectionModel().getSelectedItem();

        if (categoriaSeleccionada == null) {
            return;
        }

        txtNombreCategoria.setText(
                categoriaSeleccionada.getNombreCategoria()
        );

        txtDescripcionCategoria.setText(
                categoriaSeleccionada.getDescripcion() == null
                        ? ""
                        : categoriaSeleccionada.getDescripcion()
        );

        lblEstadoCategoria.setText(
                categoriaSeleccionada.getEstado() == 1
                        ? "Activa" : "Inactiva"
        );
    }

    @FXML
    private void limpiarFormulario() {
        categoriaSeleccionada = null;

        tbCategorias.getSelectionModel().clearSelection();
        txtNombreCategoria.clear();
        txtDescripcionCategoria.clear();

        lblEstadoCategoria.setText(
                "Nueva - se guardará como Activa"
        );
    }

    @FXML
    private void guardarCategoria() {
        if (categoriaSeleccionada != null) {
            mostrarMensaje(
                    Alert.AlertType.WARNING,
                    "Pulse Limpiar para registrar una categoría nueva. "
                    + "Use Editar para modificar la seleccionada."
            );
            return;
        }

        try {
            resultado(
                    categoriaDAO.insertar(leerFormulario()),
                    "Categoría registrada correctamente."
            );

        } catch (IllegalArgumentException ex) {
            mostrarMensaje(
                    Alert.AlertType.WARNING,
                    ex.getMessage()
            );
        }
    }

    @FXML
    private void editarCategoria() {
        if (!haySeleccion()) {
            return;
        }

        try {
            resultado(
                    categoriaDAO.actualizar(leerFormulario()),
                    "Categoría actualizada correctamente."
            );

        } catch (IllegalArgumentException ex) {
            mostrarMensaje(
                    Alert.AlertType.WARNING,
                    ex.getMessage()
            );
        }
    }

    @FXML
    private void eliminarCategoria() {
        if (!haySeleccion()) {
            return;
        }

        Alert confirmar = new Alert(
                Alert.AlertType.CONFIRMATION,
                "¿Eliminar definitivamente la categoría seleccionada? "
                + "Si tiene productos relacionados, utilice Desactivar.",
                ButtonType.YES,
                ButtonType.NO
        );

        confirmar.setHeaderText("Confirmar eliminación");

        ButtonType respuesta =
                confirmar.showAndWait().orElse(ButtonType.NO);

        if (respuesta == ButtonType.YES) {
            resultado(
                    categoriaDAO.eliminar(
                            categoriaSeleccionada
                                    .getIdCategoriaProducto()
                    ),
                    "Categoría eliminada correctamente."
            );
        }
    }

    @FXML
    private void activarCategoria() {
        cambiarEstado(1);
    }

    @FXML
    private void desactivarCategoria() {
        cambiarEstado(0);
    }

    private void cambiarEstado(int estado) {
        if (!haySeleccion()) {
            return;
        }

        if (categoriaSeleccionada.getEstado() == estado) {
            mostrarMensaje(
                    Alert.AlertType.INFORMATION,
                    "La categoría ya tiene ese estado."
            );
            return;
        }

        resultado(
                categoriaDAO.cambiarEstado(
                        categoriaSeleccionada.getIdCategoriaProducto(),
                        estado
                ),
                estado == 1
                        ? "Categoría activada correctamente."
                        : "Categoría desactivada correctamente."
        );
    }

    private boolean haySeleccion() {
        if (categoriaSeleccionada != null) {
            return true;
        }

        mostrarMensaje(
                Alert.AlertType.WARNING,
                "Seleccione una categoría de la tabla."
        );

        return false;
    }

    private CategoriaProducto leerFormulario() {
        CategoriaProducto categoria = new CategoriaProducto();

        categoria.setNombreCategoria(
                ValidacionDulceria.nombre(
                        txtNombreCategoria.getText()
                )
        );

        categoria.setDescripcion(
                ValidacionDulceria.descripcion(
                        txtDescripcionCategoria.getText()
                )
        );

        if (categoriaSeleccionada != null) {
            categoria.setIdCategoriaProducto(
                    categoriaSeleccionada.getIdCategoriaProducto()
            );

            categoria.setEstado(
                    categoriaSeleccionada.getEstado()
            );
        }

        return categoria;
    }

    private void resultado(boolean correcto, String mensaje) {
        if (!correcto) {
            mostrarMensaje(
                    Alert.AlertType.ERROR,
                    categoriaDAO.getUltimoError()
            );
            return;
        }

        limpiarFormulario();
        cargarCategorias();

        mostrarMensaje(
                Alert.AlertType.INFORMATION,
                mensaje
        );
    }

    @FXML
    private void regresarDashboard() throws IOException {
        Principal.mostrarDashboardSegunRol();
    }

    private void mostrarMensaje(
            Alert.AlertType tipo,
            String mensaje) {

        Alert alerta = new Alert(tipo);

        alerta.setTitle("Categorías de productos");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}