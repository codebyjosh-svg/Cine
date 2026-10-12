package org.cine.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.cine.model.MovimientoInventario;
import org.cine.model.Producto;

/** Contrato de entradas, salidas y consulta del historial de inventario. */
public interface MovimientoInventarioDAO {
    List<MovimientoInventario> listarHistorial() throws SQLException;
    List<Producto> listarProductosActivos() throws SQLException;
    Optional<Producto> buscarProducto(int idProducto) throws SQLException;
    void registrar(MovimientoInventario movimiento) throws SQLException;

    default void registrarEntrada(int idProducto, int idUsuario, int cantidad,
            String observacion) throws SQLException {
        registrar(new MovimientoInventario(idProducto, idUsuario, "entrada", cantidad, observacion));
    }

    default void registrarSalida(int idProducto, int idUsuario, int cantidad,
            String observacion) throws SQLException {
        registrar(new MovimientoInventario(idProducto, idUsuario, "salida", cantidad, observacion));
    }
}
