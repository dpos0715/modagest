package com.modagest.dao;

import com.modagest.modelo.Categoria;
import com.modagest.modelo.Producto;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Prueba por consola del CRUD de productos.
 * Ejecuta las cuatro operaciones en orden y muestra el resultado de cada una.
 */
public class PruebaProductoDAO {

    public static void main(String[] args) {
        ProductoDAO productoDAO = new ProductoDAO();
        CategoriaDAO categoriaDAO = new CategoriaDAO();

        try {
            // Categorias disponibles
            System.out.println("=== CATEGORIAS ===");
            List<Categoria> categorias = categoriaDAO.listar();
            for (Categoria categoria : categorias) {
                System.out.println(categoria.getIdCategoria() + " - " + categoria.getNombre());
            }

            // R - Listar los productos que ya existen
            System.out.println("\n=== LISTAR (antes) ===");
            imprimirProductos(productoDAO.listar());

            // C - Insertar un producto nuevo (categoria 4 = Accesorios)
            System.out.println("\n=== INSERTAR ===");
            Producto nuevo = new Producto(
                    "Gorra Deportiva", "Gorra ajustable", new BigDecimal("35000"), 4);
            productoDAO.insertar(nuevo);
            System.out.println("Producto insertado con id: " + nuevo.getIdProducto());

            // R - Buscar el producto recien creado
            System.out.println("\n=== BUSCAR POR ID ===");
            Producto encontrado = productoDAO.buscarPorId(nuevo.getIdProducto());
            System.out.println(encontrado);

            // U - Actualizar su nombre y precio
            System.out.println("\n=== ACTUALIZAR ===");
            encontrado.setNombre("Gorra Deportiva Pro");
            encontrado.setPrecio(new BigDecimal("42000"));
            boolean actualizado = productoDAO.actualizar(encontrado);
            System.out.println("Actualizado: " + actualizado);
            System.out.println(productoDAO.buscarPorId(encontrado.getIdProducto()));

            // D - Eliminar el producto de prueba
            System.out.println("\n=== ELIMINAR ===");
            boolean eliminado = productoDAO.eliminar(encontrado.getIdProducto());
            System.out.println("Eliminado: " + eliminado);

            // Listado final: debe quedar igual que al principio
            System.out.println("\n=== LISTAR (despues) ===");
            imprimirProductos(productoDAO.listar());

        } catch (SQLException e) {
            System.out.println("Error de base de datos: " + e.getMessage());
        }
    }

    private static void imprimirProductos(List<Producto> productos) {
        for (Producto producto : productos) {
            System.out.println(producto);
        }
    }
}