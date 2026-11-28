package com.modagest.dao;

import com.modagest.modelo.Usuario;
import com.modagest.util.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Acceso a datos para la autenticacion de usuarios.
 */
public class UsuarioDAO {

    // La contrasena se compara cifrada: SHA2(?, 256) cifra lo que escribio el usuario
    // y lo compara con el hash guardado. Asi nunca se maneja la clave en texto plano.
    private static final String SQL_AUTENTICAR =
            "SELECT u.idUsuario, u.nombre, u.apellido, u.correo, r.nombre AS nombreRol "
            + "FROM Usuario u JOIN Rol r ON r.idRol = u.idRol "
            + "WHERE u.correo = ? AND u.contrasena = SHA2(?, 256) AND u.estado = TRUE";

    /**
     * Valida las credenciales.
     *
     * @return el usuario si son correctas, o null si el correo o la clave fallan
     */
    public Usuario autenticar(String correo, String clave) throws SQLException {
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_AUTENTICAR)) {

            sentencia.setString(1, correo);
            sentencia.setString(2, clave);

            try (ResultSet resultado = sentencia.executeQuery()) {
                if (resultado.next()) {
                    return new Usuario(
                            resultado.getInt("idUsuario"),
                            resultado.getString("nombre"),
                            resultado.getString("apellido"),
                            resultado.getString("correo"),
                            resultado.getString("nombreRol"));
                }
            }
        }
        return null;
    }
}