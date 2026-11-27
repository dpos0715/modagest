package com.modagest.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Clase de prueba: verifica que Java se conecta a MySQL
 * y que puede leer datos de la tabla Usuario.
 */
public class PruebaConexion {

    public static void main(String[] args) {
        // try-with-resources: cierra la conexion automaticamente al terminar
        try (Connection conexion = ConexionBD.obtenerConexion();
             Statement sentencia = conexion.createStatement();
             ResultSet resultado = sentencia.executeQuery(
                     "SELECT nombre, apellido, correo FROM Usuario")) {

            System.out.println("Conexion exitosa a la base de datos ModaGest.");
            System.out.println("Usuarios registrados:");

            // Recorre cada fila que devolvio la consulta
            while (resultado.next()) {
                String nombre = resultado.getString("nombre");
                String apellido = resultado.getString("apellido");
                String correo = resultado.getString("correo");
                System.out.println(" - " + nombre + " " + apellido + " (" + correo + ")");
            }

        } catch (SQLException e) {
            System.out.println("Error de conexion: " + e.getMessage());
        }
    }
}
