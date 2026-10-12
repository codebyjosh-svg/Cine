package org.cine.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import org.cine.util.Conexion;
import org.cine.util.SecurityUtil;

/** Datos aislados de inventario; requiere una copia desechable de la BD. */
public final class DatosInventarioPrueba implements AutoCloseable {
    private final Connection conexion;
    public int categoria, producto, productoVacio, productoInactivo, usuarioInactivo;
    public int admin, bodega, taquillero, cliente;
    public final String sufijo = UUID.randomUUID().toString().substring(0, 8);

    public DatosInventarioPrueba() throws SQLException {
        if (!Boolean.getBoolean("cine.pruebas.bd")) {
            throw new IllegalStateException("Usa una base de pruebas y -Dcine.pruebas.bd=true.");
        }
        conexion = Conexion.getInstancia().conectar();
        try {
            admin = usuario("admin.cine");
            bodega = usuario("bodega.cine");
            taquillero = usuario("taquilla.cine");
            cliente = usuario("cliente.cine");
            categoria = insertar("INSERT INTO categorias_producto(nombre_categoria) VALUES (?)",
                    "Prueba inventario " + sufijo);
            producto = producto("Palomitas prueba " + sufijo, 10, 1);
            productoVacio = producto("Bebida prueba " + sufijo, 0, 1);
            productoInactivo = producto("Producto inactivo " + sufijo, 5, 0);
            usuarioInactivo = insertar("INSERT INTO usuarios(nombre_usuario,apellido_usuario,username,"
                    + "correo_electronico,contrasena_hash,id_rol,estado) "
                    + "SELECT 'Prueba','Inactivo',?,?,?,id_rol,0 FROM roles WHERE nombre_rol='bodega'",
                    "inactivo.inv." + sufijo, "inventario." + sufijo + "@prueba.test",
                    SecurityUtil.hashPassword("ClaveSoloPruebas2026!"));
        } catch (SQLException | RuntimeException ex) {
            try { close(); } catch (SQLException limpieza) { ex.addSuppressed(limpieza); }
            throw ex;
        }
    }

    private int usuario(String username) throws SQLException {
        try (PreparedStatement cs = conexion.prepareStatement("SELECT id_usuario FROM usuarios WHERE username=?")) {
            cs.setString(1, username);
            try (ResultSet rs = cs.executeQuery()) {
                if (!rs.next()) throw new SQLException("Falta una cuenta de ejemplo del DML: " + username);
                return rs.getInt(1);
            }
        }
    }

    private int producto(String nombre, int stock, int estado) throws SQLException {
        return insertar("INSERT INTO productos(nombre_producto,precio,stock,id_categoria_producto,estado) VALUES (?,10,?,?,?)",
                nombre, stock, categoria, estado);
    }

    private int insertar(String sql, Object... valores) throws SQLException {
        try (PreparedStatement cs = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < valores.length; i++) cs.setObject(i + 1, valores[i]);
            cs.executeUpdate();
            try (ResultSet rs = cs.getGeneratedKeys()) {
                if (!rs.next()) throw new SQLException("El dato de prueba no devolvió su ID.");
                return rs.getInt(1);
            }
        }
    }

    public void cambiarEstadoProducto(int id, int estado) throws SQLException {
        if (id != producto && id != productoVacio && id != productoInactivo) {
            throw new IllegalArgumentException("Solo se puede modificar un producto de la prueba.");
        }
        try (PreparedStatement cs = conexion.prepareStatement("UPDATE productos SET estado=? WHERE id_producto=?")) {
            cs.setInt(1, estado);
            cs.setInt(2, id);
            cs.executeUpdate();
        }
    }

    @Override
    public void close() throws SQLException {
        try (Statement cs = conexion.createStatement()) {
            String productos = producto + "," + productoVacio + "," + productoInactivo;
            cs.executeUpdate("DELETE FROM movimientos_inventario WHERE id_producto IN (" + productos + ")");
            cs.executeUpdate("DELETE FROM productos WHERE id_producto IN (" + productos + ")");
            cs.executeUpdate("DELETE FROM categorias_producto WHERE id_categoria_producto=" + categoria);
            cs.executeUpdate("DELETE FROM usuarios WHERE id_usuario=" + usuarioInactivo);
        } finally {
            conexion.close();
        }
    }
}
