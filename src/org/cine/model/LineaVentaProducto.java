package org.cine.model;

import java.math.BigDecimal;

/** Una sola línea por producto y venta; la cantidad puede incrementarse. */
public final class LineaVentaProducto {
    private final int idProducto;
    private final String nombreProducto;
    private final int cantidad;
    private final BigDecimal precioUnitario;

    public LineaVentaProducto(int idProducto, String nombreProducto,
            int cantidad, BigDecimal precioUnitario) {
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public int getIdProducto() { return idProducto; }
    public String getNombreProducto() { return nombreProducto; }
    public int getCantidad() { return cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public BigDecimal getSubtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }
}
