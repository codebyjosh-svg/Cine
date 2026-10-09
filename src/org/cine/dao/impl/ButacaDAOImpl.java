package org.cine.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.cine.dao.ButacaDAO;
import org.cine.model.Butaca;
import org.cine.util.Conexion;

public class ButacaDAOImpl implements ButacaDAO {

    private String ultimoError = "";

    @Override
    public List<Butaca> listarTodos() {
        ultimoError = "";
        List<Butaca> butacas = new ArrayList<>();

        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall("{call sp_listarbutacas()}");
             ResultSet rs = cs.executeQuery()) {

            while (rs.next()) {
                butacas.add(mapearButaca(rs));
            }
        } catch (SQLException e) {
            registrarError("listar butacas", e);
        }

        return butacas;
    }

    @Override
    public List<Butaca> listarPorSala(int idSala) {
        List<Butaca> butacasSala = new ArrayList<>();
        for (Butaca butaca : listarTodos()) {
            if (butaca.getIdSala() == idSala) {
                butacasSala.add(butaca);
            }
        }
        return butacasSala;
    }

    @Override
    public Butaca buscarButaca(int idButaca) {
        ultimoError = "";

        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall("{call sp_buscarbutaca(?)}")) {

            cs.setInt(1, idButaca);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    return mapearButaca(rs);
                }
            }
        } catch (SQLException e) {
            registrarError("buscar butaca", e);
        }

        return null;
    }

    @Override
    public boolean insertar(Butaca butaca) {
        ultimoError = "";
        String sql = "{call sp_insertarbutaca(?, ?, ?)}";

        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, butaca.getIdSala());
            cs.setString(2, limpiar(butaca.getFila()));
            cs.setInt(3, butaca.getNumero());

            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    butaca.setIdButaca(rs.getInt("id_butaca"));
                    butaca.setEstado(1);
                    return true;
                }
            }

            ultimoError = "La base de datos no devolvió el ID de la butaca.";
        } catch (SQLException e) {
            registrarError("insertar butaca", e);
        }

        return false;
    }

    @Override
    public boolean actualizar(Butaca butaca) {
        ultimoError = "";
        String sql = "{call sp_actualizarbutaca(?, ?, ?, ?)}";

        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, butaca.getIdButaca());
            cs.setInt(2, butaca.getIdSala());
            cs.setString(3, limpiar(butaca.getFila()));
            cs.setInt(4, butaca.getNumero());
            if (cs.executeUpdate() > 0) {
                return true;
            }
            ultimoError = "La butaca seleccionada ya no existe.";
        } catch (SQLException e) {
            registrarError("actualizar butaca", e);
        }
        return false;
    }

    @Override
    public boolean eliminar(int idButaca) {
        ultimoError = "";

        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall("{call sp_eliminarbutaca(?)}")) {

            cs.setInt(1, idButaca);
            if (cs.executeUpdate() > 0) {
                return true;
            }
            ultimoError = "La butaca seleccionada ya no existe.";
        } catch (SQLException e) {
            registrarError("eliminar butaca", e);
        }
        return false;
    }

    @Override
    public boolean cambiarEstado(int idButaca, int estado) {
        ultimoError = "";

        if (estado != 0 && estado != 1) {
            ultimoError = "El estado de la butaca debe ser 0 o 1.";
            return false;
        }

        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(
                     "{call sp_cambiarestadobutaca(?, ?)}")) {

            cs.setInt(1, idButaca);
            cs.setInt(2, estado);
            if (cs.executeUpdate() > 0) {
                return true;
            }
            ultimoError = "La butaca seleccionada ya no existe.";
        } catch (SQLException e) {
            registrarError("cambiar estado de butaca", e);
        }
        return false;
    }

    @Override
    public String getUltimoError() {
        return ultimoError;
    }

    private Butaca mapearButaca(ResultSet rs) throws SQLException {
        return new Butaca(
                rs.getInt("id_butaca"),
                rs.getInt("id_sala"),
                rs.getString("nombre_sala"),
                rs.getString("fila"),
                rs.getInt("numero"),
                rs.getInt("estado")
        );
    }


  private String limpiar(String valor) {
        return valor == null ? null : valor.trim();
    }

    private void registrarError(String operacion, SQLException e) {
        if (e.getErrorCode() == 1062) {
            ultimoError = "Ya existe una butaca con esa fila y número "
                    + "en la sala.";
        } else if (e.getErrorCode() == 1451) {
            ultimoError = "No se puede eliminar la butaca porque tiene "
                    + "boletos relacionados.";
        } else if (e.getErrorCode() == 1452) {
            ultimoError = "La sala seleccionada no existe.";
        } else {
            ultimoError = "No se pudo " + operacion + ": " + e.getMessage();

}
        }
}
