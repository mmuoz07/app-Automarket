package com.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import com.model.CocheDAO;

@WebServlet("/api/eliminar-coche")
public class EliminarCocheServlet extends HttpServlet {
    private final CocheDAO cocheDAO = new CocheDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> respuesta = new HashMap<>();

        try {
            StringBuilder jsonRecibido = new StringBuilder();
            BufferedReader reader = request.getReader();
            String linea;
            while ((linea = reader.readLine()) != null) {
                jsonRecibido.append(linea);
            }

            JsonObject datos = gson.fromJson(jsonRecibido.toString(), JsonObject.class);
            
            if (datos == null || !datos.has("id")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                respuesta.put("ok", false);
                respuesta.put("mensaje", "ID de vehículo inválido.");
                response.getWriter().write(gson.toJson(respuesta));
                return;
            }

            int id = datos.get("id").getAsInt();
            boolean eliminado = cocheDAO.eliminarCoche(id);

            if (eliminado) {
                respuesta.put("ok", true);
                respuesta.put("mensaje", "Vehículo eliminado con éxito.");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                respuesta.put("ok", false);
                respuesta.put("mensaje", "El vehículo no existe o no pudo ser eliminado.");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            respuesta.put("ok", false);
            respuesta.put("mensaje", "Error al eliminar vehículo: " + e.getMessage());
        }
        
        response.getWriter().write(gson.toJson(respuesta));
    }
}