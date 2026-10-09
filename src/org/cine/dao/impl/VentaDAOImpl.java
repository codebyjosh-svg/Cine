package org.cine.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

import org.cine.dao.VentaDAO;
import org.cine.model.Boleto;
import org.cine.model.Venta;
import org.cine.util.Conexion;

public class VentaDAOImpl implements VentaDAO {

    private Connection con() throws SQLException {
        return Conexion.getInstance().getConnection();
    }

    @Override
    public List<Opcion> clientes() throws SQLException {

        List<Opcion> l = new ArrayList<>();

        try (
                Connection c = con(); CallableStatement s
                = c.prepareCall("{call sp_listarclientesactivos()}"); ResultSet rs = s.executeQuery()) {

            while (rs.next()) {
                l.add(
                        new Opcion(
                                rs.getInt("id_cliente"),
                                rs.getString("nombre_cliente")
                                + " "
                                + rs.getString("apellido_cliente")
                        )
                );
            }
        }

        return l;
    }

    @Override
    public List<Funcion> funciones() throws SQLException {

        List<Funcion> l = new ArrayList<>();

        String q
                = "SELECT "
                + "f.id_funcion, "
                + "p.titulo, "
                + "s.nombre_sala, "
                + "f.fecha_inicio, "
                + "f.precio_boleto "
                + "FROM funciones f "
                + "JOIN peliculas p "
                + "ON p.id_pelicula = f.id_pelicula "
                + "JOIN salas s "
                + "ON s.id_sala = f.id_sala "
                + "WHERE f.estado = 'programada' "
                + "AND f.fecha_inicio > NOW() "
                + "AND p.estado = 1 "
                + "AND s.estado = 1 "
                + "ORDER BY f.fecha_inicio";

        try (
                Connection c = con(); PreparedStatement s = c.prepareStatement(q); ResultSet rs = s.executeQuery()) {

            while (rs.next()) {

                l.add(
                        new Funcion(
                                rs.getInt("id_funcion"),
                                rs.getString("titulo")
                                + " / "
                                + rs.getString("nombre_sala")
                                + " / "
                                + rs.getTimestamp("fecha_inicio"),
                                rs.getBigDecimal("precio_boleto")
                        )
                );
            }
        }

        return l;
    }

    @Override
    public List<Butaca> butacas(int f) throws SQLException {

        List<Butaca> l = new ArrayList<>();

        try (
                Connection c = con(); CallableStatement s
                = c.prepareCall("{call sp_listarbutacasfuncion(?)}")) {

            s.setInt(1, f);

            try (ResultSet rs = s.executeQuery()) {

                while (rs.next()) {

                    String disponibilidad
                            = rs.getString("disponibilidad");

                    boolean disponible
                            = disponibilidad != null
                            && "DISPONIBLE".equalsIgnoreCase(
                                    disponibilidad.trim()
                            );

                    l.add(
                            new Butaca(
                                    rs.getInt("id_butaca"),
                                    rs.getString("fila")
                                    + rs.getInt("numero"),
                                    disponible
                            )
                    );
                }
            }
        }

        return l;
    }

