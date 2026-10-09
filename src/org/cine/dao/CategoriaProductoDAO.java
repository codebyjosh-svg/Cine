package org.cine.dao;
import java.sql.SQLException;import java.util.List;import org.cine.model.CategoriaProducto;
public interface CategoriaProductoDAO {List<CategoriaProducto> listar()throws SQLException;void guardar(Integer id,String nombre,String descripcion)throws SQLException;void cambiarEstado(int id,boolean activo)throws SQLException;void eliminar(int id)throws SQLException;}
