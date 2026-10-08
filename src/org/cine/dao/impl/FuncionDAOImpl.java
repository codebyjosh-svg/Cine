package org.cine.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.cine.dao.FuncionDAO;
import org.cine.model.Funcion;
import org.cine.model.Pelicula;
import org.cine.model.Sala;
import org.cine.util.Conexion;

/** Acceso a funciones mediante los procedimientos existentes del cine. */
public class FuncionDAOImpl implements FuncionDAO {
    @Override
    public List<Funcion> listarTodos() throws SQLException {
        List<Funcion> lista = new ArrayList<>();
        try (Connection conexion = Conexion.getInstancia().conectar();
                CallableStatement sentencia = conexion.prepareCall("{call sp_listarfunciones()}");
                ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) lista.add(convertir(resultado));
        }
        return lista;
    }

    @Override
    public Optional<Funcion> buscarPorId(int idFuncion) throws SQLException {
        try (Connection conexion = Conexion.getInstancia().conectar();
                CallableStatement sentencia = conexion.prepareCall("{call sp_buscarfuncion(?)}")) {
            sentencia.setInt(1, idFuncion);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? Optional.of(convertir(resultado)) : Optional.empty();
            }
        }
    }

    @Override
    public int insertar(Funcion funcion) throws SQLException {
        validar(funcion);
        try (Connection conexion = Conexion.getInstancia().conectar();
                CallableStatement sentencia = conexion.prepareCall("{call sp_insertarfuncion(?, ?, ?, ?)}")) {
            parametros(sentencia, funcion, 1);
            try (ResultSet resultado = sentencia.executeQuery()) {
                if (!resultado.next()) throw new SQLException("La base no devolvió el ID de la función creada.");
                int id = resultado.getInt("id_funcion");
                funcion.setIdFuncion(id);
                return id;
            }
        }
    }

    @Override
    public void actualizar(Funcion funcion) throws SQLException {
        validar(funcion);
        if (funcion.getIdFuncion() <= 0) throw new IllegalArgumentException("Selecciona una función para editar.");
        try (Connection conexion = Conexion.getInstancia().conectar();
                CallableStatement sentencia = conexion.prepareCall("{call sp_actualizarfuncion(?, ?, ?, ?, ?)}")) {
            sentencia.setInt(1, funcion.getIdFuncion());
            parametros(sentencia, funcion, 2);
            sentencia.execute();
        }
    }

    @Override
    public void cancelar(int idFuncion) throws SQLException {
        cambiarEstado("{call sp_cancelarfuncion(?)}", idFuncion);
    }

    @Override
    public void finalizar(int idFuncion) throws SQLException {
        cambiarEstado("{call sp_finalizarfuncion(?)}", idFuncion);
    }

    private void cambiarEstado(String sql, int idFuncion) throws SQLException {
        if (idFuncion <= 0) throw new IllegalArgumentException("Selecciona una función válida.");
        try (Connection conexion = Conexion.getInstancia().conectar();
                CallableStatement sentencia = conexion.prepareCall(sql)) {
            sentencia.setInt(1, idFuncion);
            sentencia.execute();
        }
    }

    @Override
    public List<Pelicula> listarPeliculasActivas() throws SQLException {
        List<Pelicula> lista = new ArrayList<>();
        try (Connection conexion = Conexion.getInstancia().conectar();
                CallableStatement sentencia = conexion.prepareCall("{call sp_listarpeliculas()}");
                ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                if (resultado.getInt("estado") != 1) continue;
                Pelicula pelicula = new Pelicula();
                pelicula.setIdPelicula(resultado.getInt("id_pelicula"));
                pelicula.setTitulo(resultado.getString("titulo"));
                pelicula.setDuracionMinutos(resultado.getInt("duracion_minutos"));
                pelicula.setEstado(1);
                lista.add(pelicula);
            }
        }
        return lista;
    }

    @Override
    public List<Sala> listarSalasActivas() throws SQLException {
        List<Sala> lista = new ArrayList<>();
        try (Connection conexion = Conexion.getInstancia().conectar();
                CallableStatement sentencia = conexion.prepareCall("{call sp_listarsalas()}");
                ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                if (resultado.getInt("estado") == 1) {
                    lista.add(new Sala(resultado.getInt("id_sala"), resultado.getString("nombre_sala"),
                            resultado.getString("formato"), 1));
                }
            }
        }
        return lista;
    }

    @Override
    public LocalDateTime obtenerHoraServidor() throws SQLException {
        try (Connection conexion = Conexion.getInstancia().conectar();
                Statement sentencia = conexion.createStatement();
                ResultSet resultado = sentencia.executeQuery("SELECT CURRENT_TIMESTAMP AS ahora")) {
            resultado.next();
            return resultado.getTimestamp("ahora").toLocalDateTime();
        }
    }

    private void parametros(CallableStatement sentencia, Funcion funcion, int inicio) throws SQLException {
        sentencia.setInt(inicio, funcion.getIdPelicula());
        sentencia.setInt(inicio + 1, funcion.getIdSala());
        sentencia.setTimestamp(inicio + 2, Timestamp.valueOf(funcion.getFechaInicio()));
        sentencia.setBigDecimal(inicio + 3, funcion.getPrecioBoleto());
    }

    private void validar(Funcion funcion) {
        if (funcion == null || funcion.getIdPelicula() <= 0 || funcion.getIdSala() <= 0
                || funcion.getFechaInicio() == null || funcion.getPrecioBoleto() == null
                || funcion.getPrecioBoleto().signum() <= 0 || funcion.getPrecioBoleto().scale() > 2
                || funcion.getPrecioBoleto().precision() - funcion.getPrecioBoleto().scale() > 8) {
            throw new IllegalArgumentException("La función requiere película, sala, fecha y un precio positivo con hasta dos decimales.");
        }
    }

    private Funcion convertir(ResultSet resultado) throws SQLException {
        Funcion funcion = new Funcion();
        funcion.setIdFuncion(resultado.getInt("id_funcion"));
        funcion.setIdPelicula(resultado.getInt("id_pelicula"));
        funcion.setTituloPelicula(resultado.getString("titulo"));
        funcion.setDuracionMinutos(resultado.getInt("duracion_minutos"));
        funcion.setIdSala(resultado.getInt("id_sala"));
        funcion.setNombreSala(resultado.getString("nombre_sala"));
        funcion.setFormatoSala(resultado.getString("formato"));
        funcion.setFechaInicio(resultado.getTimestamp("fecha_inicio").toLocalDateTime());
        funcion.setFechaFin(resultado.getTimestamp("fecha_fin").toLocalDateTime());
        funcion.setPrecioBoleto(resultado.getBigDecimal("precio_boleto"));
        funcion.setEstado(resultado.getString("estado"));
        funcion.setCapacidad(resultado.getInt("capacidad"));
        funcion.setBoletosReservados(resultado.getInt("boletos_reservados"));
        funcion.setBoletosVendidos(resultado.getInt("boletos_vendidos"));
        return funcion;
    }
}
