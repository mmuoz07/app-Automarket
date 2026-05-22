package com.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDAO {
    
    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    static {
        // Si Railway nos da las variables de entorno detalladas, construimos la URL JDBC perfecta
        if (System.getenv("MYSQLHOST") != null) {
            String host = System.getenv("MYSQLHOST");
            String port = System.getenv("MYSQLPORT") != null ? System.getenv("MYSQLPORT") : "3306";
            String database = System.getenv("MYSQLDATABASE") != null ? System.getenv("MYSQLDATABASE") : "railway";
            
            URL = "jdbc:mysql://" + host + ":" + port + "/" + database;
            USER = System.getenv("MYSQLUSER");
            PASSWORD = System.getenv("MYSQLPASSWORD");
        } else {
            // Fallback impecable para tu entorno de Docker local
            URL = "jdbc:mysql://mysql:3306/automarket_db";
            USER = "root";
            PASSWORD = "root";
        }
    }

    public UsuarioDAO() {
        try { 
            Class.forName("com.mysql.cj.jdbc.Driver"); 
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Driver de MySQL no encontrado: " + e.getMessage());
        }
    }

    public Usuario registrarUsuarioYObtener(String username, String nombre, String apellidos, String email, String password) {
        System.out.println("DEBUG REGISTRO -> Username: [" + username + "], Email: [" + email + "]");
        
        String sqlBuscarEmail = "SELECT COUNT(*) FROM usuarios WHERE email = ?";
        String sqlBuscarUser = "SELECT COUNT(*) FROM usuarios WHERE username = ?";
        String sqlInsertar = "INSERT INTO usuarios (nombre, apellidos, email, password, username) VALUES (?, ?, ?, ?, ?)";
        String sqlObtener = "SELECT id FROM usuarios WHERE email = ?";

        try (Connection conexion = DriverManager.getConnection(URL, USER, PASSWORD)) {
            
            // 1. Validar Email
            try (PreparedStatement checkEmail = conexion.prepareStatement(sqlBuscarEmail)) {
                checkEmail.setString(1, email.trim());
                try (ResultSet rs = checkEmail.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        System.out.println("❌ El correo '" + email + "' ya está registrado.");
                        return null; 
                    }
                }
            }

            // 2. Validar Username
            try (PreparedStatement checkUser = conexion.prepareStatement(sqlBuscarUser)) {
                checkUser.setString(1, username.trim());
                try (ResultSet rs = checkUser.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        System.out.println("❌ El nombre de usuario '" + username + "' ya está registrado.");
                        return null; 
                    }
                }
            }

            // 3. Insertar datos (5 campos estrictos)
            try (PreparedStatement insert = conexion.prepareStatement(sqlInsertar)) {
                insert.setString(1, nombre.trim());
                insert.setString(2, apellidos.trim());
                insert.setString(3, email.trim());
                insert.setString(4, password.trim());
                insert.setString(5, username.trim());
                insert.executeUpdate();
                System.out.println("✅ Usuario insertado con éxito en la base de datos.");
            }

            // 4. Mapear objeto de retorno
            try (PreparedStatement select = conexion.prepareStatement(sqlObtener)) {
                select.setString(1, email.trim());
                try (ResultSet rs = select.executeQuery()) {
                    if (rs.next()) {
                        Usuario u = new Usuario();
                        u.setId(rs.getInt("id"));
                        u.setNombre(nombre); 
                        u.setApellidos(apellidos);
                        u.setEmail(email);
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