package org.cine.model;
import java.math.BigDecimal;
/** Indicadores administrativos obtenidos del procedimiento sp_indicadoresadmin. */
public record IndicadoresAdmin(int peliculasActivas, int clientesActivos, int usuariosActivos,
 int funcionesPendientes, int stockCritico, int ventasConfirmadas, BigDecimal ventasTotales) {}
