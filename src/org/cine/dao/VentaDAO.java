package org.cine.dao;

import java.sql.SQLException;
import java.util.List;
import org.cine.model.ClienteVinculo;
import org.cine.model.FacturaVenta;
import org.cine.model.LineaVentaProducto;
import org.cine.model.Producto;
import org.cine.model.VentaResumen;

/** Operaciones para integrar dulcería en ventas nuevas o abiertas con boletos. */
public interface VentaDAO {
    List<Producto> listarProductosDisponibles() throws SQLException;
    List<ClienteVinculo> listarClientesActivos() throws SQLException;
    int abrirVenta(int idCliente, int idUsuario) throws SQLException;
    VentaResumen buscarVenta(int idVenta) throws SQLException;
    List<LineaVentaProducto> listarProductosVenta(int idVenta) throws SQLException;
    void agregarProducto(int idVenta, int idProducto, int cantidad) throws SQLException;
    void cambiarCantidad(int idVenta, int idProducto, int cantidad) throws SQLException;
    void quitarProducto(int idVenta, int idProducto) throws SQLException;
    void validarStock(int idVenta) throws SQLException;
    void confirmarVenta(int idVenta) throws SQLException;
    FacturaVenta obtenerFactura(int idVenta) throws SQLException;
}
