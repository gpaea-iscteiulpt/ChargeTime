package com.tese.chargingtime;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import static android.location.LocationManager.GPS_PROVIDER;
import static com.tese.chargingtime.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import static com.tese.chargingtime.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;

public class Menu extends AppCompatActivity {

    private boolean mLocationPermissionGranted = false;
    private static final String TAG = Home.class.getSimpleName();
    private Location mLastLocation;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationManager mLocationManager;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private StorageReference storageRef;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private ArrayList<Park> mParks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        storageRef = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        fillParks();
        getLocation();
        fillUserPoints();
    }

    private void fillParks() {
        mDatabase.child("parks").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Park mPark = postSnapshot.getValue(Park.class);
                    if(mPark.getName().equals("Red Park")){
                        mPark.setIconPicture(R.drawable.parkinghalf);
                    } else if(mPark.getName().equals("Blue Park")) {
                        mPark.setIconPicture(R.drawable.parkingfull);
                    } else {
                        mPark.setIconPicture(R.drawable.parkingfree);
                    }
                    mParks.add(mPark);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showMessage("Error accessing database.");
            }
        });
    }

    private void fillUserPoints(){
        View includeLayout = findViewById(R.id.custom_userpoints);
        TextView userPoints = (TextView) includeLayout.findViewById(R.id.userpoints);
        userPoints.setText(Html.fromHtml("<b>" + Constants.getUsername() + "</b> - " + Constants.getUserPoints() + " points"));
    }

    public void checkMap(View view) {
        Intent intent = new Intent(this, Maps.class);
        intent.putExtra("LastLocation", mLastLocation);
        intent.putExtra("WhereFrom", "FromMap");
        intent.putExtra("Parks", mParks);
        startActivity(intent);
    }

    public void checkNavigate(View view){
        Intent intent = new Intent(this, Home.class);
        intent.putExtra("LastLocation", mLastLocation);
        intent.putExtra("Parks", mParks);
        startActivity(intent);
    }

    public void takePhoto(View view){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isMapsEnabled()) {
            if (mLocationPermissionGranted) {
                getLocation();
            } else {
                getLocationPermission();
            }
        }
    }

    private void getLocation(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            mLastLocation = location;
                        }
                    }
                });
    }

    public boolean isMapsEnabled() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!mLocationManager.isProviderEnabled(GPS_PROVIDER)) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("This application requires GPS to run, do you want to enable it?").setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent enableGpsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
            return false;
        }
        return true;
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            getLocation();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS:
                if (mLocationPermissionGranted) {
                    mLastLocation = getLastKnownLocation();
                } else {
                    getLocationPermission();
                }
                break;
            case REQUEST_IMAGE_CAPTURE:
                if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
                    showMessage("Uploading image...");
                    StorageReference filepath = storageRef.child("Photos");

                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] byteArray = baos.toByteArray();

                    filepath.putBytes(byteArray).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            incrementPointsToUser();
                            showMessage("Upload successful!");
                        }
                    });

                }
                break;
        }
    }

    private void incrementPointsToUser(){
        mAuth = FirebaseAuth.getInstance();
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getEmail().replace(".", ""));
        Constants.incrementUserPoints();
        myRef.child("points").setValue(Constants.getUserPoints());
        fillUserPoints();
    }

    private Location getLastKnownLocation() {
        mLocationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }
            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    private void showMessage(String str){
        Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
    }

}
