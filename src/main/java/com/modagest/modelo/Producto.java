package com.modagest.modelo;

import java.math.BigDecimal;

/**
 * Representa un producto (la referencia general de una prenda).
 * Las tallas y colores se manejan en VarianteProducto.
 */
public class Producto {

    private int idProducto;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;      // BigDecimal es el tipo correcto para dinero
    private int idCategoria;
    private String nombreCategoria; // Solo para mostrar en tablas (viene de un JOIN)

    public Producto() {
    }

    // Constructor para crear un producto nuevo (aun sin id)
    public Producto(String nombre, String descripcion, BigDecimal precio, int idCategoria) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.idCategoria = idCategoria;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public void setNombreCategoria(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }

    @Override
    public String toString() {
        return idProducto + " - " + nombre + " ($" + precio + ") [" + nombreCategoria + "]";
    }
}