package com.modagest.modelo;

/**
 * Representa una categoria de productos (Camisas, Pantalones, etc.).
 */
public class Categoria {

    private int idCategoria;
    private String nombre;

    public Categoria() {
    }

    public Categoria(int idCategoria, String nombre) {
        this.idCategoria = idCategoria;
        this.nombre = nombre;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    // Lo usara la lista desplegable de la interfaz para mostrar el nombre
    @Override
    public String toString() {
        return nombre;
    }
}