package org.cine.controller;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.cine.dao.impl.ReporteDAOImpl;
import org.cine.model.IndicadoresAdmin;
import java.sql.SQLException;
import org.cine.model.Usuario;
import org.cine.service.SesionContext;
import org.cine.system.Principal;

public class DashboardAdminController {

    @FXML
    private Label lblUsuario;

    @FXML
    private Label lblRol;
    @FXML private Label lblKpiUsuarios, lblKpiPeliculas, lblKpiClientes, lblKpiVentas;

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
        actualizarIndicadores();
    }

    @FXML private void abrirReportes() throws IOException { Principal.mostrarReportes(); }

    @FXML private void actualizarIndicadores() {
        try {
            IndicadoresAdmin k = new ReporteDAOImpl().indicadores();
            if (lblKpiUsuarios != null) lblKpiUsuarios.setText(String.valueOf(k.usuariosActivos()));
            if (lblKpiPeliculas != null) lblKpiPeliculas.setText(String.valueOf(k.peliculasActivas()));
            if (lblKpiClientes != null) lblKpiClientes.setText(String.valueOf(k.clientesActivos()));
            if (lblKpiVentas != null) lblKpiVentas.setText(String.valueOf(k.ventasConfirmadas()));
        } catch (SQLException ex) {
            System.err.println("No se pudieron cargar los KPIs: " + ex.getMessage());
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
    // CERRAR SESIÓN
    // ==============================

    @FXML private void abrirVenta() throws IOException { Principal.mostrarVenta(); }

    @FXML
    private void cerrarSesion() throws IOException {
        Principal.cerrarSesion();
    }
}