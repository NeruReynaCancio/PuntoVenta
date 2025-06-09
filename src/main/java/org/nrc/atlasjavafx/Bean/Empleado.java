package org.nrc.atlasjavafx.Bean;

public class Empleado {
    private String id;
    private String nombre;
    private String apellidos;
    private String contacto;
    private String genero;
    private String departamento;
    private Direccion direccion; // Nuevo campo

    public Empleado(String id, String nombre, String apellidos, String contacto, String genero, String departamento, Direccion direccion) {
        this.id = id;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.contacto = contacto;
        this.genero = genero;
        this.departamento = departamento;
        this.direccion = direccion;
    }

    // Getters
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getApellidos() { return apellidos; }
    public String getContacto() { return contacto; }
    public String getGenero() { return genero; }
    public String getDepartamento() { return departamento; }
    public Direccion getDireccion() { return direccion; }
    // Setters
    public void setId(String id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public void setContacto(String contacto) { this.contacto = contacto; }
    public void setGenero(String genero) { this.genero = genero; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }
    public void setDireccion(Direccion direccion) { this.direccion = direccion; }

}