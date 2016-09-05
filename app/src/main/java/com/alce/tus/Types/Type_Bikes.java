package com.alce.tus.Types;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Alberto on 16/8/16.
 */
public class Type_Bikes implements Parcelable {

    public static final Parcelable.Creator<Type_Bikes> CREATOR = new Parcelable.Creator<Type_Bikes>() {
        public Type_Bikes createFromParcel(Parcel in) {
            return new Type_Bikes(in);
        }

        public Type_Bikes[] newArray(int size) {
            return new Type_Bikes[size];
        }
    };
    private static ArrayList<Type_Bikes> data = null;
    private final String id;
    private final String name;
    private final String lat;
    private final String lng;

    public Type_Bikes(String id, String name, String lat, String lng) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
    }

    private Type_Bikes(Parcel in) {
        id = in.readString();
        name = in.readString();
        lat = in.readString();
        lng = in.readString();
    }

    public static ArrayList<Type_Bikes> getStations() {
        return data;
    }

    public static void setStations(ArrayList<Type_Bikes> station) {
        data = station;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(lat);
        dest.writeString(lng);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLat() {
        return lat;
    }

    public String getLng() {
        return lng;
    }
}
