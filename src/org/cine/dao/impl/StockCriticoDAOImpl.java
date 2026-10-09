package org.cine.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.cine.dao.StockCriticoDAO;
import org.cine.model.StockCritico;
import org.cine.util.Conexion;

/** Usa el procedimiento existente y propaga los errores para mostrarlos en la vista. */
public final class StockCriticoDAOImpl implements StockCriticoDAO {
    @Override
    public List<StockCritico> listar() throws SQLException {
        List<StockCritico> productos = new ArrayList<>();
        try (Connection cn = Conexion.getInstancia().getConnection();
                CallableStatement cs = cn.prepareCall("{call sp_listarstockcritico()}");
                ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                productos.add(new StockCritico(rs.getInt("id_producto"),
                        rs.getString("nombre_producto"), rs.getString("nombre_categoria"),
                        rs.getBigDecimal("precio"), rs.getInt("stock"), rs.getInt("stock_minimo")));
            }
        }
        return productos;
    }
}
