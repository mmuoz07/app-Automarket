package com.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDAO {
    private static final String URL = "jdbc:mysql://mysql:3306/automarket_db";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    public UsuarioDAO() {
        try { 
            Class.forName("com.mysql.cj.jdbc.Driver"); 
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Driver de MySQL no encontrado: " + e.getMessage());
        }
    }

    /**
     * Registra un usuario y lo devuelve con su ID y Rol asignado si todo sale bien.
     */
    public Usuario registrarUsuarioYObtener(String username, String email, String password) {
        String sqlBuscar = "SELECT COUNT(*) FROM usuarios WHERE username = ? OR email = ?";
        String sqlInsertar = "INSERT INTO usuarios (username, email, password, rol) VALUES (?, ?, ?, 'USER')";
        String sqlObtener = "SELECT id, rol FROM usuarios WHERE username = ?";

        try (Connection conexion = DriverManager.getConnection(URL, USER, PASSWORD)) {
            
            // PASO 1: Validar si ya existe el username o el email
            try (PreparedStatement check = conexion.prepareStatement(sqlBuscar)) {
                check.setString(1, username);
                check.setString(2, email);
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        return null; // Retorna null si ya está duplicado
                    }
                }
            }

            // PASO 2: Insertar el nuevo usuario con rol por defecto 'USER'
            try (PreparedStatement insert = conexion.prepareStatement(sqlInsertar)) {
                insert.setString(1, username);
                insert.setString(2, email);
                insert.setString(3, password);
                insert.executeUpdate();
            }

            // PASO 3: Obtener los datos creados (ID y Rol) para devolver el objeto estructurado
            try (PreparedStatement select = conexion.prepareStatement(sqlObtener)) {
                select.setString(1, username);
                try (ResultSet rs = select.executeQuery()) {
                    if (rs.next()) {
                        Usuario u = new Usuario();
                        u.setId(rs.getInt("id"));
                        u.setUsername(username);
                        u.setEmail(email);
                        u.setRol(rs.getString("rol"));
                        return u; // Devuelve el usuario listo
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("⚠️ Error SQL en UsuarioDAO: " + e.getMessage());
        }
        return null;
    }
}