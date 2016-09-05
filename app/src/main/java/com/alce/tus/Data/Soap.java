package com.alce.tus.Data;

import android.util.Log;

import com.alce.tus.Types.Type_Info;

import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.alce.tus.Data.Json.convertInputStreamToString;

/**
 * Extract the information about the bus data.
 */

@SuppressWarnings("FieldCanBeLocal")
public class Soap {

    private static final String NAMESPACE = "http://tempuri.org/";
    private static final String URL = "http://www.ayto-santander.es:9001/services/dinamica.asmx?";
    private static final String METHOD_NAME = "GetPasoParada";
    private static final String METHOD_NAME_RESULT = "GetPasoParadaResult";
    private static final String SOAP_ACTION = "http://tempuri.org/GetPasoParada";
    private static final String PROPERTY = "parada";

    private final ArrayList<Type_Info> values = new ArrayList<>();

    public ArrayList<Type_Info> getInfoBus(String Id) {
        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;

        request.addProperty(PROPERTY, Id);
        envelope.setOutputSoapObject(request);
        HttpTransportSE a = new HttpTransportSE(URL);

        try {

            a.call(SOAP_ACTION, envelope);
            SoapObject result = (SoapObject) envelope.bodyIn;
            SoapObject Object = (SoapObject) result.getProperty(METHOD_NAME_RESULT);

            int i = 0;
            do {
                SoapObject Object2 = (SoapObject) Object.getProperty(i);

                String linea = Object2.getProperty("linea").toString();
                String ruta = Object2.getProperty("ruta").toString();

                SoapObject linea1 = (SoapObject) Object2.getProperty("e1");
                int tiempo1 = Integer.parseInt(linea1.getProperty("minutos").toString());
                int distancia1 = Integer.parseInt(linea1.getProperty("metros").toString());

                SoapObject linea2 = (SoapObject) Object2.getProperty("e2");
                int tiempo2 = Integer.parseInt(linea2.getProperty("minutos").toString());
                int distancia2 = Integer.parseInt(linea2.getProperty("metros").toString());

                values.add(new Type_Info(linea, ruta, tiempo1, distancia1, tiempo2, distancia2));

                i++;
            } while (i != Object.getPropertyCount());

            Collections.sort(values, new Comparator<Type_Info>() {
                @Override
                public int compare(Type_Info lhs, Type_Info rhs) {
                    int time1 = lhs.getTiempo1();
                    int time2 = rhs.getTiempo1();
                    return time1 - time2;
                }
            });

        } catch (Exception e) {
            Log.d("DEBUG", e.toString());
        }

        return values;

    }

    public ArrayList<Type_Info> getInfoBike(String ID) {

        try {
            URL url = new URL("https://api.jcdecaux.com/vls/v1/stations/" + ID +
                    "?contract=Santander&apiKey=7d1208434834eb568ebf7a2b2d9785edc422e588");

            URLConnection urlConnection = url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());

            String xml_parse = convertInputStreamToString(in);
            JSONObject jsonObject = new JSONObject(xml_parse);

            String name = jsonObject.getString("name");
            String[] realName = name.split("_");
            String stands = jsonObject.getString("available_bike_stands");
            String bikes = jsonObject.getString("available_bikes");

            ArrayList<Type_Info> arrayList = new ArrayList<>();
            arrayList.add(new Type_Info(realName[1], stands, bikes));
            return arrayList;

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}