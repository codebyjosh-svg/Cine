package org.cine.controller;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.cine.dao.UsuarioDAO;
import org.cine.dao.impl.UsuarioDAOImpl;
import org.cine.model.Usuario;
import org.cine.service.AutenticacionException;
import org.cine.service.SesionContext;
import org.cine.system.Principal;

public class LoginController {
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblMensaje;

    private final UsuarioDAO usuarioDAO = new UsuarioDAOImpl();

    @FXML
    private void initialize() {
        lblMensaje.setText("");
    }

    @FXML
    private void iniciarSesion() {
        lblMensaje.setText("");
        String username = txtUsername.getText() == null ? "" : txtUsername.getText().trim();
        String password = txtPassword.getText() == null ? "" : txtPassword.getText();

        if (username.isBlank() || password.isBlank()) {
            lblMensaje.setText("Ingrese usuario y contraseña.");
            return;
        }

        try {
            Usuario usuario = usuarioDAO.autenticar(username, password);
            SesionContext.iniciarSesion(usuario);
            Principal.mostrarDashboardSegunRol();
        } catch (AutenticacionException ex) {
            lblMensaje.setText(ex.getMessage());
            txtPassword.clear();
        } catch (IOException | IllegalArgumentException ex) {
            SesionContext.cerrarSesion();
            lblMensaje.setText("No se pudo abrir la pantalla del usuario.");
        }
    }

    @FXML
    private void limpiar() {
        txtUsername.clear();
        txtPassword.clear();
        lblMensaje.setText("");
        txtUsername.requestFocus();
    }
}
