package org.cine.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.cine.model.ClienteVinculo;
import org.cine.model.Rol;
import org.cine.model.Usuario;
import org.cine.service.AutenticacionException;

public interface UsuarioDAO {
    Optional<Usuario> buscarPorUsername(String username) throws SQLException;
    Usuario autenticar(String username, String password) throws AutenticacionException;
    List<Usuario> listar() throws SQLException;
    Optional<Usuario> buscarPorId(int idUsuario) throws SQLException;
    int insertar(Usuario usuario) throws SQLException;
    void actualizar(Usuario usuario) throws SQLException;
    void cambiarEstado(int idUsuario, boolean activo) throws SQLException;
    void eliminar(int idUsuario) throws SQLException;
    List<Rol> listarRoles() throws SQLException;
    List<ClienteVinculo> listarClientes() throws SQLException;
}
