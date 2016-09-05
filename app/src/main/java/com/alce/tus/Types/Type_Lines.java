package com.alce.tus.Types;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Type Lines.
 */
public class Type_Lines implements Parcelable {
    public static final Parcelable.Creator<Type_Lines> CREATOR = new Parcelable.Creator<Type_Lines>() {
        public Type_Lines createFromParcel(Parcel in) {
            return new Type_Lines(in);
        }

        public Type_Lines[] newArray(int size) {
            return new Type_Lines[size];
        }
    };
    private static ArrayList<Type_Lines> lines = null;
    private static List<Integer> linesPos;
    private final String linea;
    private final String numero;
    private final String nombre;
    private final String type;

    public Type_Lines(String linea, String numero, String nombre, String type) {
        this.linea = linea;
        this.numero = numero;
        this.nombre = nombre;
        this.type = type;
    }

    private Type_Lines(Parcel in) {
        linea = in.readString();
        numero = in.readString();
        nombre = in.readString();
        type = in.readString();
    }

    public static void setSublines(List<Integer> pos, ArrayList<Type_Lines> data) {
        linesPos = pos;
        lines = data;
    }

    public static List<Integer> getSublineasPos() {
        return linesPos;
    }

    public static ArrayList<Type_Lines> getSublines() {
        return lines;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(linea);
        dest.writeString(numero);
        dest.writeString(nombre);
        dest.writeString(type);
    }

    public String getLinea() {
        return linea;
    }

    public String getNumero() {
        return numero;
    }

    public String getNombre() {
        return nombre;
    }

    public String getType() {
        return type;
    }
}
