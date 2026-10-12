package org.cine.controller;

import java.io.IOException;
import java.sql.*;
import org.cine.util.Conexion;
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
    @FXML private Label lblKpiUsuarios, lblKpiPeliculas, lblKpiGeneros, lblKpiSalas;

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
        cargarIndicadoresReales();
    }

    private void cargarIndicadoresReales() {
        String sql = "SELECT "
            + "(SELECT COUNT(*) FROM usuarios) AS usuarios, "
            + "(SELECT COUNT(*) FROM peliculas) AS peliculas, "
            + "(SELECT COUNT(*) FROM generos) AS generos, "
            + "(SELECT COUNT(*) FROM salas) AS salas";
        try (Connection c = Conexion.getInstance().getConnection();
             PreparedStatement st = c.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            if (rs.next()) {
                if (lblKpiUsuarios != null) lblKpiUsuarios.setText(rs.getString("usuarios"));
                if (lblKpiPeliculas != null) lblKpiPeliculas.setText(rs.getString("peliculas"));
                if (lblKpiGeneros != null) lblKpiGeneros.setText(rs.getString("generos"));
                if (lblKpiSalas != null) lblKpiSalas.setText(rs.getString("salas"));
            }
        } catch (SQLException ex) {
            System.err.println("No se pudieron cargar los indicadores del Dashboard: " + ex.getMessage());
            if (lblKpiUsuarios != null) lblKpiUsuarios.setText("—");
            if (lblKpiPeliculas != null) lblKpiPeliculas.setText("—");
            if (lblKpiGeneros != null) lblKpiGeneros.setText("—");
            if (lblKpiSalas != null) lblKpiSalas.setText("—");
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
    // SALAS Y BUTACAS - US-2.1
    // ==============================

    @FXML
    private void abrirSalas() throws IOException {
        Principal.mostrarSalas();
    }

    // ==============================
    // VENTA DE BOLETOS - US-2.4
    // ==============================

    @FXML
    private void abrirVenta() throws IOException {

        System.out.println("=================================");
        System.out.println("BOTON VENDER BOLETOS PRESIONADO");
        System.out.println("=================================");

        Principal.mostrarVenta();
    }

    // ==============================
    // FUNCIONES - US-2.2
    // ==============================

    @FXML
    private void abrirFunciones() throws IOException {
        Principal.mostrarProgramacion();
    }

    @FXML
    private void abrirConfiteria() throws IOException {
        Principal.mostrarConfiteria();
    }

    @FXML
    private void abrirReportes() throws IOException {
        Principal.mostrarReportes();
    }

    // ==============================
    // CERRAR SESIÓN
    // ==============================

    @FXML
    private void cerrarSesion() throws IOException {
        Principal.cerrarSesion();
    }
}