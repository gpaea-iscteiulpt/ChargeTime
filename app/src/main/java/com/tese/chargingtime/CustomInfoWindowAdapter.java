package com.tese.chargingtime;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoWindowAdapter implements InfoWindowAdapter {

    private Context mContext;

    public CustomInfoWindowAdapter(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {

        View view = ((Activity) mContext).getLayoutInflater().inflate(R.layout.custom_endlocation_marker, null);

        TextView title = view.findViewById(R.id.title);
        title.setText(marker.getTitle());

        TextView duration = view.findViewById(R.id.duration);
        duration.setText(marker.getSnippet());

        return view;

    }
}
