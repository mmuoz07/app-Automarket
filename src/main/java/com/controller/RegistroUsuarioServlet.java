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

// Importaciones correctas de tus clases del paquete model
import com.model.Usuario;
import com.model.UsuarioDAO;

@WebServlet("/api/registrar-usuario")
public class RegistroUsuarioServlet extends HttpServlet {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> respuestaJson = new HashMap<>();

        try {
            // Leer el cuerpo del JSON enviado por el cliente
            StringBuilder jsonRecibido = new StringBuilder();
            BufferedReader reader = request.getReader();
            String linea;
            while ((linea = reader.readLine()) != null) { 
                jsonRecibido.append(linea); 
            }

            // Mapeamos los datos del JSON de entrada
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
            String email = datos.get("email");
            String password = datos.get("password");

            // Validación estricta de campos vacíos
            if (username == null || username.trim().isEmpty() || 
                email == null || email.trim().isEmpty() || 
                password == null || password.trim().isEmpty()) {
                
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                respuestaJson.put("ok", false);
                respuestaJson.put("mensaje", "Todos los campos son obligatorios.");
                response.getWriter().write(gson.toJson(respuestaJson));
                return;
            }

            // Llamar al DAO para registrar y procesar al usuario
            Usuario nuevoUsuario = usuarioDAO.registrarUsuarioYObtener(username.trim(), email.trim(), password);

            if (nuevoUsuario != null) {
                // Crear o recuperar la sesión en el servidor Jakarta EE
                HttpSession session = request.getSession(true); 
                
                // Guardamos el objeto usuario completo en la sesión
                session.setAttribute("usuarioLogueado", nuevoUsuario);

                response.setStatus(HttpServletResponse.SC_CREATED); // 201 Created
                respuestaJson.put("ok", true);
                respuestaJson.put("mensaje", "¡Cuenta creada y sesión iniciada!");
                respuestaJson.put("rol", nuevoUsuario.getRol());
            } else {
                response.setStatus(HttpServletResponse.SC_CONFLICT); // 409 Conflict
                respuestaJson.put("ok", false);
                respuestaJson.put("mensaje", "El nombre de usuario o correo electrónico ya están en uso.");
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500 Internal Error
            respuestaJson.put("ok", false);
            respuestaJson.put("mensaje", "Error crítico en el servidor: " + e.getMessage());
        }

        // Enviar la respuesta JSON estructurada al cliente
        response.getWriter().write(gson.toJson(respuestaJson));
    }
}