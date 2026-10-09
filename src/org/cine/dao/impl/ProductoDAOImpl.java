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

        ultimoError = null;

        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {

            while (rs.next()) {
                productos.add(mapearProducto(rs));
            }

        } catch (SQLException ex) {
            registrarError("listar productos", ex);
        }

        return productos;
    }

    @Override
    public Producto buscarProducto(int idProducto) {
        ultimoError = null;

        if (!validarId(idProducto)) {
            return null;
        }

        String sql = "{call sp_buscarproducto(?)}";

        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, idProducto);

            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    return mapearProducto(rs);
                }
            }

        } catch (SQLException ex) {
            registrarError("buscar producto", ex);
        }

        return null;
    }

    @Override
    public boolean insertar(Producto producto) {
        ultimoError = null;

        if (!validarProducto(producto, true)) {
            return false;
        }

        String sql = "{call sp_insertarproducto(?, ?, ?, ?, ?, ?)}";

        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, producto.getIdCategoriaProducto());
            cs.setString(2, producto.getNombreProducto().trim());
            cs.setString(3, producto.getDescripcion());
            cs.setBigDecimal(4, producto.getPrecio());
            cs.setInt(5, producto.getStock());
            cs.setInt(6, producto.getStockMinimo());

            boolean hayResultado = cs.execute();

            // Un CALL puede devolver contadores antes del SELECT del ID.
            while (hayResultado || cs.getUpdateCount() != -1) {

                if (hayResultado) {
                    try (ResultSet rs = cs.getResultSet()) {

                        if (rs != null && rs.next()) {
                            int idProducto = rs.getInt("id_producto");

                            if (idProducto <= 0) {
                                ultimoError =
                                        "El procedimiento devolvió "
                                        + "un ID de producto inválido.";
                                return false;
                            }

                            producto.setIdProducto(idProducto);
                            producto.setEstado(1);

                            return true;
                        }
                    }
                }

                hayResultado = cs.getMoreResults();
            }

            ultimoError =
                    "El procedimiento no devolvió el ID del producto.";

        } catch (SQLException ex) {
            registrarError("insertar producto", ex);
        }

        return false;
    }

    @Override
    public boolean actualizar(Producto producto) {
        ultimoError = null;

        if (!validarProducto(producto, false)) {
            return false;
        }

        if (!validarId(producto.getIdProducto())) {
            return false;
        }

        String sql = "{call sp_actualizarproducto(?, ?, ?, ?, ?, ?)}";

        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, producto.getIdProducto());
            cs.setInt(2, producto.getIdCategoriaProducto());
            cs.setString(3, producto.getNombreProducto().trim());
            cs.setString(4, producto.getDescripcion());
            cs.setBigDecimal(5, producto.getPrecio());
            cs.setInt(6, producto.getStockMinimo());

            boolean actualizado = cs.executeUpdate() > 0;

            if (!actualizado) {
                ultimoError =
                        "No se actualizó el producto. "
                        + "Verifique que exista y que haya cambios.";
            }

            return actualizado;

        } catch (SQLException ex) {
            registrarError("actualizar producto", ex);
            return false;
        }
    }

    @Override
    public boolean eliminar(int idProducto) {
        ultimoError = null;

        if (!validarId(idProducto)) {
            return false;
        }

        String sql = "{call sp_eliminarproducto(?)}";

        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, idProducto);

            boolean eliminado = cs.executeUpdate() > 0;

            if (!eliminado) {
                ultimoError =
                        "No se eliminó el producto. "
                        + "Verifique que exista.";
            }

            return eliminado;

        } catch (SQLException ex) {
            registrarError("eliminar producto", ex);
            return false;
        }
    }

    @Override
    public boolean cambiarEstado(int idProducto, int estado) {
        ultimoError = null;

        if (!validarId(idProducto)) {
            return false;
        }

        if (estado != 0 && estado != 1) {
            ultimoError =
                    "El estado debe ser 1 (activo) o 0 (inactivo).";
            return false;
        }

        String sql = "{call sp_cambiarestadoproducto(?, ?)}";

        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, idProducto);
            cs.setInt(2, estado);

            boolean actualizado = cs.executeUpdate() > 0;

            if (!actualizado) {
                ultimoError =
                        "No se cambió el estado. Verifique que "
                        + "el producto exista y tenga otro estado.";
            }

            return actualizado;

        } catch (SQLException ex) {
            registrarError("cambiar estado del producto", ex);
            return false;
        }
    }

    @Override
    public String getUltimoError() {
        return ultimoError;
    }

    private boolean validarId(int idProducto) {
        if (idProducto <= 0) {
            ultimoError = "Seleccione un producto válido.";
            return false;
        }

        return true;
    }

    private boolean validarProducto(
            Producto producto,
            boolean registroNuevo) {

        if (producto == null) {
            ultimoError = "Los datos del producto son obligatorios.";
            return false;
        }

        if (producto.getIdCategoriaProducto() <= 0) {
            ultimoError = "Seleccione una categoría válida.";
            return false;
        }

        String nombre = producto.getNombreProducto();

        if (nombre == null
                || nombre.trim().isEmpty()
                || nombre.trim().length() > 100) {

            ultimoError =
                    "El nombre es obligatorio y admite "
                    + "hasta 100 caracteres.";
            return false;
        }

        String descripcion = producto.getDescripcion();

        if (descripcion != null && descripcion.length() > 255) {
            ultimoError =
                    "La descripción admite hasta 255 caracteres.";
            return false;
        }

        if (producto.getPrecio() == null
                || producto.getPrecio().signum() < 0
                || producto.getPrecio().compareTo(
                        new java.math.BigDecimal("99999999.99")) > 0
                || producto.getPrecio()
                        .stripTrailingZeros().scale() > 2) {

            ultimoError =
                    "El precio debe estar entre 0 y 99999999.99 "
                    + "y tener como máximo dos decimales.";
            return false;
        }

        if (registroNuevo && producto.getStock() < 0) {
            ultimoError = "El stock inicial no puede ser negativo.";
            return false;
        }

        if (producto.getStockMinimo() < 0) {
            ultimoError = "El stock mínimo no puede ser negativo.";
            return false;
        }

        return true;
    }

    private Producto mapearProducto(ResultSet rs)
            throws SQLException {

        Producto producto = new Producto();

        producto.setIdProducto(
                rs.getInt("id_producto")
        );

        producto.setIdCategoriaProducto(
                rs.getInt("id_categoria_producto")
        );

        producto.setNombreCategoria(
                rs.getString("nombre_categoria")
        );

        producto.setNombreProducto(
                rs.getString("nombre_producto")
        );

        producto.setDescripcion(
                rs.getString("descripcion")
        );

        producto.setPrecio(
                rs.getBigDecimal("precio")
        );

        producto.setStock(
                rs.getInt("stock")
        );

        producto.setStockMinimo(
                rs.getInt("stock_minimo")
        );

        producto.setEstado(
                rs.getInt("estado")
        );

        return producto;
    }

    private void registrarError(String accion, SQLException ex) {
        switch (ex.getErrorCode()) {

            case 1451:
                ultimoError =
                        "No se puede eliminar el producto porque "
                        + "tiene registros relacionados. "
                        + "Utilice Desactivar.";
                break;

            case 1452:
                ultimoError =
                        "La categoría seleccionada no existe. "
                        + "Actualice las categorías e intente nuevamente.";
                break;

            case 1062:
                ultimoError =
                        "Ya existe un registro con esos datos.";
                break;

            default:
                ultimoError =
                        "Error al " + accion + ": " + ex.getMessage();
                break;
        }

        System.err.println(ultimoError);
    }
}