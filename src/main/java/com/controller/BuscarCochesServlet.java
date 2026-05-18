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

// Importamos tus clases desde el paquete exacto que tienes en la imagen
import com.model.Coche;
import com.model.CocheDAO;

@WebServlet("/api/buscar-coches")
public class BuscarCochesServlet extends HttpServlet {
    
    // FÍJATE AQUÍ: Ya no hace falta poner "com.modelo.model.", usamos CocheDAO directamente
    private final CocheDAO cocheDAO = new CocheDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");

        try {
            // Leer el cuerpo del JSON (el texto que el usuario escribe en el buscador)
            StringBuilder jsonRecibido = new StringBuilder();
            BufferedReader reader = request.getReader();
            String linea;
            while ((linea = reader.readLine()) != null) {
                jsonRecibido.append(linea);
            }

            Map<String, String> datos = gson.fromJson(jsonRecibido.toString(), Map.class);
            // "texto" es lo que viene del input de búsqueda en tu Figma
            String textoBusqueda = datos != null && datos.get("texto") != null ? datos.get("texto").trim() : "";

            // Llamamos al método del modelo que busca en la base de datos MySQL
            // FÍJATE AQUÍ: Usamos "List<Coche>" a secas, sin rutas largas
            List<Coche> resultados = cocheDAO.buscarCoches(textoBusqueda);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("ok", true);
            respuesta.put("resultados", resultados);

            response.getWriter().write(gson.toJson(respuesta));
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, Object> error = new HashMap<>();
            error.put("ok", false);
            error.put("mensaje", "Error al buscar vehículos");
            response.getWriter().write(gson.toJson(error));
        }
    }
}