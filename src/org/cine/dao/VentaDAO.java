package org.cine.dao;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import org.cine.model.Boleto;
import org.cine.model.ClienteVinculo;
import org.cine.model.FacturaVenta;
import org.cine.model.LineaVentaProducto;
import org.cine.model.Producto;
import org.cine.model.Venta;
import org.cine.model.VentaResumen;

/**
 * DAO para la gestión de ventas del sistema de cine.
 *
 * Integra:
 * - Clientes
 * - Funciones
 * - Butacas
 * - Boletos
 * - Carrito de venta
 * - Productos de dulcería
 * - Confirmación de ventas
 * - Cancelación
 * - Facturación
 */
public interface VentaDAO {

    // =========================================================
    // OPCIONES PARA COMBOS
    // =========================================================

    record Opcion(int id, String nombre) {

        @Override
        public String toString() {
            return nombre;
        }
    }

    record Funcion(
            int id,
            String nombre,
            BigDecimal precio) {

        @Override
        public String toString() {
            return nombre + " - Q" + precio;
        }
    }

    record Butaca(
            int id,
            String nombre,
            boolean disponible) {

        @Override
        public String toString() {
            return nombre;
        }
    }

    // =========================================================
    // CLIENTES
    // =========================================================

    /**
     * Lista los clientes disponibles para realizar una venta.
     */
    List<Opcion> clientes() throws SQLException;

    /**
     * Lista los clientes activos.
     */
    List<ClienteVinculo> listarClientesActivos()
            throws SQLException;

    /**
     * Crea un nuevo cliente.
     */
    Opcion crearCliente(
            String cui,
            String nombre,
            String apellido,
            String correo,
            String telefono
    ) throws SQLException;

    // =========================================================
    // FUNCIONES
    // =========================================================

    /**
     * Lista las funciones disponibles.
     */
    List<Funcion> funciones() throws SQLException;

    // =========================================================
    // BUTACAS
    // =========================================================

    /**
     * Lista las butacas de una función.
     */
    List<Butaca> butacas(
            int funcion
    ) throws SQLException;

    // =========================================================
    // VENTA
    // =========================================================

    /**
     * Abre una nueva venta.
     */
    int abrir(
            int cliente,
            int usuario
    ) throws SQLException;

    /**
     * Abre una nueva venta.
     */
    int abrirVenta(
            int idCliente,
            int idUsuario
    ) throws SQLException;

    /**
     * Consulta una venta por ID.
     */
    Venta consultar(
            int id
    ) throws SQLException;

    /**
     * Busca el resumen de una venta.
     */
    VentaResumen buscarVenta(
            int idVenta
    ) throws SQLException;

    /**
     * Lista las ventas existentes.
     */
    List<Venta> ventas()
            throws SQLException;

    // =========================================================
    // BOLETOS
    // =========================================================

    /**
     * Agrega un boleto a una venta.
     */
    void agregar(
            int venta,
            int funcion,
            int butaca
    ) throws SQLException;

    /**
     * Lista los boletos de una venta.
     */
    List<Boleto> boletos(
            int venta
    ) throws SQLException;

    /**
     * Elimina un boleto de la venta.
     */
    void quitar(
            int venta,
            int boleto
    ) throws SQLException;

    // =========================================================
    // PRODUCTOS / DULCERÍA
    // =========================================================

    /**
     * Lista los productos disponibles de dulcería.
     */
    List<Producto> listarProductosDisponibles()
            throws SQLException;

    /**
     * Lista los productos de dulcería
     * incluidos en una venta.
     */
    List<LineaVentaProducto> listarProductosVenta(
            int idVenta
    ) throws SQLException;

    /**
     * Agrega productos de dulcería a una venta.
     */
    void agregarProducto(
            int idVenta,
            int idProducto,
            int cantidad
    ) throws SQLException;

    /**
     * Cambia la cantidad de un producto
     * dentro del carrito.
     */
    void cambiarCantidad(
            int idVenta,
            int idProducto,
            int cantidad
    ) throws SQLException;

    /**
     * Retira un producto del carrito.
     */
    void quitarProducto(
            int idVenta,
            int idProducto
    ) throws SQLException;

    /**
     * Valida el stock de los productos
     * antes de confirmar la venta.
     */
    void validarStock(
            int idVenta
    ) throws SQLException;

    // =========================================================
    // CONFIRMACIÓN
    // =========================================================

    /**
     * Confirma una venta utilizando
     * el método de pago indicado.
     */
    Venta confirmar(
            int venta,
            String metodoPago
    ) throws SQLException;

    /**
     * Confirma una venta abierta.
     */
    void confirmarVenta(
            int idVenta
    ) throws SQLException;

    // =========================================================
    // CANCELACIÓN
    // =========================================================

    /**
     * Cancela una venta abierta.
     */
    void cancelar(
            int id
    ) throws SQLException;

    // =========================================================
    // FACTURA
    // =========================================================

    /**
     * Obtiene la información de factura
     * estructurada de una venta.
     */
    FacturaVenta obtenerFactura(
            int idVenta
    ) throws SQLException;

    /**
     * Obtiene las líneas de factura
     * utilizadas por el módulo de factura.
     */
    List<String> factura(
            int id
    ) throws SQLException;
}