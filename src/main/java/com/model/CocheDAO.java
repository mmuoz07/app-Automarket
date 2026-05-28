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
                    coche.setUbicacion(rs.getString("ubicacion"));

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

    public Coche crearCoches(String marca, String modelo, int ano, int precio, int km, String combustible, List<String> imgs, String descripcion, String estado, String vendedor, String ubicacion) {
        System.out.println("Entro en crear coches");
        String sqlInsertar = "INSERT INTO coches (marca, modelo, ano, precio, km, combustible, imgs, descripcion, estado, publicado_por, ubicacion) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
            insert.executeUpdate();
            System.out.println("✅ Coche insertado con éxito en la base de datos.");

            Coche cocheMostrar = new Coche();
            cocheMostrar.setMarca(marca);
            cocheMostrar.setModelo(modelo);
            cocheMostrar.setAno(ano);
            cocheMostrar.setPrecio(precio);
            cocheMostrar.setKm(km);
            cocheMostrar.setCombustible(combustible);
            cocheMostrar.setImgs(imgs);
            cocheMostrar.setDescripcion(descripcion);
            cocheMostrar.setEstado(estado);
            cocheMostrar.setVendedor(vendedor);
             cocheMostrar.setUbicacion(ubicacion);

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
        String sqlConsultarCoche = "SELECT * FROM coches WHERE marca = ? AND modelo = ? AND ano = ? AND precio = ? AND km = ? AND combustible = ? AND descripcion = ? AND estado = ? AND publicacion_por = ? AND  ubicacion = ?";
        try (Connection conexion = obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sqlConsultarCoche)) { 
            ps.setString(1, coche.getMarca().trim());
            ps.setString(2, coche.getModelo().trim());
            ps.setInt(3, coche.getAno());
            ps.setInt(4, coche.getPrecio());
            ps.setInt(5, coche.getKm());
            ps.setString(6, coche.getCombustible().trim());
            ps.setString(7, coche.getDescripcion().trim());
            ps.setString(8, coche.getEstado().trim());
            ps.setString(8, coche.getVendedor().trim());
            ps.setString(8, coche.getUbicacion().trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Coche cocheMostrar = new Coche();
                    cocheMostrar.setId(rs.getInt("id"));
                    cocheMostrar.setMarca(rs.getString("marca"));
                    cocheMostrar.setModelo(rs.getString("modelo"));
                    coche.setTransmision(rs.getString("transmision"));
                    cocheMostrar.setAno(rs.getInt("ano"));
                    cocheMostrar.setPrecio(rs.getInt("precio"));
                    cocheMostrar.setKm(rs.getInt("km"));
                    cocheMostrar.setCombustible(rs.getString("combustible"));
                    cocheMostrar.setUbicacion(rs.getString("ubicacion"));
                    String jsonImgs = rs.getString("imgs");
                    Type tipoLista = new TypeToken<ArrayList<String>>(){}.getType();
                    List<String> listaImgs = gson.fromJson(jsonImgs, tipoLista);
                    cocheMostrar.setImgs(listaImgs);
                    cocheMostrar.setDescripcion(rs.getString("descripcion"));
                    cocheMostrar.setEstado(rs.getString("estado"));
                    cocheMostrar.setVendedor(rs.getString("vendedor"));
                    System.out.println("Devuelvo coche");
                    return cocheMostrar;
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Error al recuperar vehículo en AutoMarket", e);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Error al recuperar vehículo en AutoMarket", ex);
        }
        return null;
    }

    public boolean modificarCoche(int id, String marca, String modelo, int ano, int precio, int km, String combustible, List<String> imgs, String descripcion, String estado) {
        System.out.println("Entro en modificar coche ID: " + id);
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

            int filasAfectadas = ps.executeUpdate();
            System.out.println("🔄 Resultado de modificación: " + (filasAfectadas > 0 ? "Éxito" : "No se encontró el ID"));
            return filasAfectadas > 0;
        } catch (SQLException e) {
            System.err.println("⚠️ Error SQL crítico en CocheDao modificar: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminarCoche(int id) {
        System.out.println("Entro en eliminar coche ID: " + id);
        String sqlDelete = "DELETE FROM coches WHERE id = ?";

        try (Connection conexion = obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sqlDelete)) {
            ps.setInt(1, id);
            int filasAfectadas = ps.executeUpdate();
            System.out.println("❌ Resultado de eliminación: " + (filasAfectadas > 0 ? "Éxito" : "No se encontró el ID"));
            return filasAfectadas > 0;
        } catch (SQLException e) {
            System.err.println("⚠️ Error SQL crítico en CocheDao eliminar: " + e.getMessage());
            return false;
        }
    }
}