package org.cine.test;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.cine.dao.MovimientoInventarioDAO;
import org.cine.dao.impl.MovimientoInventarioDAOImpl;
import org.cine.model.MovimientoInventario;

/** Integración JDBC real de entradas, salidas, stock insuficiente e historial. */
public class PruebaInventario {
    private static final MovimientoInventarioDAO DAO = new MovimientoInventarioDAOImpl();
    private static int comprobaciones;
    @FunctionalInterface private interface Accion { void ejecutar() throws Exception; }

    private static void comprobar(boolean condicion, String mensaje) {
        if (!condicion) throw new AssertionError(mensaje);
        System.out.println("OK " + (++comprobaciones) + " · " + mensaje);
    }

    private static int stock(int id) throws SQLException {
        return DAO.buscarProducto(id).orElseThrow().getStock();
    }

    private static long historial(int id) throws SQLException {
        return DAO.listarHistorial().stream().filter(m -> m.getIdProducto() == id).count();
    }

    private static void rechazarSql(Accion accion, String parte, String mensaje) throws Exception {
        try {
            accion.ejecutar();
            throw new AssertionError("Se permitió: " + mensaje);
        } catch (SQLException ex) {
            if (!"45000".equals(ex.getSQLState()) || !ex.getMessage().contains(parte)) throw ex;
            comprobar(true, mensaje);
        }
    }

    private static void rechazarJava(Accion accion, String mensaje) throws Exception {
        try {
            accion.ejecutar();
            throw new AssertionError("Se permitió: " + mensaje);
        } catch (IllegalArgumentException ex) {
            comprobar(true, mensaje);
        }
    }

