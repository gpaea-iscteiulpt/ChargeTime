package com.tese.chargingtime;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.ArrayList;
import java.util.Arrays;


public class Home extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = Home.class.getSimpleName();
    private Location mLastLocation;
    private Place mDestinationPlace;
    public ArrayList<Park> mParks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        }

        mLastLocation = (Location) getIntent().getParcelableExtra("LastLocation");
        mParks = (ArrayList<Park>) getIntent().getExtras().getSerializable("Parks");

        PlacesClient placesClient = Places.createClient(this);

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.input_search);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG));
        autocompleteFragment.setCountry("pt");
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                mDestinationPlace = place;
            }

            @Override
            public void onError(Status status) {
                Toast.makeText(Home.this, status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        fillUserPoints();
    }

    private void fillUserPoints(){
        View includeLayout = findViewById(R.id.custom_userpoints);
        TextView userPoints = (TextView) includeLayout.findViewById(R.id.userpoints);
        userPoints.setText(Html.fromHtml("<b>" + Constants.getUsername() + "</b> - " + Constants.getUserPoints() + " points"));
    }

    public void goForSearch(View view){
        Intent intent = new Intent(this, Maps.class);
        intent.putExtra("LastLocation", mLastLocation);
        intent.putExtra("WhereFrom", "FromSearch");
        intent.putExtra("DestinationPlace", mDestinationPlace);
        intent.putExtra("Parks", mParks);
        EditText radius = (EditText) findViewById(R.id.searchRadius);
        Constants.setSearchRadius(Integer.parseInt(radius.getText().toString()));
        intent.putExtra("Radius", Constants.SEARCH_RADIUS);
        startActivity(intent);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) { }

}
