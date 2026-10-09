package org.cine.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.cine.dao.MovimientoInventarioDAO;
import org.cine.model.MovimientoInventario;
import org.cine.model.Producto;
import org.cine.util.Conexion;

/** Usa los procedimientos existentes; el registro y el stock son una transacción. */
public class MovimientoInventarioDAOImpl implements MovimientoInventarioDAO {
    @Override
    public List<MovimientoInventario> listarHistorial() throws SQLException {
        List<MovimientoInventario> movimientos = new ArrayList<>();
        try (Connection cn = Conexion.getInstancia().conectar();
                CallableStatement cs = cn.prepareCall("{CALL sp_listarmovimientosinventario()}");
                ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                MovimientoInventario movimiento = new MovimientoInventario();
                movimiento.setIdMovimiento(rs.getInt("id_movimiento"));
                movimiento.setIdProducto(rs.getInt("id_producto"));
                movimiento.setNombreProducto(rs.getString("nombre_producto"));
                movimiento.setIdUsuario(rs.getInt("id_usuario"));
                movimiento.setUsuario(rs.getString("usuario"));
                int venta = rs.getInt("id_venta");
                movimiento.setIdVenta(rs.wasNull() ? null : venta);
                movimiento.setTipoMovimiento(rs.getString("tipo_movimiento"));
                movimiento.setCantidad(rs.getInt("cantidad"));
                movimiento.setFechaMovimiento(rs.getTimestamp("fecha_movimiento").toLocalDateTime());
                movimiento.setObservacion(rs.getString("observacion"));
                movimientos.add(movimiento);
            }
        }
        return movimientos;
    }

    @Override
    public List<Producto> listarProductosActivos() throws SQLException {
        List<Producto> productos = new ArrayList<>();
        try (Connection cn = Conexion.getInstancia().conectar();
                CallableStatement cs = cn.prepareCall("{CALL sp_listarproductos()}");
                ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                if (rs.getInt("estado") == 1) productos.add(mapearProducto(rs));
            }
        }
        return productos;
    }

    @Override
    public Optional<Producto> buscarProducto(int idProducto) throws SQLException {
        if (idProducto <= 0) throw new IllegalArgumentException("Selecciona un producto válido.");
        try (Connection cn = Conexion.getInstancia().conectar();
                CallableStatement cs = cn.prepareCall("{CALL sp_buscarproducto(?)}")) {
            cs.setInt(1, idProducto);
            try (ResultSet rs = cs.executeQuery()) {
                return rs.next() ? Optional.of(mapearProducto(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public void registrar(MovimientoInventario movimiento) throws SQLException {
        validar(movimiento);
        try (Connection cn = Conexion.getInstancia().conectar();
                CallableStatement cs = cn.prepareCall("{CALL sp_registrarmovimientoinventario(?, ?, ?, ?, ?)}")) {
            cs.setInt(1, movimiento.getIdProducto());
            cs.setInt(2, movimiento.getIdUsuario());
            cs.setString(3, movimiento.getTipoMovimiento());
            cs.setInt(4, movimiento.getCantidad());
            cs.setString(5, movimiento.getObservacion().strip());
            cs.execute();
        }
    }

    private void validar(MovimientoInventario movimiento) {
        if (movimiento == null || movimiento.getIdProducto() <= 0 || movimiento.getIdUsuario() <= 0) {
            throw new IllegalArgumentException("El movimiento requiere un producto y un usuario válidos.");
        }
        if (!"entrada".equals(movimiento.getTipoMovimiento()) && !"salida".equals(movimiento.getTipoMovimiento())) {
            throw new IllegalArgumentException("Selecciona Entrada o Salida para el movimiento manual.");
        }
        if (movimiento.getCantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser un número entero mayor que cero.");
        }
        String observacion = movimiento.getObservacion();
        if (observacion == null || observacion.isBlank()) {
            throw new IllegalArgumentException("Escribe una observación para el movimiento.");
        }
        observacion = observacion.strip();
        if (observacion.codePointCount(0, observacion.length()) > 200) {
            throw new IllegalArgumentException("La observación puede tener hasta 200 caracteres.");
        }
    }

    private Producto mapearProducto(ResultSet rs) throws SQLException {
        return new Producto(rs.getInt("id_producto"), rs.getString("nombre_producto"),
                rs.getInt("stock"), rs.getInt("estado"));
    }
}
