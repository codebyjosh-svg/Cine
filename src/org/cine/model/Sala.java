package org.cine.model;

/** Sala utilizada como opción de programación, con su estado y formato. */
public class Sala {
    private int idSala;
    private String nombreSala;
    private String formato;
    private int estado;

    public Sala() {
    }

    public Sala(int idSala, String nombreSala, String formato, int estado) {
        this.idSala = idSala;
        this.nombreSala = nombreSala;
        this.formato = formato;
        this.estado = estado;
    }

    public int getIdSala() { return idSala; }
    public void setIdSala(int valor) { idSala = valor; }
    public String getNombreSala() { return nombreSala; }
    public void setNombreSala(String valor) { nombreSala = valor; }
    public String getFormato() { return formato; }
    public void setFormato(String valor) { formato = valor; }
    public int getEstado() { return estado; }
    public void setEstado(int valor) { estado = valor; }

    @Override
    public String toString() { return nombreSala + " · " + formato; }
}
