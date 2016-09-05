package com.alce.tus.Types;

import java.util.HashMap;

/**
 * Type Near.
 */

public class Type_Near {
    private static HashMap searchable = null;
    private final String NumParada;
    private final String NParada;
    private final Double Lat;
    private final Double Lng;
    private final Double Dist;
    private final String Type;

    public Type_Near(String NumParada, String NParada, Double lat, Double lng, Double dist, String type) {
        this.NumParada = NumParada;
        this.NParada = NParada;
        this.Lat = lat;
        this.Lng = lng;
        this.Dist = dist;
        this.Type = type;
    }

    public static HashMap getSearchable() {
        return searchable;
    }

    public static void setSearchable(HashMap searchable) {
        Type_Near.searchable = searchable;
    }

    public String getNumParada() {
        return NumParada;
    }

    public String getNParada() {
        return NParada;
    }

    public Double getLat() {
        return Lat;
    }

    public Double getLng() {
        return Lng;
    }

    public Double getDist() {
        return Dist;
    }

    public String getType() {
        return Type;
    }
}