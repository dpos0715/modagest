package com.modagest.dao;

import com.modagest.modelo.VarianteProducto;
import com.modagest.util.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de las variantes de producto (talla + color + codigo de barras)
 * junto con su inventario. Implementa el CRUD completo.
 */
public class VarianteDAO {

    // Consulta base: une variante, producto e inventario
    private static final String SQL_SELECT_BASE =
            "SELECT v.idVariante, v.idProducto, v.talla, v.color, v.codigoBarras, "
            + "p.nombre AS nombreProducto, i.cantidad, i.stockMinimo, i.ubicacion "
            + "FROM VarianteProducto v "
            + "JOIN Producto p ON p.idProducto = v.idProducto "
            + "JOIN Inventario i ON i.idVariante = v.idVariante ";

    private static final String SQL_LISTAR =
            SQL_SELECT_BASE + "ORDER BY p.nombre, v.color, v.talla";

    private static final String SQL_LISTAR_POR_PRODUCTO =
            SQL_SELECT_BASE + "WHERE v.idProducto = ? ORDER BY v.color, v.talla";

    private static final String SQL_BUSCAR_POR_CODIGO =
            SQL_SELECT_BASE + "WHERE v.codigoBarras = ?";

    private static final String SQL_LISTAR_STOCK_BAJO =
            SQL_SELECT_BASE + "WHERE i.cantidad < i.stockMinimo ORDER BY i.cantidad";

    private static final String SQL_INSERTAR_VARIANTE =
            "INSERT INTO VarianteProducto (idProducto, talla, color, codigoBarras) VALUES (?, ?, ?, ?)";

    private static final String SQL_INSERTAR_INVENTARIO =
            "INSERT INTO Inventario (idVariante, cantidad, stockMinimo, ubicacion) VALUES (?, ?, ?, ?)";

    private static final String SQL_ACTUALIZAR_VARIANTE =
            "UPDATE VarianteProducto SET talla = ?, color = ?, codigoBarras = ? WHERE idVariante = ?";

    private static final String SQL_ACTUALIZAR_INVENTARIO =
            "UPDATE Inventario SET stockMinimo = ?, ubicacion = ? WHERE idVariante = ?";

    private static final String SQL_ELIMINAR =
            "DELETE FROM VarianteProducto WHERE idVariante = ?";

    // ------------------------------------------------------------------
    // C - CREATE: crea la variante y su inventario en una transaccion
    // ------------------------------------------------------------------
    /**
     * Inserta la variante y su registro de inventario inicial.
     * Si el codigo de barras ya existe, MySQL lanza SQLException y no se guarda nada.
     *
     * @param cantidadInicial unidades con las que empieza en inventario
     * @param stockMinimo     umbral para la alerta de stock bajo
     */
    public boolean insertar(VarianteProducto variante, int cantidadInicial,
                            int stockMinimo, String ubicacion) throws SQLException {
        Connection conexion = null;
        try {
            conexion = ConexionBD.obtenerConexion();
            conexion.setAutoCommit(false);

            // 1. Guarda la variante y recupera su id
            try (PreparedStatement sentencia = conexion.prepareStatement(
                    SQL_INSERTAR_VARIANTE, Statement.RETURN_GENERATED_KEYS)) {
                sentencia.setInt(1, variante.getIdProducto());
                sentencia.setString(2, variante.getTalla());
                sentencia.setString(3, variante.getColor());
                sentencia.setString(4, variante.getCodigoBarras());
                sentencia.executeUpdate();

                try (ResultSet claves = sentencia.getGeneratedKeys()) {
                    if (claves.next()) {
                        variante.setIdVariante(claves.getInt(1));
                    }
                }
            }

            // 2. Crea su inventario
            try (PreparedStatement sentencia = conexion.prepareStatement(SQL_INSERTAR_INVENTARIO)) {
                sentencia.setInt(1, variante.getIdVariante());
                sentencia.setInt(2, cantidadInicial);
                sentencia.setInt(3, stockMinimo);
                sentencia.setString(4, ubicacion);
                sentencia.executeUpdate();
            }

            // 3. Si nace con stock bajo, genera la alerta
            InventarioDAO.sincronizarAlerta(conexion, variante.getIdVariante());

            conexion.commit();
            return true;

        } catch (SQLException e) {
            if (conexion != null) {
                conexion.rollback();
            }
            throw e;
        } finally {
            if (conexion != null) {
                conexion.setAutoCommit(true);
                conexion.close();
            }
        }
    }

    // ------------------------------------------------------------------
    // R - READ: listar, filtrar, buscar por codigo y ver stock bajo
    // ------------------------------------------------------------------
    /** Todas las variantes con su stock. */
    public List<VarianteProducto> listar() throws SQLException {
        return consultarLista(SQL_LISTAR, null);
    }

