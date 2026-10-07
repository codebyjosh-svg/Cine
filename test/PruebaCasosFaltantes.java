package org.cine;
 
import java.sql.Connection;
import java.sql.PreparedStatement;
 
import org.cine.dao.UsuarioDAO;
import org.cine.dao.impl.UsuarioDAOImpl;
import org.cine.model.Usuario;
import org.cine.service.AutenticacionException;
import org.cine.service.SesionContext;
import org.cine.util.Conexion;
 
public class PruebaCasosFaltantes {
 
    public static void main(String[] args) throws Exception {
 
        UsuarioDAO dao = new UsuarioDAOImpl();
 
        probarUsuarioInactivo(dao);
        probarLogout(dao);
 
        System.out.println("[OK] T1.1.10 completada.");
    }
 
    private static void probarUsuarioInactivo(
            UsuarioDAO dao) throws Exception {
 
        cambiarEstadoUsuario("admin.cine", 0);
 
        try {
 
            try {
 
                dao.autenticar(
                        "admin.cine",
                        "AdminCine2026!"
                );
 
                throw new AssertionError(
                        "Se esperaba rechazo del usuario inactivo."
                );
 
            } catch (AutenticacionException ex) {
 
                if (ex.getMotivo()
                        != AutenticacionException.Motivo.USUARIO_INACTIVO) {
 
                    throw new AssertionError(
                            "Motivo inesperado: "
                            + ex.getMotivo()
                    );
                }
 
                System.out.println(
                        "[OK] Usuario inactivo rechazado correctamente."
                );
            }
 
        } finally {
 
            cambiarEstadoUsuario("admin.cine", 1);
        }
    }
 
    private static void probarLogout(
            UsuarioDAO dao) throws Exception {
 
        Usuario usuario = dao.autenticar(
                "admin.cine",
                "AdminCine2026!"
        );
 
        SesionContext.iniciarSesion(usuario);
 
        if (!SesionContext.haySesionActiva()) {
            throw new AssertionError(
                    "La sesión debería estar activa."
            );
        }
 
        System.out.println(
                "[OK] Sesión iniciada correctamente."
        );
 
        SesionContext.cerrarSesion();
 
        if (SesionContext.haySesionActiva()) {
            throw new AssertionError(
                    "La sesión debería estar cerrada."
            );
        }
 
        if (SesionContext.getUsuarioActual() != null) {
            throw new AssertionError(
                    "El usuario actual debería ser null."
            );
        }
 
        System.out.println(
                "[OK] Logout limpia correctamente la sesión."
        );
    }
 
    private static void cambiarEstadoUsuario(
            String username,
            int estado) throws Exception {
 
        String sql =
                "UPDATE usuarios SET estado = ? WHERE username = ?";
 
        try (
                Connection con =
                        Conexion.getInstancia().conectar();
                PreparedStatement ps =
                        con.prepareStatement(sql)
        ) {
 
            ps.setInt(1, estado);
            ps.setString(2, username);
            ps.executeUpdate();
        }
    }
}