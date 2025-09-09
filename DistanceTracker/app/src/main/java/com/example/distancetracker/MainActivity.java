package com.example.distancetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener {
    private static final int REQUEST_PERMISSION_FINE_LOCATION_RESULT = 0;
    private TextView latitude, longitude, address, distance, timeSpent;
    private LocationManager locationManager;
    private int dist;
    private Location previousLocation;
    private ArrayList<String> locations;
    private ArrayList<Long> times;
    private long lastTime, sum;
    private LocationListener locationListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dist = 0;
        sum = 0;
        latitude = (TextView) findViewById(R.id.textView);
        longitude = (TextView) findViewById(R.id.textView2);
        address = (TextView) findViewById(R.id.textView3);
        distance = (TextView) findViewById(R.id.textView4);
        timeSpent = (TextView) findViewById(R.id.textView5);
        //timeSpent.setText("Total Time Spent: " + (sum/ 1000) + " seconds");
        lastTime = SystemClock.elapsedRealtime();;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) {
                locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
            }
            else{
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Toast.makeText(getApplicationContext(), "Need to access location", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_FINE_LOCATION_RESULT);
            }
        }
        else {
            locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
        }
    }

    public void onLocationChanged(Location location) {
        latitude.setText("Latitude: " + location.getLatitude());
        longitude.setText("Longitude: " + location.getLongitude());
        try{
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            address.setText(addresses.get(0).getAddressLine(0));
            if(previousLocation!=null)
                dist += location.distanceTo(previousLocation);
            previousLocation = location;
            distance.setText("Total Distance Traveled: "+(dist)/1600.00+ " miles/ " + dist + " meters");

            if(locations.contains(address.getText().toString())){
                long timePassed = SystemClock.elapsedRealtime() - lastTime;
                int i = locations.indexOf(address.getText().toString());
                times.set(i, times.get(i)+timePassed);
            }

            else{
                locations.add(address.getText().toString());
                long timePassed = SystemClock.elapsedRealtime() - lastTime;
                times.add(timePassed);
            }


            for(int i = 0; i<times.size(); i++){
                sum+=times.get(i);
            }
            //timeSpent.setText("Total Time Spent: " + (sum/ 1000) + " seconds");

            lastTime = SystemClock.elapsedRealtime();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_FINE_LOCATION_RESULT){
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(getApplicationContext(), " You need location permission", Toast.LENGTH_SHORT).show();
            }
        }
    }

    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {
        Toast.makeText(MainActivity.this, "You need GPS and Internet", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("Distance", dist);
        outState.putString("Latitude", latitude.getText().toString());
        outState.putString("Longitude", longitude.getText().toString());
        outState.putString("Address", address.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        dist = savedInstanceState.getInt("Distance");
        distance.setText("Total Distance Traveled: "+(dist)/1600.00+ " miles/ " + dist + " meters");
        latitude.setText(savedInstanceState.getString("Latitude"));
        longitude.setText(savedInstanceState.getString("Longitude"));
        address.setText(savedInstanceState.getString("Address"));
    }
}