    /** Variantes de un producto (RF05: stock desagregado por talla y color). */
    public List<VarianteProducto> listarPorProducto(int idProducto) throws SQLException {
        return consultarLista(SQL_LISTAR_POR_PRODUCTO, idProducto);
    }

    /** Variantes con stock por debajo del minimo (tablero de alertas, HU-004). */
    public List<VarianteProducto> listarStockBajo() throws SQLException {
        return consultarLista(SQL_LISTAR_STOCK_BAJO, null);
    }

    /**
     * Busca una variante por su codigo de barras (lo usara la caja al escanear).
     *
     * @return la variante, o null si no existe
     */
    public VarianteProducto buscarPorCodigoBarras(String codigoBarras) throws SQLException {
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_BUSCAR_POR_CODIGO)) {

            sentencia.setString(1, codigoBarras);

            try (ResultSet resultado = sentencia.executeQuery()) {
                if (resultado.next()) {
                    return convertirFila(resultado);
                }
            }
        }
        return null;
    }

    // ------------------------------------------------------------------
    // U - UPDATE: modifica talla, color, codigo, stock minimo y ubicacion
    // ------------------------------------------------------------------
    /**
     * Actualiza los datos de la variante y los parametros de su inventario.
     * La cantidad NO se modifica aqui: solo cambia con entradas y ventas.
     */
    public boolean actualizar(VarianteProducto variante) throws SQLException {
        Connection conexion = null;
        try {
            conexion = ConexionBD.obtenerConexion();
            conexion.setAutoCommit(false);

            int filasVariante;
            try (PreparedStatement sentencia = conexion.prepareStatement(SQL_ACTUALIZAR_VARIANTE)) {
                sentencia.setString(1, variante.getTalla());
                sentencia.setString(2, variante.getColor());
                sentencia.setString(3, variante.getCodigoBarras());
                sentencia.setInt(4, variante.getIdVariante());
                filasVariante = sentencia.executeUpdate();
            }

            try (PreparedStatement sentencia = conexion.prepareStatement(SQL_ACTUALIZAR_INVENTARIO)) {
                sentencia.setInt(1, variante.getStockMinimo());
                sentencia.setString(2, variante.getUbicacion());
                sentencia.setInt(3, variante.getIdVariante());
                sentencia.executeUpdate();
            }

            // Si cambio el stock minimo, la alerta puede aparecer o desaparecer
            InventarioDAO.sincronizarAlerta(conexion, variante.getIdVariante());

            conexion.commit();
            return filasVariante > 0;

        } catch (SQLException e) {
            if (conexion != null) {
                conexion.rollback();
            }
            throw e;
        } finally {
            if (conexion != null) {
                conexion.setAutoCommit(true);
                conexion.close();
            }
        }
    }

    // ------------------------------------------------------------------
    // D - DELETE: elimina la variante (su inventario y alertas se borran en cascada)
    // ------------------------------------------------------------------
    /**
     * Elimina la variante. Si ya fue vendida, MySQL lo impide con SQLException
     * para no dañar el historial de ventas.
     */
    public boolean eliminar(int idVariante) throws SQLException {
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_ELIMINAR)) {

            sentencia.setInt(1, idVariante);
            return sentencia.executeUpdate() > 0;
        }
    }

    // ------------------------------------------------------------------
    // Metodos auxiliares
    // ------------------------------------------------------------------

    /** Ejecuta una consulta de lista; el parametro es opcional (null = sin parametro). */
    private List<VarianteProducto> consultarLista(String sql, Integer parametro) throws SQLException {
        List<VarianteProducto> variantes = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            if (parametro != null) {
                sentencia.setInt(1, parametro);
            }

            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    variantes.add(convertirFila(resultado));
                }
            }
        }
        return variantes;
    }

    /** Convierte una fila del ResultSet en un objeto VarianteProducto. */
    private VarianteProducto convertirFila(ResultSet resultado) throws SQLException {
        VarianteProducto variante = new VarianteProducto();
        variante.setIdVariante(resultado.getInt("idVariante"));
        variante.setIdProducto(resultado.getInt("idProducto"));
        variante.setTalla(resultado.getString("talla"));
        variante.setColor(resultado.getString("color"));
        variante.setCodigoBarras(resultado.getString("codigoBarras"));
        variante.setNombreProducto(resultado.getString("nombreProducto"));
        variante.setCantidad(resultado.getInt("cantidad"));
        variante.setStockMinimo(resultado.getInt("stockMinimo"));
        variante.setUbicacion(resultado.getString("ubicacion"));
        return variante;
    }
}