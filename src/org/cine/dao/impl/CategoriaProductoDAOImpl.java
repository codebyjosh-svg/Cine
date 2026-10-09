package org.cine.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.cine.dao.CategoriaProductoDAO;
import org.cine.model.CategoriaProducto;
import org.cine.util.Conexion;

public class CategoriaProductoDAOImpl implements CategoriaProductoDAO {

    private String ultimoError;

    @Override
    public List<CategoriaProducto> listarTodos() {
        List<CategoriaProducto> categorias = new ArrayList<>();
        String sql = "{call sp_listarcategoriasproducto()}";

        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {

            while (rs.next()) {
                categorias.add(mapearCategoria(rs));
            }
            ultimoError = null;
        } catch (SQLException e) {
            registrarError("listar categorías", e);
        }

        return categorias;
    }

    @Override
    public CategoriaProducto buscarCategoriaProducto(int idCategoriaProducto) {
        String sql = "{call sp_buscategoriaproducto(?)}";

        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, idCategoriaProducto);

            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    ultimoError = null;
                    return mapearCategoria(rs);
                }
            }
            ultimoError = null;
        } catch (SQLException e) {
            registrarError("buscar categoría", e);
        }

        return null;
    }

    @Override
    public boolean insertar(CategoriaProducto categoria) {
        String sql = "{call sp_insertarcategoriaproducto(?, ?)}";

        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setString(1, categoria.getNombreCategoria());
            cs.setString(2, categoria.getDescripcion());

            boolean hayResultado = cs.execute();
            while (hayResultado) {
                try (ResultSet rs = cs.getResultSet()) {
                    if (rs != null && rs.next()) {
                        categoria.setIdCategoriaProducto(
                                rs.getInt("id_categoria_producto"));
                        ultimoError = null;
                        return true;
                    }
                }
                hayResultado = cs.getMoreResults();
            }
            ultimoError = "El procedimiento no devolvió el ID de la categoría.";
        } catch (SQLException e) {
            registrarError("insertar categoría", e);
        }

        return false;
    }

    @Override
    public boolean actualizar(CategoriaProducto categoria) {
        String sql = "{call sp_actualizarcategoriaproducto(?, ?, ?)}";

        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, categoria.getIdCategoriaProducto());
            cs.setString(2, categoria.getNombreCategoria());
             cs.setString(3, categoria.getDescripcion());
            boolean resultado = cs.executeUpdate() > 0;
            ultimoError = resultado ? null : "No se actualizó la categoría.";
            return resultado;
        } catch (SQLException e) {
            registrarError("actualizar categoría", e);
            return false;
        }
    }

    @Override
    public boolean eliminar(int idCategoriaProducto) {
        String sql = "{call sp_eliminarcategoriaproducto(?)}";

        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, idCategoriaProducto);
            boolean resultado = cs.executeUpdate() > 0;
            ultimoError = resultado ? null : "No se eliminó la categoría.";
            return resultado;
        } catch (SQLException e) {
            registrarError("eliminar categoría", e);
            return false;
        }
    }

    @Override
    public boolean cambiarEstado(int idCategoriaProducto, int estado) {
        String sql = "{call sp_cambiarestadocategoriaproducto(?, ?)}";

        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, idCategoriaProducto);
            cs.setInt(2, estado);
            boolean resultado = cs.executeUpdate() > 0;
            ultimoError = resultado ? null : "No se cambió el estado de la categoría.";
            return resultado;
        } catch (SQLException e) {
            registrarError("cambiar estado de categoría", e);
            return false;
        }
    }

    @Override
    public String getUltimoError() {
        return ultimoError;
    }

    private CategoriaProducto mapearCategoria(ResultSet rs) throws SQLException {
        CategoriaProducto categoria = new CategoriaProducto();
        categoria.setIdCategoriaProducto(rs.getInt("id_categoria_producto"));
        categoria.setNombreCategoria(rs.getString("nombre_categoria"));
        categoria.setDescripcion(rs.getString("descripcion"));
        categoria.setEstado(rs.getInt("estado"));
        return categoria;
    }

    private void registrarError(String accion, SQLException e) {
        ultimoError = "Error al " + accion + ": " + e.getMessage();
        System.err.println(ultimoError);
    }
}
