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
import java.util.Map;

// Importamos tu DAO del paquete model
import com.model.UsuarioDAO;

@WebServlet("/api/registrar-usuario")
public class RegistroUsuarioServlet extends HttpServlet {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Configuramos los mismos encodados que en tu buscador de coches
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> respuestaJson = new HashMap<>();

        try {
            // 1. Leer el JSON enviado desde el Frontend
            StringBuilder jsonRecibido = new StringBuilder();
            BufferedReader reader = request.getReader();
            String linea;
            while ((linea = reader.readLine()) != null) {
                jsonRecibido.append(linea);
            }

            // Mapeamos el JSON mapeando los valores de "username" y "password"
            Map<String, String> datos = gson.fromJson(jsonRecibido.toString(), Map.class);
            String username = datos != null ? datos.get("username") : null;
            String password = datos != null ? datos.get("password") : null;

            // Validación rápida de campos vacíos
            if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // Error 400
                respuestaJson.put("ok", false);
                respuestaJson.put("mensaje", "El usuario y la contraseña son obligatorios.");
                response.getWriter().write(gson.toJson(respuestaJson));
                return;
            }

            // 2. Llamamos a tu método boolean de UsuarioDAO
            boolean exito = usuarioDAO.registrarUsuario(username.trim(), password);

            if (exito) {
                // Todo salió bien (Tu DAO devolvió true: el usuario no existía y se guardó)
                response.setStatus(HttpServletResponse.SC_CREATED); // Estado 201 Created
                respuestaJson.put("ok", true);
                respuestaJson.put("mensaje", "¡Usuario registrado con éxito!");
            } else {
                // Tu DAO devolvió false (Ya existía el usuario o hubo un error SQL)
                response.setStatus(HttpServletResponse.SC_CONFLICT); // Estado 409 Conflict
                respuestaJson.put("ok", false);
                respuestaJson.put("mensaje", "El usuario ya existe o los datos son inválidos.");
            }

        } catch (Exception e) {
            // En caso de que ocurra cualquier otra excepción inesperada
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // Error 500
            respuestaJson.put("ok", false);
            respuestaJson.put("mensaje", "Error al procesar el registro en el servidor.");
        }

        // 3. Escribir la respuesta JSON final
        response.getWriter().write(gson.toJson(respuestaJson));
    }
}