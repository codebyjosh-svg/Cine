package org.cine.controller;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.cine.model.Usuario;
import org.cine.service.SesionContext;
import org.cine.system.Principal;

public class DashboardController {
    @FXML private Label lblUsuario;
    @FXML private Label lblRol;

    @FXML
    private void initialize() {
        Usuario usuario = SesionContext.getUsuarioActual();
        if (usuario != null) {
            lblUsuario.setText(usuario.getNombreCompleto());
            lblRol.setText(usuario.getNombreRol());
        }
    }

    @FXML
    private void abrirFunciones() throws IOException {
        Principal.mostrarProgramacion();
    }

    @FXML
    private void cerrarSesion() throws IOException {
        Principal.cerrarSesion();
    }
}
