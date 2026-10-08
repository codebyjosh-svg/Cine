package org.cine.model;

/** Datos de película necesarios para programar una función. */
public class Pelicula {
    private int idPelicula;
    private String titulo;
    private int duracionMinutos;
    private int estado;

    public int getIdPelicula() { return idPelicula; }
    public void setIdPelicula(int valor) { idPelicula = valor; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String valor) { titulo = valor; }
    public int getDuracionMinutos() { return duracionMinutos; }
    public void setDuracionMinutos(int valor) { duracionMinutos = valor; }
    public int getEstado() { return estado; }
    public void setEstado(int valor) { estado = valor; }

    @Override
    public String toString() { return titulo; }
}
