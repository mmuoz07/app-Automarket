package com.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;
import java.sql.SQLException;

public class CocheDAO {
    private final Gson gson = new Gson();
    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    static {
        String railwayUrl = System.getenv("MYSQL_URL");
        if (railwayUrl != null) {
            URL = "jdbc:" + railwayUrl;
            USER = null;
            PASSWORD = null;
            System.out.println("🚀 CocheDAO: Conectando a Railway con URL parseada correctamente.");
        } else {
            URL = "jdbc:mysql://mysql:3306/automarket_db";
            USER = "root";
            PASSWORD = "root";
            System.out.println("💻 CocheDAO: Conectando a Localhost (Docker).");
        }
    }

    public CocheDAO() {
        try { 
            Class.forName("com.mysql.cj.jdbc.Driver"); 
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Driver de MySQL no encontrado en CocheDAO: " + e.getMessage());
        }
    }

    private Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public List<Coche> buscarCoches(String textoBusqueda) {
        List<Coche> lista = new ArrayList<>();
        String sql = "SELECT * FROM coches WHERE (marca LIKE ? OR modelo LIKE ?) AND (estado = 'Aprobado' OR estado = 'pendiente') ORDER BY marca ASC";

        try (Connection conexion = obtenerConexion();
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
                    coche.setPrecio(rs.getInt("precio"));
                    coche.setKm(rs.getInt("km"));
                    coche.setCombustible(rs.getString("combustible"));
                    coche.setMotor(rs.getString("combustible")); // Respaldo frontend
                    coche.setTransmision(rs.getString("transmision")); // ¡Arreglado!
                    coche.setUbicacion(rs.getString("ubicacion"));
                    coche.setCiudad(rs.getString("ubicacion")); // Respaldo frontend

                    String jsonImgs = rs.getString("imgs");
                    Type tipoLista = new TypeToken<ArrayList<String>>(){}.getType();
                    List<String> listaImgs = gson.fromJson(jsonImgs, tipoLista);
                    coche.setImgs(listaImgs);

                    coche.setDescripcion(rs.getString("descripcion"));
                    coche.setEstado(rs.getString("estado"));
                    coche.setVendedor(rs.getString("publicado_por"));
                    lista.add(coche);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al buscar vehículos en AutoMarket", e);
        }
        return lista;
    }

    public Coche crearCoches(String marca, String modelo, int ano, int precio, int km, String combustible, List<String> imgs, String descripcion, String estado, String vendedor, String ubicacion, String transmision) {
        System.out.println("Entro en crear coches");
        String sqlInsertar = "INSERT INTO coches (marca, modelo, ano, precio, km, combustible, imgs, descripcion, estado, publicado_por, ubicacion, transmision) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conexion = obtenerConexion();
             PreparedStatement insert = conexion.prepareStatement(sqlInsertar, Statement.RETURN_GENERATED_KEYS)) {
            insert.setString(1, marca.trim());
            insert.setString(2, modelo.trim());
            insert.setInt(3, ano);
            insert.setInt(4, precio);
            insert.setInt(5, km);
            insert.setString(6, combustible.trim());
            String imagenesJson = gson.toJson(imgs);
            insert.setString(7, imagenesJson);
            insert.setString(8, descripcion.trim());
            insert.setString(9, estado.trim());
            insert.setString(10, vendedor.trim());
            insert.setString(11, ubicacion.trim());
            insert.setString(12, transmision.trim());
            insert.executeUpdate();

            Coche cocheMostrar = new Coche();
            cocheMostrar.setMarca(marca);
            cocheMostrar.setModelo(modelo);
            cocheMostrar.setAno(ano);
            cocheMostrar.setPrecio(precio);
            cocheMostrar.setKm(km);
            cocheMostrar.setCombustible(combustible);
            cocheMostrar.setMotor(combustible);
            cocheMostrar.setImgs(imgs);
            cocheMostrar.setDescripcion(descripcion);
            cocheMostrar.setEstado(estado);
            cocheMostrar.setVendedor(vendedor);
            cocheMostrar.setUbicacion(ubicacion);
            cocheMostrar.setCiudad(ubicacion);
            cocheMostrar.setTransmision(transmision);

            try (ResultSet generatedKeys = insert.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    cocheMostrar.setId(generatedKeys.getInt(1));
                }
            }
            return cocheMostrar;
        } catch (SQLException e) {
            System.err.println("⚠️ Error SQL crítico en CocheDao insertar: " + e.getMessage());
        }
        return null;
    }

    public Coche recuperarCoche(Coche coche) {
        System.out.println("Entro en recuperar coche");
        String sqlConsultarCoche = "SELECT * FROM coches WHERE id = ?";
        try (Connection conexion = obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sqlConsultarCoche)) { 
            ps.setInt(1, coche.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Coche cocheMostrar = new Coche();
                    cocheMostrar.setId(rs.getInt("id"));
                    cocheMostrar.setMarca(rs.getString("marca"));
                    cocheMostrar.setModelo(rs.getString("modelo"));
                    cocheMostrar.setTransmision(rs.getString("transmision")); // ¡Arreglado!
                    cocheMostrar.setAno(rs.getInt("ano"));
                    cocheMostrar.setPrecio(rs.getInt("precio"));
                    cocheMostrar.setKm(rs.getInt("km"));
                    cocheMostrar.setCombustible(rs.getString("combustible"));
                    cocheMostrar.setMotor(rs.getString("combustible"));
                    cocheMostrar.setUbicacion(rs.getString("ubicacion"));
                    cocheMostrar.setCiudad(rs.getString("ubicacion"));
                    
                    String jsonImgs = rs.getString("imgs");
                    Type tipoLista = new TypeToken<ArrayList<String>>(){}.getType();
                    List<String> listaImgs = gson.fromJson(jsonImgs, tipoLista);
                    cocheMostrar.setImgs(listaImgs);
                    
                    cocheMostrar.setDescripcion(rs.getString("descripcion"));
                    cocheMostrar.setEstado(rs.getString("estado"));
                    cocheMostrar.setVendedor(rs.getString("publicado_por"));
                    return cocheMostrar;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public boolean modificarCoche(int id, String marca, String modelo, int ano, int precio, int km, String combustible, List<String> imgs, String descripcion, String estado) {
        String sqlUpdate = "UPDATE coches SET marca = ?, modelo = ?, ano = ?, precio = ?, km = ?, combustible = ?, imgs = ?, descripcion = ?, estado = ? WHERE id = ?";
        try (Connection conexion = obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sqlUpdate)) {
            ps.setString(1, marca.trim());
            ps.setString(2, modelo.trim());
            ps.setInt(3, ano);
            ps.setInt(4, precio);
            ps.setInt(5, km);
            ps.setString(6, combustible.trim());
            String imagenesJson = gson.toJson(imgs);
            ps.setString(7, imagenesJson);
            ps.setString(8, descripcion.trim());
            ps.setString(9, estado.trim());
            ps.setInt(10, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean eliminarCoche(int id) {
        String sqlDelete = "DELETE FROM coches WHERE id = ?";
        try (Connection conexion = obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sqlDelete)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }
}