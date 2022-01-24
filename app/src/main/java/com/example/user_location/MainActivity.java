package com.example.user_location;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener, OnMapReadyCallback {

    private static final int REQUEST_LOCATION_WRITE = 100;
    LocationManager locationManager;
    ProgressDialog progressDialog;
    GoogleMap map;

    TextView tvstate, tvcity, tvpincode, tvaddress;
    double longitude = 0, latitude = 0;


    LinearLayout button;


    //    shared preference saving state
    public static final String MYPREFERENCE = "mypref";
    public static final String KEY_NAME = "name";
    public static final String KEY_PHON = "phone";
    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {

            initialization();

            button.setOnClickListener(view -> {

                //editing shared preference using editor
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(KEY_NAME, null);
                editor.putString(KEY_PHON, null);
                editor.apply();
                startActivity(new Intent(this, Login.class));
                finish();

            });


            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setCancelable(true);
            progressDialog.setMessage("Please Wait..");
            progressDialog.show();
            progressDialog.setCancelable(true);


            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_update_address);


            if (mapFragment != null) {
                mapFragment.getMapAsync(this);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }


    }


    private void initialization() {
        tvstate = findViewById(R.id.tvstate);
        tvcity = findViewById(R.id.tvcity);
        tvpincode = findViewById(R.id.tvpincode);
        tvaddress = findViewById(R.id.tvaddress);
        button = findViewById(R.id.button);
        sharedPreferences = getSharedPreferences(MYPREFERENCE, MODE_PRIVATE);

    }


    @Override
    protected void onStart() {
        super.onStart();
        try {
//            checking permission
            checking_permission();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void checking_permission() {

        try {

            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                progressDialog.dismiss();
                //Add self permission
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                }, REQUEST_LOCATION_WRITE);
            } else {
//            getting current location
                getlocation();


            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    boolean check_location_is_enable_or_not_start() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gpsenable = false;
        boolean networkenable = false;

        try {
            gpsenable = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            networkenable = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return (gpsenable || networkenable);
    }

    private void getlocation() {

        if (check_location_is_enable_or_not_start()) {
            try {
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 5, (LocationListener) this);

            } catch (SecurityException e) {
                e.printStackTrace();
                Log.d("TAG", "permission error" + e.getMessage());
            }
        } else {
            try {
                progressDialog.dismiss();
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Enable GPS Service")
                        .setCancelable(false)
                        .setPositiveButton("Enable", (dialogInterface, i) -> {
                            progressDialog.show();
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }).setNegativeButton("Cancle", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this, "please enable gps service", Toast.LENGTH_SHORT).show();
                    }
                }).show();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_WRITE) {
            if (grantResults.length > 0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {
                progressDialog.show();
//              getlocation
                getlocation();

            } else {

                try {
                    progressDialog.dismiss();
                    AlertDialog dialog = new AlertDialog.Builder(this)
                            .setTitle("Confirm ")
                            .setMessage("Allow Permission to get your current location address.")
                            .setPositiveButton("Ok", (dialogInterface, i) -> {
                                // Create intent to start new activity
                                //Open the specific App Info page:
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.parse("package:" + getPackageName()));
                                startActivity(intent);
                            }).setNegativeButton("Cancel", null)
                            .show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        try {
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

            progressDialog.dismiss();

            try {
                tvstate.setText(addresses.get(0).getAdminArea());
                tvcity.setText(addresses.get(0).getLocality());
                tvpincode.setText(addresses.get(0).getPostalCode());
                tvaddress.setText(addresses.get(0).getAddressLine(0));
                latitude = location.getLatitude();
                longitude = location.getLongitude();


                // Add a marker in Sydney and move the camera
                LatLng locationn = new LatLng(location.getLatitude(), location.getLongitude());
                map.clear();
                map.getUiSettings().setScrollGesturesEnabled(false);
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 1));
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                        .zoom(16)                    // Sets the zoom
                        .build();               // Creates a CameraPosition from the builder
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                Marker marker = map.addMarker(
                        new MarkerOptions().
                                position(locationn).
                                title("Your Current Location"));
//                                .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView())));

                if (marker != null) {
                    marker.showInfoWindow();
                }


// Write and save file

                    String fileName = "location" + ".txt";
                    //create file
                    File file = new File(getApplicationContext().getExternalFilesDir("mission"), fileName);
                    //write file
                    try {
                        file.createNewFile();
                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(addresses.get(0).getAddressLine(0).getBytes());
                        fos.close();
                        Toast.makeText(this, "Saved to :\n" + getApplicationContext().getExternalFilesDir("mission") + "/" + fileName, Toast.LENGTH_LONG).show();

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }catch (IOException e) {
                        e.printStackTrace();
                    }




            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            Log.d("TAG", "onLocationChanged " + e.getMessage());
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        try {
            map = googleMap;
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private boolean checkAndRequestPermissions() {
        int permissionSendMessage = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (permissionSendMessage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[0]), REQUEST_LOCATION_WRITE);
            return false;
        }
        return true;
    }


}