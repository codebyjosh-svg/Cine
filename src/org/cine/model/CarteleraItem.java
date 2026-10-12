package org.cine.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO utilizado para representar un elemento de la cartelera.
 *
 * Contiene la información de una película programada en una sala determinada.
 *
 * @author Joshua
 */
public class CarteleraItem {

    private int idFuncion;
    private int idPelicula;
    private String tituloPelicula;
    private String genero;
    private String clasificacion;

    private int idSala;
    private String nombreSala;

    private LocalDate fecha;
    private LocalTime hora;

    private BigDecimal precio;
    private int asientosDisponibles;
    private String estado;

    public CarteleraItem() {
    }

    public CarteleraItem(
            int idFuncion,
            int idPelicula,
            String tituloPelicula,
            String genero,
            String clasificacion,
            int idSala,
            String nombreSala,
            LocalDate fecha,
            LocalTime hora,
            BigDecimal precio,
            int asientosDisponibles) {

        this.idFuncion = idFuncion;
        this.idPelicula = idPelicula;
        this.tituloPelicula = tituloPelicula;
        this.genero = genero;
        this.clasificacion = clasificacion;
        this.idSala = idSala;
        this.nombreSala = nombreSala;
        this.fecha = fecha;
        this.hora = hora;
        this.precio = precio;
        this.asientosDisponibles = asientosDisponibles;
    }

    public int getIdFuncion() {
        return idFuncion;
    }

    public void setIdFuncion(int idFuncion) {
        this.idFuncion = idFuncion;
    }

    public int getIdPelicula() {
        return idPelicula;
    }

    public void setIdPelicula(int idPelicula) {
        this.idPelicula = idPelicula;
    }

    public String getTituloPelicula() {
        return tituloPelicula;
    }

    public void setTituloPelicula(String tituloPelicula) {
        this.tituloPelicula = tituloPelicula;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getClasificacion() {
        return clasificacion;
    }

    public void setClasificacion(String clasificacion) {
        this.clasificacion = clasificacion;
    }

    public int getIdSala() {
        return idSala;
    }

    public void setIdSala(int idSala) {
        this.idSala = idSala;
    }

    public String getNombreSala() {
        return nombreSala;
    }

    public void setNombreSala(String nombreSala) {
        this.nombreSala = nombreSala;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public int getAsientosDisponibles() {
        return asientosDisponibles;
    }

    public void setAsientosDisponibles(int asientosDisponibles) {
        this.asientosDisponibles = asientosDisponibles;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "CarteleraItem{"
                + "idFuncion=" + idFuncion
                + ", tituloPelicula='" + tituloPelicula + '\''
                + ", nombreSala='" + nombreSala + '\''
                + ", fecha=" + fecha
                + ", hora=" + hora
                + ", precio=" + precio
                + ", asientosDisponibles=" + asientosDisponibles
                + '}';
    }
}
