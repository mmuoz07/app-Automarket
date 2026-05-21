package com.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDAO {

    // IMPORTANTÍSIMO: En Docker se usa el nombre del servicio "mysql", NO "localhost"
    // Usamos el puerto interno del contenedor (3306), no el externo (3307)
    private static final String URL = "jdbc:mysql://mysql:3306/automarket_db";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    /**
     * Verifica si el usuario ya existe. Si no existe, lo registra.
     * @return true si se registró con éxito, false si el usuario ya existía.
     */
    public boolean registrarUsuario(String username, String password) {
        String sqlBuscar = "SELECT COUNT(*) FROM usuarios WHERE username = ?";
        String sqlInsertar = "INSERT INTO usuarios (username, password) VALUES (?, ?)";

        // Registrar el driver de MySQL (Muy recomendado en entornos Jakarta EE tradicionales)
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ No se encontró el Driver de MySQL: " + e.getMessage());
            return false;
        }

        // Abrir conexión automáticamente con try-with-resources
        try (Connection conexion = DriverManager.getConnection(URL, USER, PASSWORD)) {
            
            // PASO 1: Validar si ya existe el nombre de usuario
            try (PreparedStatement prepararBusqueda = conexion.prepareStatement(sqlBuscar)) {
                prepararBusqueda.setString(1, username);
                
                try (ResultSet resultado = prepararBusqueda.executeQuery()) {
                    if (resultado.next() && resultado.getInt(1) > 0) {
                        System.out.println("❌ El usuario '" + username + "' ya existe en automarket_db.");
                        return false; // Cortamos la ejecución aquí
                    }
                }
            }

            // PASO 2: Si no existía, se hace el INSERT
            try (PreparedStatement prepararInsert = conexion.prepareStatement(sqlInsertar)) {
                prepararInsert.setString(1, username);
                prepararInsert.setString(2, password); // Recuerda encriptar esto en producción
                
                prepararInsert.executeUpdate();
                System.out.println("✅ Usuario '" + username + "' registrado exitosamente.");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("⚠️ Error en la base de datos: " + e.getMessage());
            return false;
        }
    }
}    