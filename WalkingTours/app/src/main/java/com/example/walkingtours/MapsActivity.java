package com.example.walkingtours;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    // Needs:
    //      implementation 'com.google.android.gms:play-services-maps:17.0.0'
    //      implementation 'com.google.android.gms:play-services-location:17.0.0'

    //      android:usesCleartextTraffic="true"

    private static final String TAG = "MapsActivity";

    private GoogleMap mMap;
    private static final int LOC_COMBO_REQUEST = 111;
    private static final int LOC_ONLY_PERM_REQUEST = 222;
    private static final int BGLOC_ONLY_PERM_REQUEST = 333;
    private static final int ACCURACY_REQUEST = 222;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Polyline llHistoryPolyline; //only for drawing a line on the map
    private final ArrayList<LatLng> latLonHistory = new ArrayList<>();
    private Polyline tourPathPolyline; //only for drawing a line on the map
    private final ArrayList<LatLng> tourPathHistory = new ArrayList<>();//storing all the points Ive passed through
    private Marker carMarker;
    private FenceMgr fenceMgr;
    private Geocoder geocoder;
    private TextView addressText;

    private CheckBox cbFences;
    private CheckBox cbAddresses;
    private CheckBox cbTourPath;
    private CheckBox cbTravelPath;

    //private final List<String> threadTourPathData = new ArrayList<>();
    private final PolylineOptions TourPathpolylineOptions = new PolylineOptions();

    private int counter = 0;

    private Typeface myCustomFont;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        cbFences = findViewById(R.id.checkBoxGeoFences);
        cbAddresses = findViewById(R.id.checkBoxAddresses);
        cbTourPath = findViewById(R.id.checkBoxTourPath);
        cbTravelPath = findViewById(R.id.checkBoxTravelPath);

        myCustomFont = Typeface.createFromAsset(getAssets(), "Acme-Regular.ttf");

        cbFences.setTypeface(myCustomFont);
        cbAddresses.setTypeface(myCustomFont);
        cbTourPath.setTypeface(myCustomFont);
        cbTravelPath.setTypeface(myCustomFont);

        fenceMgr = new FenceMgr(this);

        addressText = findViewById(R.id.addressText);

        checkLocationAccuracy();

        geocoder = new Geocoder(this);
    }

    public void initMap() {

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    /**
     * This callback is triggered when the map is ready to be used.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        /*
        mMap = googleMap;
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16.0f));

        mMap.setBuildingsEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);

         */

        mMap = googleMap;
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.setBuildingsEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);


        if (checkPermission()) {
            setupLocationListener();
        }
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOC_ONLY_PERM_REQUEST);
                return false;
            }
            return true;

        } else {

            ArrayList<String> perms = new ArrayList<>();

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {
                perms.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
                    perms.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
                }
            }

            if (!perms.isEmpty()) {
                String[] array = perms.toArray(new String[0]);
                ActivityCompat.requestPermissions(this,
                        array, LOC_COMBO_REQUEST);
                return false;
            }
        }

        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOC_ONLY_PERM_REQUEST) {
            if (permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestBgPermission();
            }
        } else if (requestCode == LOC_COMBO_REQUEST) {
            int permCount = permissions.length;
            int permSum = 0;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    permSum++;
                } else {
                    sb.append(permissions[i]).append(", ");
                }
            }
            if (permSum == permCount) {
                setupLocationListener();
            } else {
                Toast.makeText(this,
                        "Required permissions not granted: " + sb.toString(),
                        Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == BGLOC_ONLY_PERM_REQUEST) {
            if (permissions[0].equals(Manifest.permission.ACCESS_BACKGROUND_LOCATION) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupLocationListener();
            }
        }
    }

    public void requestBgPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BGLOC_ONLY_PERM_REQUEST);
            }

        }
    }

    private void setupLocationListener() { // listens to our location change

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locationListener = new MyLocListener(this);

        //minTime	    long: minimum time interval between location updates, in milliseconds
        //minDistance	float: minimum distance between location updates, in meters
        if (checkPermission() && locationManager != null) //these are parameters for listening
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, locationListener);
    }

    public void showClickedCheckBoxFences(View v) {


        if (cbFences.isChecked()) {
            fenceMgr.eraseFences();
            fenceMgr.drawFences();
        } else {
            fenceMgr.eraseFences();

        }
    }

    public void showClickedCheckBoxAddresses (View v){

        if (cbAddresses.isChecked()){
            addressText.setVisibility(View.VISIBLE);
        }
        else {
            addressText.setVisibility(View.INVISIBLE);
        }
    }

    public void showClickedCheckBoxTourPath (View v){

        if (tourPathPolyline != null)
        tourPathPolyline.setVisible(cbTourPath.isChecked());

        if (TourPathpolylineOptions != null && counter == 0) {
            tourPathPolyline = mMap.addPolyline(TourPathpolylineOptions); //these 4 lines are configuration for our polyline
            tourPathPolyline.setEndCap(new RoundCap());
            tourPathPolyline.setWidth(8);
            tourPathPolyline.setColor(Color.RED);
            tourPathPolyline.setVisible(cbTourPath.isChecked());
            counter++;
        }
    }

    public void showClickedCheckTravelPath (View v){
        if (llHistoryPolyline != null)
        llHistoryPolyline.setVisible(cbTravelPath.isChecked());
    }



    public GoogleMap getMap() {
        return mMap;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (locationManager != null && locationListener != null)
            locationManager.removeUpdates(locationListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkPermission() && locationManager != null && locationListener != null)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 10, locationListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fenceMgr.eraseFences();
        if (locationManager != null && locationListener != null)
            locationManager.removeUpdates(locationListener);
    }

    public void updateLocation(Location location) {

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        latLonHistory.add(latLng); // Add the LL to our location history

        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            Address address = addresses.get(0);
            if (cbAddresses.isChecked()){
                addressText.setVisibility(View.VISIBLE);
            }
            else {
                addressText.setVisibility(View.INVISIBLE);
            }
            addressText.setText(address.getAddressLine(0));
            addressText.setTypeface(myCustomFont);

        } catch (IOException e) {
            e.printStackTrace();
            addressText.setText("");
        }


        if (llHistoryPolyline != null) {
            llHistoryPolyline.remove(); // Remove old polyline
        }

        if (latLonHistory.size() == 1) { // First update (first location)
            mMap.addMarker(new MarkerOptions().alpha(0.5f).position(latLng).title("My Origin")); //adds the marker
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0f)); //16,0f is a zoom level
            return; //we dont draw a polyline here because its the first location
        }

        if (latLonHistory.size() > 1) { // Second (or more) update
            PolylineOptions polylineOptions = new PolylineOptions();

            for (LatLng ll : latLonHistory) {
                polylineOptions.add(ll);
            }
            llHistoryPolyline = mMap.addPolyline(polylineOptions); //these 4 lines are configuration for our polyline
            llHistoryPolyline.setEndCap(new RoundCap());
            llHistoryPolyline.setWidth(8);
            llHistoryPolyline.setColor(Color.BLUE);
            llHistoryPolyline.setVisible(cbTravelPath.isChecked());


            float r = getRadius();

            if (r > 0) {

                Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.walker_left);
                Bitmap resized = Bitmap.createScaledBitmap(icon, (int) r, (int) r, false);

                BitmapDescriptor iconBitmap = BitmapDescriptorFactory.fromBitmap(resized);

                MarkerOptions options = new MarkerOptions();
                options.position(latLng);
                options.icon(iconBitmap);
                options.rotation(location.getBearing());

                if (carMarker != null) {
                    carMarker.remove();
                }

                carMarker = mMap.addMarker(options);
            }
        }
        if (counter <= 1){
            showClickedCheckBoxFences(null);
            showClickedCheckBoxTourPath (null);
        }

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0f));
    }

    public void tourPath (List<String> ab){


        if (tourPathPolyline != null) {
            tourPathPolyline.remove(); // Remove old polyline , (very important, otherwise app crashes)
        }

        if (tourPathPolyline  == null){
            //PolylineOptions TourPathpolylineOptions = new PolylineOptions();

            for (int i = 0; i < ab.size(); i++){

                String[] split = ab.get(i).split(",");
                double lat = Double.parseDouble(split[1].trim());
                double lon = Double.parseDouble(split[0].trim());

                TourPathpolylineOptions.add(new LatLng(lat, lon));

            }
            /*
            if (mMap != null){
                tourPathPolyline = mMap.addPolyline(TourPathpolylineOptions); //these 4 lines are configuration for our polyline
                tourPathPolyline.setEndCap(new RoundCap());
                tourPathPolyline.setWidth(8);
                tourPathPolyline.setColor(Color.RED);
                tourPathPolyline.setVisible(cbTourPath.isChecked());
            }

             */


        }



    }

    private float getRadius() {
        float z = mMap.getCameraPosition().zoom;
        return 15f * z - 145f;
    }


    private void checkLocationAccuracy() {

        Log.d(TAG, "checkLocationAccuracy: ");
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, locationSettingsResponse -> {
            Log.d(TAG, "onSuccess: High Accuracy Already Present");
            initMap();
        });

        task.addOnFailureListener(this, e -> {
            if (e instanceof ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(MapsActivity.this, ACCURACY_REQUEST);
                } catch (IntentSender.SendIntentException sendEx) {
                    sendEx.printStackTrace();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACCURACY_REQUEST && resultCode == RESULT_OK) {
            Log.d(TAG, "onActivityResult: ");
            initMap();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("High-Accuracy Location Services Required");
            builder.setMessage("High-Accuracy Location Services Required");
            builder.setPositiveButton("OK", (dialog, id) -> finish());
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
}