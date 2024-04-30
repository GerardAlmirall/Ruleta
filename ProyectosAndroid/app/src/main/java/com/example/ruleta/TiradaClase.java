package com.example.ruleta;

public class TiradaClase {
    private long id;
    private int resultado;
    private int premioSeleccionado;
    private int apuesta;
    private long usuarioid;
    private int monedasTotales;

    public TiradaClase(long id, int resultado, int premioSeleccionado, int apuesta, long usuarioid, int monedasTotales) {
        this.id = id;
        this.resultado = resultado;
        this.premioSeleccionado = premioSeleccionado;
        this.apuesta = apuesta;
        this.usuarioid = usuarioid;
        this.monedasTotales = monedasTotales;
    }


    // Getters y setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public int getResultado() { return resultado; }
    public void setResultado(int resultado) { this.resultado = resultado; }
    public int getPremioSeleccionado() { return premioSeleccionado; }
    public void setPremioSeleccionado(int premioSeleccionado) { this.premioSeleccionado = premioSeleccionado; }
    public int getApuesta() { return apuesta; }
    public void setApuesta(int apuesta) { this.apuesta = apuesta; }
    public long getUsuarioid() { return usuarioid; }
    public void setUsuarioid(long usuarioid) { this.usuarioid = usuarioid; }
    public int getMonedasTotales() { return monedasTotales; }
    public void setMonedasTotales(int monedasTotales) { this.monedasTotales = monedasTotales; }
}
