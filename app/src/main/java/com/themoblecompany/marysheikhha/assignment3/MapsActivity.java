package com.themoblecompany.marysheikhha.assignment3;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

/**
 * <li>Shows map</li>
 * <li>Checks for ACCESS_FINE_LOCATION permission (since this is an dangerous permission) </li>
 * <li>Shows user current location</li>
 * <li>Reads gpx files and find milemarker lat and long </li>
 * <li>Shows milemarker as marker on map</li>
 * <p>Note that running this application need assigning proper key in google_maps_api</p>
 * <br/>
 * Created by mary on 2/15/16.
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    //request code for gaining ACCESS_FINE_LOCATION permission (in android 6 and above)
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 8;
    // googleMap instance
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // find user location
        getMyLocation();
        // add mileMarkers
        getMileMarkers();
    }

    /**
     * for gaining permission in android 6 and above
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay!
                    getMyLocation();

                } else {

                    // permission denied, boo!
                    showToast();
                }
            }
        }
    }

    /**
     * calling {@link GPXParser} and parsing gpx files and adding maleMarkers to map
     */
    private void getMileMarkers() {
        new GPXParser(new GPXParser.OnParseFinishListener() {
            @Override
            public void showMarkers(final List markers) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mMap != null) {
                            for (Object obj : markers) {
                                if (obj instanceof GPXParser.WPT) {
                                    GPXParser.WPT wpt = (GPXParser.WPT) obj;
                                    mMap.addMarker(new MarkerOptions().position(new LatLng(wpt.lat, wpt.lon)));
                                }
                            }
                        }
                    }
                });
            }

            @Override
            public void finish() {
                // TODO: 2/17/16 add proper functionality
            }
        }, this).start();


    }


    /**
     * check ACCESS_FINE_LOCATION, if it was granted, shows user location, request permission otherwise.
     */
    private void getMyLocation() {

        //check permission
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            //check gps status
            if (!((LocationManager) getSystemService(LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER))
                showGPSDisabledAlert();

            mMap.setMyLocationEnabled(true);
            // Check if we were successful in obtaining the map.
            if (mMap != null) {

                mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {

                    @Override
                    public void onMyLocationChange(Location arg0) {
                        // TODO Auto-generated method stub
                    }
                });

            }
        } else {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously*
                showPermissionExplanationAlert();

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

            }
        }

    }

    /**
     * shows toast to inform user whitch this applications will not work correctly,
     * since he/she denied ACCESS_FINE_LOCATION permission
     */
    private void showToast() {
        Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_LONG).show();
    }

    /**
     * shows an alert which gps is off and open setting page for turning it on, if the user selects positive Button.
     */
    private void showGPSDisabledAlert() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(R.string.gps_disable)
                .setPositiveButton(R.string.enable,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                            }
                        });
        alertDialogBuilder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    /**
     * shows an alert which tell why we need to ACCESS_FINE_LOCATION permission
     */
    private void showPermissionExplanationAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.permission_explanation_dialog)
                .setTitle(R.string.permission_explanation_title);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showToast();
            }
        });
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ActivityCompat.requestPermissions(MapsActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

            }
        });

        builder.create().show();
    }

}
