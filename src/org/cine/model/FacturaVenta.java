package org.cine.model;

import java.util.List;

/** Factura inmutable de una venta confirmada. */
public record FacturaVenta(VentaResumen venta, List<LineaFactura> lineas) {
    public FacturaVenta {
        lineas = List.copyOf(lineas);
    }
}
