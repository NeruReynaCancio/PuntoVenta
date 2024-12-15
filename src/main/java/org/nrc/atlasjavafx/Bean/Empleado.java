package org.nrc.atlasjavafx.Bean;

public class Empleado {
    private String id;
    private String nombre;
    private String apellidos;
    private String contacto;
    private String genero;
    private String departamento;

    // Constructor
    public Empleado(String id, String nombre, String apellidos, String contacto, String genero, String departamento) {
        this.id = id;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.contacto = contacto;
        this.genero = genero;
        this.departamento = departamento;
    }

    // Getters
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getApellidos() { return apellidos; }
    public String getContacto() { return contacto; }
    public String getGenero() { return genero; }
    public String getDepartamento() { return departamento; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public void setContacto(String contacto) { this.contacto = contacto; }
    public void setGenero(String genero) { this.genero = genero; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }
}