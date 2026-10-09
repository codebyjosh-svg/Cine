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

        ultimoError = null;

        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {

            while (rs.next()) {
                categorias.add(mapearCategoria(rs));
            }

        } catch (SQLException ex) {
            registrarError("listar categorías", ex);
        }

        return categorias;
    }

    @Override
    public CategoriaProducto buscarCategoriaProducto(
            int idCategoriaProducto) {

        ultimoError = null;

        if (!validarId(idCategoriaProducto)) {
            return null;
        }

        String sql = "{call sp_buscategoriaproducto(?)}";

        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, idCategoriaProducto);

            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    return mapearCategoria(rs);
                }
            }

        } catch (SQLException ex) {
            registrarError("buscar categoría", ex);
        }

        return null;
    }

    @Override
    public boolean insertar(CategoriaProducto categoria) {
        ultimoError = null;

        if (!validarCategoria(categoria)) {
            return false;
        }

        String sql = "{call sp_insertarcategoriaproducto(?, ?)}";

        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setString(1, categoria.getNombreCategoria().trim());
            cs.setString(2, categoria.getDescripcion());

            boolean hayResultado = cs.execute();

            // Recorre contadores y resultados hasta encontrar el ID.
            while (hayResultado || cs.getUpdateCount() != -1) {

                if (hayResultado) {
                    try (ResultSet rs = cs.getResultSet()) {

                        if (rs != null && rs.next()) {
                            int idCategoria =
                                    rs.getInt("id_categoria_producto");

                            if (idCategoria <= 0) {
                                ultimoError =
                                        "El procedimiento devolvió "
                                        + "un ID de categoría inválido.";
                                return false;
                            }

                            categoria.setIdCategoriaProducto(
                                    idCategoria
                            );

                            categoria.setEstado(1);

                            return true;
                        }
                    }
                }

                hayResultado = cs.getMoreResults();
            }

            ultimoError =
                    "El procedimiento no devolvió el ID de la categoría.";

        } catch (SQLException ex) {
            registrarError("insertar categoría", ex);
        }

        return false;
    }

    @Override
    public boolean actualizar(CategoriaProducto categoria) {
        ultimoError = null;

        if (!validarCategoria(categoria)) {
            return false;
        }

        if (!validarId(categoria.getIdCategoriaProducto())) {
            return false;
        }

        String sql = "{call sp_actualizarcategoriaproducto(?, ?, ?)}";

        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, categoria.getIdCategoriaProducto());
            cs.setString(2, categoria.getNombreCategoria().trim());
            cs.setString(3, categoria.getDescripcion());

            boolean actualizada = cs.executeUpdate() > 0;

            if (!actualizada) {
                ultimoError =
                        "No se actualizó la categoría. "
                        + "Verifique que exista y que haya cambios.";
            }

            return actualizada;

        } catch (SQLException ex) {
            registrarError("actualizar categoría", ex);
            return false;
        }
    }

    @Override
    public boolean eliminar(int idCategoriaProducto) {
        ultimoError = null;

        if (!validarId(idCategoriaProducto)) {
            return false;
        }

        String sql = "{call sp_eliminarcategoriaproducto(?)}";

        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, idCategoriaProducto);

            boolean eliminada = cs.executeUpdate() > 0;

            if (!eliminada) {
                ultimoError =
                        "No se eliminó la categoría. "
                        + "Verifique que exista.";
            }

            return eliminada;

        } catch (SQLException ex) {
            registrarError("eliminar categoría", ex);
            return false;
        }
    }

    @Override
    public boolean cambiarEstado(
            int idCategoriaProducto,
            int estado) {

        ultimoError = null;

        if (!validarId(idCategoriaProducto)) {
            return false;
        }

        if (estado != 0 && estado != 1) {
            ultimoError =
                    "El estado debe ser 1 (activa) o 0 (inactiva).";
            return false;
        }

        String sql =
                "{call sp_cambiarestadocategoriaproducto(?, ?)}";

        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, idCategoriaProducto);
            cs.setInt(2, estado);

            boolean actualizada = cs.executeUpdate() > 0;

            if (!actualizada) {
                ultimoError =
                        "No se cambió el estado. Verifique que "
                        + "la categoría exista y tenga otro estado.";
            }

            return actualizada;

        } catch (SQLException ex) {
            registrarError("cambiar estado de categoría", ex);
            return false;
        }
    }

    @Override
    public String getUltimoError() {
        return ultimoError;
    }

    private boolean validarId(int idCategoriaProducto) {
        if (idCategoriaProducto <= 0) {
            ultimoError = "Seleccione una categoría válida.";
            return false;
        }

        return true;
    }

    private boolean validarCategoria(CategoriaProducto categoria) {
        if (categoria == null) {
            ultimoError =
                    "Los datos de la categoría son obligatorios.";
            return false;
        }

        String nombre = categoria.getNombreCategoria();

        if (nombre == null
                || nombre.trim().isEmpty()
                || nombre.trim().length() > 100) {

            ultimoError =
                    "El nombre es obligatorio y admite "
                    + "hasta 100 caracteres.";
            return false;
        }

        String descripcion = categoria.getDescripcion();

        if (descripcion != null && descripcion.length() > 255) {
            ultimoError =
                    "La descripción admite hasta 255 caracteres.";
            return false;
        }

        return true;
    }

    private CategoriaProducto mapearCategoria(ResultSet rs)
            throws SQLException {

        CategoriaProducto categoria = new CategoriaProducto();

        categoria.setIdCategoriaProducto(
                rs.getInt("id_categoria_producto")
        );

        categoria.setNombreCategoria(
                rs.getString("nombre_categoria")
        );

        categoria.setDescripcion(
                rs.getString("descripcion")
        );

        categoria.setEstado(
                rs.getInt("estado")
        );

        return categoria;
    }

    private void registrarError(String accion, SQLException ex) {
        switch (ex.getErrorCode()) {

            case 1451:
                ultimoError =
                        "No se puede eliminar la categoría porque "
                        + "tiene productos relacionados. "
                        + "Utilice Desactivar.";
                break;

            case 1062:
                ultimoError =
                        "Ya existe una categoría con ese nombre.";
                break;

            default:
                ultimoError =
                        "Error al " + accion + ": " + ex.getMessage();
                break;
        }

        System.err.println(ultimoError);
    }
}