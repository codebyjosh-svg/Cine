package org.cine.controller;

import java.sql.Timestamp;
import java.util.List;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import org.cine.dao.ClienteDAO;
import org.cine.dao.impl.ClienteDAOImpl;
import org.cine.model.Cliente;

public class ClienteController {

    @FXML
    private TextField txtCui;

    @FXML
    private TextField txtNombreCliente;

    @FXML
    private TextField txtApellidoCliente;

    @FXML
    private TextField txtCorreoElectronico;

    @FXML
    private TextField txtTelefono;

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
    private Button btnActualizar;

    @FXML
    private TableView<Cliente> tbClientes;

    @FXML
    private TableColumn<Cliente, Integer> colIdCliente;

    @FXML
    private TableColumn<Cliente, String> colCui;

    @FXML
    private TableColumn<Cliente, String> colNombreCliente;

    @FXML
    private TableColumn<Cliente, String> colApellidoCliente;

    @FXML
    private TableColumn<Cliente, String> colCorreoElectronico;

    @FXML
    private TableColumn<Cliente, String> colTelefono;

    @FXML
    private TableColumn<Cliente, String> colEstado;

    @FXML
    private TableColumn<Cliente, Timestamp> colFechaRegistro;

    private final ClienteDAO clienteDAO = new ClienteDAOImpl();

    private Cliente clienteSeleccionado;

    @FXML
    private void initialize() {

        configurarTabla();

        btnGuardar.setDisable(true);
        btnEditar.setDisable(true);
        btnEliminar.setDisable(true);
        btnActivar.setDisable(true);
        btnDesactivar.setDisable(true);

        cargarClientes();
    }

    private void configurarTabla() {

        colIdCliente.setCellValueFactory(
                new PropertyValueFactory<>("idCliente")
        );

        colCui.setCellValueFactory(
                new PropertyValueFactory<>("cui")
        );

        colNombreCliente.setCellValueFactory(
                new PropertyValueFactory<>("nombreCliente")
        );

        colApellidoCliente.setCellValueFactory(
                new PropertyValueFactory<>("apellidoCliente")
        );

        colCorreoElectronico.setCellValueFactory(
                new PropertyValueFactory<>("correoElectronico")
        );

        colTelefono.setCellValueFactory(
                new PropertyValueFactory<>("telefono")
        );

        colEstado.setCellValueFactory(
                datos -> new ReadOnlyStringWrapper(
                        datos.getValue().getEstado() == 1
                                ? "Activo"
                                : "Inactivo"
                )
        );

        colFechaRegistro.setCellValueFactory(
                new PropertyValueFactory<>("fechaRegistro")
        );
    }

    @FXML
    private void cargarClientes() {

        List<Cliente> clientes = clienteDAO.listarTodos();

        if (!clienteDAO.getUltimoError().isEmpty()) {

            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Error al cargar clientes",
                    clienteDAO.getUltimoError()
            );

            return;
        }

        tbClientes.setItems(
                FXCollections.observableArrayList(clientes)
        );

        limpiarFormulario();
    }

    @FXML
    private void limpiarFormulario() {

        clienteSeleccionado = null;

        tbClientes.getSelectionModel().clearSelection();

        txtCui.clear();
        txtNombreCliente.clear();
        txtApellidoCliente.clear();
        txtCorreoElectronico.clear();
        txtTelefono.clear();

        lblEstado.setText(
                "Nuevo - se guardará como Activo"
        );

        txtNombreCliente.requestFocus();
    }

    @FXML
    private void seleccionarCliente() {
    }

    @FXML
    private void guardarCliente() {
    }

    @FXML
    private void editarCliente() {
    }

    @FXML
    private void eliminarCliente() {
    }

    @FXML
    private void activarCliente() {
    }

    @FXML
    private void desactivarCliente() {
    }

    private void mostrarAlerta(Alert.AlertType tipo,
            String titulo, String mensaje) {

        Alert alerta = new Alert(tipo);

        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);

        alerta.showAndWait();
    }
}