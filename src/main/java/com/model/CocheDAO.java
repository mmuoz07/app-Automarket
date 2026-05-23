package com.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CocheDAO {

    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    static {
        String railwayUrl = System.getenv("MYSQL_URL");
        if (railwayUrl != null) {
            URL = "jdbc:" + railwayUrl;
            USER = null;
            PASSWORD = null;
        } else {
            URL = "jdbc:mysql://mysql:3306/automarket_db";
            USER = "root";
            PASSWORD = "root";
        }
    }

    public CocheDAO() {
        try { 
            Class.forName("com.mysql.cj.jdbc.Driver"); 
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Driver de MySQL no encontrado: " + e.getMessage());
        }
    }

    public boolean guardarCoche(Coche coche) {
        String sql = "INSERT INTO coches (marca, modelo, precio, km, ano, motor, transmision, ciudad, imgs, descripcion, vendedor, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conexion = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            
            ps.setString(1, coche.getMarca());
            ps.setString(2, coche.getModelo());
            ps.setString(3, coche.getPrecio());
            ps.setString(4, coche.getKm());
            ps.setInt(5, coche.getAno());
            ps.setString(6, coche.getMotor());
            ps.setString(7, coche.getTransmision());
            ps.setString(8, coche.getCiudad());
            ps.setString(9, coche.getImgs() != null ? String.join(",", coche.getImgs()) : ""); 
            ps.setString(10, coche.getDesc());
            ps.setString(11, coche.getVendedor());
            ps.setString(12, coche.getEstado());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("⚠️ Error al guardar coche en DB: " + e.getMessage());
            return false;
        }
    }

    public List<Coche> buscarCoches(String textoBusqueda) {
        List<Coche> lista = new ArrayList<>();
        String sql = "SELECT * FROM coches WHERE (marca LIKE ? OR modelo LIKE ?) AND estado = 'aprobado' ORDER BY marca ASC";

        try (Connection conexion = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            String comodin = "%" + textoBusqueda + "%";
            ps.setString(1, comodin);
            ps.setString(2, comodin);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Coche coche = new Coche();
                    coche.setId(rs.getInt("id"));
                    coche.setMarca(rs.getString("marca"));
                    coche.setModelo(rs.getString("modelo"));
                    coche.setAno(rs.getInt("ano")); 
                    coche.setPrecio(rs.getString("precio"));
                    coche.setKm(rs.getString("km"));
                    coche.setMotor(rs.getString("motor"));
                    coche.setCiudad(rs.getString("ciudad"));
                    coche.setTransmision(rs.getString("transmision"));
                    coche.setVendedor(rs.getString("vendedor"));
                    coche.setDesc(rs.getString("descripcion"));
                    coche.setEstado(rs.getString("estado"));
                    
                    String imagenesString = rs.getString("imgs");
                    if (imagenesString != null && !imagenesString.isEmpty()) {
                        coche.setImgs(List.of(imagenesString.split(",")));
                    } else {
                        coche.setImgs(new ArrayList<>());
                    }
                    
                    lista.add(coche);
                }
            }
        } catch (SQLException e) {
            System.err.println("⚠️ Error al buscar coches en DB: " + e.getMessage());
        }
        return lista;
    }
}