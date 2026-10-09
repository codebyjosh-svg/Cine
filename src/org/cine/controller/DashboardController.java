package org.cine.controller;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.cine.model.Usuario;
import org.cine.service.SesionContext;
import org.cine.system.Principal;

/** Controlador de los dashboards de personal y cliente referenciados por los FXML. */
public final class DashboardController {
    @FXML private Label lblUsuario, lblRol;

    @FXML
    private void initialize() {
        Usuario usuario = SesionContext.getUsuarioActual();
        if (usuario != null) {
            lblUsuario.setText(usuario.getNombreCompleto());
            lblRol.setText(usuario.getNombreRol());
        }
    }

    @FXML private void abrirVentas() throws IOException { Principal.mostrarVentas(); }
    @FXML private void abrirStockCritico() throws IOException { Principal.mostrarStockCritico(); }
    @FXML private void cerrarSesion() throws IOException { Principal.cerrarSesion(); }
}
