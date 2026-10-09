package org.cine.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import org.cine.dao.impl.UsuarioDAOImpl;
import org.cine.model.ClienteVinculo;
import org.cine.model.Rol;
import org.cine.model.Usuario;
import org.cine.service.UsuarioService;
import org.cine.service.SesionContext;
import org.cine.system.Principal;

public class UsuarioController {
    @FXML private VBox bloqueCliente;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtUsername;
    @FXML private TextField txtCorreo;
    @FXML private PasswordField txtContrasena;
    @FXML private PasswordField txtConfirmacion;
    @FXML private ComboBox<Rol> cmbRol;
    @FXML private ComboBox<ClienteVinculo> cmbCliente;
    @FXML private ComboBox<String> cmbFiltroEstado;
    @FXML private TextField txtBusqueda;
    @FXML private TableView<Usuario> tblUsuarios;
    @FXML private TableColumn<Usuario, Integer> colId;
    @FXML private TableColumn<Usuario, String> colNombre;
    @FXML private TableColumn<Usuario, String> colUsername;
    @FXML private TableColumn<Usuario, String> colCorreo;
    @FXML private TableColumn<Usuario, String> colRol;
    @FXML private TableColumn<Usuario, Integer> colCliente;
    @FXML private TableColumn<Usuario, String> colEstado;
    @FXML private TableColumn<Usuario, LocalDateTime> colFecha;
    @FXML private Label lblModo;
    @FXML private Label lblError;
    @FXML private Label lblStatus;
    @FXML private Label lblResultados;
    @FXML private Button btnGuardar;
    @FXML private Button btnEditar;
    @FXML private Button btnEstado;
    @FXML private Button btnEliminar;

    private final UsuarioService servicio = new UsuarioService(new UsuarioDAOImpl());
    private final ObservableList<Usuario> usuarios = FXCollections.observableArrayList();
    private int idEdicion;

