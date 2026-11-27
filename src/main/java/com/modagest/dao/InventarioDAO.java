package com.modagest.dao;

import com.modagest.util.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Acceso a datos del inventario: entradas de mercancia (HU-003)
 * y sincronizacion de alertas de stock minimo (HU-004).
 */
public class InventarioDAO {

    private static final String ESTADO_ALERTA = "Requerido Pedido";
    private static final String ESTADO_RESUELTA = "Resuelta";

    private static final String SQL_INSERTAR_ENTRADA =
            "INSERT INTO EntradaInventario (fecha, cantidad, idProveedor, idVariante, idUsuario) "
            + "VALUES (CURDATE(), ?, ?, ?, ?)";

    private static final String SQL_SUMAR_STOCK =
            "UPDATE Inventario SET cantidad = cantidad + ? WHERE idVariante = ?";

    private static final String SQL_CONSULTAR_INVENTARIO =
            "SELECT idInventario, cantidad, stockMinimo FROM Inventario WHERE idVariante = ?";

    private static final String SQL_ALERTA_ABIERTA =
            "SELECT 1 FROM AlertaStock WHERE idInventario = ? AND estado = ?";

    private static final String SQL_CREAR_ALERTA =
            "INSERT INTO AlertaStock (fecha, estado, idInventario) VALUES (CURDATE(), ?, ?)";

    private static final String SQL_RESOLVER_ALERTAS =
            "UPDATE AlertaStock SET estado = ? WHERE idInventario = ? AND estado = ?";

    /**
     * Registra la entrada de mercancia de una variante: guarda el registro de la entrada,
     * suma la cantidad al inventario y actualiza la alerta. Todo en una transaccion.
     *
     * @return true si la entrada quedo registrada
     * @throws IllegalArgumentException si la cantidad no es positiva
     */
    public boolean registrarEntrada(int idVariante, int cantidad, int idProveedor, int idUsuario)
            throws SQLException {

        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }

        Connection conexion = null;
        try {
            conexion = ConexionBD.obtenerConexion();
            conexion.setAutoCommit(false); // inicia la transaccion

            // 1. Guarda el registro de la entrada (trazabilidad)
            try (PreparedStatement sentencia = conexion.prepareStatement(SQL_INSERTAR_ENTRADA)) {
                sentencia.setInt(1, cantidad);
                sentencia.setInt(2, idProveedor);
                sentencia.setInt(3, idVariante);
                sentencia.setInt(4, idUsuario);
                sentencia.executeUpdate();
            }

            // 2. Suma la cantidad al inventario
            try (PreparedStatement sentencia = conexion.prepareStatement(SQL_SUMAR_STOCK)) {
                sentencia.setInt(1, cantidad);
                sentencia.setInt(2, idVariante);
                if (sentencia.executeUpdate() == 0) {
                    throw new SQLException("La variante " + idVariante + " no tiene inventario.");
                }
            }

            // 3. Revisa si hay que crear o resolver la alerta
            sincronizarAlerta(conexion, idVariante);

            conexion.commit(); // todo salio bien: guarda los cambios
            return true;

        } catch (SQLException e) {
            if (conexion != null) {
                conexion.rollback(); // algo fallo: deshace todo
            }
            throw e;
        } finally {
            if (conexion != null) {
                conexion.setAutoCommit(true);
                conexion.close();
            }
        }
    }

    /**
     * Revisa el stock de una variante y ajusta su alerta:
     * - Si cantidad < stockMinimo y no hay alerta abierta, la crea.
     * - Si cantidad >= stockMinimo, marca como "Resuelta" las alertas abiertas.
     *
     * Recibe la conexion de quien la llama para ejecutarse dentro de su transaccion.
     */
    static void sincronizarAlerta(Connection conexion, int idVariante) throws SQLException {
        int idInventario;
        int cantidad;
        int stockMinimo;

        try (PreparedStatement sentencia = conexion.prepareStatement(SQL_CONSULTAR_INVENTARIO)) {
            sentencia.setInt(1, idVariante);
            try (ResultSet resultado = sentencia.executeQuery()) {
                if (!resultado.next()) {
                    return; // la variante no tiene inventario: nada que sincronizar
                }
                idInventario = resultado.getInt("idInventario");
                cantidad = resultado.getInt("cantidad");
                stockMinimo = resultado.getInt("stockMinimo");
            }
        }

        if (cantidad < stockMinimo) {
            // Stock bajo: crea la alerta solo si no existe una abierta
            boolean yaExiste;
            try (PreparedStatement sentencia = conexion.prepareStatement(SQL_ALERTA_ABIERTA)) {
                sentencia.setInt(1, idInventario);
                sentencia.setString(2, ESTADO_ALERTA);
                try (ResultSet resultado = sentencia.executeQuery()) {
                    yaExiste = resultado.next();
                }
            }
            if (!yaExiste) {
                try (PreparedStatement sentencia = conexion.prepareStatement(SQL_CREAR_ALERTA)) {
                    sentencia.setString(1, ESTADO_ALERTA);
                    sentencia.setInt(2, idInventario);
                    sentencia.executeUpdate();
                }
            }
        } else {
            // Stock suficiente: cierra las alertas abiertas
            try (PreparedStatement sentencia = conexion.prepareStatement(SQL_RESOLVER_ALERTAS)) {
                sentencia.setString(1, ESTADO_RESUELTA);
                sentencia.setInt(2, idInventario);
                sentencia.setString(3, ESTADO_ALERTA);
                sentencia.executeUpdate();
            }
        }
    }
}