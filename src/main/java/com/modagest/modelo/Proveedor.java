package com.modagest.modelo;

/**
 * Representa un proveedor de mercancia.
 */
public class Proveedor {

    private final int idProveedor;
    private final String nombre;

    public Proveedor(int idProveedor, String nombre) {
        this.idProveedor = idProveedor;
        this.nombre = nombre;
    }

    public int getIdProveedor() {
        return idProveedor;
    }

    public String getNombre() {
        return nombre;
    }

    // Lo usan las listas desplegables para mostrar el nombre
    @Override
    public String toString() {
        return nombre;
    }
}