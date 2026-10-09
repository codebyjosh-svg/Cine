package org.cine.test;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.cine.dao.StockCriticoDAO;
import org.cine.dao.VentaDAO;
import org.cine.dao.impl.StockCriticoDAOImpl;
import org.cine.dao.impl.VentaDAOImpl;
import org.cine.model.FacturaVenta;
import org.cine.model.StockCritico;
import org.cine.util.Conexion;

/** T3.3.11 y T3.3.12. Crea datos propios y los elimina en finally. Sin JUnit. */
public final class PruebaStockDulceriaBD {
    private final VentaDAO ventas = new VentaDAOImpl();
    private final StockCriticoDAO stock = new StockCriticoDAOImpl();
    private final List<Integer> idsVentas = new ArrayList<>();
    private final List<Integer> idsProductos = new ArrayList<>();
    private int categoria, cliente, genero, pelicula, sala, butaca, funcion, vendedor;
    private int pruebas;
    private final String marca = "US33_" + System.nanoTime();

    public static void main(String[] args) throws Exception {
        PruebaStockDulceriaBD prueba = new PruebaStockDulceriaBD();
        try { prueba.ejecutar(); }
        finally { prueba.limpiar(); }
    }

    private void ejecutar() throws Exception {
        categoria = insertar("INSERT INTO categorias_producto(nombre_categoria) VALUES (?)", marca);
        cliente = insertar("INSERT INTO clientes(nombre_cliente,apellido_cliente,correo_electronico) VALUES (?,?,?)",
                "Prueba", "Dulcería", marca + "@example.com");
        vendedor = numero("SELECT u.id_usuario FROM usuarios u JOIN roles r ON r.id_rol=u.id_rol "
                + "WHERE u.estado=1 AND r.nombre_rol IN ('admin','taquillero') ORDER BY u.id_usuario LIMIT 1");
        comprobar(vendedor > 0, "Hay un vendedor activo para la prueba");
        int igual = producto("igual", 5, 5, true);
        int menor = producto("menor", 2, 5, true);
        int normal = producto("normal", 6, 5, true);
        int cero = producto("cero", 0, 0, true);
        int inactivo = producto("inactivo", 1, 5, false);
        List<StockCritico> criticos = stock.listar();
        comprobar(criticos.stream().anyMatch(p -> p.getIdProducto() == igual), "Stock igual al mínimo aparece en la alerta");
        comprobar(criticos.stream().anyMatch(p -> p.getIdProducto() == menor), "Stock menor al mínimo aparece en la alerta");
        comprobar(criticos.stream().noneMatch(p -> p.getIdProducto() == normal), "Stock mayor al mínimo queda fuera de la alerta");
        comprobar(criticos.stream().anyMatch(p -> p.getIdProducto() == cero && "Sin existencias".equals(p.getNivel())),
                "Stock cero genera alerta aunque el mínimo sea cero");
        comprobar(criticos.stream().noneMatch(p -> p.getIdProducto() == inactivo), "Producto inactivo queda fuera de la alerta");

        rechazado(() -> ventas.abrirVenta(0, vendedor), "Venta sin cliente es rechazada");
        int venta = abrir();
        rechazado(() -> ventas.confirmarVenta(venta), "Carrito vacío no puede confirmarse");
        rechazado(() -> ventas.agregarProducto(venta, inactivo, 1), "Producto inactivo no se agrega al carrito");
        rechazado(() -> ventas.agregarProducto(venta, cero, 1), "Producto agotado no se agrega al carrito");
        rechazado(() -> ventas.agregarProducto(venta, igual, 0), "Cantidad cero es rechazada");
        ventas.agregarProducto(venta, igual, 1);
        ventas.agregarProducto(venta, igual, 1);
        actualizar("UPDATE productos SET precio=99.99 WHERE id_producto=?", igual);
        ventas.agregarProducto(venta, igual, 1);
        var lineas = ventas.listarProductosVenta(venta);
        comprobar(lineas.size() == 1 && lineas.get(0).getCantidad() == 3,
                "Agregar 1x tres veces produce una línea con cantidad 3");
        comprobar(lineas.get(0).getPrecioUnitario().compareTo(new BigDecimal("10.25")) == 0,
                "La línea conserva el precio original al incrementar cantidad");
        comprobar(lineas.get(0).getSubtotal().compareTo(new BigDecimal("30.75")) == 0,
                "Subtotal de productos es cantidad por precio exacto");
        comprobar(numero("SELECT stock FROM productos WHERE id_producto=" + igual) == 5,
                "Agregar al carrito no descuenta ni reserva stock");
        rechazado(() -> ventas.agregarProducto(venta, igual, 3), "Cantidad acumulada superior al stock es rechazada");
        comprobar(ventas.listarProductosVenta(venta).get(0).getCantidad() == 3,
                "Agregado rechazado conserva la cantidad anterior");
        ventas.cambiarCantidad(venta, igual, 4);
        comprobar(ventas.listarProductosVenta(venta).get(0).getCantidad() == 4,
                "Guardar cantidad reemplaza el valor en la misma línea");
        rechazado(() -> ventas.cambiarCantidad(venta, igual, 6), "Edición de cantidad superior al stock es rechazada");
        rechazado(() -> ventas.cambiarCantidad(venta, igual, -1), "Cantidad negativa es rechazada");
        ventas.cambiarCantidad(venta, igual, 3);
        ventas.agregarProducto(venta, normal, 1);
        ventas.quitarProducto(venta, normal);
        comprobar(ventas.listarProductosVenta(venta).size() == 1,
                "Quitar una línea conserva las demás líneas");

        crearBoleto(venta);
        var resumen = ventas.buscarVenta(venta);
        comprobar(resumen.totalBoletos().compareTo(new BigDecimal("40.00")) == 0
                && resumen.totalProductos().compareTo(new BigDecimal("30.75")) == 0
                && resumen.totalVenta().compareTo(new BigDecimal("70.75")) == 0,
                "Total general combina boletos y dulcería sin duplicar importes");
        ventas.confirmarVenta(venta);
        comprobar(numero("SELECT stock FROM productos WHERE id_producto=" + igual) == 2,
                "Confirmar descuenta exactamente la cantidad vendida");
        comprobar(numero("SELECT COUNT(*) FROM movimientos_inventario WHERE id_venta=" + venta) == 1
                && numero("SELECT cantidad FROM movimientos_inventario WHERE id_venta=" + venta) == 3,
                "Se registra una sola salida por venta y producto");
        comprobar(numero("SELECT COUNT(*) FROM boletos WHERE id_venta=" + venta + " AND estado='vendido'") == 1,
                "La venta mixta confirma su boleto");
        FacturaVenta factura = ventas.obtenerFactura(venta);
        try (Connection cn = Conexion.getInstancia().getConnection();
                PreparedStatement ps = cn.prepareStatement("SELECT fecha_venta FROM ventas WHERE id_venta=?")) {
            ps.setInt(1, venta);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                comprobar(factura.venta().fechaVenta().equals(rs.getObject(1, java.time.LocalDateTime.class)),
                        "La factura conserva la fecha DATETIME sin desplazarla por la zona horaria de Java");
            }
        }
        comprobar(factura.lineas().size() == 2 && factura.lineas().stream().anyMatch(l -> "boleto".equals(l.tipoArticulo()))
                && factura.lineas().stream().anyMatch(l -> "producto".equals(l.tipoArticulo())),
                "La factura contiene el boleto y una línea de dulcería");
        comprobar(factura.lineas().stream().map(l -> l.subtotal()).reduce(BigDecimal.ZERO, BigDecimal::add)
                .compareTo(factura.venta().totalVenta()) == 0, "Las líneas de factura suman el total persistido");
        rechazado(() -> ventas.confirmarVenta(venta), "Una venta confirmada no se confirma dos veces");
        comprobar(numero("SELECT stock FROM productos WHERE id_producto=" + igual) == 2,
                "Doble confirmación rechazada no vuelve a descontar stock");

