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
import java.util.ArrayList;
import java.util.List;
import com.model.CocheDAO;

@WebServlet("/api/modificar-coche")
public class ModificarCocheServlet extends HttpServlet {
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
                respuesta.put("mensaje", "ID de vehículo no proporcionado.");
                response.getWriter().write(gson.toJson(respuesta));
                return;
            }

            int id = datos.get("id").getAsInt();
            String marca = datos.has("marca") ? datos.get("marca").getAsString() : "";
            String modelo = datos.has("modelo") ? datos.get("modelo").getAsString() : "";
            int ano = datos.has("ano") ? datos.get("ano").getAsInt() : 0;

            int precio = 0;
            if (datos.has("precio")) {
                precio = Integer.parseInt(datos.get("precio").getAsString().replace(".", ""));
            }

            int km = 0;
            if (datos.has("km")) {
                km = Integer.parseInt(datos.get("km").getAsString().replace(".", ""));
            }

            String combustible = datos.has("motor") ? datos.get("motor").getAsString() : "";
            String descripcion = datos.has("desc") ? datos.get("desc").getAsString() : "";
            String estado = datos.has("estado") ? datos.get("estado").getAsString() : "pendiente";

            List<String> imgs = new ArrayList<>();
            if (datos.has("imgs") && !datos.get("imgs").isJsonNull()) {
                java.lang.reflect.Type tipoLista = new com.google.gson.reflect.TypeToken<ArrayList<String>>(){}.getType();
                imgs = gson.fromJson(datos.get("imgs"), tipoLista);
            }

            boolean actualizado = cocheDAO.modificarCoche(id, marca, modelo, ano, precio, km, combustible, imgs, descripcion, estado);

            if (actualizado) {
                respuesta.put("ok", true);
                respuesta.put("mensaje", "Vehículo actualizado correctamente.");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                respuesta.put("ok", false);
                respuesta.put("mensaje", "No se pudo actualizar el vehículo en la base de datos.");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            respuesta.put("ok", false);
            respuesta.put("mensaje", "Error al modificar vehículo: " + e.getMessage());
        }

        response.getWriter().write(gson.toJson(respuesta));
    }
}