CREATE DATABASE automarket_db;
USE automarket_db;

-- 2. Creamos la tabla de COCHES con todos los campos de tu Figma
CREATE TABLE coches (
    id INT AUTO_INCREMENT PRIMARY KEY,
    marca VARCHAR(50) NOT NULL,       -- Ej: BMW
    modelo VARCHAR(50) NOT NULL,      -- Ej: Serie 3
    ano INT NOT NULL,                 -- Ej: 2020
    precio INT NOT NULL,              -- Ej: 28500
    kilometros INT NOT NULL,          -- Ej: 45000
    combustible VARCHAR(30),          -- Diésel, Gasolina, etc.
    transmision VARCHAR(30),          -- Manual/Automático
    ubicacion VARCHAR(100),           -- Madrid, Barcelona...
    imagen_url VARCHAR(500),          -- La URL de la foto del coche
    descripcion TEXT,                 -- El texto largo de abajo
    estado VARCHAR(20) DEFAULT 'Pendiente', -- IMPORTANTE: Para el Admin
    publicado_por VARCHAR(100)        -- <--- ¡Quitado el 'Usuario Demo'!
);

-- 3. Creamos la tabla de USUARIOS (Para el Login y Registro que pediste)
CREATE TABLE usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    rol VARCHAR(20) DEFAULT 'user'    -- 'user' o 'admin'
);