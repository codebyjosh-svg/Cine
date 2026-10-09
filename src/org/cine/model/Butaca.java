package org.cine.model;

public class Butaca {

    private int idButaca;
    private int idSala;
    private String nombreSala;
    private String fila;
    private int numero;
    private int estado;

    public Butaca() {
        this.estado = 1;
    }

    public Butaca(int idButaca, int idSala, String fila, int numero,
            int estado) {
        this.idButaca = idButaca;
        this.idSala = idSala;
        this.fila = fila;
        this.numero = numero;
        this.estado = estado;
    }

    public Butaca(int idButaca, int idSala, String nombreSala, String fila,
            int numero, int estado) {
        this(idButaca, idSala, fila, numero, estado);
        this.nombreSala = nombreSala;
    }

    public int getIdButaca() {
        return idButaca;
    }

    public void setIdButaca(int idButaca) {
        this.idButaca = idButaca;
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

    public String getFila() {
        return fila;
    }

    public void setFila(String fila) {
        this.fila = fila;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    public String getUbicacion() {
        return fila + numero;
    }

    @Override
    public String toString() {
        return getUbicacion();
    }
}
