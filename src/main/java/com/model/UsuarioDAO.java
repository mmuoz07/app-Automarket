package com.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDAO {
    
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

    public Usuario registrarUsuarioYObtener(String username, String nombre, String apellidos, String email, String password) {
        System.out.println("DEBUG REGISTRO -> Username: [" + username + "], Email: [" + email + "]");
        
        // Consultas atómicas aisladas para evitar colisiones lógicas
        String sqlBuscarEmail = "SELECT COUNT(*) FROM usuarios WHERE email = ?";
        String sqlBuscarUser = "SELECT COUNT(*) FROM usuarios WHERE username = ?";
        
        // CAMBIO: Se elimina 'user' y el sexto '?' de la inserción. Ahora son 5 campos.
        String sqlInsertar = "INSERT INTO usuarios (nombre, apellidos, email, password, username) VALUES (?, ?, ?, ?, ?)";
        
        // CAMBIO: Se elimina ', user' de la selección.
        String sqlObtener = "SELECT id FROM usuarios WHERE email = ?";

        try (Connection conexion = DriverManager.getConnection(URL, USER, PASSWORD)) {
            
            // Validación aislada A: Email
            try (PreparedStatement checkEmail = conexion.prepareStatement(sqlBuscarEmail)) {
                checkEmail.setString(1, email.trim());
                try (ResultSet rs = checkEmail.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        System.out.println("❌ El correo '" + email + "' ya está registrado.");
                        return null; 
                    }
                }
            }

            // Validación aislada B: Username
            try (PreparedStatement checkUser = conexion.prepareStatement(sqlBuscarUser)) {
                checkUser.setString(1, username.trim());
                try (ResultSet rs = checkUser.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        System.out.println("❌ El nombre de usuario '" + username + "' ya está registrado.");
                        return null; 
                    }
                }
            }

            // Inserción parametrizada rigurosa (5 parámetros correspondientes a tus 5 '?')
            try (PreparedStatement insert = conexion.prepareStatement(sqlInsertar)) {
                insert.setString(1, nombre.trim());
                insert.setString(2, apellidos.trim());
                insert.setString(3, email.trim());
                insert.setString(4, password.trim());
                insert.setString(5, username.trim());
                insert.executeUpdate();
                System.out.println("✅ Usuario insertado con éxito en la base de datos.");
            }

            // Mapeo seguro sobre las propiedades estándar de la clase modelo Usuario
            try (PreparedStatement select = conexion.prepareStatement(sqlObtener)) {
                select.setString(1, email.trim());
                try (ResultSet rs = select.executeQuery()) {
                    if (rs.next()) {
                        Usuario u = new Usuario();
                        u.setId(rs.getInt("id"));
                        u.setNombre(nombre); 
                        u.setApellidos(apellidos);
                        u.setEmail(email);
                        // CAMBIO: Eliminada la línea u.setUser(...) que provocaba el fallo de columna desconocida
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