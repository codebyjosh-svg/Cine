package org.cine.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.cine.dao.UsuarioDAO;
import org.cine.model.ClienteVinculo;
import org.cine.model.Rol;
import org.cine.model.Usuario;
import org.cine.util.Conexion;
import org.cine.util.SecurityUtil;
import org.cine.service.AutenticacionException;

public class UsuarioDAOImpl implements UsuarioDAO {

    private static final String SP_BUSCAR_LOGIN = "{call sp_buscarusuario_login(?)}";

    @Override
    public Optional<Usuario> buscarPorUsername(String username) throws SQLException {

        if (username == null || username.isBlank()) {
            return Optional.empty();
        }

        try (Connection cn = Conexion.getInstancia().conectar(); CallableStatement cs = cn.prepareCall(
                "{call sp_buscarusuario_login(?)}")) {

            cs.setString(1, username.trim());

            try (ResultSet rs = cs.executeQuery()) {

                if (rs.next()) {

                    Usuario usuario = convertir(rs);

                    usuario.setContrasenaHash(
                            rs.getString("contrasena_hash")
                    );

                    return Optional.of(usuario);
                }

                return Optional.empty();
            }
        }
    }

    @Override
    public Usuario autenticar(String username, String password)
            throws AutenticacionException {

        if (username == null || username.isBlank()
                || password == null || password.isEmpty()) {

            throw new AutenticacionException(
                    AutenticacionException.Motivo.CREDENCIALES_INVALIDAS,
                    "Ingrese usuario y contraseña."
            );
        }

        try {

            Optional<Usuario> encontrado
                    = buscarPorUsername(username);

            if (encontrado.isEmpty()) {

                throw new AutenticacionException(
                        AutenticacionException.Motivo.CREDENCIALES_INVALIDAS,
                        "Usuario o contraseña incorrectos."
                );
            }

            Usuario usuario = encontrado.get();

            boolean correcta
                    = SecurityUtil.verifyPassword(
                            password,
                            usuario.getContrasenaHash()
                    );

            if (!correcta) {

                throw new AutenticacionException(
                        AutenticacionException.Motivo.CREDENCIALES_INVALIDAS,
                        "Usuario o contraseña incorrectos."
                );
            }

            if (!usuario.isEstado()) {

                throw new AutenticacionException(
                        AutenticacionException.Motivo.USUARIO_INACTIVO,
                        "El usuario se encuentra inactivo."
                );
            }

            return usuario;

        } catch (SQLException ex) {

            ex.printStackTrace();

            throw new AutenticacionException(
                    AutenticacionException.Motivo.ERROR_DATOS,
                    "No fue posible validar las credenciales.",
                    ex
            );
        }
    }

