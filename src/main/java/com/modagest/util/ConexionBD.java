package com.modagest.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase de utilidad que entrega una conexion JDBC a la base de datos ModaGest.
 * Todos los DAO del proyecto usan esta clase para hablar con MySQL.
 */
public class ConexionBD {

    // Datos de conexion. Se declaran como constantes (MAYUSCULAS).
    private static final String URL_BASE_DATOS =
            "jdbc:mysql://localhost:3306/modagest"
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Bogota";
    private static final String USUARIO = "root";
    private static final String CLAVE = ""; // XAMPP no trae contrasena por defecto

    // Constructor privado: esta clase no se instancia, solo se usan sus metodos estaticos.
    private ConexionBD() {
    }

    /**
     * Abre una nueva conexion con la base de datos.
     * Quien la pida es responsable de cerrarla (se usa con try-with-resources).
     *
     * @return una conexion lista para usar
     * @throws SQLException si MySQL esta apagado o los datos son incorrectos
     */
    public static Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(URL_BASE_DATOS, USUARIO, CLAVE);
    }
}