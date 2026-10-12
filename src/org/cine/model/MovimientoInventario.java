package org.cine.model;

import java.time.LocalDateTime;

/** Movimiento del historial de confitería; el servidor asigna ID y fecha. */
public class MovimientoInventario {
    private int idMovimiento;
    private int idProducto;
    private String nombreProducto;
    private int idUsuario;
    private String usuario;
    private Integer idVenta;
    private String tipoMovimiento;
    private int cantidad;
    private LocalDateTime fechaMovimiento;
    private String observacion;

    public MovimientoInventario() {
    }

    public MovimientoInventario(int idProducto, int idUsuario, String tipoMovimiento,
            int cantidad, String observacion) {
        this.idProducto = idProducto;
        this.idUsuario = idUsuario;
        this.tipoMovimiento = tipoMovimiento;
        this.cantidad = cantidad;
        this.observacion = observacion;
    }

    public int getIdMovimiento() { return idMovimiento; }
    public void setIdMovimiento(int valor) { idMovimiento = valor; }
    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int valor) { idProducto = valor; }
    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String valor) { nombreProducto = valor; }
    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int valor) { idUsuario = valor; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String valor) { usuario = valor; }
    public Integer getIdVenta() { return idVenta; }
    public void setIdVenta(Integer valor) { idVenta = valor; }
    public String getTipoMovimiento() { return tipoMovimiento; }
    public void setTipoMovimiento(String valor) { tipoMovimiento = valor; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int valor) { cantidad = valor; }
    public LocalDateTime getFechaMovimiento() { return fechaMovimiento; }
    public void setFechaMovimiento(LocalDateTime valor) { fechaMovimiento = valor; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String valor) { observacion = valor; }
}
