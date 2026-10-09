package org.cine.model;

import java.math.BigDecimal;

/**
 * Producto de dulcería.
 *
 * Permite administrar los datos del producto y consultar
 * sus existencias y estado para las ventas.
 */
public class Producto {

    private int idProducto;
    private int idCategoriaProducto;
    private String nombreCategoria;
    private String nombreProducto;
    private String descripcion;
    private BigDecimal precio;
    private int stock;
    private int stockMinimo;
    private int estado;

    // ==========================================
    // CONSTRUCTOR VACÍO
    // ==========================================

    public Producto() {
        this.precio = BigDecimal.ZERO;
        this.stock = 0;
        this.stockMinimo = 5;
        this.estado = 1;
    }

    // ==========================================
    // CONSTRUCTOR PARA CRUD
    // ==========================================

    public Producto(
            int idProducto,
            int idCategoriaProducto,
            String nombreProducto,
            String descripcion,
            BigDecimal precio,
            int stock,
            int stockMinimo,
            int estado) {

        this.idProducto = idProducto;
        this.idCategoriaProducto = idCategoriaProducto;
        this.nombreProducto = nombreProducto;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stock = stock;
        this.stockMinimo = stockMinimo;
        this.estado = estado;
    }

    // ==========================================
    // CONSTRUCTOR CON CATEGORÍA
    // ==========================================

    public Producto(
            int idProducto,
            int idCategoriaProducto,
            String nombreCategoria,
            String nombreProducto,
            String descripcion,
            BigDecimal precio,
            int stock,
            int stockMinimo,
            int estado) {

        this(
                idProducto,
                idCategoriaProducto,
                nombreProducto,
                descripcion,
                precio,
                stock,
                stockMinimo,
                estado
        );

        this.nombreCategoria = nombreCategoria;
    }

    // ==========================================
    // CONSTRUCTOR PARA VENTA / DULCERÍA
    // ==========================================

    public Producto(
            int idProducto,
            String nombreProducto,
            String nombreCategoria,
            BigDecimal precio,
            int stock,
            int stockMinimo,
            boolean estado) {

        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.nombreCategoria = nombreCategoria;
        this.precio = precio;
        this.stock = stock;
        this.stockMinimo = stockMinimo;
        this.estado = estado ? 1 : 0;
    }

    // ==========================================
    // GETTERS Y SETTERS
    // ==========================================

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public int getIdCategoriaProducto() {
        return idCategoriaProducto;
    }

    public void setIdCategoriaProducto(int idCategoriaProducto) {
        this.idCategoriaProducto = idCategoriaProducto;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public void setNombreCategoria(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    // ==========================================
    // ESTADO
    // ==========================================

    public boolean isEstado() {
        return estado == 1;
    }

    // ==========================================
    // STOCK CRÍTICO
    // ==========================================

    public boolean isStockCritico() {
        return isEstado() && stock <= stockMinimo;
    }

    // ==========================================
    // TEXTO
    // ==========================================

    @Override
    public String toString() {
        return nombreProducto;
    }
}