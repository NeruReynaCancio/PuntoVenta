package org.nrc.atlasjavafx.Bean;

public class Proveedor {
    private String id;
    private String nombre;
    private String area;
    private String contacto;
    private String direccion;
    private String antiguedad;

    // Constructor
    public Proveedor(String id, String nombre, String area,
                     String contacto, String direccion, String antiguedad) {
        this.id = id;
        this.nombre = nombre;
        this.area = area;
        this.contacto = contacto;
        this.direccion = direccion;
        this.antiguedad = antiguedad;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public String getContacto() { return contacto; }
    public void setContacto(String contacto) { this.contacto = contacto; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getAntiguedad() { return antiguedad; }
    public void setAntiguedad(String antiguedad) { this.antiguedad = antiguedad; }
}