package org.cine.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import org.cine.dao.ClienteDAO;
import org.cine.model.Cliente;
import org.cine.util.Conexion;

public class ClienteDAOImpl implements ClienteDAO {

    private String ultimoError = "";

    @Override
    public List<Cliente> listarTodos() {

        ultimoError = "";
        List<Cliente> lista = new ArrayList<>();

        String sql = "{call sp_listarclientes()}";

        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {

            while (rs.next()) {
                lista.add(obtenerCliente(rs));
            }

        } catch (SQLException e) {
            registrarError(e);
        }

        return lista;
    }

    @Override
    public Cliente buscarCliente(int idCliente) {

        ultimoError = "";

        String sql = "{call sp_buscarcliente(?)}";

        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, idCliente);

            try (ResultSet rs = cs.executeQuery()) {

                if (rs.next()) {
                    return obtenerCliente(rs);
                }
            }

        } catch (SQLException e) {
            registrarError(e);
        }

        return null;
    }

    @Override
    public boolean insertar(Cliente cliente) {

        ultimoError = "";

        String sql = "{call sp_insertarcliente(?, ?, ?, ?, ?)}";

        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql)) {

            asignarDatos(cs, cliente, 1);

            try (ResultSet rs = cs.executeQuery()) {

                if (rs.next()) {
                    cliente.setIdCliente(rs.getInt("id_cliente"));
                    cliente.setEstado(1);
                    return true;
                }
            }

            ultimoError = "No se recibió el ID del cliente registrado.";

        } catch (SQLException e) {
            registrarError(e);
        }

        return false;
    }

    @Override
    public boolean actualizar(Cliente cliente) {

        ultimoError = "";

        String sql = "{call sp_actualizarcliente(?, ?, ?, ?, ?, ?)}";

        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, cliente.getIdCliente());

            asignarDatos(cs, cliente, 2);

            cs.execute();

            return true;

        } catch (SQLException e) {
            registrarError(e);
        }

        return false;
    }

    @Override
    public boolean eliminar(int idCliente) {

        ultimoError = "";

        String sql = "{call sp_eliminarcliente(?)}";

        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, idCliente);

            if (cs.executeUpdate() > 0) {
                return true;
            }

            ultimoError = "El cliente ya no existe.";

        } catch (SQLException e) {
            registrarError(e);
        }

        return false;
    }

    @Override
    public boolean cambiarEstado(int idCliente, int estado) {

        ultimoError = "";

        if (estado != 0 && estado != 1) {
            ultimoError = "El estado debe ser 0 o 1.";
            return false;
        }

        String sql = "{call sp_cambiarestadocliente(?, ?)}";

        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, idCliente);
            cs.setInt(2, estado);

            cs.execute();

            return true;

        } catch (SQLException e) {
            registrarError(e);
        }

        return false;
    }

    @Override
    public String getUltimoError() {
        return ultimoError;
    }

    private Cliente obtenerCliente(ResultSet rs) throws SQLException {

        Cliente cliente = new Cliente();

        cliente.setIdCliente(rs.getInt("id_cliente"));
        cliente.setCui(rs.getString("cui"));
        cliente.setNombreCliente(rs.getString("nombre_cliente"));
        cliente.setApellidoCliente(rs.getString("apellido_cliente"));
        cliente.setCorreoElectronico(rs.getString("correo_electronico"));
        cliente.setTelefono(rs.getString("telefono"));
        cliente.setEstado(rs.getInt("estado"));
        cliente.setFechaRegistro(rs.getTimestamp("fecha_registro"));

        return cliente;
    }

    private void asignarDatos(CallableStatement cs, Cliente cliente,
            int posicion) throws SQLException {

        asignarOpcional(cs, posicion, cliente.getCui());

        cs.setString(posicion + 1, cliente.getNombreCliente());
        cs.setString(posicion + 2, cliente.getApellidoCliente());
        cs.setString(posicion + 3, cliente.getCorreoElectronico());

        asignarOpcional(cs, posicion + 4, cliente.getTelefono());
    }

    private void asignarOpcional(CallableStatement cs, int posicion,
            String valor) throws SQLException {

        if (valor == null || valor.isBlank()) {
            cs.setNull(posicion, Types.VARCHAR);
        } else {
            cs.setString(posicion, valor.trim());
        }
    }

    private void registrarError(SQLException e) {

        switch (e.getErrorCode()) {

            case 1062:
                ultimoError = "Ya existe un cliente con ese CUI "
                        + "o correo electrónico.";
                break;

            case 1451:
                ultimoError = "No se puede eliminar este cliente "
                        + "porque tiene registros relacionados. "
                        + "Puedes desactivarlo.";
                break;

            case 1044:
            case 1045:
                ultimoError = "Revisa el usuario, la contraseña "
                        + "y los permisos de la base de datos.";
                break;

            default:
                ultimoError = "No se pudo completar la operación: "
                        + e.getMessage();
                break;
        }

        System.err.println(ultimoError);
    }
}