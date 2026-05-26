# Proyecto Jakarta EE + Docker Compose + MySQL + phpMyAdmin

Este proyecto incluye:

- **Servidor**: Tomcat 10 (compatible con Jakarta Servlet)
- **Maven**: compilación del proyecto Java en una imagen Docker multietapa
- **Frontend**: HTML, CSS y JavaScript (SPA / Fetch API)
- **Backend**: Servlets + Arquitectura DAO (Data Access Object)
- **Conector MySQL**: MySQL Connector/J
- **Base de datos**: MySQL 8
- **Gestor visual**: phpMyAdmin

## Estructura Real del Proyecto

```bash
jakartaee-docker-compose-project/
├── docker-compose.yml
├── Dockerfile
├── pom.xml
├── README.md
├── mysql/
│   └── init/
│       └── 01-bd1.sql
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       ├── controller/
        │       │   ├── BuscarCochesServlet.java
        │       │   ├── CrearCochesServlet.java
        │       │   └── RegistroUsuarioServlet.java
        │       └── model/
        │           ├── Coche.java
        │           ├── CocheDAO.java
        │           ├── ConexionBD.java
        │           ├── Usuario.java
        │           └── UsuarioDAO.java
        └── webapp/
            ├── css/
            │   └── estilos.css
            ├── js/
            │   └── app.js
            ├── WEB-INF/
            │   └── web.xml
            └── index.html