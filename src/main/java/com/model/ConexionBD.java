package com.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {

    // Cambiado el nombre por defecto a 'automarket_db' para que coincida con el nuevo SQL
    private static final String DB_HOST = System.getenv().getOrDefault("DB_HOST", "mysql");
    private static final String DB_PORT = System.getenv().getOrDefault("DB_PORT", "3306");
    private static final String DB_NAME = System.getenv().getOrDefault("DB_NAME", "automarket_db");
    private static final String DB_USER = System.getenv().getOrDefault("DB_USER", "root");
    private static final String DB_PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", "root");

    // Configuración de la URL de conexión con parámetros de compatibilidad para Docker/Railway
    private static final String URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8";

    // Bloque estático para cargar el driver de MySQL (Requisito para Servlets/Jakarta EE)
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Error crítico: No se encontró el driver de MySQL en el proyecto", e);
        }
    }

    /**
     * Método para obtener una conexión activa a la base de datos.
     * Se debe cerrar después de su uso en el DAO.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, DB_USER, DB_PASSWORD);
    }
}