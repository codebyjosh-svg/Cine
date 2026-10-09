package org.cine.dao.impl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.cine.dao.CarteleraDAO;
import org.cine.model.CarteleraItem;
import org.cine.util.Conexion;

/**
 * Implementación del DAO para consultar la cartelera.
 *
 * @author Joshua
 */
public class CarteleraDAOImpl implements CarteleraDAO {

    @Override
    public List<CarteleraItem> listarCartelera() throws SQLException {

        List<CarteleraItem> lista = new ArrayList<>();

        String sql = """
                SELECT
                    id_funcion,
                    id_pelicula,
                    titulo,
                    nombre_genero,
                    clasificacion,
                    id_sala,
                    nombre_sala,
                    fecha_inicio,
                    precio_boleto,
                    butacas_disponibles,
                    estado
                FROM vw_cartelera
                ORDER BY fecha_inicio, titulo
                """;

        try (
                Connection con = Conexion.getInstancia().conectar();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {

                Timestamp timestamp =
                        rs.getTimestamp("fecha_inicio");

                LocalDate fecha =
                        timestamp.toLocalDateTime().toLocalDate();

                LocalTime hora =
                        timestamp.toLocalDateTime().toLocalTime();

                BigDecimal precio =
                        rs.getBigDecimal("precio_boleto");

                CarteleraItem item = new CarteleraItem(
                        rs.getInt("id_funcion"),
                        rs.getInt("id_pelicula"),
                        rs.getString("titulo"),
                        rs.getString("nombre_genero"),
                        rs.getString("clasificacion"),
                        rs.getInt("id_sala"),
                        rs.getString("nombre_sala"),
                        fecha,
                        hora,
                        precio,
                        rs.getInt("butacas_disponibles")
                );

                lista.add(item);
            }
        }

        return lista;
    }
}