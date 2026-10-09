package org.cine.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.cine.dao.ProductoDAO;
import org.cine.model.Producto;
import org.cine.util.Conexion;

public class ProductoDAOImpl implements ProductoDAO {

    private String ultimoError;

    @Override
    public List<Producto> listarTodos() {
        List<Producto> productos = new ArrayList<>();
        String sql = "{call sp_listarproductos()}";

        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {

            while (rs.next()) {
                productos.add(mapearProducto(rs));
            }
            ultimoError = null;
        } catch (SQLException e) {
            registrarError("listar productos", e);
        }

        return productos;
    }

    @Override
    public Producto buscarProducto(int idProducto) {
        String sql = "{call sp_buscarproducto(?)}";

        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, idProducto);

            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    ultimoError = null;
                    return mapearProducto(rs);
                }
            }
            ultimoError = null;
        } catch (SQLException e) {
            registrarError("buscar producto", e);
        }

        return null;
    }

    @Override
    public boolean insertar(Producto producto) {
        String sql = "{call sp_insertarproducto(?, ?, ?, ?, ?, ?)}";

        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, producto.getIdCategoriaProducto());
            cs.setString(2, producto.getNombreProducto());
            cs.setString(3, producto.getDescripcion());
            cs.setBigDecimal(4, producto.getPrecio());
            cs.setInt(5, producto.getStock());
            cs.setInt(6, producto.getStockMinimo());

            boolean hayResultado = cs.execute();
            while (hayResultado) {
                try (ResultSet rs = cs.getResultSet()) {
                    if (rs != null && rs.next()) {
                        producto.setIdProducto(rs.getInt("id_producto"));
                        ultimoError = null;
                        return true;
                    }
                }
                hayResultado = cs.getMoreResults();
            }
            ultimoError = "El procedimiento no devolvió el ID del producto.";
        } catch (SQLException e) {
            registrarError("insertar producto", e);
        }

        return false;
    }

    @Override
    public boolean actualizar(Producto producto) {
        String sql = "{call sp_actualizarproducto(?, ?, ?, ?, ?, ?)}";

        try (Connection con = Conexion.getInstancia().conectar();
                  CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, producto.getIdProducto());
            cs.setInt(2, producto.getIdCategoriaProducto());
            cs.setString(3, producto.getNombreProducto());
            cs.setString(4, producto.getDescripcion());
            cs.setBigDecimal(5, producto.getPrecio());
            cs.setInt(6, producto.getStockMinimo());
            boolean resultado = cs.executeUpdate() > 0;
            ultimoError = resultado ? null : "No se actualizó el producto.";
            return resultado;
        } catch (SQLException e) {
            registrarError("actualizar producto", e);
            return false;
        }
    }

    @Override
    public boolean eliminar(int idProducto) {
        String sql = "{call sp_eliminarproducto(?)}";

        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, idProducto);
            boolean resultado = cs.executeUpdate() > 0;
            ultimoError = resultado ? null : "No se eliminó el producto.";
            return resultado;
        } catch (SQLException e) {
            registrarError("eliminar producto", e);
            return false;
        }
    }

    @Override
    public boolean cambiarEstado(int idProducto, int estado) {
        String sql = "{call sp_cambiarestadoproducto(?, ?)}";

        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, idProducto);
            cs.setInt(2, estado);
            boolean resultado = cs.executeUpdate() > 0;
            ultimoError = resultado ? null : "No se cambió el estado del producto.";
            return resultado;
        } catch (SQLException e) {
            registrarError("cambiar estado del producto", e);
            return false;
        }
    }

    @Override
    public String getUltimoError() {
        return ultimoError;
    }

    private Producto mapearProducto(ResultSet rs) throws SQLException {
        Producto producto = new Producto();
        producto.setIdProducto(rs.getInt("id_producto"));
        producto.setIdCategoriaProducto(rs.getInt("id_categoria_producto"));
        producto.setNombreCategoria(rs.getString("nombre_categoria"));
        producto.setNombreProducto(rs.getString("nombre_producto"));
        producto.setDescripcion(rs.getString("descripcion"));
        producto.setPrecio(rs.getBigDecimal("precio"));
        producto.setStock(rs.getInt("stock"));
        producto.setStockMinimo(rs.getInt("stock_minimo"));
        producto.setEstado(rs.getInt("estado"));
        return producto;
    }

    private void registrarError(String accion, SQLException e) {
        ultimoError = "Error al " + accion + ": " + e.getMessage();
        System.err.println(ultimoError);
    }
}
