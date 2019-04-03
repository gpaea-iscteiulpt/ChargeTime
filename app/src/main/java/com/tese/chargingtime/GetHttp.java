package com.tese.chargingtime;

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

public class GetHttp {

    public static int GetHttpToServer(String urlLink, StringBuffer response) {
        try {
            URL obj = new URL(urlLink);
            HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
        } catch (MalformedURLException ex) {
            Log.e("GetHttp", Log.getStackTraceString(ex));
            return 2;
        } catch (NoRouteToHostException ex) {
            Log.e("GetHttp", Log.getStackTraceString(ex));
            return 3;
        } catch (SocketTimeoutException ex){
            Log.e("GetHttp", Log.getStackTraceString(ex));
            return 4;
        } catch (SSLException ex){
            Log.e("GetHttp", Log.getStackTraceString(ex));
            return 5;
        } catch (IOException ex) {
            Log.e("GetHttp", Log.getStackTraceString(ex));
            return 6;
        } catch (Exception e){
            Log.e("GetHttp", Log.getStackTraceString(e));
            return 7;
        }
        return 0;
    }

}
