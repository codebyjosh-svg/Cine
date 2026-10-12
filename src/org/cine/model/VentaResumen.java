package org.cine.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Encabezado y totales de vw_lista_ventas, calculados por la base de datos. */
public record VentaResumen(int idVenta, int idCliente, int idUsuario,
        String cliente, String taquillero, LocalDateTime fechaVenta, String estado,
        int cantidadBoletos, BigDecimal totalBoletos, BigDecimal totalProductos,
        BigDecimal totalVenta) {
}
