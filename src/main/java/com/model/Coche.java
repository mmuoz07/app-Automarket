package com.model;

import java.util.List;

public class Coche {
    private int id;
    private String marca;
    private String modelo;
    private int precio; // Cambiado a String para coincidir con el DAO y Servlet
    private int km;     // Cambiado a String para coincidir con el DAO y Servlet
    private int ano;       // Mantenido como int para el filtro de años
    private String motor;
    private String transmision;
    private String ciudad;
    private List<String> imgs; // Lista de Strings para las imágenes en Base64 o URLs
    private String descripcion;       // Atributo "desc" mapeado a la descripción
    private String vendedor;
    private String estado;
    private String combustible;
    private String ubicacion;
    // Constructor vacío por defecto (Obligatorio para Gson)
    public Coche() {
    }

    // Getters y Setters Estrictos
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public int getPrecio() {
        return precio;
    }

    public void setPrecio(int precio) {
        this.precio = precio;
    }

    public int getKm() {
        return km;
    }

    public void setKm(int km) {
        this.km = km;
    }

    public int getAno() {
        return ano;
    }

    public void setAno(int ano) {
        this.ano = ano;
    }

    public String getMotor() {
        return motor;
    }

    public void setMotor(String motor) {
        this.motor = motor;
    }

    public String getTransmision() {
        return transmision;
    }

    public void setTransmision(String transmision) {
        this.transmision = transmision;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public List<String> getImgs() {
        return imgs;
    }

    public void setImgs(List<String> imgs) {
        this.imgs = imgs;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getVendedor() {
        return vendedor;
    }

    public void setVendedor(String vendedor) {
        this.vendedor = vendedor;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCombustible(){
        return combustible;
    }

    public void  setCombustible(String combustible){
        this.combustible = combustible;
    }

    public String getUbicacion(){
        return ubicacion;
    }

    public void  setUbicacion   (String ubicacion){
        this.ubicacion = ubicacion;
    }

}