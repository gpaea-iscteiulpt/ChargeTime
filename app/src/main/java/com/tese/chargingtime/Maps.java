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
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
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
import java.util.Date;
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
    private ArrayList<Park> mParks = new ArrayList<>();

    private String mWhereFrom;
    private Place mDestinationPlace;
    private Location mLocation;
    private Park mClosestPark;
    private float mClosestDistance;

    private Weather mCurrentWeather;
    private Date mCurrentDateAndTime;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        URL weatherUrl = WeatherApi.buildUrlWeather();
        new JsonTask().execute(weatherUrl);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mCurrentDateAndTime = Calendar.getInstance().getTime();
        mParks = (ArrayList<Park>) getIntent().getExtras().getSerializable("Parks");

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
        mLocation = (Location) getIntent().getParcelableExtra("LastLocation");
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

    private void prepareNavigate(){
        int mSearchRadius = (int) getIntent().getIntExtra("Radius", 50);
        mDestinationPlace = (Place) getIntent().getParcelableExtra("DestinationPlace");
        Marker newMarker = mMap.addMarker(new MarkerOptions().position(mDestinationPlace.getLatLng()).title(mDestinationPlace.getId()));
        newMarker.setTag("Destination");
        Location temp = new Location(LocationManager.GPS_PROVIDER);
        temp.setLatitude(mDestinationPlace.getLatLng().latitude);
        temp.setLongitude(mDestinationPlace.getLatLng().longitude);

        Circle circle = mMap.addCircle(new CircleOptions()
                .center(newMarker.getPosition())
                .radius(mSearchRadius)
                .strokeColor(getColor(R.color.circleStrokeBlue))
                .fillColor(getColor(R.color.circleInsideBlue)));

        float lessDistance = checkParkInsideRadius(circle);
        if(lessDistance > 0) {
            for (Marker marker : mMarkersArray) {
                if (marker.getTag().equals(mClosestPark)) {
                    mMarkerSelected = marker;
                    calculateDirections(marker);
                    break;
                }
            }
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Closest parking lot is at " + Math.round(mClosestDistance) + " meters from the destination. Want to navigate to there?")
                    .setTitle("No parking lot found in the search radius.");
            builder.setPositiveButton("Navigate", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    for (Marker marker : mMarkersArray) {
                        if (marker.getTag().equals(mClosestPark)) {
                            mMarkerSelected = marker;
                            calculateDirections(marker);
                            break;
                        }
                    }
                }
            });
            builder.setCancelable(false);
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                    finish();
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
        setCameraView(temp);
    }

    private float checkParkInsideRadius(Circle circle){
        float[] distance = new float[2];
        float lessDistance = 0;
        mClosestDistance = 0;

        for(Park park : mParks) {
            Location.distanceBetween(park.getLatitude(), park.getLongitude(),
                    circle.getCenter().latitude, circle.getCenter().longitude, distance);

            if (distance[0] < circle.getRadius() && (lessDistance > distance[0] || lessDistance == 0)) {
                lessDistance = distance[0];
            }

            if (distance[0] < mClosestDistance || mClosestDistance == 0) {
                mClosestDistance = distance[0];
                mClosestPark = park;
            }
        }

        return lessDistance;
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
        for (Park park : mParks) {
            Bitmap b = BitmapFactory.decodeResource(getResources(), park.getIconPicture());
            Bitmap icon = Bitmap.createScaledBitmap(b, b.getWidth()/5,b.getHeight()/5, false);
            Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(park.getLatitude(), park.getLongitude())).title(park.getName()).icon(BitmapDescriptorFactory.fromBitmap(icon)));
            marker.setTag(park);
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

        if(marker.getTag().equals("Destination")) {

        }else {

            Park mPark = (Park) marker.getTag();

            TextView name = (TextView) popupView.findViewById(R.id.name);
            name.setText(mPark.getName());
            TextView description = (TextView) popupView.findViewById(R.id.description);
            description.setText(mPark.getDescription());
            TextView address = (TextView) popupView.findViewById(R.id.address);
            address.setText(mPark.getAddress());
            TextView occupancy = (TextView) popupView.findViewById(R.id.occupancy);
            occupancy.setText(mPark.getOccupancyPercentage() + "%");
            TextView hours = (TextView) popupView.findViewById(R.id.hours);
            hours.setText(mPark.getWorkHours());
            TextView period = (TextView) popupView.findViewById(R.id.period);
            period.setText(mPark.getWorkPeriod());
            TextView price = (TextView) popupView.findViewById(R.id.price);
            price.setText(mPark.getPricePerHour() + "€ p/h");

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
                addPolylinesToMap(result);
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e("Calculate", "onFailure: " + e.getMessage() );
            }
        });
    }

    private void addPolylinesToMap(final DirectionsResult result){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {

                if(mPolylinesData.size() > 0){
                    for(PolylineData polylineData: mPolylinesData){
                        polylineData.getPolyline().remove();
                    }
                    mPolylinesData.clear();
                }

                double duration = 99999999;
                for(DirectionsRoute route: result.routes){
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                    List<com.google.android.gms.maps.model.LatLng> newDecodedPath = new ArrayList<>();

                    for(com.google.maps.model.LatLng latLng: decodedPath){
                        newDecodedPath.add(new com.google.android.gms.maps.model.LatLng(latLng.lat, latLng.lng));
                    }

                    Polyline polyline = mMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));

                    polyline.setClickable(true);
                    polyline.setColor(ContextCompat.getColor(Maps.this, R.color.grey));
                    mPolylinesData.add(new PolylineData(polyline, route.legs[0]));

                    mMarkerSelected.setVisible(false);

                    double tempDuration = route.legs[0].duration.inSeconds;
                    if(tempDuration < duration){
                        duration = tempDuration;
                        onPolylineClick(polyline);
                        zoomRoute(polyline.getPoints());
                    }
                }
            }
        });
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
                Marker marker = mMap.addMarker(new MarkerOptions().position(endLocation)
                                        .title("Trip: #" + index)
                                        .snippet("Duration: " + polylineData.getLeg().duration));

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

}