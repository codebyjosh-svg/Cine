package org.cine.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.cine.dao.SalaDAO;
import org.cine.model.Sala;
import org.cine.util.Conexion;

public class SalaDAOImpl implements SalaDAO {

    private String ultimoError = "";

    @Override
    public List<Sala> listarTodos() {
        ultimoError = "";
        List<Sala> salas = new ArrayList<>();

        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall("{call sp_listarsalas()}");
             ResultSet rs = cs.executeQuery()) {

            while (rs.next()) {
                salas.add(mapearSala(rs));
            }
        } catch (SQLException e) {
            registrarError("listar salas", e);
        }

        return salas;
    }

    @Override
    public Sala buscarSala(int idSala) {
        ultimoError = "";

        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall("{call sp_buscarsala(?)}")) {

            cs.setInt(1, idSala);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    return mapearSala(rs);
                }
            }
        } catch (SQLException e) {
            registrarError("buscar sala", e);
        }

        return null;
    }

    @Override
    public boolean insertar(Sala sala) {
        ultimoError = "";
        String sql = "{call sp_insertarsala(?, ?)}";

        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setString(1, limpiar(sala.getNombreSala()));
            cs.setString(2, limpiar(sala.getFormato()));

            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    sala.setIdSala(rs.getInt("id_sala"));
                    sala.setEstado(1);
                    return true;
                }
            }

            ultimoError = "La base de datos no devolvió el ID de la sala.";
        } catch (SQLException e) {
            registrarError("insertar sala", e);
        }

        return false;
    }

    @Override
    public boolean actualizar(Sala sala) {
        ultimoError = "";
        String sql = "{call sp_actualizarsala(?, ?, ?)}";

        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, sala.getIdSala());
            cs.setString(2, limpiar(sala.getNombreSala()));
            cs.setString(3, limpiar(sala.getFormato()));
            if (cs.executeUpdate() > 0) {
                return true;
                      }
            ultimoError = "La sala seleccionada ya no existe.";
        } catch (SQLException e) {
            registrarError("actualizar sala", e);
        }
        return false;
    }

    @Override
    public boolean eliminar(int idSala) {
        ultimoError = "";

        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall("{call sp_eliminarsala(?)}")) {

            cs.setInt(1, idSala);
            if (cs.executeUpdate() > 0) {
                return true;
            }
            ultimoError = "La sala seleccionada ya no existe.";
        } catch (SQLException e) {
            registrarError("eliminar sala", e);
        }
        return false;
    }

    @Override
    public boolean cambiarEstado(int idSala, int estado) {
        ultimoError = "";

        if (estado != 0 && estado != 1) {
            ultimoError = "El estado de la sala debe ser 0 o 1.";
            return false;
        }

        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(
                     "{call sp_cambiarestadosala(?, ?)}")) {

            cs.setInt(1, idSala);
            cs.setInt(2, estado);
            if (cs.executeUpdate() > 0) {
                return true;
            }
            ultimoError = "La sala seleccionada ya no existe.";
        } catch (SQLException e) {
            registrarError("cambiar estado de sala", e);
        }
        return false;
    }

    @Override
    public String getUltimoError() {
        return ultimoError;
    }

    private Sala mapearSala(ResultSet rs) throws SQLException {
        return new Sala(
                rs.getInt("id_sala"),
                rs.getString("nombre_sala"),
                rs.getString("formato"),
                rs.getInt("estado")
        );
    }

    private String limpiar(String valor) {
        return valor == null ? null : valor.trim();
    }

    private void registrarError(String operacion, SQLException e) {
        if (e.getErrorCode() == 1062) {
            ultimoError = "Ya existe una sala con ese nombre.";
        } else if (e.getErrorCode() == 1451) {
            ultimoError = "No se puede eliminar la sala porque tiene "
                    + "butacas o funciones relacionadas.";
        } else {
            ultimoError = "No se pudo " + operacion + ": " + e.getMessage();
        }

        System.err.println(ultimoError);
    }
}
