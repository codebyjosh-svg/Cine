package org.cine.model;
import java.time.LocalDateTime;
public record MovimientoInventario(int id,int producto,int usuario,String nombreProducto,String nombreUsuario,String tipo,int cantidad,LocalDateTime fecha,String observacion) {}
