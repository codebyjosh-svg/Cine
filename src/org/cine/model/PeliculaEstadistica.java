package org.cine.model;

import java.math.BigDecimal;

/** Totales de asistencia e ingresos de boletos por película confirmada. */
public record PeliculaEstadistica(String pelicula, int boletos, BigDecimal ingresos) {}
