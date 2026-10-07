package org.cine.dao;

import java.sql.SQLException;
import java.util.Optional;
import org.cine.model.Usuario;
import org.cine.service.AutenticacionException;

public interface UsuarioDAO {
    Optional<Usuario> buscarPorUsername(String username) throws SQLException;
    Usuario autenticar(String username, String password) throws AutenticacionException;
}
