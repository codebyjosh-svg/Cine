package org.cine.dao;

import java.util.List;
import org.cine.model.Producto;

public interface ProductoDAO {

    List<Producto> listarTodos();

    Producto buscarProducto(int idProducto);

    boolean insertar(Producto producto);

    boolean actualizar(Producto producto);

    boolean eliminar(int idProducto);

    boolean cambiarEstado(int idProducto, int estado);

    String getUltimoError();
}
