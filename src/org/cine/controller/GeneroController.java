package org.cine.controller;

import java.util.List;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.ButtonType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;

import org.cine.dao.GeneroDAO;
import org.cine.dao.impl.GeneroDAOImpl;
import org.cine.model.Genero;

public class GeneroController {

    @FXML
    private TextField txtNombreGenero;

    @FXML
    private TextArea txtDescripcion;

    @FXML
    private Label lblEstado;

    @FXML
    private Button btnGuardar;

    @FXML
    private Button btnEditar;

    @FXML
    private Button btnEliminar;

    @FXML
    private Button btnActivar;

    @FXML
    private Button btnDesactivar;

    @FXML
    private Button btnLimpiar;

    @FXML
    private TableView<Genero> tbGeneros;

    @FXML
    private TableColumn<Genero, Integer> colIdGenero;

    @FXML
    private TableColumn<Genero, String> colNombreGenero;

    @FXML
    private TableColumn<Genero, String> colDescripcion;

    @FXML
    private TableColumn<Genero, String> colEstado;

    private GeneroDAO generoDAO = new GeneroDAOImpl();

    private Genero generoSeleccionado;

    @FXML
    public void initialize() {

        configurarTabla();
        cargarGeneros();
        limpiarFormulario();
    }

    private void configurarTabla() {

        colIdGenero.setCellValueFactory(
                new PropertyValueFactory<>("idGenero")
        );

        colNombreGenero.setCellValueFactory(
                new PropertyValueFactory<>("nombreGenero")
        );

        colDescripcion.setCellValueFactory(
                new PropertyValueFactory<>("descripcion")
        );

        colEstado.setCellValueFactory(
                datos -> new SimpleStringProperty(
                        datos.getValue().getEstado() == 1
                                ? "Activo"
                                : "Inactivo"
                )
        );
    }

    private void cargarGeneros() {

        List<Genero> lista = generoDAO.listarTodos();

        tbGeneros.setItems(
                FXCollections.observableArrayList(lista)
        );
    }

    @FXML
    private void seleccionarGenero() {

        generoSeleccionado =
                tbGeneros.getSelectionModel()
                        .getSelectedItem();

        if (generoSeleccionado == null) {
            return;
        }

        txtNombreGenero.setText(
                generoSeleccionado.getNombreGenero()
        );

        txtDescripcion.setText(
                generoSeleccionado.getDescripcion()
        );

        if (generoSeleccionado.getEstado() == 1) {

            lblEstado.setText("Activo");

        } else {

            lblEstado.setText("Inactivo");
        }
    }

    @FXML
    private void guardarGenero() {

        String nombre =
                txtNombreGenero.getText().trim();

        String descripcion =
                txtDescripcion.getText().trim();

        if (nombre.isEmpty()) {

            mostrarAdvertencia(
                    "Debe ingresar el nombre del género."
            );

            return;
        }

        Genero genero = new Genero(
                0,
                nombre,
                descripcion,
                1
        );

        if (generoDAO.insertar(genero)) {

            mostrarInformacion(
                    "Género guardado correctamente."
            );

            cargarGeneros();
            limpiarFormulario();

        } else {

            mostrarError(
                    "No se pudo guardar el género."
            );
        }
    }

    @FXML
    private void editarGenero() {

        if (generoSeleccionado == null) {

            mostrarAdvertencia(
                    "Seleccione un género para editar."
            );

            return;
        }

        String nombre =
                txtNombreGenero.getText().trim();

        String descripcion =
                txtDescripcion.getText().trim();

        if (nombre.isEmpty()) {

            mostrarAdvertencia(
                    "Debe ingresar el nombre del género."
            );

            return;
        }

        generoSeleccionado.setNombreGenero(nombre);

        generoSeleccionado.setDescripcion(
                descripcion
        );

        if (generoDAO.actualizar(generoSeleccionado)) {

            mostrarInformacion(
                    "Género actualizado correctamente."
            );

            cargarGeneros();
            limpiarFormulario();

        } else {

            mostrarError(
                    "No se pudo actualizar el género."
            );
        }
    }

    @FXML
    private void eliminarGenero() {

        if (generoSeleccionado == null) {

            mostrarAdvertencia(
                    "Seleccione un género para eliminar."
            );

            return;
        }

        Alert confirmacion = new Alert(
                Alert.AlertType.CONFIRMATION
        );

        confirmacion.setTitle("Eliminar género");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText(
                "¿Está seguro de eliminar el género seleccionado?"
        );

        if (confirmacion.showAndWait().orElse(
                ButtonType.CANCEL
        ) == ButtonType.OK) {

            if (generoDAO.eliminar(
                    generoSeleccionado.getIdGenero())) {

                mostrarInformacion(
                        "Género eliminado correctamente."
                );

                cargarGeneros();
                limpiarFormulario();

            } else {

                mostrarError(
                        "No se pudo eliminar el género."
                );
            }
        }
    }

    @FXML
    private void activarGenero() {

        cambiarEstado(1);
    }

    @FXML
    private void desactivarGenero() {

        cambiarEstado(0);
    }

    private void cambiarEstado(int estado) {

        if (generoSeleccionado == null) {

            mostrarAdvertencia(
                    "Seleccione un género."
            );

            return;
        }

        if (generoSeleccionado.getEstado() == estado) {

            if (estado == 1) {

                mostrarAdvertencia(
                        "El género ya está activo."
                );

            } else {

                mostrarAdvertencia(
                        "El género ya está inactivo."
                );
            }

            return;
        }

        if (generoDAO.cambiarEstado(
                generoSeleccionado.getIdGenero(),
                estado)) {

            if (estado == 1) {

                mostrarInformacion(
                        "Género activado correctamente."
                );

            } else {

                mostrarInformacion(
                        "Género desactivado correctamente."
                );
            }

            cargarGeneros();
            limpiarFormulario();

        } else {

            mostrarError(
                    "No se pudo cambiar el estado del género."
            );
        }
    }

    @FXML
    private void limpiarFormulario() {

        txtNombreGenero.clear();
        txtDescripcion.clear();

        lblEstado.setText(
                "Nuevo - se guardará como Activo"
        );

        generoSeleccionado = null;

        tbGeneros.getSelectionModel()
                .clearSelection();
    }

    private void mostrarInformacion(
            String mensaje) {

        Alert alerta = new Alert(
                Alert.AlertType.INFORMATION
        );

        alerta.setTitle("Información");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void mostrarAdvertencia(
            String mensaje) {

        Alert alerta = new Alert(
                Alert.AlertType.WARNING
        );

        alerta.setTitle("Advertencia");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void mostrarError(
            String mensaje) {

        Alert alerta = new Alert(
                Alert.AlertType.ERROR
        );

        alerta.setTitle("Error");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}