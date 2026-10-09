package org.cine.model;

import java.math.BigDecimal;

/** Artículo de factura: boleto o producto, con el precio guardado al vender. */
public record LineaFactura(String tipoArticulo, String descripcion,
        int cantidad, BigDecimal precioUnitario, BigDecimal subtotal) {
}
