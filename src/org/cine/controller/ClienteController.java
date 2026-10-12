package org.cine.controller;

import java.sql.Timestamp;
import java.util.List;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import org.cine.dao.ClienteDAO;
import org.cine.dao.impl.ClienteDAOImpl;
import org.cine.model.Cliente;
import org.cine.system.Principal;
import java.io.IOException;

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
        limpiarFormulario();

        tbClientes.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, anterior, actual) -> {

                    if (actual != null) {
                        seleccionarCliente();
                    } else {
                        limpiarFormulario();
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
        btnEliminar.setDisable(false);

        btnActivar.setDisable(
                seleccionado.getEstado() == 1
        );

        btnDesactivar.setDisable(
                seleccionado.getEstado() == 0
        );
    }

    private Cliente obtenerDatosFormulario() {

        Cliente cliente = new Cliente();

        cliente.setCui(txtCui.getText().trim());
        cliente.setNombreCliente(txtNombreCliente.getText().trim());
        cliente.setApellidoCliente(txtApellidoCliente.getText().trim());
        cliente.setCorreoElectronico(txtCorreoElectronico.getText().trim());
        cliente.setTelefono(txtTelefono.getText().trim());

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

    private boolean validarFormulario() {

        String cui = txtCui.getText().trim();
        String nombre = txtNombreCliente.getText().trim();
        String apellido = txtApellidoCliente.getText().trim();
        String correo = txtCorreoElectronico.getText().trim();
        String telefono = txtTelefono.getText().trim();

        if (!cui.isEmpty() && !cui.matches("[0-9]{13}")) {

            return indicarDatoInvalido(
                    txtCui,
                    "El CUI debe tener exactamente 13 dígitos. "
                            + "También puedes dejarlo vacío."
            );
        }

        if (nombre.isEmpty()) {

            return indicarDatoInvalido(
                    txtNombreCliente,
                    "Debes ingresar los nombres del cliente."
            );
        }

        if (nombre.length() > 100) {

            return indicarDatoInvalido(
                    txtNombreCliente,
                    "Los nombres de los clientes no pueden superar los 100 caracteres."
            );
        }

        String patronNombre =
                "[\\p{L}\\p{M}]+(?:[ '\\-’][\\p{L}\\p{M}]+)*";

        if (!nombre.matches(patronNombre)) {

            return indicarDatoInvalido(
                    txtNombreCliente,
                    "Los nombres deben contener letras. "
                            + "Se permiten espacios, guiones y apóstrofos."
            );
        }

        if (apellido.isEmpty()) {

            return indicarDatoInvalido(
                    txtApellidoCliente,
                    "Debes ingresar los apellidos del cliente."
            );
        }

        if (apellido.length() > 100) {

            return indicarDatoInvalido(
                    txtApellidoCliente,
                    "Los apellidos no pueden superar los 100 caracteres."
            );
        }

        if (!apellido.matches(patronNombre)) {

            return indicarDatoInvalido(
                    txtApellidoCliente,
                    "Los apellidos deben contener letras. "
                            + "Se permiten espacios, guiones y apóstrofos."
            );
        }

        if (correo.isEmpty()) {

            return indicarDatoInvalido(
                    txtCorreoElectronico,
                    "Debes ingresar el correo electrónico."
            );
        }

        if (correo.length() > 120) {

            return indicarDatoInvalido(
                    txtCorreoElectronico,
                    "El correo no puede superar los 120 caracteres."
            );
        }

        String patronCorreo =
                "[A-Za-z0-9_%+\\-]+"
                + "(?:\\.[A-Za-z0-9_%+\\-]+)*"
                + "@"
                + "(?:[A-Za-z0-9](?:[A-Za-z0-9\\-]*[A-Za-z0-9])?\\.)+"
                + "[A-Za-z]{2,}";

        if (!correo.matches(patronCorreo)) {

            return indicarDatoInvalido(
                    txtCorreoElectronico,
                    "Ingresa un correo válido, por ejemplo: "
                            + "cliente@correo.com. No se permiten espacios, "
                            + "puntos consecutivos ni un punto antes de @."
            );
        }

        if (telefono.length() > 20) {

            return indicarDatoInvalido(
                    txtTelefono,
                    "El teléfono no puede superar los 20 caracteres."
            );
        }

        return true;
    }

    private boolean indicarDatoInvalido(TextField campo, String mensaje) {

        mostrarAlerta(
                Alert.AlertType.WARNING,
                "Datos inválidos",
                mensaje
        );

        campo.requestFocus();
        campo.selectAll();

        return false;
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

        if (!validarFormulario()) {
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

        if (!hayClienteSeleccionado()) {
            return;
        }

        if (!validarFormulario()) {
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
    private void eliminarCliente() {

        if (!hayClienteSeleccionado()) {
            return;
        }

        int idCliente = clienteSeleccionado.getIdCliente();

        String nombreCompleto = clienteSeleccionado.getNombreCliente()
                + " " + clienteSeleccionado.getApellidoCliente();

        if (!confirmarOperacion(
                "Eliminar cliente",
                "¿Deseas eliminar definitivamente a "
                        + nombreCompleto + "?"
        )) {
            return;
        }

        if (clienteDAO.eliminar(idCliente)) {

            mostrarAlerta(
                    Alert.AlertType.INFORMATION,
                    "Cliente eliminado",
                    "El cliente fue eliminado correctamente."
            );

            limpiarFormulario();
            cargarClientes();

        } else {

            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "No se pudo eliminar",
                    clienteDAO.getUltimoError()
            );
        }
    }

    @FXML
    private void activarCliente() {
        cambiarEstadoCliente(1);
    }

    @FXML
    private void desactivarCliente() {
        cambiarEstadoCliente(0);
    }

    private void cambiarEstadoCliente(int estado) {

        if (!hayClienteSeleccionado()) {
            return;
        }

        if (clienteSeleccionado.getEstado() == estado) {

            mostrarAlerta(
                    Alert.AlertType.INFORMATION,
                    "Estado del cliente",
                    estado == 1
                            ? "El cliente ya está activo."
                            : "El cliente ya está inactivo."
            );

            return;
        }

        int idCliente = clienteSeleccionado.getIdCliente();

        String accion = estado == 1
                ? "activar"
                : "desactivar";

        String nombreCompleto = clienteSeleccionado.getNombreCliente()
                + " " + clienteSeleccionado.getApellidoCliente();

        if (!confirmarOperacion(
                "Cambiar estado",
                "¿Deseas " + accion + " a " + nombreCompleto + "?"
        )) {
            return;
        }

        if (clienteDAO.cambiarEstado(idCliente, estado)) {

            mostrarAlerta(
                    Alert.AlertType.INFORMATION,
                    "Estado actualizado",
                    estado == 1
                            ? "El cliente fue activado correctamente."
                            : "El cliente fue desactivado correctamente."
            );

            limpiarFormulario();
            cargarClientes();

        } else {

            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Error al cambiar estado",
                    clienteDAO.getUltimoError()
            );
        }
    }

    private boolean hayClienteSeleccionado() {

        if (clienteSeleccionado == null) {

            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Selecciona un cliente",
                    "Selecciona un cliente de la tabla."
            );

            return false;
        }

        return true;
    }

    private boolean confirmarOperacion(String titulo, String mensaje) {

        Alert alerta = new Alert(
                Alert.AlertType.CONFIRMATION,
                mensaje,
                ButtonType.OK,
                ButtonType.CANCEL
        );

        alerta.setTitle(titulo);
        alerta.setHeaderText(null);

        if (tbClientes.getScene() != null) {
            alerta.initOwner(tbClientes.getScene().getWindow());
        }

        return alerta.showAndWait()
                .orElse(ButtonType.CANCEL) == ButtonType.OK;
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

    private void mostrarAlerta(Alert.AlertType tipo,
            String titulo, String mensaje) {

        Alert alerta = new Alert(tipo);

        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);

        if (tbClientes.getScene() != null) {
            alerta.initOwner(tbClientes.getScene().getWindow());
        }

        alerta.showAndWait();
    }
    @FXML
    private void volver() {
        try {
            Principal.mostrarDashboardSegunRol();
        } catch (IOException ex) {
            mostrarAlerta(Alert.AlertType.ERROR,
                    "Error de navegación",
                    "No se pudo volver al dashboard: " + ex.getMessage());
        }
    }

}
