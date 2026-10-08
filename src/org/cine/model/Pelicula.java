package org.cine.model;

import java.sql.Date;

public class Pelicula {

    private int idPelicula;
    private String titulo;
    private String sinopsis;
    private String director;
    private int duracionMinutos;
    private String clasificacion;
    private String idioma;
    private Date fechaEstreno;
    private int idGenero;
    private int estado;

    public Pelicula() {
    }

    public Pelicula(int idPelicula, String titulo, String sinopsis,
            String director, int duracionMinutos, String clasificacion,
            String idioma, Date fechaEstreno, int idGenero, int estado) {

        this.idPelicula = idPelicula;
        this.titulo = titulo;
        this.sinopsis = sinopsis;
        this.director = director;
        this.duracionMinutos = duracionMinutos;
        this.clasificacion = clasificacion;
        this.idioma = idioma;
        this.fechaEstreno = fechaEstreno;
        this.idGenero = idGenero;
        this.estado = estado;
    }

    public int getIdPelicula() {
        return idPelicula;
    }

    public void setIdPelicula(int idPelicula) {
        this.idPelicula = idPelicula;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getSinopsis() {
        return sinopsis;
    }

    public void setSinopsis(String sinopsis) {
        this.sinopsis = sinopsis;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public int getDuracionMinutos() {
        return duracionMinutos;
    }

    public void setDuracionMinutos(int duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
    }

    public String getClasificacion() {
        return clasificacion;
    }

    public void setClasificacion(String clasificacion) {
        this.clasificacion = clasificacion;
    }

    public String getIdioma() {
        return idioma;
    }

    public void setIdioma(String idioma) {
        this.idioma = idioma;
    }

    public Date getFechaEstreno() {
        return fechaEstreno;
    }

    public void setFechaEstreno(Date fechaEstreno) {
        this.fechaEstreno = fechaEstreno;
    }

    public int getIdGenero() {
        return idGenero;
    }

    public void setIdGenero(int idGenero) {
        this.idGenero = idGenero;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return titulo;
    }
}