package com.alce.tus.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.alce.tus.Data.Database;
import com.alce.tus.Data.DatabaseManager;

import java.util.Calendar;

/**
 * Splash Screen.
 */

public class SplashActivity extends Activity {

    private String season_update;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Task task = new Task();
        task.execute("");
    }

    private class Task extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... strings) {
            SharedPreferences prefs = getApplicationContext()
                    .getSharedPreferences("preferences", Context.MODE_PRIVATE);
            String DB_DATA = "info.db";
            if (!Database.checkDatabase(getApplicationContext())) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("summer_update", true);
                editor.putBoolean("winter_update", true);
                editor.putBoolean("bikes_update", true);
                editor.apply();

                return 0;
            }
            if (Database.isTime2Update(getApplicationContext())) {

                Calendar rightNow = Calendar.getInstance();
                Boolean Summer = rightNow.get(Calendar.MONTH) == Calendar.JULY && rightNow.get(Calendar.DAY_OF_MONTH) == 1;
                Boolean Winter = rightNow.get(Calendar.MONTH) == Calendar.OCTOBER && rightNow.get(Calendar.DAY_OF_MONTH) == 1;

                if (Summer)
                    season_update = "summer";
                if (Winter)
                    season_update = "winter";

                if ((Summer && !prefs.getBoolean("summer_update", false) ||
                        (Winter && !prefs.getBoolean("winter_update", false)))) {
                    Database.deleteDatabase(getApplicationContext(), DB_DATA);
                } else {
                    return 2;
                }

                return 1;
            } else if (!prefs.getBoolean("bikes_update", false)) {
                // Forzamos el borrado de la base de datos para inicializar los valores de las estaciones
                // de bicicletas
                Database.deleteDatabase(getApplicationContext(), DB_DATA);

                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("bikes_update", true);
                editor.apply();

                return 11;
            } else {
                Database database = new Database(getApplicationContext(), DB_DATA);
                SQLiteDatabase db = database.getReadableDatabase();
                DatabaseManager databaseManager = new DatabaseManager(getApplicationContext(), db, DB_DATA);
                databaseManager.extractLines();
                databaseManager.extractBikes();
                databaseManager.getSearchInfo();

                String DB_FAV = "fav.db";
                database = new Database(getApplicationContext(), DB_FAV);
                database.getWritableDatabase();
                return 2;
            }


        }

        @Override
        protected void onPostExecute(Integer database) {
            super.onPostExecute(database);
            Intent intent;

            Log.d("DEBUG", "Type : " + Integer.toString(database));

            switch (database) {
                case 0:
                    intent = new Intent(getApplicationContext(), Introduction.class);
                    intent.putExtra("Type", 0);
                    break;
                case 1:
                    intent = new Intent(getApplicationContext(), Introduction.class);
                    intent.putExtra("Type", 1);
                    intent.putExtra("event", season_update);
                    break;
                case 11:
                    intent = new Intent(getApplicationContext(), Introduction.class);
                    intent.putExtra("Type", 1);
                    intent.putExtra("event", "bikeUpdate");
                    break;
                default:
                    intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.putExtra("Type", 2);
                    break;
            }

            startActivity(intent);
            finish();
        }
    }
}