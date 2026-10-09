package org.cine.model;
import java.math.BigDecimal;
public record Boleto(int id, int butacaId, String ubicacion, BigDecimal precio) {public String toString(){return ubicacion+" - Q"+precio;}}
