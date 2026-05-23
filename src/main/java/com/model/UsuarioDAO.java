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
        // Miramos si existe la variable MYSQL_URL de tu captura
        String railwayUrl = System.getenv("MYSQL_URL");
        
        if (railwayUrl != null) {
            URL = "jdbc:" + railwayUrl;
            USER = null;
            PASSWORD = null;
            System.out.println("🚀 Conectando a Railway con URL parseada correctamente.");
        } else {
            URL = "jdbc:mysql://mysql:3306/automarket_db";
            USER = "root";
            PASSWORD = "root";
            System.out.println("💻 Conectando a Localhost (Docker).");
        }
    }

    public UsuarioDAO() {
        try { 
            Class.forName("com.mysql.cj.jdbc.Driver"); 
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Driver de MySQL no encontrado: " + e.getMessage());
        }
    }

    // NUEVO MÉTODO: Añadido para procesar el inicio de sesión contra tu DB
    public Usuario loginUsuario(String username, String password) {
        String sql = "SELECT * FROM usuarios WHERE username = ? AND password = ?";
        
        try (Connection conexion = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            
            ps.setString(1, username);
            ps.setString(2, password);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Usuario u = new Usuario();
                    u.setId(rs.getInt("id"));
                    u.setNombre(rs.getString("nombre"));
                    u.setApellidos(rs.getString("apellidos"));
                    u.setEmail(rs.getString("email"));
                    u.setPassword(rs.getString("password"));
                    u.setUsername(rs.getString("username"));
                    u.setUser(rs.getString("user")); // Recupera 'user' o 'admin' para el rol del front
                    return u;
                }
            }
        } catch (SQLException e) {
            System.err.println("⚠️ Error en loginUsuario DAO: " + e.getMessage());
        }
        return null;
    }

    public Usuario registrarUsuarioYObtener(String username, String nombre, String apellidos, String email, String password) {
        System.out.println("DEBUG REGISTRO -> Username: [" + username + "], Email: [" + email + "]");
        
        String sqlBuscarEmail = "SELECT COUNT(*) FROM usuarios WHERE email = ?";
        String sqlBuscarUser = "SELECT COUNT(*) FROM usuarios WHERE username = ?";
        String sqlInsertar = "INSERT INTO usuarios (nombre, apellidos, email, password, username, user) VALUES (?, ?, ?, ?, ?, 'user')";
        String sqlObtener = "SELECT * FROM usuarios WHERE email = ?";

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

            // 3. Insertar datos (con rol 'user' por defecto)
            try (PreparedStatement insert = conexion.prepareStatement(sqlInsertar)) {
                insert.setString(1, nombre.trim());
                insert.setString(2, apellidos.trim());
                insert.setString(3, email.trim());
                insert.setString(4, password.trim());
                insert.setString(5, username.trim());
                insert.executeUpdate();
                System.out.println("✅ Usuario insertado con éxito en la base de datos.");
            }

            // 4. Mapear objeto de retorno completo
            try (PreparedStatement select = conexion.prepareStatement(sqlObtener)) {
                select.setString(1, email.trim());
                try (ResultSet rs = select.executeQuery()) {
                    if (rs.next()) {
                        Usuario u = new Usuario();
                        u.setId(rs.getInt("id"));
                        u.setNombre(rs.getString("nombre")); 
                        u.setApellidos(rs.getString("apellidos"));
                        u.setEmail(rs.getString("email"));
                        u.setUsername(rs.getString("username"));
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