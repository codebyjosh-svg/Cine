package org.cine.dao;

import java.sql.SQLException;
import java.util.List;
import org.cine.model.CarteleraItem;

/**
 * DAO para consultar la cartelera del cine.
 *
 * @author Joshua
 */
public interface CarteleraDAO {

    /**
     * Obtiene las funciones disponibles para mostrar
     * en la cartelera.
     *
     * @return lista de elementos de cartelera
     * @throws SQLException si ocurre un error con la base de datos
     */
    List<CarteleraItem> listarCartelera() throws SQLException;
}