    private int id(String call, int... args) throws SQLException {

        try (
                Connection c = con(); CallableStatement s = c.prepareCall(call)) {

            for (int i = 0; i < args.length; i++) {
                s.setInt(i + 1, args[i]);
            }

            try (ResultSet rs = s.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new SQLException("No devolvió ID");
    }

    private void ejecutar(String call, int... args)
            throws SQLException {

        try (
                Connection c = con(); CallableStatement s = c.prepareCall(call)) {

            for (int i = 0; i < args.length; i++) {
                s.setInt(i + 1, args[i]);
            }

            s.execute();
        }
    }

    @Override
    public int abrir(int cliente, int usuario)
            throws SQLException {

        return id(
                "{call sp_abrirventa(?,?)}",
                cliente,
                usuario
        );
    }

    @Override
    public void agregar(
            int venta,
            int funcion,
            int butaca
    ) throws SQLException {

        id(
                "{call sp_agregarboleto(?,?,?)}",
                venta,
                funcion,
                butaca
        );
    }

    @Override
    public void quitar(
            int venta,
            int boleto
    ) throws SQLException {

        ejecutar(
                "{call sp_quitarboleto(?,?)}",
                venta,
                boleto
        );
    }

    @Override
    public void cancelar(int venta)
            throws SQLException {

        ejecutar(
                "{call sp_eliminarventaabierta(?)}",
                venta
        );
    }

    @Override
    public List<Boleto> boletos(int venta)
            throws SQLException {

        List<Boleto> l = new ArrayList<>();

        try (
                Connection c = con(); CallableStatement s
                = c.prepareCall("{call sp_listarboletosventa(?)}")) {

            s.setInt(1, venta);

            try (ResultSet rs = s.executeQuery()) {

                while (rs.next()) {

                    l.add(
                            new Boleto(
                                    rs.getInt("id_boleto"),
                                    rs.getInt("id_butaca"),
                                    rs.getString("butaca"),
                                    rs.getBigDecimal("precio_unitario")
                            )
                    );
                }
            }
        }

        return l;
    }

    private Venta map(ResultSet rs)
            throws SQLException {

        return new Venta(
                rs.getInt("id_venta"),
                rs.getString("cliente"),
                rs.getString("estado"),
                rs.getBigDecimal("total_venta")
        );
    }

    @Override
    public Venta confirmar(int venta, String metodoPago) throws SQLException {
        try (
                Connection c = con(); CallableStatement s = c.prepareCall("{call sp_confirmarventa(?,?)}")) {
            s.setInt(1, venta);
            s.setString(2, metodoPago);

            try (ResultSet rs = s.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        }

        throw new SQLException("Venta no confirmada");
    }

    @Override
    public Venta consultar(int venta)
            throws SQLException {

        try (
                Connection c = con(); CallableStatement s
                = c.prepareCall("{call sp_buscarventa(?)}")) {

            s.setInt(1, venta);

            try (ResultSet rs = s.executeQuery()) {

                if (rs.next()) {
                    return map(rs);
                }
            }
        }

        throw new SQLException("Venta no encontrada");
    }

    @Override
    public List<Venta> ventas()
            throws SQLException {

        List<Venta> l = new ArrayList<>();

        try (
                Connection c = con(); CallableStatement s
                = c.prepareCall("{call sp_listarventas()}"); ResultSet rs = s.executeQuery()) {

            while (rs.next()) {
                l.add(map(rs));
            }
        }

        return l;
    }

    @Override
   public List<String> factura(int venta) throws SQLException {

    List<String> l = new ArrayList<>();

    try (
        Connection c = con();
        CallableStatement s = c.prepareCall("{call sp_verfactura(?)}")
    ) {

        s.setInt(1, venta);

        boolean hayResultados = s.execute();
        int numeroResultado = 0;

        while (true) {

            if (hayResultados) {

                try (ResultSet rs = s.getResultSet()) {

                    // RESULTADO 0 = ENCABEZADO
                    if (numeroResultado == 0) {

                        // No necesitamos agregar el encabezado
                        // porque la pantalla de factura ya muestra
                        // el ID de la venta.

                        while (rs.next()) {
                            // Se consume el ResultSet para poder
                            // avanzar al siguiente.
                        }
                    }

                    // RESULTADO 1 = BOLETOS
                    else if (numeroResultado == 1) {

                        while (rs.next()) {

                            String pelicula =
                                    rs.getString("pelicula");

                            String sala =
                                    rs.getString("nombre_sala");

                            String butaca =
                                    rs.getString("butaca");

                            BigDecimal precio =
                                    rs.getBigDecimal("precio_unitario");

                            l.add(
                                "BOLETO | "
                                + pelicula
                                + " | "
                                + sala
                                + " | Butaca "
                                + butaca
                                + " | Q"
                                + precio
                            );
                        }
                    }

                    // RESULTADO 2 = PRODUCTOS
                    else if (numeroResultado == 2) {

                        while (rs.next()) {

                            String producto =
                                    rs.getString("nombre_producto");

                            int cantidad =
                                    rs.getInt("cantidad");

                            BigDecimal precio =
                                    rs.getBigDecimal("precio_unitario");

                            BigDecimal subtotal =
                                    rs.getBigDecimal("subtotal");

                            l.add(
                                "PRODUCTO | "
                                + producto
                                + " | "
                                + cantidad
                                + " x Q"
                                + precio
                                + " = Q"
                                + subtotal
                            );
                        }
                    }
                }

                numeroResultado++;
            } else {

                if (s.getUpdateCount() == -1) {
                    break;
                }
            }

            hayResultados = s.getMoreResults();
        }
    }

    return l;
}
}
