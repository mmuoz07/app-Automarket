package com.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDAO {
    
    // CONFIGURACIÓN INTELIGENTE: Detecta si Railway ha inyectado sus variables de entorno. 
    // Si no existen, usa los valores por defecto de tu Docker local.
    private static final String URL = System.getenv("MYSQL_URL") != null 
            ? System.getenv("MYSQL_URL") 
            : "jdbc:mysql://mysql:3306/automarket_db";
            
    private static final String USER = System.getenv("MYSQLUSER") != null 
            ? System.getenv("MYSQLUSER") 
            : "root";
            
    private static final String PASSWORD = System.getenv("MYSQLPASSWORD") != null 
            ? System.getenv("MYSQLPASSWORD") 
            : "root";

    public UsuarioDAO() {
        try { 
            Class.forName("com.mysql.cj.jdbc.Driver"); 
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Driver de MySQL no encontrado: " + e.getMessage());
        }
    }

    /**
     * Registra un usuario validando email y username de forma independiente y segura.
     */
    public Usuario registrarUsuarioYObtener(String username, String nombre, String apellidos, String email, String password) {
        System.out.println("DEBUG REGISTRO -> Username: [" + username + "], Email: [" + email + "]");
        
        // Separamos las consultas para evitar falsos positivos en el conteo
        String sqlBuscarEmail = "SELECT COUNT(*) FROM usuarios WHERE email = ?";
        String sqlBuscarUser = "SELECT COUNT(*) FROM usuarios WHERE username = ?";
        String sqlInsertar = "INSERT INTO usuarios (nombre, apellidos, email, password, username, user) VALUES (?, ?, ?, ?, ?, 'user')";
        String sqlObtener = "SELECT id, user FROM usuarios WHERE email = ?";

        try (Connection conexion = DriverManager.getConnection(URL, USER, PASSWORD)) {
            
            // PASO 1.A: Validar si ya existe el EMAIL
            try (PreparedStatement checkEmail = conexion.prepareStatement(sqlBuscarEmail)) {
                checkEmail.setString(1, email.trim());
                try (ResultSet rs = checkEmail.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        System.out.println("❌ El correo '" + email + "' ya está registrado.");
                        return null; 
                    }
                }
            }

            // PASO 1.B: Validar si ya existe el USERNAME
            try (PreparedStatement checkUser = conexion.prepareStatement(sqlBuscarUser)) {
                checkUser.setString(1, username.trim());
                try (ResultSet rs = checkUser.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        System.out.println("❌ El nombre de usuario '" + username + "' ya está registrado.");
                        return null; 
                    }
                }
            }

            // PASO 2: Insertar datos en orden riguroso
            try (PreparedStatement insert = conexion.prepareStatement(sqlInsertar)) {
                insert.setString(1, nombre.trim());
                insert.setString(2, apellidos.trim());
                insert.setString(3, email.trim());
                insert.setString(4, password.trim());
                insert.setString(5, username.trim());
                insert.executeUpdate();
                System.out.println("✅ Usuario insertado con éxito en la base de datos.");
            }

            // PASO 3: Obtener el objeto de sesión para el retorno al cliente
            try (PreparedStatement select = conexion.prepareStatement(sqlObtener)) {
                select.setString(1, email.trim());
                try (ResultSet rs = select.executeQuery()) {
                    if (rs.next()) {
                        Usuario u = new Usuario();
                        u.setId(rs.getInt("id"));
                        u.setNombre(nombre); 
                        u.setApellidos(apellidos);
                        u.setEmail(email);
                        u.setUser(rs.getString("user")); 
                        return u; 
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("⚠️ Error SQL crítico en UsuarioDAO: " + e.getMessage());
        }
        return null;
    }
}