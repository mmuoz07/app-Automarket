CREATE DATABASE automarket_db;
USE automarket_db;

CREATE TABLE coches (
    id INT AUTO_INCREMENT PRIMARY KEY,
    marca VARCHAR(50) NOT NULL,
    modelo VARCHAR(50) NOT NULL,
    ano INT NOT NULL,
    precio INT NOT NULL,
    km INT NOT NULL,
    combustible VARCHAR(30),
    transmision VARCHAR(30),
    ubicacion VARCHAR(100),
    imgs JSON,
    descripcion TEXT,
    estado VARCHAR(20) DEFAULT 'Pendiente',
    publicado_por VARCHAR(100)
);

CREATE TABLE usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    username VARCHAR(30) NOT NULL
);