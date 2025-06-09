package org.nrc.atlasjavafx.Bean;

public class Direccion {
    private String calle;
    private String ciudad;
    private String cp;

    public Direccion(String calle, String ciudad, String cp) {
        this.calle = calle;
        this.ciudad = ciudad;
        this.cp = cp;
    }

    public String getCalle() { return calle; }
    public String getCiudad() { return ciudad; }
    public String getCp() { return cp; }

    public void setCalle(String calle) { this.calle = calle; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }
    public void setCp(String cp) { this.cp = cp; }
}