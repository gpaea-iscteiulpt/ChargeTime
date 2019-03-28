package com.tese.chargingtime;

import android.location.Location;
import android.view.View;
import android.widget.PopupWindow;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.GeoApiContext;

import java.util.ArrayList;

public class CustomOnClickListener implements View.OnClickListener {

    private Marker marker;
    private GeoApiContext mGeoApiContext;
    private Location mLocation;
    private GoogleMap mMap;
    private PopupWindow mPopupWindow;
    private ArrayList<PolylineData> mPolylinesData;

    public CustomOnClickListener(Marker marker, GeoApiContext mGeoApiContext, Location mLocation, GoogleMap mMap, PopupWindow mPopupWindow, ArrayList<PolylineData> mPolylinesData) {
        this.marker = marker;
        this.mGeoApiContext = mGeoApiContext;
        this.mLocation = mLocation;
        this.mMap = mMap;
        this.mPopupWindow = mPopupWindow;
        this.mPolylinesData = mPolylinesData;
    }

    @Override
    public void onClick(View v) {
        //calculateDirections(marker);
    }
}
