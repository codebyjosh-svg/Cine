package org.cine.dao.impl;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.cine.dao.VentaDAO;
import org.cine.model.Boleto;
import org.cine.model.ClienteVinculo;
import org.cine.model.FacturaVenta;
import org.cine.model.LineaFactura;
import org.cine.model.LineaVentaProducto;
import org.cine.model.Producto;
import org.cine.model.Venta;
import org.cine.model.VentaResumen;
import org.cine.util.Conexion;

/**
 * Implementación JDBC del DAO de ventas.
 *
 * Integra:
 * - Clientes
 * - Funciones
 * - Butacas
 * - Boletos
 * - Ventas
 * - Productos de dulcería
 * - Confirmación
 * - Cancelación
 * - Facturación
 */
public final class VentaDAOImpl implements VentaDAO {

    // =========================================================
    // CONEXIÓN
    // =========================================================

    private Connection con() throws SQLException {
        return Conexion.getInstancia().getConnection();
    }

    // =========================================================
    // CLIENTES
    // =========================================================

    @Override
    public List<Opcion> clientes() throws SQLException {

        List<Opcion> lista = new ArrayList<>();

        try (
                Connection cn = con();
                CallableStatement cs =
                        cn.prepareCall("{call sp_listarclientesactivos()}");
                ResultSet rs = cs.executeQuery()
        ) {

            while (rs.next()) {

                lista.add(
                        new Opcion(
                                rs.getInt("id_cliente"),
                                rs.getString("nombre_cliente")
                                + " "
                                + rs.getString("apellido_cliente")
                        )
                );
            }
        }

        return lista;
    }

    @Override
    public List<ClienteVinculo> listarClientesActivos()
            throws SQLException {

        List<ClienteVinculo> lista = new ArrayList<>();

        try (
                Connection cn = con();
                CallableStatement cs =
                        cn.prepareCall("{call sp_listarclientesactivos()}");
                ResultSet rs = cs.executeQuery()
        ) {

            while (rs.next()) {

                lista.add(
                        new ClienteVinculo(
                                rs.getInt("id_cliente"),
                                rs.getString("nombre_cliente")
                                + " "
                                + rs.getString("apellido_cliente"),
                                rs.getString("correo_electronico"),
                                true
                        )
                );
            }
        }

        return lista;
    }

    @Override
    public Opcion crearCliente(
            String cui,
            String nombre,
            String apellido,
            String correo,
            String telefono
    ) throws SQLException {

        String nombreCompleto =
                nombre.trim() + " " + apellido.trim();

        try (
                Connection cn = con();
                CallableStatement cs =
                        cn.prepareCall(
                                "{call sp_insertarcliente(?,?,?,?,?)}"
                        )
        ) {

            if (cui == null || cui.isBlank()) {
                cs.setNull(1, java.sql.Types.CHAR);
            } else {
                cs.setString(1, cui.trim());
            }

            cs.setString(2, nombre.trim());
            cs.setString(3, apellido.trim());
            cs.setString(4, correo.trim());

            if (telefono == null || telefono.isBlank()) {
                cs.setNull(5, java.sql.Types.VARCHAR);
            } else {
                cs.setString(5, telefono.trim());
            }

            try (ResultSet rs = cs.executeQuery()) {

                if (rs.next()) {

                    return new Opcion(
                            rs.getInt("id_cliente"),
                            nombreCompleto
                    );
                }
            }
        }

        throw new SQLException(
                "No se pudo obtener el ID del nuevo cliente."
        );
    }

    // =========================================================
    // FUNCIONES
    // =========================================================

