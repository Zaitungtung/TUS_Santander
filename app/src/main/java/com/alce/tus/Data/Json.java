package com.alce.tus.Data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.alce.tus.Types.Item;
import com.alce.tus.Types.SearchResponse;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 * With this class update the Database.
 */
public class Json {

    // http://datos.santander.es/api/rest/datasets/programacionTUS_horariosLineas.json?items=14499&data=ayto:tipoDia,ayto:nombreParada,ayto:descEvento,ayto:tipoParada,ayto:hora&query=ayto\:tipoDia:160N1VLA&Order=ayto\:hora&sort=desc

    public static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line, result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;
        inputStream.close();
        return result;
    }

    public void main(Context mContext, SQLiteDatabase db) {

        URL[] url = new URL[4];
        JSONArray jsonArray = null;

        String DB_DATA = "info.db";
        DatabaseManager databaseManager = new DatabaseManager(mContext, db, DB_DATA);

        for (int p = 0; p != 4; p++) {
            try {
                url[0] = new URL("http://datos.santander.es/api/rest/datasets/lineas_bus.json?data=dc:identifier,ayto:numero,dc:name");
                url[1] = new URL("http://datos.santander.es/api/rest/datasets/lineas_bus_secuencia.json?items=5000&data=ayto:PuntoKM,ayto:Seccion,ayto:Linea,ayto:Ruta,ayto:NParada,ayto:NombreSublinea,ayto:NombreParada");
                url[2] = new URL("http://datos.santander.es/api/rest/datasets/paradas_bus.json?items=5000&data=ayto:numero,ayto:parada,wgs84_pos:lat,wgs84_pos:long");
                url[3] = new URL("https://api.jcdecaux.com/vls/v1/stations?contract=Santander&apiKey=7d1208434834eb568ebf7a2b2d9785edc422e588");

                URLConnection urlConnection = url[p].openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                Log.d("DEBUG", Integer.toString(p));

                Gson gson = new Gson();
                Reader reader = new InputStreamReader(in);
                ArrayList<Item> results = null;
                ContentValues[] cvArray;
                if (p < 3) {
                    SearchResponse response = gson.fromJson(reader, SearchResponse.class);
                    results = response.items;
                    cvArray = new ContentValues[results.size()];
                } else {
                    String xml_parse = convertInputStreamToString(in);
                    jsonArray = new JSONArray(xml_parse);
                    cvArray = new ContentValues[jsonArray.length()];
                }

                switch (p) {
                    case 0:
                        for (int i = 0; i != results.size(); i++) {
                            ContentValues values = new ContentValues();
                            values.put("Linea", results.get(i).dc_identifier);
                            values.put("Numero", results.get(i).ayto_numero);
                            values.put("Nombre", results.get(i).dc_name);

                            cvArray[i] = values;
                        }
                        databaseManager.insertData(cvArray, 0);
                        Log.d("DEBUG", "Termine 1");
                        break;
                    case 1:
                        for (int i = 0; i != results.size(); i++) {
                            ContentValues values = new ContentValues();
                            int Seccion = results.get(i).ayto_Seccion;
                            switch (Seccion) {
                                case 140:
                                    Seccion = 137;
                                    break;
                                case 148:
                                    Seccion = 145;
                                    break;
                                case 154:
                                    Seccion = 159;
                                    break;
                                case 106:
                                    Seccion = 102;
                                    break;
                                case 110:
                                    Seccion = 107;
                                    break;
                                case 184:
                                    Seccion = 180;
                                    break;
                                case 57:
                                    Seccion = 191;
                                    break;
                                case 67:
                                    Seccion = 72;
                                    break;
                            }
                            values.put("Linea", results.get(i).ayto_Linea);
                            values.put("Ruta", results.get(i).ayto_Ruta);
                            values.put("PuntoKM", results.get(i).ayto_PuntoKM);
                            values.put("Seccion", Integer.toString(Seccion));
                            values.put("NumParada", results.get(i).ayto_NParada);
                            values.put("NSublinea", results.get(i).ayto_NombreSublinea);
                            values.put("NParada", results.get(i).ayto_NombreParada);

                            cvArray[i] = values;
                        }
                        databaseManager.insertData(cvArray, 1);
                        Log.d("DEBUG", "Termine 2");
                        break;
                    case 2:
                        for (int i = 0; i != results.size(); i++) {
                            ContentValues values = new ContentValues();
                            values.put("NumParada", results.get(i).ayto_numero);
                            values.put("NParada", results.get(i).ayto_parada);
                            values.put("Lat", results.get(i).wgs84_pos_lat);
                            values.put("Lng", results.get(i).wgs84_pos_long);

                            cvArray[i] = values;
                        }
                        databaseManager.insertData(cvArray, 2);
                        Log.d("DEBUG", "Termine 3");
                        break;
                    case 3:
                        for (int i = 0; i != jsonArray.length(); i++) {
                            JSONObject jObject = jsonArray.getJSONObject(i);

                            JSONObject position = jObject.getJSONObject("position");

                            String name = jObject.getString("name");
                            String[] realName = name.split("_");

                            ContentValues values = new ContentValues();
                            values.put("ID", jObject.getString("number"));
                            values.put("NParada", realName[1]);
                            values.put("Lat", position.getString("lat"));
                            values.put("Lng", position.getString("lng"));

                            cvArray[i] = values;
                        }
                        databaseManager.insertData(cvArray, 3);
                        Log.d("DEBUG", "Termine 4");
                        break;
                }
            } catch (Exception e) {
                Log.e("JSONException", "Error: " + e.toString());
            }
        }
    }
}