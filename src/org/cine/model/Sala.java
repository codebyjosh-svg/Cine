package org.cine.model;

public class Sala {

    private int idSala;
    private String nombreSala;
    private String formato;
    private int estado;

    public Sala() {
        this.estado = 1;
    }

    public Sala(int idSala, String nombreSala, String formato, int estado) {
        this.idSala = idSala;
        this.nombreSala = nombreSala;
        this.formato = formato;
        this.estado = estado;
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

    public String getFormato() {
        return formato;
    }

    public void setFormato(String formato) {
        this.formato = formato;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return nombreSala;
    }
}
