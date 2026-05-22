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
     * Registra un usuario con la estructura sincronizada de la base de datos.
     */
    public Usuario registrarUsuarioYObtener(String nombre, String apellidos, String email, String password) {
        System.out.println("DEBUG REGISTRO -> Nombre: [" + nombre + "], Apellidos: [" + apellidos + "], Email: [" + email + "]");
        
        String sqlBuscar = "SELECT COUNT(*) FROM usuarios WHERE email = ?";
        String sqlInsertar = "INSERT INTO usuarios (nombre, apellidos, email, password, user) VALUES (?, ?, ?, ?, 'user')";
        String sqlObtener = "SELECT id, user FROM usuarios WHERE email = ?";

        try (Connection conexion = DriverManager.getConnection(URL, USER, PASSWORD)) {
            
            // PASO 1: Validar si ya existe el email en la base de datos
            try (PreparedStatement check = conexion.prepareStatement(sqlBuscar)) {
                check.setString(1, email);
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        System.out.println("❌ El correo '" + email + "' ya está registrado.");
                        return null; 
                    }
                }
            }

            // PASO 2: Insertar el nuevo usuario usando las columnas correctas
            try (PreparedStatement insert = conexion.prepareStatement(sqlInsertar)) {
                insert.setString(1, nombre);
                insert.setString(2, apellidos);
                insert.setString(3, email);
                insert.setString(4, password);
                insert.executeUpdate();
            }

            // PASO 3: Obtener el ID generado y el tipo de cuenta ('user' o 'admin')
            try (PreparedStatement select = conexion.prepareStatement(sqlObtener)) {
                select.setString(1, email);
                try (ResultSet rs = select.executeQuery()) {
                    if (rs.next()) {
                        Usuario u = new Usuario();
                        u.setId(rs.getInt("id"));
                        u.setNombre(nombre); 
                        u.setApellidos(apellidos);
                        u.setEmail(email);
                        u.setUser(rs.getString("user")); // Cambiado setRol por setUser
                        return u; 
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("⚠️ Error SQL en UsuarioDAO: " + e.getMessage());
        }
        return null;
    }
}