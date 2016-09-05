package com.alce.tus.Data;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.util.Calendar;

/**
 * Control low level database.
 */
public class Database extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String TEXT_TYPE = " TEXT";
    private static final String NUMBER_TYPE = " NUMBER";
    private static final String COMMA_SEP = ",";
    private static final String DB_DATA = "info.db";
    private static final String DB_FAV = "fav.db";
    private SQLiteDatabase mDatabase;
    private boolean data = false, fav = false;

    /**
     * Constructor
     * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
     *
     * @param context Application Context
     */

    public Database(Context context, String name) {
        super(context, name, null, DB_VERSION);

        if (name.equals(DB_DATA)) {
            this.data = true;
        }
        if (name.equals(DB_FAV)) {
            this.fav = true;
        }
    }

    /**
     * Check if Database exist
     *
     * @param mContext Context which need to search the database.
     * @return Boolean value => True if exist; False if not.
     */
    public static boolean checkDatabase(Context mContext) {
        File dbFile = mContext.getDatabasePath(DB_DATA);
        return dbFile.exists();
    }

    /**
     * Check if is time to update the database.
     *
     * @param mContext Context which need to access preferences.
     * @return If its time return true.
     */
    public static boolean isTime2Update(Context mContext) {
        Calendar rightNow = Calendar.getInstance();

        Boolean Summer = rightNow.get(Calendar.MONTH) == Calendar.JULY && rightNow.get(Calendar.DAY_OF_MONTH) == 1;
        Boolean Winter = rightNow.get(Calendar.MONTH) == Calendar.OCTOBER && rightNow.get(Calendar.DAY_OF_MONTH) == 1;

        SharedPreferences prefs =
                mContext.getSharedPreferences("preferences", Context.MODE_PRIVATE);

        // If is Summer & is not updated return true.
        // Otherwise return Winter AND !updated: 1 AND !0 = 1 (True)
        if (Summer)
            return !prefs.getBoolean("db_updated_Summer", false);
        return Winter && !prefs.getBoolean("db_updated_Winter", false);

    }

    /**
     * If is time to update, is necessary delete  the database.
     *
     * @param mContext Context which need to delete the database.
     * @return true if the database is successfully deleted.
     */
    public static void deleteDatabase(Context mContext, String DATABASE) {
        File dbFile = mContext.getDatabasePath(DATABASE);
        dbFile.delete();
    }

    /**
     * creates an empty database on the system and rewrites it *
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        if (data) {
            String SQL_CREATE_ENTRIES =
                    "CREATE TABLE " + "Lineas" + " (" +
                            "ID" + NUMBER_TYPE + COMMA_SEP +
                            "Linea" + NUMBER_TYPE + COMMA_SEP +
                            "Numero" + TEXT_TYPE + COMMA_SEP +
                            "Nombre" + TEXT_TYPE +
                            " )";

            db.execSQL(SQL_CREATE_ENTRIES);

            SQL_CREATE_ENTRIES =
                    "CREATE TABLE " + "Sublineas" + " (" +
                            "ID" + NUMBER_TYPE + COMMA_SEP +
                            "Linea" + NUMBER_TYPE + COMMA_SEP +
                            "Ruta" + NUMBER_TYPE + COMMA_SEP +
                            "PuntoKM" + NUMBER_TYPE + COMMA_SEP +
                            "Seccion" + NUMBER_TYPE + COMMA_SEP +
                            "NumParada" + NUMBER_TYPE + COMMA_SEP +
                            "NSublinea" + TEXT_TYPE + COMMA_SEP +
                            "NParada" + TEXT_TYPE +
                            " )";

            db.execSQL(SQL_CREATE_ENTRIES);

            SQL_CREATE_ENTRIES =
                    "CREATE TABLE " + "Paradas" + " (" +
                            "ID" + NUMBER_TYPE + COMMA_SEP +
                            "NumParada" + TEXT_TYPE + COMMA_SEP +
                            "NParada" + TEXT_TYPE + COMMA_SEP +
                            "Lat" + NUMBER_TYPE + COMMA_SEP +
                            "Lng" + NUMBER_TYPE +
                            " )";

            db.execSQL(SQL_CREATE_ENTRIES);

            SQL_CREATE_ENTRIES =
                    "CREATE TABLE " + "Bicis" + " (" +
                            "ID" + NUMBER_TYPE + COMMA_SEP +
                            "NParada" + TEXT_TYPE + COMMA_SEP +
                            "Lat" + NUMBER_TYPE + COMMA_SEP +
                            "Lng" + NUMBER_TYPE +
                            " )";

            db.execSQL(SQL_CREATE_ENTRIES);
        }
        if (fav) {
            String SQL_CREATE_ENTRIES =
                    "CREATE TABLE " + "Favoritos" + " (" +
                            "ID" + NUMBER_TYPE + COMMA_SEP +
                            "NumParada" + NUMBER_TYPE + COMMA_SEP +
                            "NParada" + TEXT_TYPE + COMMA_SEP +
                            "PName" + TEXT_TYPE + COMMA_SEP +
                            "PColor" + TEXT_TYPE +
                            " )";

            db.execSQL(SQL_CREATE_ENTRIES);
        }
    }

    @Override
    public synchronized void close() {
        if (mDatabase != null)
            mDatabase.close();
        super.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}