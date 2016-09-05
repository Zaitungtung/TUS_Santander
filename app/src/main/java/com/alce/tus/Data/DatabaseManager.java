package com.alce.tus.Data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;

import com.alce.tus.Types.Type_Bikes;
import com.alce.tus.Types.Type_Fav;
import com.alce.tus.Types.Type_Lines;
import com.alce.tus.Types.Type_Near;
import com.alce.tus.Types.Type_Sublines;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Control the function of the Database.
 */
public class DatabaseManager extends Database {

    private final SQLiteDatabase db;

    /**
     * Constructor
     * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
     *
     * @param context Application Context
     */
    public DatabaseManager(Context context, SQLiteDatabase db, String database) {
        super(context, database);
        this.db = db;
    }

    /**
     * Insert the necesary data in the "Linea" and "Sublinea" Tables.
     *
     * @param contentValues Values for the table
     * @param type          0 - "Linea"'s Table; 1 - "Sublinea"'s Table; 2 - "Parada"'s Table.
     */
    public void insertData(ContentValues[] contentValues, int type) {
        if (db != null && contentValues != null) {
            if (type == 0) {
                int ID;
                for (int i = 0; i != contentValues.length; i++) {
                    switch (contentValues[i].getAsInteger("Linea")) {
                        case 51:
                            ID = 5;
                            break;
                        case 52:
                            ID = 6;
                            break;
                        case 61:
                            ID = 7;
                            break;
                        case 62:
                            ID = 8;
                            break;
                        case 71:
                            ID = 9;
                            break;
                        case 72:
                            ID = 10;
                            break;
                        case 30:
                            ID = 50;
                            break;
                        case 31:
                            ID = 51;
                            break;
                        default:
                            ID = contentValues[i].getAsInteger("Linea");
                            break;
                    }

                    db.execSQL("INSERT INTO Lineas (ID, Linea, Numero, Nombre) " +
                            "VALUES (" + ID + ", '" +
                            contentValues[i].get("Linea") + "', '" +
                            contentValues[i].get("Numero") + "', '" +
                            contentValues[i].get("Nombre") + "')");

                }
            }
            if (type == 1) {
                for (int i = 0; i != contentValues.length; i++) {
                    db.execSQL("INSERT INTO Sublineas (ID, Linea, Ruta, PuntoKM, Seccion, NumParada, NSublinea, NParada) " +
                            "VALUES (" + i + ", " +
                            contentValues[i].get("Linea") + "," +
                            contentValues[i].get("Ruta") + "," +
                            contentValues[i].get("PuntoKM") + "," +
                            contentValues[i].get("Seccion") + "," +
                            contentValues[i].get("NumParada") + ",'" +
                            contentValues[i].get("NSublinea") + "','" +
                            contentValues[i].get("NParada")
                            + "')");
                }
            }
            if (type == 2) {
                for (int i = 0; i != contentValues.length; i++) {
                    db.execSQL("INSERT INTO Paradas (ID, NumParada, NParada, Lat, Lng) " +
                            "VALUES (" + i + ", " +
                            contentValues[i].get("NumParada") + ",'" +
                            contentValues[i].get("NParada") + "'," +
                            contentValues[i].get("Lat") + "," +
                            contentValues[i].get("Lng")
                            + ")");
                }
            }
            if (type == 3) {
                for (int i = 0; i != contentValues.length; i++) {
                    db.execSQL("INSERT INTO Bicis (ID, NParada, Lat, Lng) " +
                            "VALUES (" +
                            contentValues[i].get("ID") + ",'" +
                            contentValues[i].get("NParada") + "'," +
                            contentValues[i].get("Lat") + "," +
                            contentValues[i].get("Lng")
                            + ")");
                }

                db.close();
            }

        }
    }

    public void extractLines() {
        Cursor c = db.rawQuery("SELECT Linea, Numero, Nombre FROM Lineas ORDER BY ID", null);

        ArrayList<Type_Lines> arrayList = new ArrayList<>();
        List<Integer> position = new ArrayList<>();
        Boolean N = true, E = true, U = true;
        String Type = "";
        int i = 0;

        if (c.moveToFirst()) {
            do {
                String Linea = c.getString(0);
                String Numero = c.getString(1);
                String Nombre = c.getString(2);

                if (Numero.substring(0, 1).equals("N") && N) {
                    Type = "Nocturno";
                    i++;
                    position.add(i);
                    arrayList.add(new Type_Lines(null, null, null, Type));
                    N = false;
                } else if (Numero.substring(0, 1).equals("E") && E) {
                    Type = "Especial";
                    i++;
                    position.add(i);
                    arrayList.add(new Type_Lines(null, null, null, Type));
                    E = false;
                } else if (U) {
                    Type = "Urbano";
                    position.add(i);
                    arrayList.add(new Type_Lines(null, null, null, Type));
                    U = false;
                }

                arrayList.add(new Type_Lines(Linea, Numero, Nombre, Type));
                i++;
                position.add(0);

            } while (c.moveToNext());
        }

        c.close();

        Type_Lines.setSublines(position, arrayList);

    }

