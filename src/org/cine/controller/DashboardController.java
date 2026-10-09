package org.cine.controller;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.cine.model.Usuario;
import org.cine.service.SesionContext;
import org.cine.system.Principal;

/**
 * Controlador de los dashboards de personal y cliente
 * referenciados por los archivos FXML.
 */
public final class DashboardController {

    @FXML
    private Label lblUsuario;

    @FXML
    private Label lblRol;

    @FXML
    private void initialize() {

        Usuario usuario = SesionContext.getUsuarioActual();

        if (usuario != null) {

            if (lblUsuario != null) {
                lblUsuario.setText(usuario.getNombreCompleto());
            }

            if (lblRol != null) {
                lblRol.setText(usuario.getNombreRol());
            }
        }
    }

    // ==============================
    // VENTAS
    // ==============================

    @FXML
    private void abrirVentas() throws IOException {
        Principal.mostrarVentas();
    }

    // ==============================
    // FUNCIONES - US-2.2
    // ==============================

    @FXML
    private void abrirFunciones() throws IOException {
        Principal.mostrarProgramacion();
    }

    // ==============================
    // STOCK CRÍTICO - US-3.3
    // ==============================

    @FXML
    private void abrirStockCritico() throws IOException {
        Principal.mostrarStockCritico();
    }

    // ==============================
    // CERRAR SESIÓN
    // ==============================

    @FXML
    private void cerrarSesion() throws IOException {
        Principal.cerrarSesion();
    }
}