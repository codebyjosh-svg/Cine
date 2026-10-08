package org.cine;

import org.cine.dao.UsuarioDAO;
import org.cine.dao.impl.UsuarioDAOImpl;
import org.cine.model.Usuario;
import org.cine.service.AutenticacionException;

public class PruebaLoginIntegracion {
    public static void main(String[] args) throws Exception {
        UsuarioDAO dao = new UsuarioDAOImpl();
        probarCorrecto(dao, "admin.cine", "AdminCine2026!", "admin");
        probarIncorrecto(dao, "admin.cine", "incorrecta");
        System.out.println("[OK] Pruebas de login ejecutadas.");
    }

    private static void probarCorrecto(UsuarioDAO dao, String username, String password, String rol) throws Exception {
        Usuario usuario = dao.autenticar(username, password);
        if (!rol.equals(usuario.getNombreRol())) {
            throw new AssertionError("Rol inesperado: " + usuario.getNombreRol());
        }
    }

    private static void probarIncorrecto(UsuarioDAO dao, String username, String password) throws Exception {
        try {
            dao.autenticar(username, password);
            throw new AssertionError("Se esperaba rechazo de credenciales.");
        } catch (AutenticacionException ex) {
            if (ex.getMotivo() != AutenticacionException.Motivo.CREDENCIALES_INVALIDAS) {
                throw ex;
            }
        }
    }
}