    public void extractBikes() {
        Cursor c = db.rawQuery("SELECT ID, NParada, Lat, Lng FROM Bicis ORDER BY ID", null);

        ArrayList<Type_Bikes> arrayList = new ArrayList<>();

        if (c.moveToFirst()) {
            do {
                String id = c.getString(0);
                String name = c.getString(1);
                String lat = c.getString(2);
                String lng = c.getString(3);

                arrayList.add(new Type_Bikes(id, name, lat, lng));
            } while (c.moveToNext());
        }

        c.close();

        Type_Bikes.setStations(arrayList);

    }


    @SuppressWarnings("unchecked")
    public void extractSublines(String line) {
        String[] args = new String[]{line};
        Cursor c = db.rawQuery("SELECT MIN(ID) AS ID FROM Sublineas WHERE Linea=? GROUP BY Ruta", args);

        int numberSublines = c.getCount();

        String command;
        if (line.equals("101") || line.equals("13")) {
            command = "SELECT Ruta, NumParada, NSublinea, NParada " +
                    "FROM Sublineas WHERE Linea=? ORDER BY Ruta, PuntoKM";
        } else {
            command = "SELECT Ruta, NumParada, NSublinea, NParada " +
                    "FROM Sublineas WHERE Linea=? ORDER BY Ruta, Seccion";
        }
        c = db.rawQuery(command, args);

        ArrayList<Type_Sublines> arrayList = new ArrayList();
        int[] sublinesPos = new int[numberSublines];

        if (c.moveToFirst()) {
            String currentSubline, previousSubline = c.getString(0);
            int pos = 0, i = 0;
            do {
                currentSubline = c.getString(0);

                String NumParada = c.getString(1);
                String NSublinea = c.getString(2);
                String NParada = c.getString(3);

                arrayList.add(new Type_Sublines(NumParada, NSublinea, NParada));

                if (!previousSubline.equals(currentSubline)) {
                    sublinesPos[i] = pos;
                    i++;
                    previousSubline = currentSubline;
                }

                pos++;
            } while (c.moveToNext());

            sublinesPos[numberSublines - 1] = pos - 1;
        }

        c.close();

        Type_Sublines.setArrayList(sublinesPos, arrayList);
    }

    public int countFav() {
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM Favoritos", null);
        int count = c.getCount();
        c.close();
        return count;

    }

    public void insertFav(Type_Fav favorite) {

        ContentValues newRegister = new ContentValues();
        newRegister.put("ID", favorite.getNumber());
        newRegister.put("NumParada", favorite.getNumber());
        newRegister.put("NParada", favorite.getName());
        newRegister.put("PName", favorite.getCustomName());
        newRegister.put("PColor", favorite.getCustomColor());

        db.insert("Favoritos", null, newRegister);
    }

    public void updateFav(Type_Fav favorite) {

        ContentValues newRegister = new ContentValues();
        newRegister.put("ID", favorite.getNumber());
        newRegister.put("NumParada", favorite.getNumber());
        newRegister.put("NParada", favorite.getName());
        newRegister.put("PName", favorite.getCustomName());
        newRegister.put("PColor", favorite.getCustomColor());

        String Where = "ID=" + favorite.getNumber();
        db.update("Favoritos", newRegister, Where, null);
    }

    public void deleteFav(String ID) {

        String Where = "NumParada=" + ID;
        db.delete("Favoritos", Where, null);
    }

    public ArrayList<Type_Fav> getFav(Boolean ALL, String ID) {

        ArrayList<Type_Fav> arrayList = new ArrayList<>();
        if (ALL) {
            Cursor c = db.rawQuery("SELECT NumParada, NParada, PName, PColor FROM Favoritos", null);

            int i = 0;
            if (c.moveToFirst()) {
                do {
                    String NumParada = c.getString(0);
                    String NParada = c.getString(1);
                    String PName = c.getString(2);
                    int PColor = Integer.parseInt(c.getString(3));

                    arrayList.add(new Type_Fav(NumParada, NParada, i, PName, PColor));
                    i++;
                } while (c.moveToNext());
            }

            c.close();
        } else {
            Cursor c = db.rawQuery("SELECT NumParada, NParada, PName, PColor FROM Favoritos WHERE ID=" + ID, null);

            int i = 0;
            if (c.moveToFirst()) {
                do {
                    String NumParada = c.getString(0);
                    String NParada = c.getString(1);
                    String PName = c.getString(2);
                    int PColor = Integer.parseInt(c.getString(3));
                    ;

                    arrayList.add(new Type_Fav(NumParada, NParada, i, PName, PColor));
                    i++;
                } while (c.moveToNext());
            }

            c.close();
        }

        return arrayList;
    }

