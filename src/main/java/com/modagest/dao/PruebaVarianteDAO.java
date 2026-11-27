package com.modagest.dao;

import com.modagest.modelo.VarianteProducto;
import java.sql.SQLException;
import java.util.List;

/**
 * Prueba por consola del modulo de variantes e inventario:
 * CRUD de variantes, entrada de mercancia y alerta de stock minimo.
 */
public class PruebaVarianteDAO {

    private static final String CODIGO_PRUEBA = "7701000000014";

    public static void main(String[] args) {
        VarianteDAO varianteDAO = new VarianteDAO();
        InventarioDAO inventarioDAO = new InventarioDAO();

        try {
            // Limpieza: si una ejecucion anterior fallo a medias, borra la variante de prueba
            VarianteProducto sobrante = varianteDAO.buscarPorCodigoBarras(CODIGO_PRUEBA);
            if (sobrante != null) {
                varianteDAO.eliminar(sobrante.getIdVariante());
            }

            // R - Todas las variantes con su estado
            System.out.println("=== VARIANTES (stock por talla y color) ===");
            imprimir(varianteDAO.listar());

            System.out.println("\n=== ALERTAS: stock bajo (antes) ===");
            imprimir(varianteDAO.listarStockBajo());

            // C - Nueva variante de la Camiseta (producto 1) con solo 3 unidades
            System.out.println("\n=== INSERTAR variante (stock 3, minimo 10) ===");
            VarianteProducto nueva = new VarianteProducto(1, "XL", "Negro", CODIGO_PRUEBA);
            varianteDAO.insertar(nueva, 3, 10, "Bodega");
            System.out.println("Variante creada con id: " + nueva.getIdVariante());

            System.out.println("\n=== ALERTAS: stock bajo (debe incluir la nueva) ===");
            imprimir(varianteDAO.listarStockBajo());

            // R - Buscar por codigo de barras (lo que hara la caja al escanear)
            System.out.println("\n=== BUSCAR POR CODIGO DE BARRAS ===");
            System.out.println(varianteDAO.buscarPorCodigoBarras(CODIGO_PRUEBA));

            // Entrada de mercancia: llegan 20 unidades del proveedor 1, recibe el usuario 3 (Bodega)
            System.out.println("\n=== REGISTRAR ENTRADA de 20 unidades ===");
            inventarioDAO.registrarEntrada(nueva.getIdVariante(), 20, 1, 3);
            System.out.println(varianteDAO.buscarPorCodigoBarras(CODIGO_PRUEBA));

            System.out.println("\n=== ALERTAS: stock bajo (la nueva ya no debe aparecer) ===");
            imprimir(varianteDAO.listarStockBajo());

            // U - Cambia el color y sube el stock minimo a 25 (23 < 25: vuelve la alerta)
            System.out.println("\n=== ACTUALIZAR color y stock minimo ===");
            VarianteProducto editar = varianteDAO.buscarPorCodigoBarras(CODIGO_PRUEBA);
            editar.setColor("Gris");
            editar.setStockMinimo(25);
            System.out.println("Actualizado: " + varianteDAO.actualizar(editar));
            System.out.println(varianteDAO.buscarPorCodigoBarras(CODIGO_PRUEBA));

            // D - Elimina la variante de prueba
            System.out.println("\n=== ELIMINAR ===");
            System.out.println("Eliminado: " + varianteDAO.eliminar(nueva.getIdVariante()));

            System.out.println("\n=== ALERTAS: stock bajo (despues, igual que al inicio) ===");
            imprimir(varianteDAO.listarStockBajo());

        } catch (SQLException e) {
            System.out.println("Error de base de datos: " + e.getMessage());
        }
    }

    private static void imprimir(List<VarianteProducto> variantes) {
        for (VarianteProducto variante : variantes) {
            System.out.println(variante);
        }
    }
}