        int primero = producto("rollback_a", 10, 2, true);
        int segundo = producto("rollback_b", 5, 2, true);
        int fallida = abrir();
        ventas.agregarProducto(fallida, primero, 2);
        ventas.agregarProducto(fallida, segundo, 3);
        actualizar("UPDATE productos SET stock=1 WHERE id_producto=?", segundo);
        rechazado(() -> ventas.validarStock(fallida), "Prevalidación detecta stock reducido por otro vendedor");
        // Se llama directamente al SP para comprobar su rollback, sin la prevalidación del DAO.
        rechazado(() -> llamar("{call sp_confirmarventa(?)}", fallida), "El SP vuelve a validar existencias al confirmar");
        comprobar(numero("SELECT stock FROM productos WHERE id_producto=" + primero) == 10
                && numero("SELECT stock FROM productos WHERE id_producto=" + segundo) == 1,
                "Fallo en el segundo producto revierte el descuento del primero");
        comprobar(numero("SELECT COUNT(*) FROM movimientos_inventario WHERE id_venta=" + fallida) == 0
                && "abierta".equals(ventas.buscarVenta(fallida).estado()),
                "Venta rechazada permanece abierta sin movimientos parciales");
        actualizar("UPDATE productos SET stock=3 WHERE id_producto=?", segundo);
        ventas.confirmarVenta(fallida);
        comprobar(numero("SELECT stock FROM productos WHERE id_producto=" + segundo) == 0,
                "Stock exactamente suficiente puede consumirse hasta cero");

