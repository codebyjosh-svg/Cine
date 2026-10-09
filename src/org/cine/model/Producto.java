package org.cine.model;

/** Opción del formulario de inventario y stock consultado del catálogo. */
public class Producto {
    private int idProducto;
    private String nombreProducto;
    private int stock;
    private int estado;

    public Producto() {
    }

    public Producto(int idProducto, String nombreProducto, int stock, int estado) {
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.stock = stock;
        this.estado = estado;
    }

    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int valor) { idProducto = valor; }
    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String valor) { nombreProducto = valor; }
    public int getStock() { return stock; }
    public void setStock(int valor) { stock = valor; }
    public int getEstado() { return estado; }
    public void setEstado(int valor) { estado = valor; }

    @Override
    public String toString() { return nombreProducto; }
}
