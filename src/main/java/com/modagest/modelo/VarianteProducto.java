package com.modagest.modelo;

/**
 * Representa una variante de producto: una combinacion de talla y color
 * con su propio codigo de barras, junto con su inventario.
 */
public class VarianteProducto {

    private int idVariante;
    private int idProducto;
    private String talla;
    private String color;
    private String codigoBarras;

    // Datos de apoyo que vienen de otras tablas (JOIN), para mostrar en pantalla
    private String nombreProducto;
    private int cantidad;
    private int stockMinimo;
    private String ubicacion;

    public VarianteProducto() {
    }

    // Constructor para crear una variante nueva (aun sin id)
    public VarianteProducto(int idProducto, String talla, String color, String codigoBarras) {
        this.idProducto = idProducto;
        this.talla = talla;
        this.color = color;
        this.codigoBarras = codigoBarras;
    }

    public int getIdVariante() {
        return idVariante;
    }

    public void setIdVariante(int idVariante) {
        this.idVariante = idVariante;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getTalla() {
        return talla;
    }

    public void setTalla(String talla) {
        this.talla = talla;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    /**
     * Estado del articulo segun su stock (se calcula, no se guarda):
     * Agotado, Requerido Pedido (stock bajo) o Disponible.
     */
    public String getEstado() {
        if (cantidad == 0) {
            return "Agotado";
        }
        if (cantidad < stockMinimo) {
            return "Requerido Pedido";
        }
        return "Disponible";
    }

    @Override
    public String toString() {
        return nombreProducto + " | " + talla + " | " + color + " | " + codigoBarras
                + " | stock: " + cantidad + " (min " + stockMinimo + ") | " + getEstado();
    }
}