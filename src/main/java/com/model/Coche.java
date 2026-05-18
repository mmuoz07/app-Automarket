package com.model;

public class Coche {
    
    private int id;
    private String marca;
    private String modelo;
    private int ano;
    private int precio;
    private int kilometros;
    private String combustible;
    private String transmision;
    private String ubicacion;
    private String imagenUrl;
    private String descripcion;
    private String estado;
    private String publicadoPor;

    public Coche() {}

    // Getters y Setters completos
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public int getAno() { return ano; }
    public void setAno(int ano) { this.ano = ano; }

    public int getPrecio() { return precio; }
    public void setPrecio(int precio) { this.precio = precio; }

    public int getKilometros() { return kilometros; }
    public void setKilometros(int kilometros) { this.kilometros = kilometros; }

    public String getCombustible() { return combustible; }
    public void setCombustible(String combustible) { this.combustible = combustible; }

    public String getTransmision() { return transmision; }
    public void setTransmision(String transmision) { this.transmision = transmision; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getPublicadoPor() { return publicadoPor; }
    public void setPublicadoPor(String publicadoPor) { this.publicadoPor = publicadoPor; }
}