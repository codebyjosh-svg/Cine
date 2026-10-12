package org.cine.test;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.cine.dao.impl.UsuarioDAOImpl;
import org.cine.util.Conexion;
import org.cine.util.PasswordUtil;
import org.cine.service.AutenticacionException;

public class PruebaScriptsCompletos {
    private static int comprobaciones;

    private static void comprobar(boolean condicion, String mensaje) {
        if (!condicion) { throw new AssertionError(mensaje); }
        System.out.println("OK " + (++comprobaciones) + " · " + mensaje);
    }

    private static BigDecimal valor(Connection c, String sql) throws Exception {
        try (PreparedStatement s = c.prepareStatement(sql); ResultSet rs = s.executeQuery()) {
            if (!rs.next()) { throw new AssertionError("Sin resultado: " + sql); }
            return rs.getBigDecimal(1);
        }
    }

    public static void main(String[] args) throws Exception {
        if (!Boolean.getBoolean("cine.pruebas.bd")) {
            throw new IllegalStateException("Configura una BD de pruebas con ambos SQL y agrega -Dcine.pruebas.bd=true.");
        }
        UsuarioDAOImpl dao = new UsuarioDAOImpl();
        try (Connection c = Conexion.getInstance().getConnection()) {
            comprobar(valor(c, "SELECT COUNT(*) FROM usuarios").intValueExact() == 5, "DML carga cinco cuentas");
            comprobar(valor(c, "SELECT COUNT(*) FROM usuarios WHERE estado=1").intValueExact() == 4, "Las cuatro cuentas originales quedan activas");
            comprobar(!dao.buscarPorId(5).orElseThrow().isEstado(), "La cuenta inactivo.cine queda desactivada");
            comprobar(dao.buscarPorId(4).orElseThrow().getIdCliente().equals(6), "Se conserva el vínculo usuario 4 con cliente 6");
            try {
                dao.autenticar("inactivo.cine", "BodegaCine2026!");
                throw new AssertionError("La cuenta inactiva no debe iniciar sesión.");
            } catch (AutenticacionException ex) {
                comprobar(ex.getMotivo() == AutenticacionException.Motivo.USUARIO_INACTIVO,
                        "El login rechaza la cuenta inactiva del DML");
            }
            try (PreparedStatement s = c.prepareStatement("SELECT contrasena_hash FROM usuarios WHERE id_usuario=5"); ResultSet rs = s.executeQuery()) {
                rs.next();
                comprobar(PasswordUtil.verificar("BodegaCine2026!", rs.getString(1)), "Hash de la cuenta de prueba compatible con Java");
            }
            c.setAutoCommit(false);
            try (CallableStatement s = c.prepareCall("{call sp_eliminarusuario(?)}")) {
                s.setInt(1, 2);
                try {
                    s.execute();
                    throw new AssertionError("Se pudo eliminar una cuenta con ventas.");
                } catch (SQLException ex) {
                    comprobar(ex.getErrorCode() == 1451, "MySQL impide eliminar al taquillero con ventas");
                }
            } finally {
                c.rollback();
                c.setAutoCommit(true);
            }
            comprobar(dao.buscarPorId(2).isPresent(), "El usuario con historial se conserva tras el intento de borrar");
            comprobar(valor(c, "SELECT COUNT(*) FROM ventas").intValueExact() == 5, "Se conservan las cinco ventas de demostración");
            comprobar(valor(c, "SELECT SUM(total_venta) FROM vw_lista_ventas WHERE estado='confirmada'").compareTo(new BigDecimal("414.00")) == 0,
                    "Los datos de ventas siguen sumando Q414.00");
            comprobar(valor(c, "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_type='BASE TABLE'").intValueExact() == 14,
                    "El DDL conserva las catorce tablas del cine");
            comprobar(valor(c, "SELECT COUNT(*) FROM information_schema.routines WHERE routine_schema=DATABASE() AND routine_type='PROCEDURE'").intValueExact() == 85,
                    "El DDL conserva los ochenta y cinco procedimientos");
            comprobar(valor(c, "SELECT COUNT(*) FROM information_schema.views WHERE table_schema=DATABASE()").intValueExact() == 13,
                    "El DDL conserva las trece vistas");
        }
        System.out.println("RESULTADO: " + comprobaciones + " comprobaciones de los SQL completos correctas.");
    }
}
