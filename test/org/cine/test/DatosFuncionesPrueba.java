package org.cine.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.cine.dao.impl.FuncionDAOImpl;
import org.cine.model.Funcion;
import org.cine.util.Conexion;

/** Datos aislados para las pruebas de US-2.2; se retiran al terminar. */
public final class DatosFuncionesPrueba implements AutoCloseable {
    private final Connection conexion;
    private final List<Integer> ventas = new ArrayList<>();
    public int genero, pelicula1, pelicula2, peliculaInactiva;
    public int sala1, sala2, salaInactiva, salaSinButacas;
    public int butaca1, butaca2;

    public DatosFuncionesPrueba() throws SQLException {
        if (!Boolean.getBoolean("cine.pruebas.bd")) {
            throw new IllegalStateException("Usa una base de pruebas y -Dcine.pruebas.bd=true.");
        }
        conexion = Conexion.getInstancia().conectar();
        String sufijo = UUID.randomUUID().toString().substring(0, 8);
        try {
            genero = insertar("INSERT INTO generos(nombre_genero) VALUES (?)", "Prueba funciones " + sufijo);
            pelicula1 = pelicula("Horizonte de prueba", 80, 1);
            pelicula2 = pelicula("Ciudad de prueba", 110, 1);
            peliculaInactiva = pelicula("Película inactiva de prueba", 90, 0);
            sala1 = sala("Prueba A " + sufijo, 1);
            sala2 = sala("Prueba B " + sufijo, 1);
            salaInactiva = sala("Prueba inactiva " + sufijo, 0);
            salaSinButacas = sala("Prueba vacía " + sufijo, 1);
            butaca1 = insertar("INSERT INTO butacas(id_sala,fila,numero) VALUES (?, 'A', 1)", sala1);
            butaca2 = insertar("INSERT INTO butacas(id_sala,fila,numero) VALUES (?, 'A', 1)", sala2);
            insertar("INSERT INTO butacas(id_sala,fila,numero) VALUES (?, 'A', 1)", salaInactiva);
        } catch (SQLException ex) {
            close();
            throw ex;
        }
    }

    private int pelicula(String titulo, int duracion, int estado) throws SQLException {
        return insertar("INSERT INTO peliculas(titulo,duracion_minutos,clasificacion,idioma,id_genero,estado) VALUES (?,?, 'A', 'Español', ?, ?)",
                titulo, duracion, genero, estado);
    }

    private int sala(String nombre, int estado) throws SQLException {
        return insertar("INSERT INTO salas(nombre_sala,formato,estado) VALUES (?, '2D', ?)", nombre, estado);
    }

    private int insertar(String sql, Object... valores) throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < valores.length; i++) sentencia.setObject(i + 1, valores[i]);
            sentencia.executeUpdate();
            try (ResultSet resultado = sentencia.getGeneratedKeys()) {
                if (!resultado.next()) throw new SQLException("Falta el ID del dato de prueba.");
                return resultado.getInt(1);
            }
        }
    }

    public LocalDateTime inicioFuturo() throws SQLException {
        return new FuncionDAOImpl().obtenerHoraServidor().plusDays(45).withHour(12).withMinute(0).withSecond(0).withNano(0);
    }

    public int funcionVencida() throws SQLException {
        LocalDateTime ahora = new FuncionDAOImpl().obtenerHoraServidor();
        return insertar("INSERT INTO funciones(id_pelicula,id_sala,fecha_inicio,fecha_fin,precio_boleto) VALUES (?,?,?,?,35)",
                pelicula1, sala1, Timestamp.valueOf(ahora.minusHours(4)), Timestamp.valueOf(ahora.minusHours(2)));
    }

    public int reservarBoleto(Funcion funcion) throws SQLException {
        int cliente;
        int usuario;
        try (Statement sentencia = conexion.createStatement();
                ResultSet resultado = sentencia.executeQuery("SELECT id_cliente FROM clientes WHERE estado=1 LIMIT 1")) {
            if (!resultado.next()) throw new SQLException("La prueba necesita un cliente del DML.");
            cliente = resultado.getInt(1);
        }
        try (Statement sentencia = conexion.createStatement();
                ResultSet resultado = sentencia.executeQuery("SELECT u.id_usuario FROM usuarios u JOIN roles r ON r.id_rol=u.id_rol WHERE u.estado=1 AND r.nombre_rol='admin' LIMIT 1")) {
            if (!resultado.next()) throw new SQLException("La prueba necesita un administrador del DML.");
            usuario = resultado.getInt(1);
        }
        int venta = insertar("INSERT INTO ventas(id_cliente,id_usuario) VALUES (?,?)", cliente, usuario);
        ventas.add(venta);
        int butaca = funcion.getIdSala() == sala1 ? butaca1 : butaca2;
        return insertar("INSERT INTO boletos(id_venta,id_funcion,id_butaca,id_sala,precio_unitario) VALUES (?,?,?,?,?)",
                venta, funcion.getIdFuncion(), butaca, funcion.getIdSala(), funcion.getPrecioBoleto());
    }

    public void anularBoleto(int id) throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement("UPDATE boletos SET estado='anulado' WHERE id_boleto=?")) {
            sentencia.setInt(1, id);
            sentencia.executeUpdate();
        }
    }

    @Override
    public void close() throws SQLException {
        try (Statement sentencia = conexion.createStatement()) {
            String salas = sala1 + "," + sala2 + "," + salaInactiva + "," + salaSinButacas;
            sentencia.executeUpdate("DELETE b FROM boletos b JOIN funciones f ON f.id_funcion=b.id_funcion WHERE f.id_sala IN (" + salas + ")");
            for (int venta : ventas) sentencia.executeUpdate("DELETE FROM ventas WHERE id_venta=" + venta);
            sentencia.executeUpdate("DELETE FROM funciones WHERE id_sala IN (" + salas + ")");
            sentencia.executeUpdate("DELETE FROM butacas WHERE id_sala IN (" + salas + ")");
            sentencia.executeUpdate("DELETE FROM salas WHERE id_sala IN (" + salas + ")");
            sentencia.executeUpdate("DELETE FROM peliculas WHERE id_pelicula IN (" + pelicula1 + "," + pelicula2 + "," + peliculaInactiva + ")");
            sentencia.executeUpdate("DELETE FROM generos WHERE id_genero=" + genero);
        } finally {
            conexion.close();
        }
    }
}
