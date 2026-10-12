package org.cine.dao;

import java.util.List;
import org.cine.model.CategoriaProducto;

public interface CategoriaProductoDAO {

    List<CategoriaProducto> listarTodos();

    CategoriaProducto buscarCategoriaProducto(int idCategoriaProducto);

    boolean insertar(CategoriaProducto categoria);

    boolean actualizar(CategoriaProducto categoria);

    boolean eliminar(int idCategoriaProducto);

    boolean cambiarEstado(int idCategoriaProducto, int estado);

    String getUltimoError();
}