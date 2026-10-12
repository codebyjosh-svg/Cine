package org.cine.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Función de cine con los datos de película y sala.
 */
public class Funcion {

    private int idFuncion;
    private int idPelicula;
    private String tituloPelicula;
    private int duracionMinutos;

    private int idSala;
    private String nombreSala;
    private String formatoSala;

    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;

    private BigDecimal precioBoleto;
    private String estado;

    private int capacidad;
    private int boletosReservados;
    private int boletosVendidos;

    public Funcion() {
    }

    public Funcion(
            int idPelicula,
            int idSala,
            LocalDateTime fechaInicio,
            BigDecimal precioBoleto
    ) {
        this.idPelicula = idPelicula;
        this.idSala = idSala;
        this.fechaInicio = fechaInicio;
        this.precioBoleto = precioBoleto;
    }

    public int getIdFuncion() {
        return idFuncion;
    }

    public void setIdFuncion(int valor) {
        idFuncion = valor;
    }

    public int getIdPelicula() {
        return idPelicula;
    }

    public void setIdPelicula(int valor) {
        idPelicula = valor;
    }

    public String getTituloPelicula() {
        return tituloPelicula;
    }

    public void setTituloPelicula(String valor) {
        tituloPelicula = valor;
    }

    public int getDuracionMinutos() {
        return duracionMinutos;
    }

    public void setDuracionMinutos(int valor) {
        duracionMinutos = valor;
    }

    public int getIdSala() {
        return idSala;
    }

    public void setIdSala(int valor) {
        idSala = valor;
    }

    public String getNombreSala() {
        return nombreSala;
    }

    public void setNombreSala(String valor) {
        nombreSala = valor;
    }

    public String getFormatoSala() {
        return formatoSala;
    }

    public void setFormatoSala(String valor) {
        formatoSala = valor;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime valor) {
        fechaInicio = valor;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime valor) {
        fechaFin = valor;
    }

    public BigDecimal getPrecioBoleto() {
        return precioBoleto;
    }

    public void setPrecioBoleto(BigDecimal valor) {
        precioBoleto = valor;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String valor) {
        estado = valor;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int valor) {
        capacidad = valor;
    }

    public int getBoletosReservados() {
        return boletosReservados;
    }

    public void setBoletosReservados(int valor) {
        boletosReservados = valor;
    }

    public int getBoletosVendidos() {
        return boletosVendidos;
    }

    public void setBoletosVendidos(int valor) {
        boletosVendidos = valor;
    }

    public boolean isProgramada() {
        return "programada".equals(estado);
    }

    public int getBoletosActivos() {
        return boletosReservados + boletosVendidos;
    }
}