    @Override
    public List<Funcion> funciones() throws SQLException {

        List<Funcion> lista = new ArrayList<>();

        String sql =
                "SELECT "
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
                Connection cn = con();
                PreparedStatement ps =
                        cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {

                lista.add(
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

        return lista;
    }

    // =========================================================
    // BUTACAS
    // =========================================================

    @Override
    public List<Butaca> butacas(int idFuncion)
            throws SQLException {

        List<Butaca> lista = new ArrayList<>();

        try (
                Connection cn = con();
                CallableStatement cs =
                        cn.prepareCall(
                                "{call sp_listarbutacasfuncion(?)}"
                        )
        ) {

            cs.setInt(1, idFuncion);

            try (ResultSet rs = cs.executeQuery()) {

                while (rs.next()) {

                    String disponibilidad =
                            rs.getString("disponibilidad");

                    boolean disponible =
                            disponibilidad != null
                            && "DISPONIBLE".equalsIgnoreCase(
                                    disponibilidad.trim()
                            );

                    lista.add(
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

        return lista;
    }

    // =========================================================
    // MÉTODOS AUXILIARES
    // =========================================================

    /**
     * Ejecuta un procedimiento que devuelve un ID.
     */
    private int obtenerId(
            String procedimiento,
            int... parametros
    ) throws SQLException {

        try (
                Connection cn = con();
                CallableStatement cs =
                        cn.prepareCall(procedimiento)
        ) {

            for (int i = 0; i < parametros.length; i++) {
                cs.setInt(i + 1, parametros[i]);
            }

            try (ResultSet rs = cs.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new SQLException(
                "El procedimiento no devolvió un ID."
        );
    }

    /**
     * Ejecuta un procedimiento que no necesita devolver datos.
     */
    private void ejecutar(
            String procedimiento,
            int... parametros
    ) throws SQLException {

        try (
                Connection cn = con();
                CallableStatement cs =
                        cn.prepareCall(procedimiento)
        ) {

            for (int i = 0; i < parametros.length; i++) {
                cs.setInt(i + 1, parametros[i]);
            }

            cs.execute();
        }
    }

    // =========================================================
    // VENTAS
    // =========================================================

    @Override
    public int abrir(
            int cliente,
            int usuario
    ) throws SQLException {

        return obtenerId(
                "{call sp_abrirventa(?,?)}",
                cliente,
                usuario
        );
    }

    @Override
    public int abrirVenta(
            int idCliente,
            int idUsuario
    ) throws SQLException {

        return abrir(
                idCliente,
                idUsuario
        );
    }

    // =========================================================
    // BOLETOS
    // =========================================================

    @Override
    public void agregar(
            int venta,
            int funcion,
            int butaca
    ) throws SQLException {

        obtenerId(
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
    public List<Boleto> boletos(
            int venta
    ) throws SQLException {

        List<Boleto> lista = new ArrayList<>();

        try (
                Connection cn = con();
                CallableStatement cs =
                        cn.prepareCall(
                                "{call sp_listarboletosventa(?)}"
                        )
        ) {

            cs.setInt(1, venta);

            try (ResultSet rs = cs.executeQuery()) {

                while (rs.next()) {

                    lista.add(
                            new Boleto(
                                    rs.getInt("id_boleto"),
                                    rs.getInt("id_butaca"),
                                    rs.getString("butaca"),
                                    rs.getBigDecimal(
                                            "precio_unitario"
                                    )
                            )
                    );
                }
            }
        }

        return lista;
    }

    // =========================================================
    // PRODUCTOS / DULCERÍA
    // =========================================================

    @Override
    public List<Producto> listarProductosDisponibles()
            throws SQLException {

        List<Producto> lista = new ArrayList<>();

        try (
                Connection cn = con();
                CallableStatement cs =
                        cn.prepareCall(
                                "{call sp_listarproductosdisponibles()}"
                        );
                ResultSet rs = cs.executeQuery()
        ) {

            while (rs.next()) {

                lista.add(
                        new Producto(
                                rs.getInt("id_producto"),
                                rs.getString("nombre_producto"),
                                rs.getString("nombre_categoria"),
                                rs.getBigDecimal("precio"),
                                rs.getInt("stock"),
                                rs.getInt("stock_minimo"),
                                rs.getBoolean("estado")
                        )
                );
            }
        }

        return lista;
    }

    @Override
    public List<LineaVentaProducto> listarProductosVenta(
            int idVenta
    ) throws SQLException {

        List<LineaVentaProducto> lista =
                new ArrayList<>();

        try (
                Connection cn = con();
                CallableStatement cs =
                        cn.prepareCall(
                                "{call sp_listarproductosventa(?)}"
                        )
        ) {

            cs.setInt(1, idVenta);

            try (ResultSet rs = cs.executeQuery()) {

                while (rs.next()) {

                    lista.add(
                            new LineaVentaProducto(
                                    rs.getInt("id_producto"),
                                    rs.getString(
                                            "nombre_producto"
                                    ),
                                    rs.getInt("cantidad"),
                                    rs.getBigDecimal(
                                            "precio_unitario"
                                    )
                            )
                    );
                }
            }
        }

        return lista;
    }

    @Override
    public void agregarProducto(
            int idVenta,
            int idProducto,
            int cantidad
    ) throws SQLException {

        ejecutar(
                "{call sp_agregarproductoventa(?,?,?)}",
                idVenta,
                idProducto,
                cantidad
        );
    }

    @Override
    public void cambiarCantidad(
            int idVenta,
            int idProducto,
            int cantidad
    ) throws SQLException {

        ejecutar(
                "{call sp_actualizarcantidadproducto(?,?,?)}",
                idVenta,
                idProducto,
                cantidad
        );
    }

    @Override
    public void quitarProducto(
            int idVenta,
            int idProducto
    ) throws SQLException {

        ejecutar(
                "{call sp_quitarproductoventa(?,?)}",
                idVenta,
                idProducto
        );
    }

    @Override
    public void validarStock(
            int idVenta
    ) throws SQLException {

        try (
                Connection cn = con();
                CallableStatement cs =
                        cn.prepareCall(
                                "{call sp_validarstockventa(?)}"
                        )
        ) {

            cs.setInt(1, idVenta);

            try (ResultSet rs = cs.executeQuery()) {

                if (rs.next()) {

                    throw new SQLException(
                            "Stock insuficiente o producto "
                            + "inactivo: "
                            + rs.getString(
                                    "nombre_producto"
                            )
                            + ". Cantidad solicitada: "
                            + rs.getInt("cantidad")
                            + "; disponible: "
                            + rs.getInt("stock")
                            + ".",
                            "45000"
                    );
                }
            }
        }
    }

    // =========================================================
    // CONSULTAR VENTA
    // =========================================================

    private Venta mapVenta(
            ResultSet rs
    ) throws SQLException {

        return new Venta(
                rs.getInt("id_venta"),
                rs.getString("cliente"),
                rs.getString("estado"),
                rs.getBigDecimal("total_venta")
        );
    }

    @Override
    public Venta consultar(
            int id
    ) throws SQLException {

        try (
                Connection cn = con();
                CallableStatement cs =
                        cn.prepareCall(
                                "{call sp_buscarventa(?)}"
                        )
        ) {

            cs.setInt(1, id);

            try (ResultSet rs = cs.executeQuery()) {

                if (rs.next()) {
                    return mapVenta(rs);
                }
            }
        }

        throw new SQLException(
                "No existe la venta #" + id + "."
        );
    }

    @Override
    public VentaResumen buscarVenta(
            int idVenta
    ) throws SQLException {

        try (
                Connection cn = con();
                CallableStatement cs =
                        cn.prepareCall(
                                "{call sp_buscarventa(?)}"
                        )
        ) {

            cs.setInt(1, idVenta);

            try (ResultSet rs = cs.executeQuery()) {

                if (!rs.next()) {

                    throw new SQLException(
                            "No existe la venta #"
                            + idVenta
                            + "."
                    );
                }

                return new VentaResumen(
                        rs.getInt("id_venta"),
                        rs.getInt("id_cliente"),
                        rs.getInt("id_usuario"),
                        rs.getString("cliente"),
                        rs.getString("taquillero"),
                        rs.getObject(
                                "fecha_venta",
                                java.time.LocalDateTime.class
                        ),
                        rs.getString("estado"),
                        rs.getInt("cantidad_boletos"),
                        rs.getBigDecimal("total_boletos"),
                        rs.getBigDecimal("total_productos"),
                        rs.getBigDecimal("total_venta")
                );
            }
        }
    }

    @Override
    public List<Venta> ventas()
            throws SQLException {

        List<Venta> lista =
                new ArrayList<>();

        try (
                Connection cn = con();
                CallableStatement cs =
                        cn.prepareCall(
                                "{call sp_listarventas()}"
                        );
                ResultSet rs = cs.executeQuery()
        ) {

            while (rs.next()) {
                lista.add(
                        mapVenta(rs)
                );
            }
        }

        return lista;
    }

    // =========================================================
    // CONFIRMAR VENTA
    // =========================================================

    @Override
    public Venta confirmar(
            int venta,
            String metodoPago
    ) throws SQLException {

        // Primero se valida el stock de dulcería.
        validarStock(venta);

        try (
                Connection cn = con();
                CallableStatement cs =
                        cn.prepareCall(
                                "{call sp_confirmarventa(?,?)}"
                        )
        ) {

            cs.setInt(1, venta);

            if (metodoPago == null
                    || metodoPago.isBlank()) {

                cs.setString(
                        2,
                        "EFECTIVO"
                );

            } else {

                cs.setString(
                        2,
                        metodoPago
                );
            }

            try (ResultSet rs = cs.executeQuery()) {

                if (rs.next()) {
                    return mapVenta(rs);
                }
            }
        }

        // Si el SP no devuelve la venta,
        // la consultamos después de confirmarla.
        return consultar(venta);
    }

    @Override
    public void confirmarVenta(
            int idVenta
    ) throws SQLException {

        confirmar(
                idVenta,
                "EFECTIVO"
        );
    }

    // =========================================================
    // CANCELAR
    // =========================================================

    @Override
    public void cancelar(
            int id
    ) throws SQLException {

        ejecutar(
                "{call sp_eliminarventaabierta(?)}",
                id
        );
    }

    // =========================================================
    // FACTURA
    // =========================================================

    @Override
    public FacturaVenta obtenerFactura(
            int idVenta
    ) throws SQLException {

        VentaResumen venta =
                buscarVenta(idVenta);

        if (!"confirmada".equalsIgnoreCase(
                venta.estado()
        )) {

            throw new SQLException(
                    "La factura requiere una venta confirmada."
            );
        }

        List<LineaFactura> lineas =
                new ArrayList<>();

        try (
                Connection cn = con();
                CallableStatement cs =
                        cn.prepareCall(
                                "{call sp_verfactura(?)}"
                        )
        ) {

            cs.setInt(1, idVenta);

            try (ResultSet rs = cs.executeQuery()) {

                while (rs.next()) {

                    lineas.add(
                            new LineaFactura(
                                    rs.getString(
                                            "tipo_articulo"
                                    ),
                                    rs.getString(
                                            "descripcion"
                                    ),
                                    rs.getInt(
                                            "cantidad"
                                    ),
                                    rs.getBigDecimal(
                                            "precio_unitario"
                                    ),
                                    rs.getBigDecimal(
                                            "subtotal"
                                    )
                            )
                    );
                }
            }
        }

        return new FacturaVenta(
                venta,
                lineas
        );
    }

    // =========================================================
    // FACTURA COMO LISTA DE TEXTO
    // =========================================================

    @Override
    public List<String> factura(
            int id
    ) throws SQLException {

        FacturaVenta resultado =
                obtenerFactura(id);

        List<String> lista =
                new ArrayList<>();

        for (LineaFactura linea
                : resultado.lineas()) {

            StringBuilder texto =
                    new StringBuilder();

            texto.append(
                    linea.tipoArticulo()
            );

            texto.append(" | ");

            texto.append(
                    linea.descripcion()
            );

            texto.append(" | ");

            texto.append(
                    linea.cantidad()
            );

            texto.append(" x Q");

            texto.append(
                    linea.precioUnitario()
            );

            texto.append(" = Q");

            texto.append(
                    linea.subtotal()
            );

            lista.add(
                    texto.toString()
            );
        }

        return lista;
    }
}