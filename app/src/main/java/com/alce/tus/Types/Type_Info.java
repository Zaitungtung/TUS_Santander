package com.alce.tus.Types;

/**
 * Type Info.
 */
public class Type_Info {

    private String linea;
    private String ruta;
    private int tiempo1;
    private int tiempo2;
    private int distancia1;
    private int distancia2;

    private String name;
    private String stands;
    private String bikes;

    public Type_Info(String name, String stands, String bikes) {
        this.name = name;
        this.stands = stands;
        this.bikes = bikes;
    }

    public Type_Info(String linea, String ruta, int tiempo1, int distancia1,
                     int tiempo2, int distancia2) {
        this.linea = linea;
        this.ruta = ruta;
        this.tiempo1 = tiempo1;
        this.tiempo2 = tiempo2;
        this.distancia1 = distancia1;
        this.distancia2 = distancia2;
    }

    public String getLinea() {
        return linea;
    }

    public String getRuta() {
        return ruta;
    }

    public int getTiempo1() {
        return tiempo1;
    }

    public int getTiempo2() {
        return tiempo2;
    }

    public int getDistancia1() {
        return distancia1;
    }

    public int getDistancia2() {
        return distancia2;
    }

    public String getName() {
        return name;
    }

    public String getStands() {
        return stands;
    }

    public String getBikes() {
        return bikes;
    }
}
