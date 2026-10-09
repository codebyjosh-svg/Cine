package org.cine.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

import org.cine.dao.impl.FuncionDAOImpl;
import org.cine.util.Conexion;

/**
 * Datos temporales exclusivos de las pruebas de US-2.2.
 *
 * No crea tablas ni procedimientos.
 * Solo crea datos temporales y los elimina al finalizar.
 */
public final class DatosFuncionesPrueba
        implements AutoCloseable {

    private final Connection conexion;

    public int genero;

    public int pelicula1;
    public int pelicula2;
    public int peliculaInactiva;

    public int sala1;
    public int sala2;
    public int salaInactiva;
    public int salaSinButacas;

    private final String sufijo =
            UUID.randomUUID()
                    .toString()
                    .substring(0, 8);

    public DatosFuncionesPrueba()
            throws SQLException {

        if (
                !Boolean.getBoolean(
                        "cine.pruebas.bd"
                )
        ) {

            throw new IllegalStateException(
                    "Usa una base de pruebas y "
                    + "-Dcine.pruebas.bd=true."
            );
        }

        conexion =
                Conexion.getInstancia()
                        .conectar();

        try {

            genero = insertar(
                    "INSERT INTO generos(nombre_genero) "
                    + "VALUES (?)",

                    "Prueba funciones "
                    + sufijo
            );

            pelicula1 = pelicula(
                    "Horizonte de prueba",
                    80,
                    1
            );

            pelicula2 = pelicula(
                    "Ciudad de prueba",
                    110,
                    1
            );

            peliculaInactiva = pelicula(
                    "Pelicula inactiva de prueba",
                    90,
                    0
            );

            sala1 = sala(
                    "Prueba A " + sufijo,
                    1
            );

            sala2 = sala(
                    "Prueba B " + sufijo,
                    1
            );

            salaInactiva = sala(
                    "Prueba inactiva " + sufijo,
                    0
            );

            salaSinButacas = sala(
                    "Prueba vacia " + sufijo,
                    1
            );

            insertar(
                    "INSERT INTO butacas"
                    + "(id_sala,fila,numero) "
                    + "VALUES (?, 'A', 1)",

                    sala1
            );

            insertar(
                    "INSERT INTO butacas"
                    + "(id_sala,fila,numero) "
                    + "VALUES (?, 'A', 1)",

                    sala2
            );

            insertar(
                    "INSERT INTO butacas"
                    + "(id_sala,fila,numero) "
                    + "VALUES (?, 'A', 1)",

                    salaInactiva
            );

        } catch (
                SQLException
                | RuntimeException ex
        ) {

            try {
                close();
            } catch (SQLException limpieza) {
                ex.addSuppressed(
                        limpieza
                );
            }

            throw ex;
        }
    }

    private int pelicula(
            String titulo,
            int duracion,
            int estado
    ) throws SQLException {

        return insertar(
                "INSERT INTO peliculas "
                + "(titulo,duracion_minutos,"
                + "clasificacion,idioma,id_genero,estado) "
                + "VALUES "
                + "(?,?, 'A', 'Español', ?, ?)",

                titulo,
                duracion,
                genero,
                estado
        );
    }

    private int sala(
            String nombre,
            int estado
    ) throws SQLException {

        return insertar(
                "INSERT INTO salas"
                + "(nombre_sala,formato,estado) "
                + "VALUES (?, '2D', ?)",

                nombre,
                estado
        );
    }

    private int insertar(
            String sql,
            Object... valores
    ) throws SQLException {

        try (
                PreparedStatement sentencia =
                        conexion.prepareStatement(
                                sql,
                                Statement.RETURN_GENERATED_KEYS
                        )
        ) {

            for (
                    int i = 0;
                    i < valores.length;
                    i++
            ) {

                sentencia.setObject(
                        i + 1,
                        valores[i]
                );
            }

            sentencia.executeUpdate();

            try (
                    ResultSet resultado =
                            sentencia.getGeneratedKeys()
            ) {

                if (!resultado.next()) {

                    throw new SQLException(
                            "No se obtuvo el ID generado "
                            + "del dato de prueba."
                    );
                }

                return resultado.getInt(1);
            }
        }
    }

    public LocalDateTime inicioFuturo()
            throws SQLException {

        return new FuncionDAOImpl()
                .obtenerHoraServidor()
                .plusDays(45)
                .withHour(12)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
    }

    /**
     * Crea directamente una función vencida.
     *
     * Se utiliza únicamente para probar
     * la finalización.
     */
    public int funcionVencida()
            throws SQLException {

        LocalDateTime ahora =
                new FuncionDAOImpl()
                        .obtenerHoraServidor();

        return insertar(
                "INSERT INTO funciones "
                + "(id_pelicula,id_sala,"
                + "fecha_inicio,fecha_fin,"
                + "precio_boleto) "
                + "VALUES (?,?,?,?,35)",

                pelicula1,
                sala1,
                Timestamp.valueOf(
                        ahora.minusHours(4)
                ),
                Timestamp.valueOf(
                        ahora.minusHours(2)
                )
        );
    }

    @Override
    public void close()
            throws SQLException {

        SQLException error = null;

        try (
                Statement sentencia =
                        conexion.createStatement()
        ) {

            try {

                sentencia.executeUpdate(
                        "DELETE FROM funciones "
                        + "WHERE id_sala IN ("
                        + sala1 + ","
                        + sala2 + ","
                        + salaInactiva + ","
                        + salaSinButacas
                        + ")"
                );

            } catch (SQLException ex) {

                error = ex;
            }

            try {

                sentencia.executeUpdate(
                        "DELETE FROM butacas "
                        + "WHERE id_sala IN ("
                        + sala1 + ","
                        + sala2 + ","
                        + salaInactiva + ","
                        + salaSinButacas
                        + ")"
                );

            } catch (SQLException ex) {

                if (error == null) {
                    error = ex;
                } else {
                    error.addSuppressed(ex);
                }
            }

            try {

                sentencia.executeUpdate(
                        "DELETE FROM salas "
                        + "WHERE id_sala IN ("
                        + sala1 + ","
                        + sala2 + ","
                        + salaInactiva + ","
                        + salaSinButacas
                        + ")"
                );

            } catch (SQLException ex) {

                if (error == null) {
                    error = ex;
                } else {
                    error.addSuppressed(ex);
                }
            }

            try {

                sentencia.executeUpdate(
                        "DELETE FROM peliculas "
                        + "WHERE id_pelicula IN ("
                        + pelicula1 + ","
                        + pelicula2 + ","
                        + peliculaInactiva
                        + ")"
                );

            } catch (SQLException ex) {

                if (error == null) {
                    error = ex;
                } else {
                    error.addSuppressed(ex);
                }
            }

            try {

                sentencia.executeUpdate(
                        "DELETE FROM generos "
                        + "WHERE id_genero="
                        + genero
                );

            } catch (SQLException ex) {

                if (error == null) {
                    error = ex;
                } else {
                    error.addSuppressed(ex);
                }
            }

        } finally {

            try {
                conexion.close();
            } catch (SQLException ex) {

                if (error == null) {
                    error = ex;
                } else {
                    error.addSuppressed(ex);
                }
            }
        }

        if (error != null) {
            throw error;
        }
    }
}