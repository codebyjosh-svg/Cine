package org.cine.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;
import org.cine.dao.UsuarioDAO;
import org.cine.model.Usuario;
import org.cine.service.AutenticacionException;
import org.cine.util.Conexion;
import org.cine.util.SecurityUtil;

public class UsuarioDAOImpl implements UsuarioDAO {
    private static final String SP_BUSCAR_LOGIN = "{CALL sp_buscarusuario_login(?)}";

    @Override
    public Optional<Usuario> buscarPorUsername(String username) throws SQLException {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        try (Connection cn = Conexion.getInstancia().conectar();
             CallableStatement cs = cn.prepareCall(SP_BUSCAR_LOGIN)) {
            cs.setString(1, username.trim());
            try (ResultSet rs = cs.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public Usuario autenticar(String username, String password) throws AutenticacionException {
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            throw new AutenticacionException(
                    AutenticacionException.Motivo.CREDENCIALES_INVALIDAS,
                    "Ingrese usuario y contraseña.");
        }
        try {
            Optional<Usuario> encontrado = buscarPorUsername(username);
            if (encontrado.isEmpty()) {
                throw new AutenticacionException(
                        AutenticacionException.Motivo.CREDENCIALES_INVALIDAS,
                        "Usuario o contraseña incorrectos.");
            }
            Usuario usuario = encontrado.get();
            if (!SecurityUtil.verifyPassword(password, usuario.getContrasenaHash())) {
                throw new AutenticacionException(
                        AutenticacionException.Motivo.CREDENCIALES_INVALIDAS,
                        "Usuario o contraseña incorrectos.");
            }
            if (!usuario.isEstado()) {
                throw new AutenticacionException(
                        AutenticacionException.Motivo.USUARIO_INACTIVO,
                        "El usuario se encuentra inactivo.");
            }
            return usuario;
        } catch (SQLException ex) {
            throw new AutenticacionException(
                    AutenticacionException.Motivo.ERROR_DATOS,
                    "No fue posible validar las credenciales.", ex);
        }
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(rs.getInt("id_usuario"));
        usuario.setNombreUsuario(rs.getString("nombre_usuario"));
        usuario.setApellidoUsuario(rs.getString("apellido_usuario"));
        usuario.setUsername(rs.getString("username"));
        usuario.setCorreoElectronico(rs.getString("correo_electronico"));
        usuario.setContrasenaHash(rs.getString("contrasena_hash"));
        usuario.setIdRol(rs.getInt("id_rol"));
        usuario.setNombreRol(rs.getString("nombre_rol"));
        int idCliente = rs.getInt("id_cliente");
        usuario.setIdCliente(rs.wasNull() ? null : idCliente);
        usuario.setEstado(rs.getBoolean("estado"));
        Timestamp fecha = rs.getTimestamp("fecha_registro");
        usuario.setFechaRegistro(fecha == null ? null : fecha.toLocalDateTime());
        return usuario;
    }
}
