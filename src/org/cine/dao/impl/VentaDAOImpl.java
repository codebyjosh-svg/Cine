package org.cine.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.cine.dao.VentaDAO;
import org.cine.model.ClienteVinculo;
import org.cine.model.FacturaVenta;
import org.cine.model.LineaFactura;
import org.cine.model.LineaVentaProducto;
import org.cine.model.Producto;
import org.cine.model.VentaResumen;
import org.cine.util.Conexion;

/** JDBC y procedimientos de venta; las transacciones pertenecen a los SP existentes. */
public final class VentaDAOImpl implements VentaDAO {
    @Override
    public List<Producto> listarProductosDisponibles() throws SQLException {
        List<Producto> lista = new ArrayList<>();
        try (Connection cn = Conexion.getInstancia().getConnection();
                CallableStatement cs = cn.prepareCall("{call sp_listarproductosdisponibles()}");
                ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                lista.add(new Producto(rs.getInt("id_producto"), rs.getString("nombre_producto"),
                        rs.getString("nombre_categoria"), rs.getBigDecimal("precio"),
                        rs.getInt("stock"), rs.getInt("stock_minimo"), rs.getBoolean("estado")));
            }
        }
        return lista;
    }

    @Override
    public List<ClienteVinculo> listarClientesActivos() throws SQLException {
        List<ClienteVinculo> lista = new ArrayList<>();
        try (Connection cn = Conexion.getInstancia().getConnection();
                CallableStatement cs = cn.prepareCall("{call sp_listarclientesactivos()}");
                ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                lista.add(new ClienteVinculo(rs.getInt("id_cliente"),
                        rs.getString("nombre_cliente") + " " + rs.getString("apellido_cliente"),
                        rs.getString("correo_electronico"), true));
            }
        }
        return lista;
    }

    @Override
    public int abrirVenta(int idCliente, int idUsuario) throws SQLException {
        try (Connection cn = Conexion.getInstancia().getConnection();
                CallableStatement cs = cn.prepareCall("{call sp_abrirventa(?,?)}")) {
            cs.setInt(1, idCliente);
            cs.setInt(2, idUsuario);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) { return rs.getInt("id_venta"); }
                throw new SQLException("La base de datos no devolvió el número de venta.");
            }
        }
    }

    @Override
    public VentaResumen buscarVenta(int idVenta) throws SQLException {
        try (Connection cn = Conexion.getInstancia().getConnection();
                CallableStatement cs = cn.prepareCall("{call sp_buscarventa(?)}")) {
            cs.setInt(1, idVenta);
            try (ResultSet rs = cs.executeQuery()) {
                if (!rs.next()) { throw new SQLException("No existe la venta #" + idVenta + "."); }
                return new VentaResumen(rs.getInt("id_venta"), rs.getInt("id_cliente"),
                        rs.getInt("id_usuario"), rs.getString("cliente"), rs.getString("taquillero"),
                        rs.getObject("fecha_venta", java.time.LocalDateTime.class), rs.getString("estado"),
                        rs.getInt("cantidad_boletos"), rs.getBigDecimal("total_boletos"),
                        rs.getBigDecimal("total_productos"), rs.getBigDecimal("total_venta"));
            }
        }
    }

    @Override
    public List<LineaVentaProducto> listarProductosVenta(int idVenta) throws SQLException {
        List<LineaVentaProducto> lista = new ArrayList<>();
        try (Connection cn = Conexion.getInstancia().getConnection();
                CallableStatement cs = cn.prepareCall("{call sp_listarproductosventa(?)}")) {
            cs.setInt(1, idVenta);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    lista.add(new LineaVentaProducto(rs.getInt("id_producto"),
                            rs.getString("nombre_producto"), rs.getInt("cantidad"),
                            rs.getBigDecimal("precio_unitario")));
                }
            }
        }
        return lista;
    }

    @Override
    public void agregarProducto(int idVenta, int idProducto, int cantidad) throws SQLException {
        ejecutar("{call sp_agregarproductoventa(?,?,?)}", idVenta, idProducto, cantidad);
    }

    @Override
    public void cambiarCantidad(int idVenta, int idProducto, int cantidad) throws SQLException {
        ejecutar("{call sp_actualizarcantidadproducto(?,?,?)}", idVenta, idProducto, cantidad);
    }

    @Override
    public void quitarProducto(int idVenta, int idProducto) throws SQLException {
        ejecutar("{call sp_quitarproductoventa(?,?)}", idVenta, idProducto);
    }

    @Override
    public void validarStock(int idVenta) throws SQLException {
        try (Connection cn = Conexion.getInstancia().getConnection();
                CallableStatement cs = cn.prepareCall("{call sp_validarstockventa(?)}")) {
            cs.setInt(1, idVenta);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    throw new SQLException("Stock insuficiente o producto inactivo: "
                            + rs.getString("nombre_producto") + ". Cantidad solicitada: "
                            + rs.getInt("cantidad") + "; disponible: " + rs.getInt("stock") + ".", "45000");
                }
            }
        }
    }

    @Override
    public void confirmarVenta(int idVenta) throws SQLException {
        // La prevalidación informa el producto; sp_confirmarventa vuelve a bloquear y validar.
        validarStock(idVenta);
        ejecutar("{call sp_confirmarventa(?)}", idVenta);
    }

    @Override
    public FacturaVenta obtenerFactura(int idVenta) throws SQLException {
        VentaResumen venta = buscarVenta(idVenta);
        if (!"confirmada".equals(venta.estado())) {
            throw new SQLException("La factura requiere una venta confirmada.");
        }
        List<LineaFactura> lineas = new ArrayList<>();
        try (Connection cn = Conexion.getInstancia().getConnection();
                CallableStatement cs = cn.prepareCall("{call sp_verfactura(?)}")) {
            cs.setInt(1, idVenta);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    lineas.add(new LineaFactura(rs.getString("tipo_articulo"),
                            rs.getString("descripcion"), rs.getInt("cantidad"),
                            rs.getBigDecimal("precio_unitario"), rs.getBigDecimal("subtotal")));
                }
            }
        }
        return new FacturaVenta(venta, lineas);
    }

    private void ejecutar(String sql, int... parametros) throws SQLException {
        try (Connection cn = Conexion.getInstancia().getConnection();
                CallableStatement cs = cn.prepareCall(sql)) {
            for (int i = 0; i < parametros.length; i++) { cs.setInt(i + 1, parametros[i]); }
            cs.execute();
        }
    }
}
