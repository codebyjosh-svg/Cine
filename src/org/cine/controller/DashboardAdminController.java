package org.cine.controller;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.cine.model.Usuario;
import org.cine.service.SesionContext;
import org.cine.system.Principal;

public class DashboardAdminController {

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
    // USUARIOS
    // ==============================

    @FXML
    private void abrirUsuarios() throws IOException {
        Principal.mostrarUsuarios();
    }

    // ==============================
    // PELÍCULAS
    // ==============================

    @FXML
    private void abrirPeliculas() throws IOException {
        Principal.mostrarPeliculas();
    }

    // ==============================
    // GÉNEROS
    // ==============================

    @FXML
    private void abrirGeneros() throws IOException {
        Principal.mostrarGeneros();
    }

    // ==============================
    // CLIENTES
    // ==============================

    @FXML
    private void abrirClientes() throws IOException {
        Principal.mostrarClientes();
    }

    // ==============================
    // VENTA DE BOLETOS
    // ==============================

    @FXML
    private void abrirVenta() throws IOException {
        System.out.println("=================================");
        System.out.println("BOTON VENDER BOLETOS PRESIONADO");
        System.out.println("=================================");

        Principal.mostrarVenta();
    }

    // ==============================
    // FUNCIONES
    // ==============================

    @FXML
    private void abrirFunciones() throws IOException {
        Principal.mostrarProgramacion();
    }

    // ==============================
    // CERRAR SESIÓN
    // ==============================

    @FXML
    private void cerrarSesion() throws IOException {
        Principal.cerrarSesion();
    }
}