package com.alce.tus.Types;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Type Sublines.
 */
public class Type_Sublines implements Parcelable {
    public static final Parcelable.Creator<Type_Sublines> CREATOR = new Parcelable.Creator<Type_Sublines>() {
        public Type_Sublines createFromParcel(Parcel in) {
            return new Type_Sublines(in);
        }

        public Type_Sublines[] newArray(int size) {
            return new Type_Sublines[size];
        }
    };
    private static int[] sublineasPos;
    private static ArrayList<Type_Sublines> sublines;
    private final String numParada;
    private final String nSubline;
    private final String nParada;

    public Type_Sublines(String numParada, String nSubline, String nParada) {
        this.numParada = numParada;
        this.nSubline = nSubline;
        this.nParada = nParada;
    }

    private Type_Sublines(Parcel in) {
        numParada = in.readString();
        nSubline = in.readString();
        nParada = in.readString();
    }

    public static void setArrayList(int[] pos, ArrayList<Type_Sublines> data) {
        sublineasPos = pos;
        sublines = data;
    }

    public static int[] getSublineasPos() {
        return sublineasPos;
    }

    public static ArrayList<Type_Sublines> getSublines() {
        return sublines;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(numParada);
        dest.writeString(nSubline);
        dest.writeString(nParada);
    }

    public String getNumParada() {
        return numParada;
    }

    public String getnSubline() {
        return nSubline;
    }

    public String getnParada() {
        return nParada;
    }
}
