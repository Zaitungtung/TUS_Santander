package com.alce.tus.Types;

import com.google.gson.annotations.SerializedName;

public class Item {

    @SerializedName("dc:identifier")
    public int dc_identifier;

    @SerializedName("ayto:numero")
    public String ayto_numero;

    @SerializedName("ayto:parada")
    public String ayto_parada;

    @SerializedName("dc:name")
    public String dc_name;

    @SerializedName("ayto:Seccion")
    public int ayto_Seccion;

    @SerializedName("ayto:NombreParada")
    public String ayto_NombreParada;

    @SerializedName("ayto:NombreSublinea")
    public String ayto_NombreSublinea;

    @SerializedName("ayto:Linea")
    public int ayto_Linea;

    @SerializedName("ayto:Ruta")
    public int ayto_Ruta;

    @SerializedName("ayto:PuntoKM")
    public int ayto_PuntoKM;

    @SerializedName("ayto:NParada")
    public String ayto_NParada;

    @SerializedName("wgs84_pos:lat")
    public Double wgs84_pos_lat;

    @SerializedName("wgs84_pos:long")
    public Double wgs84_pos_long;


}