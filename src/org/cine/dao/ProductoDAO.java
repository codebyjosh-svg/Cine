package org.cine.dao;
import java.sql.SQLException;import java.util.List;import org.cine.model.Producto;
public interface ProductoDAO {List<Producto> listar()throws SQLException;}
