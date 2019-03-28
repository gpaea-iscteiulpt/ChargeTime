package com.tese.chargingtime;

import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class WeatherApi {

    private final static String WEATHER_BASE_URL = "http://api.openweathermap.org/data/2.5/weather?lat=38.7223&lon=-9.1393&units=metric";
    private final static String ACCUWEATHER_API_KEY = "0181b5b1e1a58fd0e62a1fcf9673353e";
    private final static String PARAM_API_KEY = "APPID";

    protected static String getResponseForAPI(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream input = urlConnection.getInputStream();
            Scanner scanner = new Scanner(input);
            scanner.useDelimiter("\\A");
            boolean hasInput = scanner.hasNext();
            if(hasInput){
                return scanner.next();
            }else{
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            urlConnection.disconnect();
        }
        return null;
    }

    protected static URL buildUrlWeather(){
        Uri buildUri = Uri.parse(WEATHER_BASE_URL).buildUpon().appendQueryParameter(PARAM_API_KEY, ACCUWEATHER_API_KEY).build();
        URL url = null;
        try{
            url = new URL(buildUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }
}
