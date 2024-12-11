package edu.niu.android.wheresmycar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.Manifest;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int LOCATION_REQUEST_CODE = 101;
    private static final String PREFS_NAME = "ParkingLocationPrefs";
    private static final String KEY_PARKED_LATITUDE = "parked_latitude";
    private static final String KEY_PARKED_LONGITUDE = "parked_longitude";
    private SharedPreferences sharedPreferences;
    private LatLng parkedLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Initialize SupportMapFragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Parking location stored by button click
        Button parkButton = findViewById(R.id.park_button);
        parkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storeParkingLocation();
            }
        });
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (mMap != null) {
            int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            // If permission is granted loop
            if (permission == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);

                LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                // Location identified by lat and long
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    // Display location
                    LatLng currentLatLng = new LatLng(latitude, longitude);
                    mMap.addMarker(new MarkerOptions().position(currentLatLng).title("Your Location"));

                    // Move UI to location
                    CameraUpdate update = CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f);
                    mMap.moveCamera(update);

                    // Previous stored location checked
                    double parkedLatitude = sharedPreferences.getFloat(KEY_PARKED_LATITUDE, 0);
                    double parkedLongitude = sharedPreferences.getFloat(KEY_PARKED_LONGITUDE, 0);
                    if (parkedLatitude != 0 && parkedLongitude != 0) {
                        // Display parking location
                        parkedLocation = new LatLng(parkedLatitude, parkedLongitude);
                        mMap.addMarker(new MarkerOptions().position(parkedLocation).title("Parking Location"));
                        mMap.addCircle(new CircleOptions().center(parkedLocation).radius(20).strokeWidth(0f).fillColor(0x5500ff00)); // Green circle around parking location
                    }
                } else {
                    Toast.makeText(this, "Unable to retrieve location", Toast.LENGTH_SHORT).show();
                }
            } else {
                requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, LOCATION_REQUEST_CODE);
            }
        }
    }
    // Function to check permission
    protected void requestPermission(String permissionType, int requestCode) {
        ActivityCompat.requestPermissions(this,
                new String[]{permissionType}, requestCode);
    }
    // Function for permission result from device (user)
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_REQUEST_CODE:
                if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Unable to show location - permission required", Toast.LENGTH_LONG).show();
                } else {
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                    mapFragment.getMapAsync(this);
                }
        }
    }
    // Function of stored parking pin
    private void storeParkingLocation() {
        if (parkedLocation != null) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putFloat(KEY_PARKED_LATITUDE, (float) parkedLocation.latitude);
            editor.putFloat(KEY_PARKED_LONGITUDE, (float) parkedLocation.longitude);
            editor.apply();
            Toast.makeText(this, "Parking location saved", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No parking location to save", Toast.LENGTH_SHORT).show();
        }
    }
}


