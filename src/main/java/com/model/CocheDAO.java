package com.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;
import java.sql.SQLException;
import com.model.ConexionBD;
// No necesitas importar Coche si está en la misma carpeta 'modelos'



// 2. IMPORTANTE: Asegúrate de que Coche.java esté en la misma carpeta 'modelos'

// Si no, añade aquí: import com.ejemplo.model.Coche; (o la ruta correcta)



public class CocheDAO {

    private final Gson gson = new Gson();
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
                    coche.setKm(rs.getInt("km"));
                    coche.setCombustible(rs.getString("combustible"));
                    coche.setUbicacion(rs.getString("ubicacion"));

                    // 1. Leemos el texto JSON que guardamos en la base de datos
                    String jsonImgs = rs.getString("imgs");

                    // 2. Definimos el tipo exacto que queremos recuperar (un List de Strings)
                    Type tipoLista = new TypeToken<ArrayList<String>>(){}.getType();

                    // 3. Convertimos el JSON de vuelta a una Lista de Java
                    List<String> listaImgs = gson.fromJson(jsonImgs, tipoLista);

                    // 4. Se lo asignamos al objeto coche
                    coche.setImgs(listaImgs);
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

    public Coche crearCoches(String marca , String modelo , int ano , int precio, int km, String combustible , List<String> imgs , String descripcion , String estado) {

         System.out.println("Entro en crear coches");
        Coche cocheDadoAlta = new Coche();
        String sqlInsertar = "INSERT INTO coches (marca, modelo, ano, precio, km, combustible, imgs, descripcion, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

         try (Connection conexion = ConexionBD.getConnection();
         
            PreparedStatement insert = conexion.prepareStatement(sqlInsertar)) {
                 System.out.println("despues de la conexion");
                insert.setString(1, marca.trim());
                insert.setString(2, modelo.trim());
                insert.setInt(3, ano);
                insert.setInt(4, precio);
                insert.setInt(5, km);
                insert.setString(6, 
                combustible.trim());
                String imagenesJson = gson.toJson(imgs);
                insert.setString(7, imagenesJson);
                insert.setString(8, descripcion.trim());
                insert.setString(9, estado.trim());
                insert.executeUpdate();
                System.out.println("✅ Coche insertado con éxito en la base de datos.");

                Coche cocheMostrar = new Coche();

                    cocheMostrar.setMarca(marca);
                    cocheMostrar.setModelo(modelo);
                    cocheMostrar.setAno(ano); // Si en la DB pusiste 'anio', cámbialo aquí
                    cocheMostrar.setPrecio(precio);
                    cocheMostrar.setKm(km);
                    cocheMostrar.setCombustible(combustible);
                    // 1. Leemos el texto JSON que guardamos en la base de datos
                    //String jsonImgs = imgs;
                    // 2. Definimos el tipo exacto que queremos recuperar (un List de Strings)
                    //Type tipoLista = new TypeToken<ArrayList<String>>(){}.getType();
                    // 3. Convertimos el JSON de vuelta a una Lista de Java
                    //List<String> listaImgs = gson.fromJson(jsonImgs, tipoLista);
                    // 4. Se lo asignamos al objeto coche
                    cocheMostrar.setImgs(imgs);
                    cocheMostrar.setDescripcion(descripcion);
                    cocheMostrar.setEstado(estado);

                     Coche cocheInsertado = new Coche();

                    cocheInsertado = recuperarCoche(cocheMostrar);

                    return cocheInsertado;

            } catch (SQLException e) {
             System.err.println("⚠️ Error SQL crítico en CocheDao insertar: " + e.getMessage());
            }

        return null;
         
    }

    public Coche recuperarCoche(Coche coche) {

        System.out.println("Entrpo en recuperar coche");

        String sqlConsultarCoche = "SELECT * FROM coches WHERE marca = ? AND modelo = ? AND ano = ? AND precio = ? AND km = ? AND combustible = ? AND imgs = ? AND descripcion = ? AND estado = ?";

         try (Connection conexion = ConexionBD.getConnection();
            PreparedStatement ps = conexion.prepareStatement(sqlConsultarCoche)) { 
            
                ps.setString(1, coche.getMarca().trim());
                ps.setString(2, coche.getModelo().trim());
                ps.setInt(3, coche.getAno());
                ps.setInt(4, coche.getPrecio());
                ps.setInt(5, coche.getKm());
                ps.setString(6, coche.getCombustible().trim());
                String imagenesJson = gson.toJson(coche.getImgs());
                ps.setString(7, imagenesJson);
                ps.setString(8, coche.getDescripcion().trim());
                ps.setString(9, coche.getEstado().trim());
                 
                try (ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {

                        Coche cocheMostrar = new Coche();

                        cocheMostrar.setId(rs.getInt("id"));
                        cocheMostrar.setMarca(rs.getString("marca"));
                       cocheMostrar.setModelo(rs.getString("modelo"));
                        cocheMostrar.setAno(rs.getInt("ano")); // Si en la DB pusiste 'anio', cámbialo aquí
                        cocheMostrar.setPrecio(rs.getInt("precio"));
                        cocheMostrar.setKm(rs.getInt("km"));
                        cocheMostrar.setCombustible(rs.getString("combustible"));
                        cocheMostrar.setUbicacion(rs.getString("ubicacion"));
                        // 1. Leemos el texto JSON que guardamos en la base de datos
                        String jsonImgs = rs.getString("imgs");
                        // 2. Definimos el tipo exacto que queremos recuperar (un List de Strings)
                        Type tipoLista = new TypeToken<ArrayList<String>>(){}.getType();
                        // 3. Convertimos el JSON de vuelta a una Lista de Java
                        List<String> listaImgs = gson.fromJson(jsonImgs, tipoLista);
                        // 4. Se lo asignamos al objeto coche
                        cocheMostrar.setImgs(listaImgs);
                        cocheMostrar.setDescripcion(rs.getString("descripcion"));
                        cocheMostrar.setEstado(rs.getString("estado"));

                        System.out.println("Devuelvo coche");
                        return cocheMostrar;
                    }
                }  catch (Exception e) {
                    e.printStackTrace(); // Para que puedas ver el error real en la consola de Docker
                    throw new RuntimeException("Error al buscar vehículos en AutoMarket", e);
                }
         }  catch (SQLException ex) {
                    ex.printStackTrace(); // Para que puedas ver el error real en la consola de Docker
                    throw new RuntimeException("Error al buscar vehículos en AutoMarket", ex);
        }
        return null;
    }
}