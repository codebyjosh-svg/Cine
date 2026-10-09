package org.cine.model;
import java.math.BigDecimal;
import java.time.LocalDateTime;
/** Registro de una venta confirmada para reportes. */
public class Reporte {
 private final int idVenta, boletos;
 private final LocalDateTime fecha;
 private final String cliente, taquillero;
 private final BigDecimal total;
 public Reporte(int idVenta, LocalDateTime fecha, String cliente, String taquillero, int boletos, BigDecimal total) {
  this.idVenta=idVenta; this.fecha=fecha; this.cliente=cliente; this.taquillero=taquillero; this.boletos=boletos; this.total=total;
 }
 public int getIdVenta(){return idVenta;} public LocalDateTime getFecha(){return fecha;}
 public String getCliente(){return cliente;} public String getTaquillero(){return taquillero;}
 public int getBoletos(){return boletos;} public BigDecimal getTotal(){return total;}
}