        int ultimo = producto("ultima_unidad", 1, 1, true);
        int ventaA = abrir(), ventaB = abrir();
        ventas.agregarProducto(ventaA, ultimo, 1);
        ventas.agregarProducto(ventaB, ultimo, 1);
        CountDownLatch preparados = new CountDownLatch(2), iniciar = new CountDownLatch(1);
        try (ExecutorService ejecutor = Executors.newFixedThreadPool(2)) {
            Future<Boolean> a = ejecutor.submit(() -> competir(ventaA, preparados, iniciar));
            Future<Boolean> b = ejecutor.submit(() -> competir(ventaB, preparados, iniciar));
            comprobar(preparados.await(10, TimeUnit.SECONDS), "Ambas ventas prevalidaron la última unidad");
            iniciar.countDown();
            int confirmadas = (a.get(15, TimeUnit.SECONDS) ? 1 : 0) + (b.get(15, TimeUnit.SECONDS) ? 1 : 0);
            comprobar(confirmadas == 1 && numero("SELECT stock FROM productos WHERE id_producto=" + ultimo) == 0,
                    "Dos confirmaciones simultáneas venden la última unidad una sola vez");
        }
        System.out.println("RESULTADO: " + pruebas + " comprobaciones correctas (T3.3.11 / T3.3.12).");
    }

    private boolean competir(int venta, CountDownLatch preparados, CountDownLatch iniciar) throws Exception {
        ventas.validarStock(venta);
        preparados.countDown();
        if (!iniciar.await(12, TimeUnit.SECONDS)) { throw new AssertionError("No inició la prueba concurrente."); }
        try { llamar("{call sp_confirmarventa(?)}", venta); return true; }
        catch (SQLException ex) {
            if (!"45000".equals(ex.getSQLState())) { throw ex; }
            return false;
        }
    }

    private int producto(String nombre, int stockActual, int minimo, boolean activo) throws Exception {
        int id = insertar("INSERT INTO productos(nombre_producto,precio,stock,stock_minimo,id_categoria_producto,estado) VALUES (?,?,?,?,?,?)",
                marca + "_" + nombre, new BigDecimal("10.25"), stockActual, minimo, categoria, activo);
        idsProductos.add(id);
        return id;
    }

    private int abrir() throws Exception {
        int id = ventas.abrirVenta(cliente, vendedor);
        idsVentas.add(id);
        return id;
    }

    private void crearBoleto(int venta) throws Exception {
        genero = insertar("INSERT INTO generos(nombre_genero) VALUES (?)", marca);
        pelicula = insertar("INSERT INTO peliculas(titulo,duracion_minutos,clasificacion,idioma,id_genero) VALUES (?,90,'Todo público','Español',?)", marca, genero);
        sala = insertar("INSERT INTO salas(nombre_sala) VALUES (?)", marca);
        butaca = insertar("INSERT INTO butacas(id_sala,fila,numero) VALUES (?,'A',1)", sala);
        funcion = insertar("INSERT INTO funciones(id_pelicula,id_sala,fecha_inicio,fecha_fin,precio_boleto) "
                + "VALUES (?,?,DATE_ADD(NOW(),INTERVAL 1 DAY),DATE_ADD(DATE_ADD(NOW(),INTERVAL 1 DAY),INTERVAL 110 MINUTE),40.00)", pelicula, sala);
        llamar("{call sp_agregarboleto(?,?,?)}", venta, funcion, butaca);
    }

    private static int insertar(String sql, Object... parametros) throws Exception {
        try (Connection cn = Conexion.getInstancia().getConnection();
                PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < parametros.length; i++) { ps.setObject(i + 1, parametros[i]); }
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); return rs.getInt(1); }
        }
    }

    private static void actualizar(String sql, int id) throws Exception {
        try (Connection cn = Conexion.getInstancia().getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id); ps.executeUpdate();
        }
    }

    private static int numero(String sql) throws Exception {
        try (Connection cn = Conexion.getInstancia().getConnection(); Statement st = cn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private static void llamar(String sql, int... parametros) throws SQLException {
        try (Connection cn = Conexion.getInstancia().getConnection(); CallableStatement cs = cn.prepareCall(sql)) {
            for (int i = 0; i < parametros.length; i++) { cs.setInt(i + 1, parametros[i]); }
            cs.execute();
        }
    }

    private void comprobar(boolean condicion, String descripcion) {
        if (!condicion) { throw new AssertionError(descripcion); }
        System.out.println("OK " + (++pruebas) + " - " + descripcion);
    }

    @FunctionalInterface private interface Operacion { void ejecutar() throws Exception; }
    private void rechazado(Operacion operacion, String descripcion) throws Exception {
        try { operacion.ejecutar(); }
        catch (SQLException ex) {
            comprobar("45000".equals(ex.getSQLState()), descripcion);
            return;
        }
        throw new AssertionError("No se rechazó: " + descripcion);
    }

    private void limpiar() throws Exception {
        for (int id : idsVentas) {
            actualizar("DELETE FROM movimientos_inventario WHERE id_venta=?", id);
            actualizar("DELETE FROM boletos WHERE id_venta=?", id);
            actualizar("DELETE FROM detalle_venta_productos WHERE id_venta=?", id);
            actualizar("DELETE FROM ventas WHERE id_venta=?", id);
        }
        if (funcion > 0) { actualizar("DELETE FROM funciones WHERE id_funcion=?", funcion); }
        if (butaca > 0) { actualizar("DELETE FROM butacas WHERE id_butaca=?", butaca); }
        if (sala > 0) { actualizar("DELETE FROM salas WHERE id_sala=?", sala); }
        if (pelicula > 0) { actualizar("DELETE FROM peliculas WHERE id_pelicula=?", pelicula); }
        if (genero > 0) { actualizar("DELETE FROM generos WHERE id_genero=?", genero); }
        for (int id : idsProductos) { actualizar("DELETE FROM productos WHERE id_producto=?", id); }
        if (categoria > 0) { actualizar("DELETE FROM categorias_producto WHERE id_categoria_producto=?", categoria); }
        if (cliente > 0) { actualizar("DELETE FROM clientes WHERE id_cliente=?", cliente); }
    }
}
