package org.cine.service;

public class AutenticacionException extends Exception {
    public enum Motivo {
        CREDENCIALES_INVALIDAS,
        USUARIO_INACTIVO,
        ERROR_DATOS
    }

    private final Motivo motivo;

    public AutenticacionException(Motivo motivo, String message) {
        super(message);
        this.motivo = motivo;
    }

    public AutenticacionException(Motivo motivo, String message, Throwable cause) {
        super(message, cause);
        this.motivo = motivo;
    }

    public Motivo getMotivo() {
        return motivo;
    }
}