    public ArrayList<Type_Near> getNearStops(Double lat, Double lng) {
        ArrayList<Type_Near> arrayList = new ArrayList<>();

        Cursor c = db.rawQuery("SELECT NumParada, NParada, Lat, Lng FROM Paradas", null);

        if (c.moveToFirst()) {
            arrayList.add(0, new Type_Near(null, null, 0.0, 0.0, 0.0, ""));
            do {
                String NumParada = c.getString(0);
                String NParada = c.getString(1);
                Double Lat = Double.parseDouble(c.getString(2));
                Double Lng = Double.parseDouble(c.getString(3));

                Location locationA = new Location("A");

                locationA.setLatitude(lat);
                locationA.setLongitude(lng);

                Location locationB = new Location("B");

                locationB.setLatitude(Lat);
                locationB.setLongitude(Lng);

                Double dist = (double) locationA.distanceTo(locationB);

                arrayList.add(new Type_Near(NumParada, NParada, Lat, Lng, dist, "bus"));

            } while (c.moveToNext());
        }

        c.close();

        c = db.rawQuery("SELECT ID, NParada, Lat, Lng FROM Bicis", null);

        if (c.moveToFirst()) {
            do {
                String NumParada = c.getString(0);
                String NParada = c.getString(1);
                Double Lat = Double.parseDouble(c.getString(2));
                Double Lng = Double.parseDouble(c.getString(3));

                Location locationA = new Location("A");

                locationA.setLatitude(lat);
                locationA.setLongitude(lng);

                Location locationB = new Location("B");

                locationB.setLatitude(Lat);
                locationB.setLongitude(Lng);

                Double dist = (double) locationA.distanceTo(locationB);

                arrayList.add(new Type_Near(NumParada, NParada, Lat, Lng, dist, "bike"));

            } while (c.moveToNext());
        }

        c.close();

        Collections.sort(arrayList, new Comparator<Type_Near>() {
            @Override
            public int compare(Type_Near lhs, Type_Near rhs) {
                return lhs.getDist().compareTo(rhs.getDist());
            }
        });

        return arrayList;
    }

    public void getSearchInfo() {
        HashMap<String, List<String>> hashMap = new HashMap<>();
        List<String> NumParada = new ArrayList<>();
        List<String> NParada = new ArrayList<>();
        List<String> Type = new ArrayList<>();
        List<String> Lat = new ArrayList<>();
        List<String> Lng = new ArrayList<>();

        Cursor c = db.rawQuery("SELECT NumParada, NParada FROM Paradas", null);

        if (c.moveToFirst()) {
            do {
                NumParada.add(c.getString(0));
                NParada.add(c.getString(1));
                Lat.add("");
                Lng.add("");
                Type.add("bus");

            } while (c.moveToNext());
        }

        c.close();

        c = db.rawQuery("SELECT ID, NParada, Lat, Lng FROM Bicis", null);

        if (c.moveToFirst()) {
            do {
                NumParada.add(c.getString(0));
                NParada.add(c.getString(1));
                Lat.add(c.getString(2));
                Lng.add(c.getString(3));
                Type.add("bike");

            } while (c.moveToNext());
        }

        c.close();

        hashMap.put("NumParada", NumParada);
        hashMap.put("NParada", NParada);
        hashMap.put("Type", Type);
        hashMap.put("Lat", Lat);
        hashMap.put("Lng", Lng);

        Type_Near.setSearchable(hashMap);
    }

    public Location getLatLng(String numParada) {
        Cursor c = db.rawQuery("SELECT Lat, Lng FROM Paradas WHERE NumParada='" + numParada + "'", null);

        Location location = new Location("");
        if (c.moveToFirst()) {
            double lat = Double.parseDouble(c.getString(0));
            double lng = Double.parseDouble(c.getString(1));

            location.setLatitude(lat);
            location.setLongitude(lng);
        }
        c.close();

        return location;
    }

    public Location isBike(String name) {
        Cursor c = db.rawQuery("SELECT NParada, Lat, Lng FROM Bicis WHERE NParada='" + name + "'", null);

        boolean isBike;
        isBike = c.getCount() == 1;

        Location location = new Location("");
        if (isBike) {
            c.moveToFirst();
            Double Lat = Double.parseDouble(c.getString(1));
            Double Lng = Double.parseDouble(c.getString(2));
            location.setLatitude(Lat);
            location.setLongitude(Lng);
        } else {
            location = null;
        }
        c.close();

        return location;
    }
}