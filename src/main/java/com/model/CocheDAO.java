package com.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
// No necesitas importar Coche si está en la misma carpeta 'modelos'

// 2. IMPORTANTE: Asegúrate de que Coche.java esté en la misma carpeta 'modelos'
// Si no, añade aquí: import com.ejemplo.model.Coche; (o la ruta correcta)

public class CocheDAO {

    public List<Coche> buscarCoches(String textoBusqueda) {
        List<Coche> lista = new ArrayList<>();

        // SQL: Asegúrate de que los nombres de columnas coincidan con tu DB
        String sql = "SELECT * FROM coches WHERE (marca LIKE ? OR modelo LIKE ?) " +
                     "AND estado = 'Aprobado' ORDER BY marca ASC";

        // 3. REVISA ESTO: 'ConexionBD' debe existir en tu proyecto
        try (Connection conexion = ConexionBD.getConnection(); 
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
                    coche.setAno(rs.getInt("ano")); // Si en la DB pusiste 'anio', cámbialo aquí
                    coche.setPrecio(rs.getInt("precio"));
                    coche.setKilometros(rs.getInt("kilometros"));
                    coche.setCombustible(rs.getString("combustible"));
                    coche.setUbicacion(rs.getString("ubicacion"));
                    coche.setImagenUrl(rs.getString("imagen_url"));
                    coche.setDescripcion(rs.getString("descripcion"));
                    coche.setEstado(rs.getString("estado"));
                    
                    lista.add(coche);
                }
            }

        } catch (Exception e) {
            e.printStackTrace(); // Para que puedas ver el error real en la consola de Docker
            throw new RuntimeException("Error al buscar vehículos en AutoMarket", e);
        }

        return lista;
    }
}