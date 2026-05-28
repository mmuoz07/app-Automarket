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
import com.model.Coche;
import com.model.CocheDAO;

@WebServlet("/api/buscar-coches")
public class BuscarCochesServlet extends HttpServlet {
 
    private final CocheDAO cocheDAO = new CocheDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");

        try {
            StringBuilder jsonRecibido = new StringBuilder();
            BufferedReader reader = request.getReader();
            String linea;

            while ((linea = reader.readLine()) != null) {
                jsonRecibido.append(linea);
            }

            @SuppressWarnings("unchecked")
            Map<String, String> datos = gson.fromJson(jsonRecibido.toString(), Map.class);

            String textoBusqueda = datos != null && datos.get("texto") != null ? datos.get("texto").trim() : "";

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