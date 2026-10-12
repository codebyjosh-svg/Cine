package org.cine.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.cine.dao.PeliculaDAO;
import org.cine.model.Pelicula;
import org.cine.util.Conexion;

public class PeliculaDAOImpl implements PeliculaDAO {

    @Override
    public List<Pelicula> listarTodos() {

        List<Pelicula> lista = new ArrayList<>();

        String sql = "{call sp_listarpeliculas()}";

        try (
                Connection con = Conexion.getInstancia().conectar(); CallableStatement cs = con.prepareCall(sql); ResultSet rs = cs.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearPelicula(rs));
            }

        } catch (SQLException e) {
            System.err.println(
                    "Error listar peliculas: " + e.getMessage()
            );
        }

        return lista;
    }

    @Override
    public Pelicula buscarPelicula(int idPelicula) {

        String sql = "{call sp_buscarpelicula(?)}";

        try (
                Connection con = Conexion.getInstancia().conectar(); CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, idPelicula);

            try (ResultSet rs = cs.executeQuery()) {

                if (rs.next()) {
                    return mapearPelicula(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println(
                    "Error buscar pelicula: " + e.getMessage()
            );
        }

        return null;
    }

    @Override
    public boolean insertar(Pelicula pelicula) {

        /*
         * sp_insertarpelicula recibe:
         *
         * 1  titulo
         * 2  sinopsis
         * 3  director
         * 4  duracion_minutos
         * 5  clasificacion
         * 6  idioma
         * 7  fecha_estreno
         * 8  imagen
         * 9  id_genero
         */
        String sql
                = "{call sp_insertarpelicula(?, ?, ?, ?, ?, ?, ?, ?, ?)}";

        try (
                Connection con = Conexion.getInstancia().conectar(); CallableStatement cs = con.prepareCall(sql)) {

            cargarParametros(cs, pelicula, false);

            boolean tieneResultado = cs.execute();

            if (tieneResultado) {

                try (ResultSet rs = cs.getResultSet()) {

                    if (rs != null && rs.next()) {

                        pelicula.setIdPelicula(
                                rs.getInt("id_pelicula")
                        );
                    }
                }
            }

            return true;

        } catch (SQLException e) {

            System.err.println(
                    "Error insertar pelicula: "
                    + e.getMessage()
            );

            return false;
        }
    }

    @Override
    public boolean actualizar(Pelicula pelicula) {

        /*
         * sp_actualizarpelicula recibe:
         *
         * 1  id_pelicula
         * 2  titulo
         * 3  sinopsis
         * 4  director
         * 5  duracion_minutos
         * 6  clasificacion
         * 7  idioma
         * 8  fecha_estreno
         * 9  imagen
         * 10 id_genero
         */
        String sql
                = "{call sp_actualizarpelicula(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";

        try (
                Connection con = Conexion.getInstancia().conectar(); CallableStatement cs = con.prepareCall(sql)) {

            System.out.println("=================================");
            System.out.println("ACTUALIZANDO PELICULA");
            System.out.println("ID película: "
                    + pelicula.getIdPelicula());
            System.out.println("Título: "
                    + pelicula.getTitulo());
            System.out.println("ID género: "
                    + pelicula.getIdGenero());
            System.out.println("Tiene imagen: "
                    + (pelicula.getImagen() != null));
            System.out.println("=================================");

            cargarParametros(cs, pelicula, true);

            cs.executeUpdate();

            return true;

        } catch (SQLException e) {

            System.err.println(
                    "Error actualizar pelicula: "
                    + e.getMessage()
            );

            return false;
        }
    }

    @Override
    public boolean eliminar(int idPelicula) {

        String sql = "{call sp_eliminarpelicula(?)}";

        try (
                Connection con = Conexion.getInstancia().conectar(); CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, idPelicula);

            cs.executeUpdate();

            return true;

        } catch (SQLException e) {

            System.err.println(
                    "Error eliminar pelicula: "
                    + e.getMessage()
            );

            return false;
        }
    }

    @Override
    public boolean cambiarEstado(int idPelicula, int estado) {

        String sql = "{call sp_cambiarestadopelicula(?, ?)}";

        try (
                Connection con = Conexion.getInstancia().conectar(); CallableStatement cs = con.prepareCall(sql)) {

            System.out.println("=================================");
            System.out.println("CAMBIANDO ESTADO DE PELICULA");
            System.out.println("ID película: " + idPelicula);
            System.out.println("Nuevo estado: " + estado);
            System.out.println("=================================");

            cs.setInt(1, idPelicula);
            cs.setInt(2, estado);

            int filas = cs.executeUpdate();

            System.out.println(
                    "Filas actualizadas: " + filas
            );

            return filas > 0;

        } catch (SQLException e) {

            System.err.println("=================================");
            System.err.println("ERROR CAMBIANDO ESTADO");
            System.err.println("Mensaje: " + e.getMessage());
            System.err.println("Código MySQL: " + e.getErrorCode());
            System.err.println("SQLState: " + e.getSQLState());
            System.err.println("=================================");

            e.printStackTrace();

            return false;
        }
    }

    /**
     * Carga los parámetros de los procedimientos de película.
     *
     * INSERT: titulo, sinopsis, director, duracion, clasificacion, idioma,
     * fecha, imagen, genero
     *
     * UPDATE: id, titulo, sinopsis, director, duracion, clasificacion, idioma,
     * fecha, imagen, genero
     */
    private void cargarParametros(
            CallableStatement cs,
            Pelicula pelicula,
            boolean incluirId) throws SQLException {

        int posicion = 1;

        /*
         * UPDATE comienza con id_pelicula.
         */
        if (incluirId) {

            cs.setInt(
                    posicion++,
                    pelicula.getIdPelicula()
            );
        }

        /*
         * 1/2 - título
         */
        cs.setString(
                posicion++,
                pelicula.getTitulo()
        );

        /*
         * 2/3 - sinopsis
         */
        cs.setString(
                posicion++,
                pelicula.getSinopsis()
        );

        /*
         * 3/4 - director
         */
        cs.setString(
                posicion++,
                pelicula.getDirector()
        );

        /*
         * 4/5 - duración
         */
        cs.setInt(
                posicion++,
                pelicula.getDuracionMinutos()
        );

        /*
         * 5/6 - clasificación
         */
        cs.setString(
                posicion++,
                pelicula.getClasificacion()
        );

        /*
         * 6/7 - idioma
         */
        cs.setString(
                posicion++,
                pelicula.getIdioma()
        );

        /*
         * 7/8 - fecha
         */
        cs.setDate(
                posicion++,
                pelicula.getFechaEstreno()
        );

        /*
         * 8/9 - IMAGEN
         *
         * IMPORTANTE:
         * La imagen va ANTES del id_genero.
         */
        if (pelicula.getImagen() != null) {

            cs.setBytes(
                    posicion++,
                    pelicula.getImagen()
            );

        } else {

            cs.setNull(
                    posicion++,
                    Types.LONGVARBINARY
            );
        }

        /*
         * 9/10 - ID GENERO
         */
        cs.setInt(
                posicion,
                pelicula.getIdGenero()
        );
    }

    /**
     * Convierte un ResultSet en objeto Pelicula.
     */
    private Pelicula mapearPelicula(
            ResultSet rs) throws SQLException {

        Pelicula pelicula = new Pelicula();

        pelicula.setIdPelicula(
                rs.getInt("id_pelicula")
        );

        pelicula.setTitulo(
                rs.getString("titulo")
        );

        pelicula.setSinopsis(
                rs.getString("sinopsis")
        );

        pelicula.setDirector(
                rs.getString("director")
        );

        pelicula.setDuracionMinutos(
                rs.getInt("duracion_minutos")
        );

        pelicula.setClasificacion(
                rs.getString("clasificacion")
        );

        pelicula.setIdioma(
                rs.getString("idioma")
        );

        pelicula.setFechaEstreno(
                rs.getDate("fecha_estreno")
        );

        /*
         * Imagen LONGBLOB.
         */
        pelicula.setImagen(
                rs.getBytes("imagen")
        );

        pelicula.setIdGenero(
                rs.getInt("id_genero")
        );

        pelicula.setEstado(
                rs.getInt("estado")
        );

        return pelicula;
    }
}
