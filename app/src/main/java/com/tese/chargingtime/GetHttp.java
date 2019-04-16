package com.tese.chargingtime;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.URL;

import javax.net.ssl.SSLException;

public class GetHttp extends AsyncTask<HttpInformation, Void, Void> {

    @Override
    protected Void doInBackground(HttpInformation... params) {
        try {
            URL obj = new URL( params[0].url);
            HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                params[0].response.append(inputLine);
            }
            in.close();
        } catch (MalformedURLException ex) {
            Log.e("GetHttp", Log.getStackTraceString(ex));
            params[0].value = 2;
        } catch (NoRouteToHostException ex) {
            Log.e("GetHttp", Log.getStackTraceString(ex));
            params[0].value = 3;
        } catch (SocketTimeoutException ex){
            Log.e("GetHttp", Log.getStackTraceString(ex));
            params[0].value = 4;
        } catch (SSLException ex){
            Log.e("GetHttp", Log.getStackTraceString(ex));
            params[0].value = 5;
        } catch (IOException ex) {
            Log.e("GetHttp", Log.getStackTraceString(ex));
            params[0].value = 6;
        } catch (Exception e){
            Log.e("GetHttp", Log.getStackTraceString(e));
            params[0].value = 7;
        }
        params[0].value = 0;
        return null;
    }
}




