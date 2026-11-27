package com.modagest.dao;

import com.modagest.modelo.Producto;
import com.modagest.util.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de la tabla Producto.
 * Implementa el CRUD: Crear (insertar), Leer (listar/buscar),
 * Actualizar y Eliminar.
 *
 * Los signos "?" de las consultas son parametros: se llenan con
 * PreparedStatement, lo que evita ataques de inyeccion SQL.
 */
public class ProductoDAO {

    private static final String SQL_INSERTAR =
            "INSERT INTO Producto (nombre, descripcion, precio, idCategoria) VALUES (?, ?, ?, ?)";

    private static final String SQL_LISTAR =
            "SELECT p.idProducto, p.nombre, p.descripcion, p.precio, p.idCategoria, "
            + "c.nombre AS nombreCategoria "
            + "FROM Producto p JOIN Categoria c ON c.idCategoria = p.idCategoria "
            + "ORDER BY p.nombre";

    private static final String SQL_BUSCAR_POR_ID =
            "SELECT p.idProducto, p.nombre, p.descripcion, p.precio, p.idCategoria, "
            + "c.nombre AS nombreCategoria "
            + "FROM Producto p JOIN Categoria c ON c.idCategoria = p.idCategoria "
            + "WHERE p.idProducto = ?";

    private static final String SQL_ACTUALIZAR =
            "UPDATE Producto SET nombre = ?, descripcion = ?, precio = ?, idCategoria = ? "
            + "WHERE idProducto = ?";

    private static final String SQL_ELIMINAR =
            "DELETE FROM Producto WHERE idProducto = ?";

    // ------------------------------------------------------------------
    // C - CREATE: inserta un producto nuevo
    // ------------------------------------------------------------------
    /**
     * Inserta el producto y le asigna el id que genero MySQL.
     *
     * @return true si se inserto correctamente
     */
    public boolean insertar(Producto producto) throws SQLException {
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(
                     SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {

            sentencia.setString(1, producto.getNombre());
            sentencia.setString(2, producto.getDescripcion());
            sentencia.setBigDecimal(3, producto.getPrecio());
            sentencia.setInt(4, producto.getIdCategoria());

            int filasAfectadas = sentencia.executeUpdate();

            // Recupera el id autogenerado y lo guarda en el objeto
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) {
                    producto.setIdProducto(claves.getInt(1));
                }
            }
            return filasAfectadas > 0;
        }
    }

    // ------------------------------------------------------------------
    // R - READ: listar todos y buscar uno por id
    // ------------------------------------------------------------------
    /**
     * Devuelve todos los productos con el nombre de su categoria.
     */
    public List<Producto> listar() throws SQLException {
        List<Producto> productos = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_LISTAR);
             ResultSet resultado = sentencia.executeQuery()) {

            while (resultado.next()) {
                productos.add(convertirFila(resultado));
            }
        }
        return productos;
    }

    /**
     * Busca un producto por su id.
     *
     * @return el producto, o null si no existe
     */
    public Producto buscarPorId(int idProducto) throws SQLException {
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_BUSCAR_POR_ID)) {

            sentencia.setInt(1, idProducto);

            try (ResultSet resultado = sentencia.executeQuery()) {
                if (resultado.next()) {
                    return convertirFila(resultado);
                }
            }
        }
        return null;
    }

    // ------------------------------------------------------------------
    // U - UPDATE: modifica un producto existente
    // ------------------------------------------------------------------
    /**
     * Actualiza los datos del producto identificado por su idProducto.
     *
     * @return true si se actualizo alguna fila
     */
    public boolean actualizar(Producto producto) throws SQLException {
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_ACTUALIZAR)) {

            sentencia.setString(1, producto.getNombre());
            sentencia.setString(2, producto.getDescripcion());
            sentencia.setBigDecimal(3, producto.getPrecio());
            sentencia.setInt(4, producto.getIdCategoria());
            sentencia.setInt(5, producto.getIdProducto());

            return sentencia.executeUpdate() > 0;
        }
    }

    // ------------------------------------------------------------------
    // D - DELETE: elimina un producto
    // ------------------------------------------------------------------
    /**
     * Elimina el producto. Sus variantes se borran en cascada, salvo que
     * alguna ya haya sido vendida: en ese caso MySQL lanza SQLException.
     *
     * @return true si se elimino alguna fila
     */
    public boolean eliminar(int idProducto) throws SQLException {
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_ELIMINAR)) {

            sentencia.setInt(1, idProducto);
            return sentencia.executeUpdate() > 0;
        }
    }

    // ------------------------------------------------------------------
    // Metodo auxiliar: convierte una fila del ResultSet en un objeto Producto
    // ------------------------------------------------------------------
    private Producto convertirFila(ResultSet resultado) throws SQLException {
        Producto producto = new Producto();
        producto.setIdProducto(resultado.getInt("idProducto"));
        producto.setNombre(resultado.getString("nombre"));
        producto.setDescripcion(resultado.getString("descripcion"));
        producto.setPrecio(resultado.getBigDecimal("precio"));
        producto.setIdCategoria(resultado.getInt("idCategoria"));
        producto.setNombreCategoria(resultado.getString("nombreCategoria"));
        return producto;
    }
}