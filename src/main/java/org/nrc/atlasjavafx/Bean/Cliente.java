package org.nrc.atlasjavafx.Bean;

public class Cliente {
    private String id;
    private String nombre;
    private String apellidos;
    private int edad;
    private String genero;
    private String contacto;

    // Constructor
    public Cliente(String id, String nombre, String apellidos, int edad, String genero, String contacto) {
        this.id = id;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.edad = edad;
        this.genero = genero;
        this.contacto = contacto;
    }

    // Getters - deben coincidir exactamente con los nombres usados en PropertyValueFactory
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getApellidos() { return apellidos; }
    public int getEdad() { return edad; }
    public String getGenero() { return genero; }
    public String getContacto() { return contacto; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public void setEdad(int edad) { this.edad = edad; }
    public void setGenero(String genero) { this.genero = genero; }
    public void setContacto(String contacto) { this.contacto = contacto; }
}