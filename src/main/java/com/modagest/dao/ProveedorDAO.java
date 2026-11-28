package com.modagest.dao;

import com.modagest.modelo.Proveedor;
import com.modagest.util.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de la tabla Proveedor (por ahora solo lectura).
 */
public class ProveedorDAO {

    private static final String SQL_LISTAR =
            "SELECT idProveedor, nombre FROM Proveedor ORDER BY nombre";

    public List<Proveedor> listar() throws SQLException {
        List<Proveedor> proveedores = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_LISTAR);
             ResultSet resultado = sentencia.executeQuery()) {

            while (resultado.next()) {
                proveedores.add(new Proveedor(
                        resultado.getInt("idProveedor"),
                        resultado.getString("nombre")));
            }
        }
        return proveedores;
    }
}