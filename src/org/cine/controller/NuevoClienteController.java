package org.cine.controller;

import java.sql.SQLException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.cine.dao.VentaDAO;
import org.cine.dao.impl.VentaDAOImpl;

public class NuevoClienteController {

    @FXML
    private TextField txtCui;

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtApellido;

    @FXML
    private TextField txtCorreo;

    @FXML
    private TextField txtTelefono;

    @FXML
    private Label lblMensaje;

    private final VentaDAO dao = new VentaDAOImpl();

    private VentaDAO.Opcion clienteCreado;


    @FXML
    private void initialize() {

        txtCui.textProperty().addListener(
                (observable, anterior, nuevo) -> {

                    if (!nuevo.matches("\\d*")) {
                        txtCui.setText(
                                nuevo.replaceAll("[^\\d]", "")
                        );
                    }

                    if (txtCui.getText().length() > 13) {
                        txtCui.setText(
                                txtCui.getText().substring(0, 13)
                        );
                    }
                }
        );


        txtTelefono.textProperty().addListener(
                (observable, anterior, nuevo) -> {

                    if (!nuevo.matches("\\d*")) {
                        txtTelefono.setText(
                                nuevo.replaceAll("[^\\d]", "")
                        );
                    }

                    if (txtTelefono.getText().length() > 20) {
                        txtTelefono.setText(
                                txtTelefono.getText().substring(0, 20)
                        );
                    }
                }
        );
    }


    @FXML
    private void guardar() {

        limpiarMensaje();


        String cui = texto(txtCui);

        String nombre = texto(txtNombre);

        String apellido = texto(txtApellido);

        String correo = texto(txtCorreo);

        String telefono = texto(txtTelefono);


        try {

            // ==============================
            // VALIDAR NOMBRE
            // ==============================

            if (nombre.isEmpty()) {

                throw new IllegalArgumentException(
                        "El nombre es obligatorio."
                );
            }

            if (!nombre.matches(
                    "^[\\p{L}][\\p{L} .'-]{1,59}$"
            )) {

                throw new IllegalArgumentException(
                        "El nombre contiene caracteres no válidos."
                );
            }


            // ==============================
            // VALIDAR APELLIDO
            // ==============================

            if (apellido.isEmpty()) {

                throw new IllegalArgumentException(
                        "El apellido es obligatorio."
                );
            }

            if (!apellido.matches(
                    "^[\\p{L}][\\p{L} .'-]{1,59}$"
            )) {

                throw new IllegalArgumentException(
                        "El apellido contiene caracteres no válidos."
                );
            }


            // ==============================
            // VALIDAR CUI
            // ==============================

            if (!cui.isEmpty()
                    && !cui.matches("\\d{13}")) {

                throw new IllegalArgumentException(
                        "El CUI debe contener exactamente 13 dígitos."
                );
            }


            // ==============================
            // VALIDAR CORREO
            // ==============================

            if (correo.isEmpty()) {

                throw new IllegalArgumentException(
                        "El correo electrónico es obligatorio."
                );
            }

            if (!correo.matches(
                    "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"
            )) {

                throw new IllegalArgumentException(
                        "El correo electrónico no es válido."
                );
            }


            // ==============================
            // VALIDAR TELÉFONO
            // ==============================

            if (!telefono.isEmpty()
                    && !telefono.matches("\\d{8,20}")) {

                throw new IllegalArgumentException(
                        "El teléfono debe contener entre 8 y 20 dígitos."
                );
            }


            // ==============================
            // GUARDAR
            // ==============================

            clienteCreado = dao.crearCliente(
                    cui.isEmpty() ? null : cui,
                    nombre,
                    apellido,
                    correo,
                    telefono.isEmpty() ? null : telefono
            );


            if (clienteCreado == null) {

                throw new SQLException(
                        "La base de datos no devolvió el cliente creado."
                );
            }


            mostrarExito(
                    "Cliente registrado correctamente."
            );

            cerrarVentana();


        } catch (IllegalArgumentException ex) {

            mostrarError(
                    ex.getMessage()
            );

        } catch (SQLException ex) {

            String mensaje = ex.getMessage();

            if (mensaje != null
                    && mensaje.toLowerCase().contains("duplicate")) {

                mostrarError(
                        "El CUI o correo electrónico ya está registrado."
                );

            } else {

                mostrarError(
                        mensaje == null
                        ? "No se pudo registrar el cliente."
                        : mensaje
                );
            }
        }
    }


    @FXML
    private void cancelar() {

        clienteCreado = null;

        cerrarVentana();
    }


    public VentaDAO.Opcion getClienteCreado() {

        return clienteCreado;
    }


    private String texto(TextField campo) {

        String valor = campo.getText();

        return valor == null
                ? ""
                : valor.trim();
    }


    private void limpiarMensaje() {

        if (lblMensaje != null) {
            lblMensaje.setText("");
        }
    }


    private void mostrarError(String mensaje) {

        if (lblMensaje != null) {

            lblMensaje.setText(
                    mensaje == null
                    ? "Datos inválidos."
                    : mensaje
            );

            lblMensaje.setStyle(
                    "-fx-text-fill: #dc2626;"
            );
        }
    }


    private void mostrarExito(String mensaje) {

        if (lblMensaje != null) {

            lblMensaje.setText(mensaje);

            lblMensaje.setStyle(
                    "-fx-text-fill: #15803d;"
            );
        }
    }


    private void cerrarVentana() {

        if (txtNombre.getScene() != null
                && txtNombre.getScene().getWindow() != null) {

            Stage stage =
                    (Stage) txtNombre
                            .getScene()
                            .getWindow();

            stage.close();
        }
    }


    private void mostrarExito(String mensaje, boolean mostrar) {

        if (!mostrar) {
            return;
        }

        Alert alerta = new Alert(
                Alert.AlertType.INFORMATION
        );

        alerta.setTitle("CINEMA");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}