    private Usuario convertir(ResultSet resultado) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(resultado.getInt("id_usuario"));
        usuario.setNombreUsuario(resultado.getString("nombre_usuario"));
        usuario.setApellidoUsuario(resultado.getString("apellido_usuario"));
        usuario.setUsername(resultado.getString("username"));
        usuario.setCorreoElectronico(resultado.getString("correo_electronico"));
        usuario.setIdRol(resultado.getInt("id_rol"));
        usuario.setNombreRol(resultado.getString("nombre_rol"));
        int idCliente = resultado.getInt("id_cliente");
        usuario.setIdCliente(resultado.wasNull() ? null : idCliente);
        usuario.setEstado(resultado.getBoolean("estado"));
        Timestamp fecha = resultado.getTimestamp("fecha_registro");
        usuario.setFechaRegistro(fecha == null ? null : fecha.toLocalDateTime());
        return usuario;
    }

    private void parametros(CallableStatement sentencia, Usuario usuario, int inicio) throws SQLException {
        sentencia.setString(inicio, usuario.getNombreUsuario());
        sentencia.setString(inicio + 1, usuario.getApellidoUsuario());
        sentencia.setString(inicio + 2, usuario.getUsername());
        sentencia.setString(inicio + 3, usuario.getCorreoElectronico());
        sentencia.setInt(inicio + 4, usuario.getIdRol());
        if (usuario.getIdCliente() == null) {
            sentencia.setNull(inicio + 5, Types.INTEGER);
        } else {
            sentencia.setInt(inicio + 5, usuario.getIdCliente());
        }
    }

    @Override
    public List<Usuario> listar() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        try (Connection conexion = Conexion.getInstance().getConnection(); CallableStatement sentencia = conexion.prepareCall("{call sp_listarusuarios()}"); ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                usuarios.add(convertir(resultado));
            }
        }
        return usuarios;
    }

    @Override
    public Optional<Usuario> buscarPorId(int idUsuario) throws SQLException {
        try (Connection conexion = Conexion.getInstance().getConnection(); CallableStatement sentencia = conexion.prepareCall("{call sp_buscarusuario(?)}")) {
            sentencia.setInt(1, idUsuario);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? Optional.of(convertir(resultado)) : Optional.empty();
            }
        }
    }

    @Override
    public int insertar(Usuario usuario) throws SQLException {
        try (Connection conexion = Conexion.getInstance().getConnection(); CallableStatement sentencia = conexion.prepareCall("{call sp_insertarusuario(?,?,?,?,?,?,?)}")) {
            parametros(sentencia, usuario, 1);
            sentencia.setString(7, usuario.getContrasenaHash());
            try (ResultSet resultado = sentencia.executeQuery()) {
                if (!resultado.next()) {
                    throw new SQLException("El procedimiento no devolvió el ID del usuario.");
                }
                return resultado.getInt("id_usuario");
            }
        }
    }

    @Override
    public void actualizar(Usuario usuario) throws SQLException {
        try (Connection conexion = Conexion.getInstance().getConnection()) {
            boolean autocommit = conexion.getAutoCommit();
            conexion.setAutoCommit(false);
            try {
                try (CallableStatement sentencia = conexion.prepareCall("{call sp_actualizarusuario(?,?,?,?,?,?,?)}")) {
                    sentencia.setInt(1, usuario.getIdUsuario());
                    parametros(sentencia, usuario, 2);
                    sentencia.execute();
                }
                if (usuario.getContrasenaHash() != null && !usuario.getContrasenaHash().isBlank()) {
                    try (CallableStatement sentencia = conexion.prepareCall("{call sp_cambiarcontrasena(?,?)}")) {
                        sentencia.setInt(1, usuario.getIdUsuario());
                        sentencia.setString(2, usuario.getContrasenaHash());
                        sentencia.execute();
                    }
                }
                conexion.commit();
            } catch (SQLException ex) {
                conexion.rollback();
                throw ex;
            } finally {
                conexion.setAutoCommit(autocommit);
            }
        }
    }

    @Override
    public void cambiarEstado(int idUsuario, boolean activo) throws SQLException {
        try (Connection conexion = Conexion.getInstance().getConnection(); CallableStatement sentencia = conexion.prepareCall("{call sp_cambiarestadousuario(?,?)}")) {
            sentencia.setInt(1, idUsuario);
            sentencia.setBoolean(2, activo);
            sentencia.execute();
        }
    }

    @Override
    public void eliminar(int idUsuario) throws SQLException {
        try (Connection conexion = Conexion.getInstance().getConnection(); CallableStatement sentencia = conexion.prepareCall("{call sp_eliminarusuario(?)}")) {
            sentencia.setInt(1, idUsuario);
            sentencia.execute();
        }
    }

    @Override
    public List<Rol> listarRoles() throws SQLException {
        List<Rol> roles = new ArrayList<>();
        try (Connection conexion = Conexion.getInstance().getConnection(); CallableStatement sentencia = conexion.prepareCall("{call sp_listarroles()}"); ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                roles.add(new Rol(resultado.getInt("id_rol"), resultado.getString("nombre_rol"),
                        resultado.getString("descripcion")));
            }
        }
        return roles;
    }

    @Override
    public List<ClienteVinculo> listarClientes() throws SQLException {
        List<ClienteVinculo> clientes = new ArrayList<>();
        try (Connection conexion = Conexion.getInstance().getConnection(); CallableStatement sentencia = conexion.prepareCall("{call sp_listarclientes()}"); ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                clientes.add(new ClienteVinculo(resultado.getInt("id_cliente"),
                        resultado.getString("nombre_cliente") + " " + resultado.getString("apellido_cliente"),
                        resultado.getString("correo_electronico"), resultado.getBoolean("estado")));
            }
        }
        return clientes;
    }
}
