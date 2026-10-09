package org.cine.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.cine.dao.FuncionDAO;
import org.cine.model.Funcion;
import org.cine.model.Pelicula;
import org.cine.model.Sala;
import org.cine.util.Conexion;

/**
 * Acceso a funciones mediante los procedimientos almacenados del cine.
 */
public class FuncionDAOImpl implements FuncionDAO {

    @Override
    public List<Funcion> listarTodos() throws SQLException {

        List<Funcion> lista = new ArrayList<>();

        try (
                Connection conexion = Conexion.getInstancia().conectar();

                CallableStatement sentencia =
                        conexion.prepareCall("{call sp_listarfunciones()}");

                ResultSet resultado = sentencia.executeQuery()
        ) {

            while (resultado.next()) {
                lista.add(convertir(resultado));
            }
        }

        return lista;
    }

    @Override
    public Optional<Funcion> buscarPorId(int idFuncion)
            throws SQLException {

        if (idFuncion <= 0) {
            return Optional.empty();
        }

        try (
                Connection conexion = Conexion.getInstancia().conectar();

                CallableStatement sentencia =
                        conexion.prepareCall("{call sp_buscarfuncion(?)}")
        ) {

            sentencia.setInt(1, idFuncion);

            try (ResultSet resultado = sentencia.executeQuery()) {

                if (resultado.next()) {
                    return Optional.of(convertir(resultado));
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public int insertar(Funcion funcion) throws SQLException {

        validar(funcion);

        try (
                Connection conexion = Conexion.getInstancia().conectar();

                CallableStatement sentencia =
                        conexion.prepareCall(
                                "{call sp_insertarfuncion(?, ?, ?, ?)}"
                        )
        ) {

            parametros(sentencia, funcion, 1);

            try (ResultSet resultado = sentencia.executeQuery()) {

                if (!resultado.next()) {
                    throw new SQLException(
                            "La base de datos no devolvió "
                            + "el ID de la función creada."
                    );
                }

                int id = obtenerEnteroPorNombreSeguro(
                        resultado,
                        "id_funcion"
                );

                funcion.setIdFuncion(id);

                return id;
            }
        }
    }

    @Override
    public void actualizar(Funcion funcion) throws SQLException {

        validar(funcion);

        if (funcion.getIdFuncion() <= 0) {
            throw new IllegalArgumentException(
                    "Selecciona una función para editar."
            );
        }

        try (
                Connection conexion = Conexion.getInstancia().conectar();

                CallableStatement sentencia =
                        conexion.prepareCall(
                                "{call sp_actualizarfuncion(?, ?, ?, ?, ?)}"
                        )
        ) {

            sentencia.setInt(
                    1,
                    funcion.getIdFuncion()
            );

            parametros(
                    sentencia,
                    funcion,
                    2
            );

            sentencia.execute();
        }
    }

    @Override
    public void cancelar(int idFuncion) throws SQLException {

        cambiarEstado(
                "{call sp_cancelarfuncion(?)}",
                idFuncion
        );
    }

    @Override
    public void finalizar(int idFuncion) throws SQLException {

        cambiarEstado(
                "{call sp_finalizarfuncion(?)}",
                idFuncion
        );
    }

    private void cambiarEstado(
            String sql,
            int idFuncion
    ) throws SQLException {

        if (idFuncion <= 0) {
            throw new IllegalArgumentException(
                    "Selecciona una función válida."
            );
        }

        try (
                Connection conexion = Conexion.getInstancia().conectar();

                CallableStatement sentencia =
                        conexion.prepareCall(sql)
        ) {

            sentencia.setInt(1, idFuncion);

            sentencia.execute();
        }
    }

    @Override
    public List<Pelicula> listarPeliculasActivas()
            throws SQLException {

        List<Pelicula> lista = new ArrayList<>();

        try (
                Connection conexion = Conexion.getInstancia().conectar();

                CallableStatement sentencia =
                        conexion.prepareCall(
                                "{call sp_listarpeliculas()}"
                        );

                ResultSet resultado = sentencia.executeQuery()
        ) {

            while (resultado.next()) {

                if (resultado.getInt("estado") != 1) {
                    continue;
                }

                Pelicula pelicula = new Pelicula();

                pelicula.setIdPelicula(
                        resultado.getInt("id_pelicula")
                );

                pelicula.setTitulo(
                        resultado.getString("titulo")
                );

                pelicula.setDuracionMinutos(
                        resultado.getInt("duracion_minutos")
                );

                pelicula.setEstado(1);

                lista.add(pelicula);
            }
        }

        return lista;
    }

    @Override
    public List<Sala> listarSalasActivas()
            throws SQLException {

        List<Sala> lista = new ArrayList<>();

        try (
                Connection conexion = Conexion.getInstancia().conectar();

                CallableStatement sentencia =
                        conexion.prepareCall(
                                "{call sp_listarsalas()}"
                        );

                ResultSet resultado = sentencia.executeQuery()
        ) {

            while (resultado.next()) {

                if (resultado.getInt("estado") == 1) {

                    lista.add(
                            new Sala(
                                    resultado.getInt("id_sala"),
                                    resultado.getString("nombre_sala"),
                                    resultado.getString("formato"),
                                    1
                            )
                    );
                }
            }
        }

        return lista;
    }

    @Override
    public LocalDateTime obtenerHoraServidor()
            throws SQLException {

        try (
                Connection conexion = Conexion.getInstancia().conectar();

                Statement sentencia = conexion.createStatement();

                ResultSet resultado =
                        sentencia.executeQuery(
                                "SELECT CURRENT_TIMESTAMP AS ahora"
                        )
        ) {

            if (!resultado.next()) {
                throw new SQLException(
                        "MySQL no devolvió la hora del servidor."
                );
            }

            return resultado
                    .getTimestamp("ahora")
                    .toLocalDateTime();
        }
    }

    private void parametros(
            CallableStatement sentencia,
            Funcion funcion,
            int inicio
    ) throws SQLException {

        sentencia.setInt(
                inicio,
                funcion.getIdPelicula()
        );

        sentencia.setInt(
                inicio + 1,
                funcion.getIdSala()
        );

        sentencia.setTimestamp(
                inicio + 2,
                Timestamp.valueOf(
                        funcion.getFechaInicio()
                )
        );

        sentencia.setBigDecimal(
                inicio + 3,
                funcion.getPrecioBoleto()
        );
    }

    private void validar(Funcion funcion) {

        if (funcion == null) {
            throw new IllegalArgumentException(
                    "La función no puede ser nula."
            );
        }

        if (funcion.getIdPelicula() <= 0) {
            throw new IllegalArgumentException(
                    "Selecciona una película válida."
            );
        }

        if (funcion.getIdSala() <= 0) {
            throw new IllegalArgumentException(
                    "Selecciona una sala válida."
            );
        }

        if (funcion.getFechaInicio() == null) {
            throw new IllegalArgumentException(
                    "La fecha y hora de inicio son obligatorias."
            );
        }

        if (
                funcion.getPrecioBoleto() == null
                || funcion.getPrecioBoleto().signum() <= 0
        ) {
            throw new IllegalArgumentException(
                    "El precio del boleto debe ser mayor que cero."
            );
        }

        if (funcion.getPrecioBoleto().scale() > 2) {
            throw new IllegalArgumentException(
                    "El precio del boleto puede tener "
                    + "como máximo dos decimales."
            );
        }

        if (
                funcion.getPrecioBoleto().precision()
                - funcion.getPrecioBoleto().scale()
                > 8
        ) {
            throw new IllegalArgumentException(
                    "El precio del boleto excede "
                    + "el tamaño permitido."
            );
        }
    }

    /**
     * Convierte un ResultSet en un objeto Funcion.
     *
     * La lectura se realiza mediante índices de columna obtenidos
     * desde ResultSetMetaData. Esto evita errores cuando MySQL/JDBC
     * devuelve un alias diferente al nombre físico de la columna.
     */
    private Funcion convertir(ResultSet resultado)
            throws SQLException {

        Funcion funcion = new Funcion();

        funcion.setIdFuncion(
                entero(
                        resultado,
                        "id_funcion"
                )
        );

        funcion.setIdPelicula(
                entero(
                        resultado,
                        "id_pelicula"
                )
        );

        funcion.setTituloPelicula(
                texto(
                        resultado,
                        "titulo",
                        "titulo_pelicula",
                        "nombre_pelicula",
                        "pelicula"
                )
        );

        funcion.setIdSala(
                entero(
                        resultado,
                        "id_sala"
                )
        );

        funcion.setNombreSala(
                texto(
                        resultado,
                        "nombre_sala",
                        "nombreSala",
                        "sala"
                )
        );

        funcion.setFormatoSala(
                texto(
                        resultado,
                        "formato",
                        "formato_sala"
                )
        );

        Timestamp inicio =
                timestamp(
                        resultado,
                        "fecha_inicio"
                );

        Timestamp fin =
                timestamp(
                        resultado,
                        "fecha_fin"
                );

        if (inicio != null) {
            funcion.setFechaInicio(
                    inicio.toLocalDateTime()
            );
        }

        if (fin != null) {
            funcion.setFechaFin(
                    fin.toLocalDateTime()
            );
        }

        funcion.setPrecioBoleto(
                decimal(
                        resultado,
                        "precio_boleto"
                )
        );

        funcion.setEstado(
                texto(
                        resultado,
                        "estado"
                )
        );

        int duracion =
                enteroOpcional(
                        resultado,
                        "duracion_minutos"
                );

        /*
         * Si el procedimiento no devuelve la duración,
         * se calcula a partir de fecha_inicio y fecha_fin.
         *
         * El sistema utiliza 20 minutos de limpieza.
         */
        if (
                duracion == 0
                && funcion.getFechaInicio() != null
                && funcion.getFechaFin() != null
        ) {

            long minutos =
                    Duration.between(
                            funcion.getFechaInicio(),
                            funcion.getFechaFin()
                    ).toMinutes();

            if (minutos >= 20) {
                duracion = (int) minutos - 20;
            }
        }

        funcion.setDuracionMinutos(duracion);

        funcion.setCapacidad(
                enteroOpcional(
                        resultado,
                        "capacidad"
                )
        );

        funcion.setBoletosReservados(
                enteroOpcional(
                        resultado,
                        "boletos_reservados"
                )
        );

        funcion.setBoletosVendidos(
                enteroOpcional(
                        resultado,
                        "boletos_vendidos"
                )
        );

        return funcion;
    }

    /**
     * Obtiene una columna de texto.
     *
     * IMPORTANTE:
     * Se obtiene primero el índice real de la columna y después
     * se utiliza getString(indice), evitando problemas con aliases
     * devueltos por MySQL.
     */
    private String texto(
            ResultSet resultado,
            String... nombres
    ) throws SQLException {

        int indice = buscarIndiceColumna(
                resultado,
                nombres
        );

        if (indice == -1) {
            return null;
        }

        return resultado.getString(indice);
    }

    /**
     * Obtiene una columna entera obligatoria.
     */
    private int entero(
            ResultSet resultado,
            String nombre
    ) throws SQLException {

        int indice = buscarIndiceColumna(
                resultado,
                nombre
        );

        if (indice == -1) {
            throw new SQLException(
                    "La consulta no devuelve "
                    + "la columna requerida: "
                    + nombre
            );
        }

        return resultado.getInt(indice);
    }

    /**
     * Obtiene una columna entera opcional.
     */
    private int enteroOpcional(
            ResultSet resultado,
            String nombre
    ) throws SQLException {

        int indice = buscarIndiceColumna(
                resultado,
                nombre
        );

        if (indice == -1) {
            return 0;
        }

        return resultado.getInt(indice);
    }

    /**
     * Obtiene una fecha/hora opcional.
     */
    private Timestamp timestamp(
            ResultSet resultado,
            String nombre
    ) throws SQLException {

        int indice = buscarIndiceColumna(
                resultado,
                nombre
        );

        if (indice == -1) {
            return null;
        }

        return resultado.getTimestamp(indice);
    }

    /**
     * Obtiene un BigDecimal obligatorio.
     */
    private java.math.BigDecimal decimal(
            ResultSet resultado,
            String nombre
    ) throws SQLException {

        int indice = buscarIndiceColumna(
                resultado,
                nombre
        );

        if (indice == -1) {
            throw new SQLException(
                    "La consulta no devuelve "
                    + "la columna requerida: "
                    + nombre
            );
        }

        return resultado.getBigDecimal(indice);
    }

    /**
     * Busca el índice real de una columna utilizando:
     *
     * 1. Column Label
     * 2. Column Name
     *
     * La comparación no distingue mayúsculas/minúsculas.
     */
    private int buscarIndiceColumna(
            ResultSet resultado,
            String... nombres
    ) throws SQLException {

        ResultSetMetaData meta =
                resultado.getMetaData();

        for (String nombreBuscado : nombres) {

            for (
                    int i = 1;
                    i <= meta.getColumnCount();
                    i++
            ) {

                String label =
                        meta.getColumnLabel(i);

                String nombre =
                        meta.getColumnName(i);

                if (
                        nombreBuscado.equalsIgnoreCase(label)
                        || nombreBuscado.equalsIgnoreCase(nombre)
                ) {
                    return i;
                }
            }
        }

        return -1;
    }

    /**
     * Obtiene un entero usando la búsqueda segura de columnas.
     */
    private int obtenerEnteroPorNombreSeguro(
            ResultSet resultado,
            String nombre
    ) throws SQLException {

        int indice =
                buscarIndiceColumna(
                        resultado,
                        nombre
                );

        if (indice == -1) {
            throw new SQLException(
                    "La base de datos no devolvió "
                    + "la columna: "
                    + nombre
            );
        }

        return resultado.getInt(indice);
    }
}