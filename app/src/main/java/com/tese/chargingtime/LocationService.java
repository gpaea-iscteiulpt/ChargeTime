package com.tese.chargingtime;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import static com.tese.chargingtime.Constants.FASTEST_INTERVAL;
import static com.tese.chargingtime.Constants.UPDATE_INTERVAL;


public class LocationService extends Service {

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final String TAG = LocationService.class.getSimpleName();
    private Location mLocation;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        super.onCreate();

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if(Build.VERSION.SDK_INT >= 26){
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "My Channel", NotificationManager.IMPORTANCE_DEFAULT);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID).setContentTitle("").setContentText("").build();
            startForeground(1, notification);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: called.");
        getLocation();
        return START_NOT_STICKY;
    }

    private void getLocation(){
        LocationRequest mLocalLocationRequestHighAccuracy = new LocationRequest();
        mLocalLocationRequestHighAccuracy.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocalLocationRequestHighAccuracy.setInterval(UPDATE_INTERVAL);
        mLocalLocationRequestHighAccuracy.setFastestInterval(FASTEST_INTERVAL);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "getLocation: stopping the location service.");
            stopSelf();
            return;
        }

        Log.d(TAG, "getLocation: getting location information.");
        mFusedLocationProviderClient.requestLocationUpdates(mLocalLocationRequestHighAccuracy, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult){
                Log.d(TAG, "onLocationResult: got location result.");

                Location location = locationResult.getLastLocation();

                if(location != null) {
                    mLocation = location;
                }
            }
        }, Looper.myLooper());

    }

}





