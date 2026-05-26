package com.controller;


import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;


// Importamos tus clases desde el paquete exacto que tienes en la imagen

import com.model.Coche;
import com.model.CocheDAO;

@WebServlet("/api/crear-coches")

public class CrearCochesServlet extends HttpServlet {
 
    // FÍJATE AQUÍ: Ya no hace falta poner "com.modelo.model.", usamos CocheDAO directamente
    private final CocheDAO cocheDAO = new CocheDAO();
    private final Gson gson = new Gson();

    @Override

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");

        try {
            // 1. Leer el cuerpo del JSON recibido desde app.js
            StringBuilder jsonRecibido = new StringBuilder();
            BufferedReader reader = request.getReader();
            String linea;
            while ((linea = reader.readLine()) != null) {
                jsonRecibido.append(linea);
            }

            // 🌟 LA CLAVE: Parsear directamente a JsonObject de Gson para evitar problemas de casteo
            com.google.gson.JsonObject datos = gson.fromJson(jsonRecibido.toString(), com.google.gson.JsonObject.class);
        
            String estado = "pendiente";
            String marca = "";
            String modelo = "";
            int ano = 0;
            int precio = 0;
            int km = 0;
            String combustible = "";
            String descripcion = "";
            List<String> imgs = new ArrayList<>();

            if (datos != null) {
                // Extraer strings de forma segura
                if (datos.has("marca") && !datos.get("marca").isJsonNull()) {
                    marca = datos.get("marca").getAsString();
                }
                if (datos.has("modelo") && !datos.get("modelo").isJsonNull()) {
                    modelo = datos.get("modelo").getAsString();
                }
                
                // Extraer enteros directamente (Adiós al ClassCastException)
                if (datos.has("ano") && !datos.get("ano").isJsonNull()) {
                    ano = datos.get("ano").getAsInt();
                }
                if (datos.has("precio") && !datos.get("precio").isJsonNull()) {
                    String precioTexto = datos.get("precio").getAsString();
    
                    // 2. Quitamos todos los puntos para dejarlo limpio (ej: "50000")
                    precioTexto = precioTexto.replace(".", "");
                    precio = Integer.parseInt(precioTexto);
                }
                if (datos.has("km") && !datos.get("km").isJsonNull()) {
                    String kmTexto = datos.get("km").getAsString();
                    kmTexto = kmTexto.replace(".", "");
                    km = Integer.parseInt(kmTexto);
                }
                
                // Mapear campos que en JS se llaman distinto (app.js envía 'motor' y 'desc')
                if (datos.has("motor") && !datos.get("motor").isJsonNull()) {
                    combustible = datos.get("motor").getAsString();
                }
                if (datos.has("desc") && !datos.get("desc").isJsonNull()) {
                    descripcion = datos.get("desc").getAsString();
                }
                
                // Procesar el array de imágenes de manera limpia
                if (datos.has("imgs") && !datos.get("imgs").isJsonNull()) {
                    java.lang.reflect.Type tipoLista = new com.google.gson.reflect.TypeToken<ArrayList<String>>(){}.getType();
                    imgs = gson.fromJson(datos.get("imgs"), tipoLista);
                }
            }

            // 2. Guardar en la base de datos usando el DAO
            Coche cocheInsertado = cocheDAO.crearCoches(marca, modelo, ano, precio, km, combustible, imgs, descripcion, estado);

            // 3. Responder al cliente
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("ok", true);
            respuesta.put("resultados", cocheInsertado);
            response.getWriter().write(gson.toJson(respuesta));

        } catch (Exception e) {
            e.printStackTrace(); // Esto te permitirá ver cualquier otro detalle en la consola de Docker
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, Object> error = new HashMap<>();
            error.put("ok", false);
            error.put("mensaje", "Error al añadir vehículo: " + e.getMessage());
            response.getWriter().write(gson.toJson(error));
        }
    }
}