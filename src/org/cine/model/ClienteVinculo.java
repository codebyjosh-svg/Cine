package org.cine.model;

public class ClienteVinculo {
    private final int idCliente;
    private final String nombreCompleto;
    private final String correoElectronico;
    private final boolean estado;

    public ClienteVinculo(int idCliente, String nombreCompleto, String correoElectronico, boolean estado) {
        this.idCliente = idCliente;
        this.nombreCompleto = nombreCompleto;
        this.correoElectronico = correoElectronico;
        this.estado = estado;
    }

    public int getIdCliente() {
        return idCliente;
    }
    public String getNombreCompleto() {
        return nombreCompleto;
    }
    public String getCorreoElectronico() {
        return correoElectronico;
    }
    public boolean isEstado() {
        return estado;
    }

    @Override
    public String toString() {
        return idCliente + " - " + nombreCompleto + (estado ? "" : " (inactivo)");
    }
}
