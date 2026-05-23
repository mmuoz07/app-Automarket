package com.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
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
            // 1. Leer el cuerpo del JSON recibido
            StringBuilder jsonRecibido = new StringBuilder();
            BufferedReader reader = request.getReader();
            String linea;
            while ((linea = reader.readLine()) != null) {
                jsonRecibido.append(linea);
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> datos = gson.fromJson(jsonRecibido.toString(), Map.class);
            Map<String, Object> respuesta = new HashMap<>();

            // 2. Control de flujos según lo que venga en el JSON (Igual que hiciste con usuarios)
            
            // CASO A: ACCIÓN DE CERRAR SESIÓN (LOGOUT)
            if (datos != null && "logout".equals(datos.get("accion"))) {
                HttpSession session = request.getSession(false);
                if (session != null) {
                    session.invalidate();
                }
                respuesta.put("ok", true);
                respuesta.put("mensaje", "Sesión cerrada correctamente");
                response.getWriter().write(gson.toJson(respuesta));
                return;
            }

            // CASO B: ACCIÓN DE PUBLICAR / GUARDAR COCHE
            if (datos != null && datos.containsKey("marca") && datos.containsKey("modelo")) {
                HttpSession session = request.getSession(false);
                String vendedor = (session != null && session.getAttribute("usuario") != null) 
                                  ? (String) session.getAttribute("usuario") 
                                  : "Invitado";

                Coche nuevoCoche = new Coche();
                nuevoCoche.setMarca((String) datos.get("marca"));
                nuevoCoche.setModelo((String) datos.get("modelo"));
                nuevoCoche.setCiudad((String) datos.get("ciudad"));
                nuevoCoche.setPrecio(String.valueOf(datos.get("precio")));
                nuevoCoche.setKm(String.valueOf(datos.get("km")));
                nuevoCoche.setMotor((String) datos.get("motor"));
                nuevoCoche.setTransmision((String) datos.get("transmision"));
                nuevoCoche.setDesc((String) datos.get("desc"));
                nuevoCoche.setVendedor(vendedor);
                nuevoCoche.setEstado("pendiente"); // Se guarda en revisión del admin

                // Convertir la lista de imágenes (Base64) que viene en el JSON
                if (datos.get("imgs") instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> listaImgs = (List<String>) datos.get("imgs");
                    nuevoCoche.setImgs(listaImgs);
                }

                boolean guardadoExitoso = cocheDAO.guardarCoche(nuevoCoche);

                if (guardadoExitoso) {
                    respuesta.put("ok", true);
                    respuesta.put("mensaje", "Vehículo publicado con éxito. Pendiente de aprobación.");
                } else {
                    respuesta.put("ok", false);
                    respuesta.put("mensaje", "Error interno al guardar en la base de datos.");
                }
                
                response.getWriter().write(gson.toJson(respuesta));
                return;
            }

            // CASO C: BÚSQUEDA DE COCHES (COMPORTAMIENTO POR DEFECTO)
            String textoBusqueda = "";
            if (datos != null && datos.get("texto") != null) {
                textoBusqueda = ((String) datos.get("texto")).trim();
            }

            List<Coche> resultados = cocheDAO.buscarCoches(textoBusqueda);

            respuesta.put("ok", true);
            respuesta.put("resultados", resultados);
            response.getWriter().write(gson.toJson(respuesta));
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, Object> error = new HashMap<>();
            error.put("ok", false);
            error.put("mensaje", "Error en el controlador de vehículos: " + e.getMessage());
            response.getWriter().write(gson.toJson(error));
        }
    }
}