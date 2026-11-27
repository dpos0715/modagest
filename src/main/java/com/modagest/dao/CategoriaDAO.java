package com.modagest.dao;

import com.modagest.modelo.Categoria;
import com.modagest.util.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de la tabla Categoria.
 */
public class CategoriaDAO {

    private static final String SQL_LISTAR =
            "SELECT idCategoria, nombre FROM Categoria ORDER BY nombre";

    /**
     * Devuelve todas las categorias (se usara para llenar listas desplegables).
     */
    public List<Categoria> listar() throws SQLException {
        List<Categoria> categorias = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_LISTAR);
             ResultSet resultado = sentencia.executeQuery()) {

            while (resultado.next()) {
                categorias.add(new Categoria(
                        resultado.getInt("idCategoria"),
                        resultado.getString("nombre")));
            }
        }
        return categorias;
    }
}