package org.cine.model;

import java.math.BigDecimal;

/** DTO de vw_stock_critico: incluye también el caso stock == stock_minimo. */
public final class StockCritico extends Producto {
    public StockCritico(int idProducto, String nombreProducto, String nombreCategoria,
            BigDecimal precio, int stock, int stockMinimo) {
        super(idProducto, nombreProducto, nombreCategoria, precio, stock, stockMinimo, true);
    }

    public String getNivel() {
        if (getStock() == 0) { return "Sin existencias"; }
        return getStock() < getStockMinimo() ? "Bajo mínimo" : "En el mínimo";
    }
}
