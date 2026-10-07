package org.cine.model;

import java.time.LocalDateTime;

public class Usuario {
    private int idUsuario;
    private String nombreUsuario;
    private String apellidoUsuario;
    private String username;
    private String correoElectronico;
    private String contrasenaHash;
    private int idRol;
    private String nombreRol;
    private Integer idCliente;
    private boolean estado;
    private LocalDateTime fechaRegistro;

    public Usuario() {
    }

    public Usuario(int idUsuario, String nombreUsuario, String apellidoUsuario, String username,
            String correoElectronico, String contrasenaHash, int idRol, String nombreRol,
            Integer idCliente, boolean estado, LocalDateTime fechaRegistro) {
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.apellidoUsuario = apellidoUsuario;
        this.username = username;
        this.correoElectronico = correoElectronico;
        this.contrasenaHash = contrasenaHash;
        this.idRol = idRol;
        this.nombreRol = nombreRol;
        this.idCliente = idCliente;
        this.estado = estado;
        this.fechaRegistro = fechaRegistro;
    }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
    public String getApellidoUsuario() { return apellidoUsuario; }
    public void setApellidoUsuario(String apellidoUsuario) { this.apellidoUsuario = apellidoUsuario; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getCorreoElectronico() { return correoElectronico; }
    public void setCorreoElectronico(String correoElectronico) { this.correoElectronico = correoElectronico; }
    public String getContrasenaHash() { return contrasenaHash; }
    public void setContrasenaHash(String contrasenaHash) { this.contrasenaHash = contrasenaHash; }
    public int getIdRol() { return idRol; }
    public void setIdRol(int idRol) { this.idRol = idRol; }
    public String getNombreRol() { return nombreRol; }
    public void setNombreRol(String nombreRol) { this.nombreRol = nombreRol; }
    public Integer getIdCliente() { return idCliente; }
    public void setIdCliente(Integer idCliente) { this.idCliente = idCliente; }
    public boolean isEstado() { return estado; }
    public void setEstado(boolean estado) { this.estado = estado; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public String getNombreCompleto() { return nombreUsuario + " " + apellidoUsuario; }
}
