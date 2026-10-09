package org.cine.model;

import java.math.BigDecimal;

/** Producto de dulcería consultado con su categoría y existencias actuales. */
public class Producto {
    private final int idProducto;
    private final String nombreProducto;
    private final String nombreCategoria;
    private final BigDecimal precio;
    private final int stock;
    private final int stockMinimo;
    private final boolean estado;

    public Producto(int idProducto, String nombreProducto, String nombreCategoria,
            BigDecimal precio, int stock, int stockMinimo, boolean estado) {
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.nombreCategoria = nombreCategoria;
        this.precio = precio;
        this.stock = stock;
        this.stockMinimo = stockMinimo;
        this.estado = estado;
    }

    public int getIdProducto() { return idProducto; }
    public String getNombreProducto() { return nombreProducto; }
    public String getNombreCategoria() { return nombreCategoria; }
    public BigDecimal getPrecio() { return precio; }
    public int getStock() { return stock; }
    public int getStockMinimo() { return stockMinimo; }
    public boolean isEstado() { return estado; }
    public boolean isStockCritico() { return estado && stock <= stockMinimo; }
}
