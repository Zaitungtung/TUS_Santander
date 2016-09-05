package com.alce.tus.Types;

/**
 * Type Fav.
 */
public class Type_Fav {
    private String number, name, customName, type;
    private int customColor, pos;
    private Double lat, lng;

    public Type_Fav() {

    }

    public Type_Fav(String number, String name, int pos, String customName, int customColor,
                    String type, Double lat, Double lng) {
        this.number = number;
        this.name = name;
        this.customName = customName;
        this.customColor = customColor;
        this.pos = pos;
        this.type = type;
        this.lat = lat;
        this.lng = lng;
    }

    public Type_Fav(String number, String name, int pos, String customName, int customColor) {
        this.number = number;
        this.name = name;
        this.customName = customName;
        this.customColor = customColor;
        this.pos = pos;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public int getCustomColor() {
        return customColor;
    }

    public void setCustomColor(int customColor) {
        this.customColor = customColor;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }
}