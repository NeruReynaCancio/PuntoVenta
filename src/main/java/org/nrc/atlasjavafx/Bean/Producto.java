package org.nrc.atlasjavafx.Bean;

public class Producto {
    private String id;
    private String nombre;
    private String precio;
    private String stock;
    private String categoria;

    public Producto(String id, String nombre, String precio, String stock, String categoria) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.categoria = categoria;
    }

    // Getters
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getPrecio() { return precio; }
    public String getStock() { return stock; }
    public String getCategoria() { return categoria; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setPrecio(String precio) { this.precio = precio; }
    public void setStock(String stock) { this.stock = stock; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
}