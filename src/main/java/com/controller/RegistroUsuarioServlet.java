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
import java.util.Map;

import com.model.Usuario;
import com.model.UsuarioDAO;

@WebServlet("/api/registrar-usuario")
public class RegistroUsuarioServlet extends HttpServlet {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "*"); 
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> respuestaJson = new HashMap<>();

        try {
            StringBuilder jsonRecibido = new StringBuilder();
            BufferedReader reader = request.getReader();
            String linea;
            while ((linea = reader.readLine()) != null) { 
                jsonRecibido.append(linea); 
            }

            @SuppressWarnings("unchecked")
            Map<String, String> datos = gson.fromJson(jsonRecibido.toString(), Map.class);
            
            if (datos == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                respuestaJson.put("ok", false);
                respuestaJson.put("mensaje", "Datos de petición vacíos.");
                response.getWriter().write(gson.toJson(respuestaJson));
                return;
            }

            String username = datos.get("username"); 
            String nombre = datos.get("nombre") != null ? datos.get("nombre") : username; 
            String apellidos = datos.get("apellidos") != null ? datos.get("apellidos") : "No especificado";
            String email = datos.get("email");
            String password = datos.get("password");
            
            if (username == null || username.trim().isEmpty() || 
                email == null || email.trim().isEmpty() || 
                password == null || password.trim().isEmpty()) {
                
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                respuestaJson.put("ok", false);
                respuestaJson.put("mensaje", "Todos los campos son obligatorios.");
                response.getWriter().write(gson.toJson(respuestaJson));
                return;
            }

            Usuario nuevoUsuario = usuarioDAO.registrarUsuarioYObtener(username.trim(), nombre.trim(), apellidos.trim(), email.trim(), password);

            if (nuevoUsuario != null) {
                HttpSession session = request.getSession(true); 
                session.setAttribute("usuarioLogueado", nuevoUsuario);

                response.setStatus(HttpServletResponse.SC_CREATED); 
                respuestaJson.put("ok", true);
                respuestaJson.put("mensaje", "¡Cuenta creada y sesión iniciada!");
                // MODIFICADO: Eliminada la línea que llamaba a nuevoUsuario.getUser()
            } else {
                response.setStatus(HttpServletResponse.SC_CONFLICT); 
                respuestaJson.put("ok", false);
                respuestaJson.put("mensaje", "El nombre de usuario o correo electrónico ya están en uso.");
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); 
            respuestaJson.put("ok", false);
            respuestaJson.put("mensaje", "Error crítico en el servidor: " + e.getMessage());
        }

        response.getWriter().write(gson.toJson(respuestaJson));
    }
}