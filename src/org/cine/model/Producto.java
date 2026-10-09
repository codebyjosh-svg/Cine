package org.cine.model;
import java.math.BigDecimal;
public record Producto(int id,int categoria,String nombre,String descripcion,BigDecimal precio,int stock,int minimo,boolean activo) {}
