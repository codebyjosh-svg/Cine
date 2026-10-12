package org.cine.dao;

import java.sql.SQLException;
import java.util.List;
import org.cine.model.StockCritico;

/** Consulta de productos activos con stock menor o igual al mínimo. */
public interface StockCriticoDAO {
    List<StockCritico> listar() throws SQLException;
}
