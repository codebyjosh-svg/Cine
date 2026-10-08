package org.cine.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
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
                Connection con = Conexion.getInstancia().conectar();
                CallableStatement cs = con.prepareCall(sql);
                ResultSet rs = cs.executeQuery()
        ) {

            while (rs.next()) {

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

                pelicula.setIdGenero(
                        rs.getInt("id_genero")
                );

                pelicula.setEstado(
                        rs.getInt("estado")
                );

                lista.add(pelicula);
            }

        } catch (SQLException e) {

            System.err.println(
                    "Error listar peliculas: "
                    + e.getMessage()
            );
        }

        return lista;
    }

    @Override
    public Pelicula buscarPelicula(int idPelicula) {

        String sql = "{call sp_buscarpelicula(?)}";

        Pelicula pelicula = null;

        try (
                Connection con = Conexion.getInstancia().conectar();
                CallableStatement cs = con.prepareCall(sql)
        ) {

            cs.setInt(1, idPelicula);

            try (ResultSet rs = cs.executeQuery()) {

                if (rs.next()) {

                    pelicula = new Pelicula(
                            rs.getInt("id_pelicula"),
                            rs.getString("titulo"),
                            rs.getString("sinopsis"),
                            rs.getString("director"),
                            rs.getInt("duracion_minutos"),
                            rs.getString("clasificacion"),
                            rs.getString("idioma"),
                            rs.getDate("fecha_estreno"),
                            rs.getInt("id_genero"),
                            rs.getInt("estado")
                    );
                }
            }

        } catch (SQLException e) {

            System.err.println(
                    "Error buscar pelicula: "
                    + e.getMessage()
            );
        }

        return pelicula;
    }

    @Override
    public boolean insertar(Pelicula pelicula) {

        String sql =
                "{call sp_insertarpelicula(?, ?, ?, ?, ?, ?, ?, ?)}";

        try (
                Connection con = Conexion.getInstancia().conectar();
                CallableStatement cs = con.prepareCall(sql)
        ) {

            cs.setString(
                    1,
                    pelicula.getTitulo()
            );

            cs.setString(
                    2,
                    pelicula.getSinopsis()
            );

            cs.setString(
                    3,
                    pelicula.getDirector()
            );

            cs.setInt(
                    4,
                    pelicula.getDuracionMinutos()
            );

            cs.setString(
                    5,
                    pelicula.getClasificacion()
            );

            cs.setString(
                    6,
                    pelicula.getIdioma()
            );

            cs.setDate(
                    7,
                    pelicula.getFechaEstreno()
            );

            cs.setInt(
                    8,
                    pelicula.getIdGenero()
            );

            try (ResultSet rs = cs.executeQuery()) {

                if (rs.next()) {

                    pelicula.setIdPelicula(
                            rs.getInt("id_pelicula")
                    );
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

        String sql =
                "{call sp_actualizarpelicula(?, ?, ?, ?, ?, ?, ?, ?, ?)}";

        try (
                Connection con = Conexion.getInstancia().conectar();
                CallableStatement cs = con.prepareCall(sql)
        ) {

            cs.setInt(
                    1,
                    pelicula.getIdPelicula()
            );

            cs.setString(
                    2,
                    pelicula.getTitulo()
            );

            cs.setString(
                    3,
                    pelicula.getSinopsis()
            );

            cs.setString(
                    4,
                    pelicula.getDirector()
            );

            cs.setInt(
                    5,
                    pelicula.getDuracionMinutos()
            );

            cs.setString(
                    6,
                    pelicula.getClasificacion()
            );

            cs.setString(
                    7,
                    pelicula.getIdioma()
            );

            cs.setDate(
                    8,
                    pelicula.getFechaEstreno()
            );

            cs.setInt(
                    9,
                    pelicula.getIdGenero()
            );

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
                Connection con = Conexion.getInstancia().conectar();
                CallableStatement cs = con.prepareCall(sql)
        ) {

            cs.setInt(
                    1,
                    idPelicula
            );

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
    public boolean cambiarEstado(
            int idPelicula,
            int estado) {

        String sql =
                "{call sp_cambiarestadopelicula(?, ?)}";

        try (
                Connection con = Conexion.getInstancia().conectar();
                CallableStatement cs = con.prepareCall(sql)
        ) {

            cs.setInt(
                    1,
                    idPelicula
            );

            cs.setInt(
                    2,
                    estado
            );

            cs.executeUpdate();

            return true;

        } catch (SQLException e) {

            System.err.println(
                    "Error cambiar estado pelicula: "
                    + e.getMessage()
            );

            return false;
        }
    }
}