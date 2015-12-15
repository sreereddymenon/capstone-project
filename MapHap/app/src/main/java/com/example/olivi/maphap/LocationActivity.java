package com.example.olivi.maphap;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Debug;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.example.olivi.maphap.utils.Constants;
import com.example.olivi.maphap.utils.LocationUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

public abstract class LocationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = LocationActivity.class.getSimpleName();
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_LOCATION = 0;

    private GoogleApiClient mGoogleApiClient;
    private LatLng mLastLocation;
    private boolean mAskPermissionForLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    abstract void onUserLocationFound(LatLng latLng);

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart() called. Attempting to connect GoogleApiClient...");
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "onLocationChanged called.");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended!");
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (isLocationPermissionGranted()) {
            Log.i(TAG, "mGoogleApiClient is connected and permission granted! Getting user location");
            mLastLocation = getUserLocation();
            onUserLocationFound(mLastLocation);
        } else {
            mAskPermissionForLocation = true;
            Log.i(TAG, "mGoogleApiClient is connected, but location permission" +
                    "has not been granted yet. Asking permission now.");
            askPermissionForLocation();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAskPermissionForLocation) {
            askPermissionForLocation();
        }
    }

    private LatLng getUserLocation() {
        Location newLoc = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        if (newLoc != null) {
            return new LatLng(newLoc.getLatitude(), newLoc.getLongitude());
        } else {
            Log.i(TAG, "couldn't get user location");
            return null;
        }
    }

    private boolean isLocationPermissionGranted() {
        boolean isLocationPermissionGranted = (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);

        Log.i(TAG, "isLocationPermissionGranted called and returns " + isLocationPermissionGranted);

        return isLocationPermissionGranted;
    }

    private void askPermissionForLocation() {
        // TODO show an explanation of why location is needed for this app
        Log.i(TAG, "askPermissionForLocation() called");
        Log.d(TAG, "askPermissionForLocation() should only be called once - on installation");

        mAskPermissionForLocation = false;
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_CONTACTS)) {

            Log.i(TAG, "shouldShowRequestPermissionsRationale is true");
            // TODO Show an expanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.

        } else {

            // No explanation needed, we can request the permission.
            Log.i(TAG, "trying to show request dialog....");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_LOCATION);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if ((mLastLocation == null) && (mGoogleApiClient.isConnected())) {
                        Log.i(TAG, "Location permission granted. Calling getUserLocation()" +
                                "and onUserLocationFound");
                        getUserLocation();
                        onUserLocationFound(mLastLocation);
                    }
                } else if (grantResults.length == 0) {
                    Log.i(TAG, "Request was canceled! Can't access user's location");
                } else {
                    Log.i(TAG, "Permission was denied! Can't access user's location");
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed!");
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camara) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
