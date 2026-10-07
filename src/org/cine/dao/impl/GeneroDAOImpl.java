package org.cine.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.cine.dao.GeneroDAO;
import org.cine.model.Genero;
import org.cine.util.Conexion;

public class GeneroDAOImpl implements GeneroDAO {

    @Override
    public List<Genero> listarTodos() {

        List<Genero> lista = new ArrayList<>();

        String sql = "{call sp_listargeneros()}";

        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {

            while (rs.next()) {

                Genero genero = new Genero();

                genero.setIdGenero(
                        rs.getInt("id_genero")
                );

                genero.setNombreGenero(
                        rs.getString("nombre_genero")
                );

                genero.setDescripcion(
                        rs.getString("descripcion")
                );

                genero.setEstado(
                        rs.getInt("estado")
                );

                lista.add(genero);
            }

        } catch (SQLException e) {

            System.err.println(
                    "Error al listar géneros: "
                    + e.getMessage()
            );
        }

        return lista;
    }

    @Override
    public Genero buscarGenero(int idGenero) {

        Genero genero = null;

        String sql = "{call sp_buscargenero(?)}";

        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, idGenero);

            try (ResultSet rs = cs.executeQuery()) {

                if (rs.next()) {

                    genero = new Genero();

                    genero.setIdGenero(
                            rs.getInt("id_genero")
                    );

                    genero.setNombreGenero(
                            rs.getString("nombre_genero")
                    );

                    genero.setDescripcion(
                            rs.getString("descripcion")
                    );

                    genero.setEstado(
                            rs.getInt("estado")
                    );
                }
            }

        } catch (SQLException e) {

            System.err.println(
                    "Error al buscar género: "
                    + e.getMessage()
            );
        }

        return genero;
    }

    @Override
    public boolean insertar(Genero genero) {

        String sql =
                "{call sp_insertargenero(?, ?)}";

        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setString(
                    1,
                    genero.getNombreGenero()
            );

            cs.setString(
                    2,
                    genero.getDescripcion()
            );

            cs.execute();

            return true;

        } catch (SQLException e) {

            System.err.println(
                    "Error al insertar género: "
                    + e.getMessage()
            );

            return false;
        }
    }

    @Override
    public boolean actualizar(Genero genero) {

        String sql =
                "{call sp_actualizargenero(?, ?, ?)}";

        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(
                    1,
                    genero.getIdGenero()
            );

            cs.setString(
                    2,
                    genero.getNombreGenero()
            );

            cs.setString(
                    3,
                    genero.getDescripcion()
            );

            return cs.executeUpdate() > 0;

        } catch (SQLException e) {

            System.err.println(
                    "Error al actualizar género: "
                    + e.getMessage()
            );

            return false;
        }
    }

    @Override
    public boolean eliminar(int idGenero) {

        String sql =
                "{call sp_eliminargenero(?)}";

        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(
                    1,
                    idGenero
            );

            return cs.executeUpdate() > 0;

        } catch (SQLException e) {

            System.err.println(
                    "Error al eliminar género: "
                    + e.getMessage()
            );

            return false;
        }
    }

    @Override
    public boolean cambiarEstado(
            int idGenero,
            int estado) {

        String sql =
                "{call sp_cambiarestadogenero(?, ?)}";

        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(
                    1,
                    idGenero
            );

            cs.setInt(
                    2,
                    estado
            );

            return cs.executeUpdate() > 0;

        } catch (SQLException e) {

            System.err.println(
                    "Error al cambiar estado del género: "
                    + e.getMessage()
            );

            return false;
        }
    }
}