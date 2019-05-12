package com.tese.chargingtime;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.model.Place;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Maps extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnPolylineClickListener, GoogleMap.OnInfoWindowClickListener{

    private static final String TAG = Maps.class.getSimpleName();
    private static final float DEFAULT_ZOOM = 15f;

    private GoogleMap mMap;
    private LatLngBounds mMapBoundary;
    private GeoApiContext mGeoApiContext = null;

    private ArrayList<PolylineData> mPolylinesData = new ArrayList<>();
    private Marker mMarkerSelected = null;
    private ArrayList<Marker> mTripMarkers = new ArrayList<>();
    private ArrayList<Marker> mMarkersArray = new ArrayList<>();
    private ArrayList<Station> mStations = new ArrayList<>();
    public ArrayList<RouteInformation> mRoutesInformation = new ArrayList<RouteInformation>();

    private String mWhereFrom;
    private Place mDestinationPlace;
    private Location mLocation;
    private Station mClosestStation;
    private float mClosestDistance;

    private double mBatteryLevelAtDestination;
    private double mMaximumReach;
    private int mCurrentBatteryLevel;
    public Station mCurrentStation;
    public HashMap<String, SnippetInformation> mPolylineInformation = new HashMap<String, SnippetInformation>();

    private Weather mCurrentWeather;
    private Date mCurrentDateAndTime;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

//        URL weatherUrl = WeatherApi.buildUrlWeather();
//        new JsonTask().execute(weatherUrl);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mCurrentDateAndTime = Calendar.getInstance().getTime();
        mStations = (ArrayList<Station>) getIntent().getExtras().getSerializable("Stations");
        mCurrentBatteryLevel = getIntent().getIntExtra("Current Battery Level", 100);

        getMaximumReach();

        Button btReset = (Button) findViewById(R.id.btReset);
        btReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetMap();
                addMapMarkers();
            }
        });

        if(mGeoApiContext == null){
            mGeoApiContext =  new GeoApiContext.Builder().apiKey(getString(R.string.google_maps_key)).build();
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //mLocation = (Location) getIntent().getParcelableExtra("LastLocation");
        Location newLocation = new Location("Dundee");
        newLocation.setLatitude(56.462033);
        newLocation.setLongitude(-2.970840);
        mLocation = newLocation;

        mWhereFrom = (String) getIntent().getStringExtra("WhereFrom");

        startLocationService();
        addMapMarkers();

        switch (mWhereFrom){
            case "FromMap":
                setCameraView(mLocation);
                break;
            case "FromSearch":
                prepareNavigate();
                break;
        }
        mMap.setOnMarkerClickListener(this);
        mMap.setOnPolylineClickListener(this);
    }

    private void prepareNavigate() {
        mDestinationPlace = (Place) getIntent().getParcelableExtra("DestinationPlace");
        Marker newMarker = mMap.addMarker(new MarkerOptions().position(mDestinationPlace.getLatLng()).title(mDestinationPlace.getId()));
        newMarker.setTag("Destination");
        Location temp = new Location(LocationManager.GPS_PROVIDER);
        temp.setLatitude(mDestinationPlace.getLatLng().latitude);
        temp.setLongitude(mDestinationPlace.getLatLng().longitude);

        checkBestStation();

        mMarkerSelected = mMarkersArray.get(0);
        Location destination = new Location("Marker Selected");
        destination.setLatitude(mMarkerSelected.getPosition().latitude);
        destination.setLongitude(mMarkerSelected.getPosition().longitude);
        mClosestDistance = Math.round(mLocation.distanceTo(destination));

        if (mMaximumReach >= mClosestDistance){
            calculateDirections(mMarkersArray.get(0));
        }else{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("With your current battery level you can't reach the closest charging station on the system. " +
                    "Closest charging station is at " + Math.round(mClosestDistance) + " meters from your location.")
                    .setTitle("No charging station found for the current battery level.");
            builder.setCancelable(false);
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
        setCameraView(temp);
    }

    private void checkBestStation(){

        Collections.sort(mMarkersArray, new Comparator<Marker>() {

            @Override
            public int compare(Marker a, Marker b) {
                Location locationA = new Location("point A");
                locationA.setLatitude(a.getPosition().latitude);
                locationA.setLongitude(a.getPosition().longitude);
                Location locationB = new Location("point B");
                locationB.setLatitude(b.getPosition().latitude);
                locationB.setLongitude(b.getPosition().longitude);
                float distanceOne = mLocation.distanceTo(locationA);
                float distanceTwo = mLocation.distanceTo(locationB);
                return Float.compare(distanceOne, distanceTwo);
            }
        });

        //Adicionar aqui os modelos de previsão.
        //Guardar informação das durações em algum lado para comparar.
        for(Marker marker : mMarkersArray){
            getRouteDurationAPI(marker);
        }
    }

    public double getRouteDurationAPI(Marker marker){
        String request = "https://maps.googleapis.com/maps/api/elevation/json?origin=" + mLocation.getLatitude() + "," + mLocation.getLongitude() +
                "&destination=" + marker.getPosition().latitude + "," + marker.getPosition().longitude + "&key=" + getString(R.string.google_api_key);
        StringBuffer response = new StringBuffer();
        HttpInformation httpInfo = new HttpInformation(request, response);
        new GetHttp().execute(httpInfo);
        int ret = httpInfo.value;
        double duration = 0;
        if (ret == 0) {
            try {
                JSONArray routes = new JSONObject(response.toString()).getJSONArray("routes");
                duration = getSmallestDuration(routes);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return duration;
    }

    private double getSmallestDuration(JSONArray routes){
        double duration = 0;
        for(int i = 0; i<routes.length(); i++){
            try {
                JSONObject obj = routes.getJSONObject(i);
                Log.d(TAG, obj.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return duration;
    }

    private void setCameraView(Location loc) {

        double bottomBoundary = loc.getLatitude() - .01;
        double leftBoundary = loc.getLongitude() - .01;
        double topBoundary = loc.getLatitude() + .01;
        double rightBoundary = loc.getLongitude() + .01;

        mMapBoundary = new LatLngBounds(new LatLng(bottomBoundary, leftBoundary), new LatLng(topBoundary, rightBoundary));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0));
    }

    private void addMapMarkers(){
        for (Station station : mStations) {
            Bitmap b = BitmapFactory.decodeResource(getResources(), station.getIcon());
            Bitmap icon = Bitmap.createScaledBitmap(b, b.getWidth()/2,b.getHeight()/2, false);
            Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(station.getLatitude(), station.getLongitude())).title(station.getName()).icon(BitmapDescriptorFactory.fromBitmap(icon)));
            marker.setTag(station);
            mMarkersArray.add(marker);
        }
        mMap.setOnInfoWindowClickListener(this);
    }

    private void startLocationService(){
        if(!isLocationServiceRunning()){
            Intent serviceIntent = new Intent(this, LocationService.class);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                Maps.this.startForegroundService(serviceIntent);
            } else{
                startService(serviceIntent);
            }
        }
    }

    private boolean isLocationServiceRunning(){
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if("com.".equals(service.service.getClassName())){
                Log.d(TAG, "isLocationServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.d(TAG, "isLocationServiceRunning: location service is not running.");
        return false;
    }

    private void removeTripMakers(){
        for(Marker marker: mTripMarkers){
            marker.remove();
        }
    }

    private void resetSelectedMarker(){
        if(mMarkerSelected != null) {
            mMarkerSelected.setVisible(true);
            mMarkerSelected = null;
            removeTripMakers();
        }
    }

    public void zoomRoute(List<LatLng> lstLatLngRoute) {
        if (mMap == null || lstLatLngRoute == null || lstLatLngRoute.isEmpty()) return;

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (LatLng latLngPoint : lstLatLngRoute)
            boundsBuilder.include(latLngPoint);

        int routePadding = 120;
        LatLngBounds latLngBounds = boundsBuilder.build();

        mMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding),
                600,
                null
        );
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = inflater.inflate(R.layout.custom_map_popup, null);

        if(!marker.getTag().equals("Destination")) {
            Station mStation = (Station) marker.getTag();

            mCurrentStation = mStation;

            mStation.setOccupancy(55);

            TextView name = (TextView) popupView.findViewById(R.id.name);
            name.setText(mStation.getName());
            TextView occupancy = (TextView) popupView.findViewById(R.id.occupancy);
            occupancy.setText(mStation.getOccupancy() + "%");
            TextView period = (TextView) popupView.findViewById(R.id.hours);
            period.setText("Open " + Integer.toString(mStation.getHours()) + "h");
            TextView price = (TextView) popupView.findViewById(R.id.price);
            price.setText(mStation.getCost() + "€ p/h");
            TextView chargingStations = (TextView) popupView.findViewById(R.id.charging_stations);
            chargingStations.setText("Charging Stations: " +  mStation.getNumberOfChargingPoints());
            TextView connectors = (TextView) popupView.findViewById(R.id.connectors);
            connectors.setText("Connectors: " + mStation.getConnectors());
            TextView type = (TextView) popupView.findViewById(R.id.type);
            type.setText(mStation.getType());

            Button go = (Button) popupView.findViewById(R.id.go);
            String tempString = "Navigate";
            SpannableString spanString = new SpannableString(tempString);
            spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);
            go.setText(spanString);

            int width = LinearLayout.LayoutParams.WRAP_CONTENT;
            int height = LinearLayout.LayoutParams.WRAP_CONTENT;
            boolean focusable = true;
            final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

            go.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    resetSelectedMarker();
                    mMarkerSelected = marker;
                    calculateDirections(marker);
                    popupWindow.dismiss();
                }
            });

            popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

            TextView close = (TextView) popupView.findViewById(R.id.close);
            close.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popupWindow.dismiss();
                    return true;
                }
            });

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                popupWindow.setElevation(20);
            }
        }

        return false;
    }


    public void calculateDirections(Marker marker){

        final Marker selectedMarker = marker;

        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                marker.getPosition().latitude,
                marker.getPosition().longitude
        );
        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext);

        directions.alternatives(true);
        directions.origin(
                new com.google.maps.model.LatLng(
                        mLocation.getLatitude(),
                        mLocation.getLongitude()
                )
        );
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                addPolylinesToMap(result, selectedMarker);
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e("Calculate", "onFailure: " + e.getMessage() );
            }
        });
    }

    private void addPolylinesToMap(final DirectionsResult result, Marker selected){
        final int batteryLevel = getElevationInformation(selected);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {

                if(mPolylinesData.size() > 0) {
                    for (PolylineData polylineData : mPolylinesData) {
                        polylineData.getPolyline().remove();
                    }
                    mPolylinesData.clear();
                }

                double duration = 999999999;

                for(DirectionsRoute route: result.routes){

                    if(route.legs[0].distance.inMeters <= (mMaximumReach * 100)) {

                        List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                        List<com.google.android.gms.maps.model.LatLng> newDecodedPath = new ArrayList<>();

                        for (com.google.maps.model.LatLng latLng : decodedPath) {
                            newDecodedPath.add(new com.google.android.gms.maps.model.LatLng(latLng.lat, latLng.lng));
                        }

                        Polyline polyline = mMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                        mPolylineInformation.put(polyline.getId(), new SnippetInformation(batteryLevel, (int) mCurrentStation.getOccupancy()));

                        polyline.setClickable(true);
                        polyline.setColor(ContextCompat.getColor(Maps.this, R.color.grey));
                        mPolylinesData.add(new PolylineData(polyline, route.legs[0]));

                        mMarkerSelected.setVisible(false);

                        double tempDuration = route.legs[0].duration.inSeconds;

                        if ((tempDuration < duration)){
                            duration = tempDuration;
                            onPolylineClick(polyline);
                            zoomRoute(polyline.getPoints());
                        }
                    } else {
                        showMessage("There are no available charging stations!");
                    }

                }
            }
        });
    }

    public int getElevationInformation(Marker selectedMarker){
        String request = "https://maps.googleapis.com/maps/api/elevation/json?locations=" + selectedMarker.getPosition().latitude
                + "," + selectedMarker.getPosition().longitude + "&key=" + getString(R.string.google_api_key);
        StringBuffer response = new StringBuffer();
        HttpInformation httpInfo = new HttpInformation(request, response);
        new GetHttp().execute(httpInfo);
        int ret = httpInfo.value;
        int batteryLevel = 0;
        if (ret == 0) {
            try {
                batteryLevel = getBatteryLevelAtDestination(new JSONObject(response.toString()).getJSONArray("results"), selectedMarker);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return batteryLevel;
    }

    public int getBatteryLevelAtDestination(JSONArray results, Marker selectedMarker){
        ArrayList<Float> elevationLevels = new ArrayList<>();
        for(int i = 0; i<results.length(); i++){
            try {
                elevationLevels.add(Float.valueOf(results.getJSONObject(i).getString("elevation")));
            } catch (JSONException e) {
                Log.d(TAG, "getBatteryLevelAtDestination: " + e.getMessage());
                e.printStackTrace();
            }
        }

        double batteryLevelAtDestination = mCurrentBatteryLevel;
        double elevation = elevationLevels.get(1) - elevationLevels.get(0);
        int distance = Math.abs(getDistanceBetweenTwoPoints(selectedMarker));
        double slope = (elevation / distance);
        if (slope > 0) {
            batteryLevelAtDestination = batteryLevelAtDestination + (((((elevation / distance) * 9.8) * 1500) * 13.33) * 0.8);
        }

        batteryLevelAtDestination = batteryLevelAtDestination - (distance * Constants.getConsumptionPerMeter());

        if (batteryLevelAtDestination>Constants.getMaximumBatteryCapacity()){
            batteryLevelAtDestination = Constants.getMaximumBatteryCapacity();
        }

        return (int) batteryLevelAtDestination;
    }

    private int getDistanceBetweenTwoPoints(Marker selected){
        Location destination = new Location("Point A");
        destination.setLatitude(selected.getPosition().latitude);
        destination.setLongitude(selected.getPosition().longitude);
        Location currentLocation = new Location("Point B");
        currentLocation.setLatitude(mLocation.getLatitude());
        currentLocation.setLongitude(mLocation.getLongitude());
        return Math.round(destination.distanceTo(currentLocation));
    }


    private void getMaximumReach(){
        mMaximumReach = Math.round((mCurrentBatteryLevel * Constants.getMaximumDistance())) * 10;
    }

    private void resetMap(){
        if(mMap != null) {
            mMap.clear();

            if(mPolylinesData.size() > 0){
                mPolylinesData.clear();
                mPolylinesData = new ArrayList<>();
            }
        }
    }

    @Override
    public void onPolylineClick(Polyline polyline) {

        int index = 0;
        for(PolylineData polylineData: mPolylinesData){
            index++;
            if(polyline.getId().equals(polylineData.getPolyline().getId())){
                polylineData.getPolyline().setColor(ContextCompat.getColor(this, R.color.lightblue));
                polylineData.getPolyline().setZIndex(1);

                LatLng endLocation = new LatLng(polylineData.getLeg().endLocation.lat, polylineData.getLeg().endLocation.lng);
                SnippetInformation info = mPolylineInformation.get(polyline.getId());
                Marker marker = mMap.addMarker(new MarkerOptions().position(endLocation)
                                            .title("Duration: " + polylineData.getLeg().duration)
                                        .snippet("Battery at Destination: " + info.batteryLevel + "% | Occupancy at ETA: " + info.ocuppancy + "%"));

                marker.showInfoWindow();

                mTripMarkers.add(marker);
            }
            else{
                polylineData.getPolyline().setColor(ContextCompat.getColor(this, R.color.grey));
                polylineData.getPolyline().setZIndex(0);
            }
        }
    }

    @Override
    public void onInfoWindowClick(final Marker markerSelected) {
        if(markerSelected.getTitle().contains("Trip: #")){
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Open Google Maps?")
                    .setCancelable(true)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            String latitude = String.valueOf(markerSelected.getPosition().latitude);
                            String longitude = String.valueOf(markerSelected.getPosition().longitude);
                            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude);
                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                            mapIntent.setPackage("com.google.android.apps.maps");

                            try{
                                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                                    startActivity(mapIntent);
                                }
                            }catch (NullPointerException e){
                                Toast.makeText(Maps.this, "Couldn't open map", Toast.LENGTH_SHORT).show();
                            }

                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            dialog.cancel();
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        }
    }

    public class JsonTask extends AsyncTask<URL, Void, String> {

        @Override
        protected String doInBackground(URL... urls) {
            URL weatherUrl = urls[0];
            String weatherSearchResults = null;

            try{
                weatherSearchResults = WeatherApi.getResponseForAPI(weatherUrl);
            }catch (IOException ioe){
                ioe.printStackTrace();
            }

            return weatherSearchResults;

        }

        @Override
        protected void onPostExecute(String weatherSearchResults){
            if(weatherSearchResults != null && !weatherSearchResults.equals("")){
                mCurrentWeather = parseJSON(weatherSearchResults);
            }
        }

        private Weather parseJSON(String weatherSearchResults){

            if(weatherSearchResults != null){
                try {
                    JSONObject rootObject = new JSONObject(weatherSearchResults);
                    mCurrentWeather = new Weather();
                    JSONArray weather = rootObject.getJSONArray("weather");
                    JSONObject weatherObj = weather.getJSONObject(0);
                    mCurrentWeather.setDescription(weatherObj.getString("description"));
                    mCurrentWeather.setMain(weatherObj.getString("main"));
                    JSONObject main = rootObject.getJSONObject("main");
                    mCurrentWeather.setTemperature(main.getDouble("temp"));
                    mCurrentWeather.setTemperatureMax(main.getDouble("temp_max"));
                    mCurrentWeather.setTemperatureMin(main.getDouble("temp_min"));
                    mCurrentWeather.setHumidity(main.getInt("humidity"));
                    mCurrentWeather.setPressure(main.getLong("pressure"));
                    JSONObject wind = rootObject.getJSONObject("wind");
                    mCurrentWeather.setWindSpeed(wind.getDouble("speed"));
                    mCurrentWeather.setWindDeg(wind.getInt("deg"));
                    mCurrentWeather.setCloudsPercentage(rootObject.getJSONObject("clouds").getInt("all"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return mCurrentWeather;
        }
    }

    private void showMessage(String str){
        Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
    }

    public class SnippetInformation{

        private int batteryLevel;
        private int ocuppancy;

        public SnippetInformation(int batteryLevel, int ocuppancy){
            this.batteryLevel = batteryLevel;
            this.ocuppancy = ocuppancy;
        }

    }

}