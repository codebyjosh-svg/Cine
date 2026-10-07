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

        btnGuardar.setDisable(false);
        btnEditar.setDisable(true);
        btnEliminar.setDisable(true);
        btnActivar.setDisable(true);
        btnDesactivar.setDisable(true);

        tbClientes.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, anterior, actual) -> {

                    if (actual != null) {
                        seleccionarCliente();
                    }
                });

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
    private void seleccionarCliente() {

        Cliente seleccionado = tbClientes.getSelectionModel()
                .getSelectedItem();

        if (seleccionado == null) {
            return;
        }

        clienteSeleccionado = seleccionado;

        txtCui.setText(
                seleccionado.getCui() == null
                        ? ""
                        : seleccionado.getCui()
        );

        txtNombreCliente.setText(
                seleccionado.getNombreCliente()
        );

        txtApellidoCliente.setText(
                seleccionado.getApellidoCliente()
        );

        txtCorreoElectronico.setText(
                seleccionado.getCorreoElectronico()
        );

        txtTelefono.setText(
                seleccionado.getTelefono() == null
                        ? ""
                        : seleccionado.getTelefono()
        );

        lblEstado.setText(
                seleccionado.getEstado() == 1
                        ? "Activo"
                        : "Inactivo"
        );

        btnGuardar.setDisable(true);
        btnEditar.setDisable(false);
    }

    private Cliente obtenerDatosFormulario() {

        Cliente cliente = new Cliente();

        cliente.setCui(
                txtCui.getText().trim()
        );

        cliente.setNombreCliente(
                txtNombreCliente.getText().trim()
        );

        cliente.setApellidoCliente(
                txtApellidoCliente.getText().trim()
        );

        cliente.setCorreoElectronico(
                txtCorreoElectronico.getText().trim()
        );

        cliente.setTelefono(
                txtTelefono.getText().trim()
        );

        if (clienteSeleccionado != null) {

            cliente.setIdCliente(
                    clienteSeleccionado.getIdCliente()
            );

            cliente.setEstado(
                    clienteSeleccionado.getEstado()
            );

            cliente.setFechaRegistro(
                    clienteSeleccionado.getFechaRegistro()
            );
        }

        return cliente;
    }

    @FXML
    private void guardarCliente() {

        if (clienteSeleccionado != null) {

            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Cliente seleccionado",
                    "Pulsa Limpiar para registrar un nuevo cliente."
            );

            return;
        }

        Cliente cliente = obtenerDatosFormulario();

        if (clienteDAO.insertar(cliente)) {

            mostrarAlerta(
                    Alert.AlertType.INFORMATION,
                    "Registro exitoso",
                    "El cliente fue registrado correctamente."
            );

            limpiarFormulario();
            cargarClientes();

        } else {

            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Error al registrar",
                    clienteDAO.getUltimoError()
            );
        }
    }

    @FXML
    private void editarCliente() {

        if (clienteSeleccionado == null) {

            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Selecciona un cliente",
                    "Selecciona un cliente de la tabla para editarlo."
            );

            return;
        }

        Cliente cliente = obtenerDatosFormulario();

        if (clienteDAO.actualizar(cliente)) {

            mostrarAlerta(
                    Alert.AlertType.INFORMATION,
                    "Actualización exitosa",
                    "Los datos del cliente fueron actualizados."
            );

            limpiarFormulario();
            cargarClientes();

        } else {

            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Error al actualizar",
                    clienteDAO.getUltimoError()
            );
        }
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

        btnGuardar.setDisable(false);
        btnEditar.setDisable(true);
        btnEliminar.setDisable(true);
        btnActivar.setDisable(true);
        btnDesactivar.setDisable(true);

        txtNombreCliente.requestFocus();
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