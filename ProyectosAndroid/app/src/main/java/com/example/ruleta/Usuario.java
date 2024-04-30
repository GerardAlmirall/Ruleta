package com.example.ruleta;

public class Usuario {

    private int id;
    private String nombreUsuario;
    private int monedasTotales;

    public Usuario(Integer id,String nombreUsuario, int monedasTotales) {
        this.id = id;
        this.nombreUsuario = nombreUsuario;
        this.monedasTotales = monedasTotales;
    }

    // Getters y setters
    public Integer id() {
        return id;
    }

    public void setid(Integer id) {
        this.id = id;
    }
    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public int getMonedasTotales() {
        return monedasTotales;
    }

    public void setMonedasTotales(int monedasTotales) {
        this.monedasTotales = monedasTotales;
    }
}