    public static void main(String[] args) throws Exception {
        try (DatosInventarioPrueba d = new DatosInventarioPrueba()) {
            comprobar(DAO.listarProductosActivos().stream().anyMatch(p -> p.getIdProducto() == d.producto)
                    && DAO.listarProductosActivos().stream().noneMatch(p -> p.getIdProducto() == d.productoInactivo),
                    "El catálogo incluye productos activos y excluye inactivos");
            comprobar(DAO.buscarProducto(Integer.MAX_VALUE).isEmpty(), "Buscar un producto inexistente devuelve vacío");
            DAO.registrarEntrada(d.producto, d.bodega, 5, "  Recepción de prueba  ");
            comprobar(stock(d.producto) == 15, "Una entrada aumenta el stock de 10 a 15");
            MovimientoInventario entrada = DAO.listarHistorial().stream().filter(m -> m.getIdProducto() == d.producto).findFirst().orElseThrow();
            comprobar("entrada".equals(entrada.getTipoMovimiento()) && entrada.getCantidad() == 5
                    && entrada.getIdUsuario() == d.bodega && entrada.getUsuario() != null && !entrada.getUsuario().isBlank()
                    && entrada.getFechaMovimiento() != null && entrada.getIdVenta() == null
                    && entrada.getObservacion().equals("Recepción de prueba"),
                    "El historial devuelve producto, usuario, fecha, tipo, cantidad y observación sin espacios externos");
            DAO.registrarSalida(d.producto, d.admin, 4, "Salida de prueba");
            comprobar(stock(d.producto) == 11, "Una salida disminuye el stock de 15 a 11");
            List<MovimientoInventario> movimientos = DAO.listarHistorial().stream().filter(m -> m.getIdProducto() == d.producto).toList();
            comprobar(movimientos.size() == 2 && movimientos.get(0).getIdUsuario() == d.admin
                    && movimientos.get(0).getIdMovimiento() > movimientos.get(1).getIdMovimiento(),
                    "El historial conserva ambos operadores y ordena los movimientos recientes primero");
            rechazarSql(() -> DAO.registrarSalida(d.producto, d.bodega, 12, "No debe registrarse"), "stock suficiente",
                    "El DAO recibe el rechazo SQL de una salida mayor que el stock");
            comprobar(stock(d.producto) == 11 && historial(d.producto) == 2,
                    "Una salida insuficiente conserva el stock y no agrega historial");
            DAO.registrarSalida(d.producto, d.bodega, 11, "Agotar existencias");
            comprobar(stock(d.producto) == 0, "Una salida exacta deja el stock en cero");
            rechazarSql(() -> DAO.registrarSalida(d.producto, d.bodega, 1, "Sin existencias"), "stock suficiente",
                    "El stock cero impide una nueva salida");
            comprobar(stock(d.producto) == 0 && historial(d.producto) == 3,
                    "El rechazo no produce stock negativo ni un movimiento adicional");
            rechazarJava(() -> DAO.registrarEntrada(d.producto, d.bodega, 0, "Cantidad cero"), "Cantidad cero rechazada");
            rechazarJava(() -> DAO.registrarSalida(d.producto, d.bodega, -1, "Cantidad negativa"), "Cantidad negativa rechazada");
            rechazarJava(() -> DAO.registrarEntrada(d.producto, d.bodega, 1, "  "), "Observación vacía rechazada");
            rechazarJava(() -> DAO.registrarEntrada(d.producto, d.bodega, 1, "x".repeat(201)), "Observación de más de 200 caracteres rechazada");
            rechazarJava(() -> DAO.registrar(new MovimientoInventario(d.producto, d.bodega, "venta", 1, "Manual inválido")),
                    "Un registro manual solo admite entrada o salida");
            rechazarJava(() -> DAO.registrarEntrada(0, d.bodega, 1, "Sin producto"), "ID de producto inválido rechazado");
            rechazarJava(() -> DAO.registrarEntrada(d.producto, 0, 1, "Sin usuario"), "ID de usuario inválido rechazado");
            comprobar(historial(d.producto) == 3, "Las validaciones Java no crean movimientos");
            rechazarSql(() -> DAO.registrarEntrada(d.productoInactivo, d.bodega, 1, "Inactivo"), "inactivo", "Producto inactivo rechazado por MySQL");
            rechazarSql(() -> DAO.registrarEntrada(Integer.MAX_VALUE, d.bodega, 1, "Inexistente"), "no existe", "Producto inexistente rechazado por MySQL");
            rechazarSql(() -> DAO.registrarEntrada(d.producto, d.taquillero, 1, "Sin permiso"), "bodega o administrador",
                    "Un taquillero no puede registrar un movimiento manual");
            rechazarSql(() -> DAO.registrarEntrada(d.producto, d.cliente, 1, "Sin permiso"), "bodega o administrador",
                    "Un cliente no puede registrar un movimiento manual");
            rechazarSql(() -> DAO.registrarEntrada(d.producto, d.usuarioInactivo, 1, "Inactivo"), "activo",
                    "Un operador inactivo no puede registrar movimientos");
            DAO.registrarEntrada(d.producto, d.admin, 2, "Historial persistente");
            d.cambiarEstadoProducto(d.producto, 0);
            comprobar(historial(d.producto) == 4 && DAO.listarProductosActivos().stream().noneMatch(p -> p.getIdProducto() == d.producto),
                    "Desactivar el producto conserva su historial y lo retira del catálogo activo");
            d.cambiarEstadoProducto(d.producto, 1);
            DAO.registrarEntrada(d.productoVacio, d.admin, 10, "Preparar concurrencia");
            ExecutorService hilos = Executors.newFixedThreadPool(2);
            CountDownLatch inicio = new CountDownLatch(1);
            try {
                java.util.concurrent.Callable<Boolean> salida = () -> {
                    inicio.await();
                    try {
                        DAO.registrarSalida(d.productoVacio, d.bodega, 7, "Salida simultánea");
                        return true;
                    } catch (SQLException ex) {
                        if ("45000".equals(ex.getSQLState()) && ex.getMessage().contains("stock suficiente")) return false;
                        throw ex;
                    }
                };
                Future<Boolean> primera = hilos.submit(salida);
                Future<Boolean> segunda = hilos.submit(salida);
                inicio.countDown();
                comprobar(primera.get(10, TimeUnit.SECONDS) != segunda.get(10, TimeUnit.SECONDS),
                        "Dos salidas simultáneas no pueden consumir las mismas existencias");
                comprobar(stock(d.productoVacio) == 3 && historial(d.productoVacio) == 2,
                        "La concurrencia deja stock correcto y un solo registro de salida");
            } finally {
                hilos.shutdownNow();
            }
            DAO.registrarEntrada(d.productoVacio, d.admin, Integer.MAX_VALUE - 3, "Límite de stock");
            try {
                DAO.registrarEntrada(d.productoVacio, d.admin, 1, "Desbordamiento");
                throw new AssertionError("Se permitió desbordamiento de stock");
            } catch (SQLException ex) {
                if (!"22003".equals(ex.getSQLState()) && ex.getErrorCode() != 1264) throw ex;
                comprobar(stock(d.productoVacio) == Integer.MAX_VALUE && historial(d.productoVacio) == 3,
                        "Superar el límite entero revierte la operación sin alterar el stock ni el historial");
            }
        }
        System.out.println("RESULTADO: " + comprobaciones + " comprobaciones de inventario correctas; datos de prueba retirados.");
    }
}
