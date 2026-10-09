package org.cine.model;
import java.math.BigDecimal;
public record Venta(int id, String cliente, String estado, BigDecimal total) {public String toString(){return "#"+id+" - "+cliente+" Q"+total;}}
