package com.modagest.modelo;

/**
 * Representa al usuario que inicio sesion (sin la contrasena).
 */
public class Usuario {

    private final int idUsuario;
    private final String nombre;
    private final String apellido;
    private final String correo;
    private final String nombreRol;

    public Usuario(int idUsuario, String nombre, String apellido, String correo, String nombreRol) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.nombreRol = nombreRol;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getCorreo() {
        return correo;
    }

    public String getNombreRol() {
        return nombreRol;
    }

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    public boolean esAdministrador() {
        return "Administrador".equals(nombreRol);
    }

    public boolean esBodega() {
        return "Bodega".equals(nombreRol);
    }
}