    @FXML
    private void initialize() {
        Usuario usuarioSesion = SesionContext.getUsuarioActual();
        if (usuarioSesion == null || !usuarioSesion.isEstado() || !"admin".equals(usuarioSesion.getNombreRol())) {
            throw new IllegalStateException("La gestión de usuarios requiere una sesión de administrador.");
        }
        setUsuarioActual(usuarioSesion);
        colId.setCellValueFactory(new PropertyValueFactory<>("idUsuario"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colCorreo.setCellValueFactory(new PropertyValueFactory<>("correoElectronico"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("nombreRol"));
        colCliente.setCellValueFactory(new PropertyValueFactory<>("idCliente"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estadoTexto"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaRegistro"));

        tblUsuarios.setPlaceholder(new Label("No hay usuarios."));
        tblUsuarios.getSelectionModel().selectedItemProperty().addListener(
                (observable, anterior, actual) -> actualizarBotones());
        cmbFiltroEstado.setItems(FXCollections.observableArrayList("Todos", "Activos", "Inactivos"));
        cmbFiltroEstado.setValue("Todos");
        txtBusqueda.textProperty().addListener((observable, anterior, actual) -> filtrar());
        cmbFiltroEstado.valueProperty().addListener((observable, anterior, actual) -> filtrar());
        cmbRol.valueProperty().addListener((observable, anterior, actual) -> mostrarCliente());

        limpiarFormulario();
        cargarDatos(null);
    }

    private void cargarDatos(Integer seleccionarId) {
        try {
            usuarios.setAll(servicio.listar());
            cmbRol.setItems(FXCollections.observableArrayList(servicio.listarRoles().stream()
                    .filter(r -> "admin".equalsIgnoreCase(r.getNombreRol())
                            || "taquillero".equalsIgnoreCase(r.getNombreRol())).toList()));
            cmbCliente.setItems(FXCollections.observableArrayList(servicio.listarClientes()));
            filtrar();
            if (seleccionarId != null) {
                for (Usuario usuario : tblUsuarios.getItems()) {
                    if (usuario.getIdUsuario() == seleccionarId) {
                        tblUsuarios.getSelectionModel().select(usuario);
                        break;
                    }
                }
            }
            lblStatus.setText("Datos actualizados. Selecciona un usuario para editarlo.");
            actualizarBotones();
        } catch (SQLException ex) {
            mostrarError(ex);
        }
    }

    private void filtrar() {
        String texto = txtBusqueda.getText().trim().toLowerCase(Locale.ROOT);
        String estado = cmbFiltroEstado.getValue();
        ObservableList<Usuario> encontrados = FXCollections.observableArrayList();
        for (Usuario usuario : usuarios) {
            boolean coincideEstado = estado == null || "Todos".equals(estado)
                    || ("Activos".equals(estado) && usuario.isEstado())
                    || ("Inactivos".equals(estado) && !usuario.isEstado());
            String datos = usuario.getNombreCompleto() + " " + usuario.getUsername()
                    + " " + usuario.getCorreoElectronico() + " " + usuario.getNombreRol();
            if (coincideEstado && datos.toLowerCase(Locale.ROOT).contains(texto)) {
                encontrados.add(usuario);
            }
        }
        tblUsuarios.setItems(encontrados);
        tblUsuarios.sort();
        lblResultados.setText(encontrados.size() + " usuarios");
    }

    private void mostrarCliente() {
        Rol rol = cmbRol.getValue();
        boolean esCliente = false; // No existen cuentas con rol cliente.
        bloqueCliente.setVisible(esCliente);
        bloqueCliente.setManaged(esCliente);
        cmbCliente.setDisable(!esCliente);
        if (!esCliente) {
            cmbCliente.setValue(null);
        }
    }

    private void limpiarFormulario() {
        idEdicion = 0;
        txtNombre.clear();
        txtApellido.clear();
        txtUsername.clear();
        txtCorreo.clear();
        txtContrasena.clear();
        txtConfirmacion.clear();
        txtContrasena.setPromptText("Mínimo 8 caracteres");
        cmbRol.setValue(null);
        cmbCliente.setValue(null);
        mostrarCliente();
        lblModo.setText("Nuevo usuario");
        lblError.setText("");
        tblUsuarios.getSelectionModel().clearSelection();
        actualizarBotones();
    }

    @FXML
    private void onNuevo() {
        limpiarFormulario();
        txtNombre.requestFocus();
    }

    @FXML
    private void onEditar() {
        Usuario usuario = tblUsuarios.getSelectionModel().getSelectedItem();
        if (usuario == null) {
            return;
        }
        idEdicion = usuario.getIdUsuario();
        txtNombre.setText(usuario.getNombreUsuario());
        txtApellido.setText(usuario.getApellidoUsuario());
        txtUsername.setText(usuario.getUsername());
        txtCorreo.setText(usuario.getCorreoElectronico());
        txtContrasena.clear();
        txtConfirmacion.clear();
        txtContrasena.setPromptText("Vacío: conservar la actual");
        cmbRol.setValue(null);
        for (Rol rol : cmbRol.getItems()) {
            if (rol.getIdRol() == usuario.getIdRol()) {
                cmbRol.setValue(rol);
                break;
            }
        }
        cmbCliente.setValue(null);
        for (ClienteVinculo cliente : cmbCliente.getItems()) {
            if (Integer.valueOf(cliente.getIdCliente()).equals(usuario.getIdCliente())) {
                cmbCliente.setValue(cliente);
                break;
            }
        }
        lblModo.setText("Editar usuario #" + idEdicion);
        lblError.setText("");
        lblStatus.setText("Editando " + usuario.getUsername());
        actualizarBotones();
    }

    @FXML
    private void onGuardar() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(idEdicion);
        usuario.setNombreUsuario(txtNombre.getText());
        usuario.setApellidoUsuario(txtApellido.getText());
        usuario.setUsername(txtUsername.getText());
        usuario.setCorreoElectronico(txtCorreo.getText());
        usuario.setIdRol(cmbRol.getValue() == null ? 0 : cmbRol.getValue().getIdRol());
        usuario.setIdCliente(cmbCliente.getValue() == null ? null : cmbCliente.getValue().getIdCliente());
        boolean nuevo = idEdicion == 0;
        try {
            int id = servicio.guardar(usuario, txtContrasena.getText(), txtConfirmacion.getText());
            limpiarFormulario();
            alerta(Alert.AlertType.INFORMATION, nuevo ? "Usuario guardado." : "Cambios guardados.").showAndWait();
            cargarDatos(id);
        } catch (SQLException | IllegalArgumentException ex) {
            mostrarError(ex);
        }
    }

    @FXML
    private void onCambiarEstado() {
        Usuario usuario = tblUsuarios.getSelectionModel().getSelectedItem();
        if (usuario == null) {
            return;
        }
        boolean activar = !usuario.isEstado();
        String accion = activar ? "Activar" : "Desactivar";
        if (!confirmar(accion + " a " + usuario.getUsername() + "?")) {
            return;
        }
        try {
            servicio.cambiarEstado(usuario.getIdUsuario(), activar);
            limpiarFormulario();
            cargarDatos(usuario.getIdUsuario());
        } catch (SQLException | IllegalArgumentException ex) {
            mostrarError(ex);
        }
    }

    @FXML
    private void onEliminar() {
        Usuario usuario = tblUsuarios.getSelectionModel().getSelectedItem();
        if (usuario == null) {
            return;
        }
        if (!confirmar("Eliminar a " + usuario.getUsername() + "? Se borrará la cuenta.")) {
            return;
        }
        try {
            servicio.eliminar(usuario.getIdUsuario());
            limpiarFormulario();
            cargarDatos(null);
        } catch (SQLException | IllegalArgumentException ex) {
            mostrarError(ex);
        }
    }

    @FXML
    private void onActualizar() {
        limpiarFormulario();
        cargarDatos(null);
    }

    @FXML
    private void onVolver() {
        try {
            Principal.mostrarDashboardSegunRol();
        } catch (IOException ex) {
            mostrarError(ex);
        }
    }

    private void actualizarBotones() {
        Usuario seleccionado = tblUsuarios.getSelectionModel().getSelectedItem();
        btnEditar.setDisable(seleccionado == null);
        btnEstado.setDisable(seleccionado == null);
        btnEliminar.setDisable(seleccionado == null);
        btnEstado.setText(seleccionado != null && !seleccionado.isEstado() ? "Activar" : "Desactivar");
        btnGuardar.setText(idEdicion == 0 ? "Guardar" : "Guardar cambios");
    }

    private void mostrarError(Exception ex) {
        String mensaje = ex.getMessage();
        if (ex instanceof SQLException) {
            SQLException sql = (SQLException) ex;
            if (sql.getErrorCode() == 1062) {
                mensaje = "El username, correo o cliente ya está registrado.";
            } else if (sql.getErrorCode() == 1451) {
                mensaje = "El usuario tiene historial. Usa Desactivar.";
            } else if (sql.getErrorCode() == 1452) {
                mensaje = "El rol o cliente ya no existe. Actualiza la lista.";
            } else if (sql.getErrorCode() == 1305) {
                mensaje = "Faltan procedimientos. Revisa que hayas cargado el DDL.";
            } else if (!"45000".equals(sql.getSQLState())) {
                mensaje = "No se pudo conectar a MySQL. Revisa src/db.properties.";
            }
        }
        lblError.setText(mensaje);
        lblStatus.setText("Revisa los datos y vuelve a intentarlo.");
    }

    private Alert alerta(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle("Usuarios");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.initOwner(tblUsuarios.getScene().getWindow());
        return alerta;
    }

    private boolean confirmar(String mensaje) {
        Optional<ButtonType> respuesta = alerta(Alert.AlertType.CONFIRMATION, mensaje).showAndWait();
        return respuesta.isPresent() && respuesta.get() == ButtonType.OK;
    }

    public void setUsuarioActual(Usuario usuario) {
        servicio.setIdUsuarioActual(usuario == null ? null : usuario.getIdUsuario());
    }

}
