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

    @FXML
    private TextField txtUsername;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private Label lblMensaje;

    private final UsuarioDAO usuarioDAO = new UsuarioDAOImpl();

    @FXML
    private void initialize() {
        lblMensaje.setText("");
    }

    @FXML
    private void iniciarSesion() {

        lblMensaje.setText("");

        String username = txtUsername.getText() == null
                ? ""
                : txtUsername.getText().trim();

        String password = txtPassword.getText() == null
                ? ""
                : txtPassword.getText();

        if (username.isBlank() || password.isBlank()) {

            lblMensaje.setText(
                    "Ingrese usuario y contraseña."
            );

            return;
        }

        try {

            System.out.println("=================================");
            System.out.println("INTENTO DE LOGIN");
            System.out.println("Usuario: " + username);
            System.out.println("=================================");

            Usuario usuario
                    = usuarioDAO.autenticar(
                            username,
                            password
                    );

            System.out.println(
                    "Usuario encontrado: "
                    + usuario.getUsername()
            );

            System.out.println(
                    "Rol: "
                    + usuario.getNombreRol()
            );

            System.out.println(
                    "Estado: "
                    + usuario.isEstado()
            );

            SesionContext.iniciarSesion(usuario);

            System.out.println(
                    "Sesión iniciada correctamente."
            );

            Principal.mostrarDashboardSegunRol();

        } catch (AutenticacionException ex) {

            System.out.println(
                    "ERROR DE AUTENTICACION: "
                    + ex.getMotivo()
            );

            ex.printStackTrace();

            lblMensaje.setText(
                    ex.getMessage()
            );

            txtPassword.clear();

        } catch (IOException ex) {

            System.out.println(
                    "ERROR AL ABRIR DASHBOARD:"
            );

            ex.printStackTrace();

            SesionContext.cerrarSesion();

            lblMensaje.setText(
                    "Error al abrir el dashboard."
            );

        } catch (IllegalArgumentException ex) {

            System.out.println(
                    "ERROR DE NAVEGACION:"
            );

            ex.printStackTrace();

            SesionContext.cerrarSesion();

            lblMensaje.setText(
                    "Rol no reconocido."
            );
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
