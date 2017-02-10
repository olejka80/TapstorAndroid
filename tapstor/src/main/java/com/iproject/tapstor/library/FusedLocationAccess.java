package com.iproject.tapstor.library;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.iproject.tapstor.R;

import java.util.ArrayList;
import java.util.List;


public class FusedLocationAccess implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public static final int REQUEST_LOCATION_ACCESS_PERMISSION = 198;
    private static final String TAG = "FusedLocationAccess";
    private static FusedLocationAccess mData = null;
    private static List<OnLocationChangedListener> onLocationChangedListeners = new ArrayList<>();
    private AppCompatActivity mAppCompatActivity;
    private boolean mHasCheckedPermissions;
    private Location mLocation;
    private boolean isEnabled = false;
    private GoogleApiClient mGoogleApiClient = null;
    private LocationRequest mLocationRequest;
    private boolean mShowLocationPermissionDirection = false;


    private FusedLocationAccess(AppCompatActivity appCompatActivity) {
        this.mAppCompatActivity = appCompatActivity;
        //If lower than Android 22, then all permissions are granted. No need to check :D
        this.mHasCheckedPermissions = Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1;


        mGoogleApiClient = new GoogleApiClient.Builder(appCompatActivity)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


    }

    public static synchronized FusedLocationAccess getInstance(AppCompatActivity appCompatActivity) {
        if (mData == null)
            mData = new FusedLocationAccess(appCompatActivity); // Create the instance
        mData.checkPermission();
        return mData;
    }

    public boolean isPermissionGranted() {
        if (!mHasCheckedPermissions)
            throw new SecurityException("Use checkPermission method first.");
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) { //If higher than Android API 23.
            int hasLocationPermission = ContextCompat.checkSelfPermission(mAppCompatActivity, Manifest.permission.ACCESS_FINE_LOCATION);
            return hasLocationPermission == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {

        if (requestCode == REQUEST_LOCATION_ACCESS_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                if (!mShowLocationPermissionDirection) {
                    mShowLocationPermissionDirection = true;
                    showMessage(R.string.access_location_not_granted);
                }
            }

        }

    }

    public void checkPermission() {
        mHasCheckedPermissions = true;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) return;
        if (this.isPermissionGranted()) return;

        if (ActivityCompat.shouldShowRequestPermissionRationale(mAppCompatActivity, Manifest.permission.ACCESS_FINE_LOCATION)) {
            mShowLocationPermissionDirection = false;


            showMessageOKCancel(R.string.access_location_description,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(mAppCompatActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_LOCATION_ACCESS_PERMISSION);
                        }
                    });
            return;
        } else {
            if (mShowLocationPermissionDirection) {
                showMessage(R.string.access_location_directions);
            }
        }
        ActivityCompat.requestPermissions(mAppCompatActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_LOCATION_ACCESS_PERMISSION);
    }

    public void enableLocationEnabled(@NonNull GoogleMap mMap) {
        if (isPermissionGranted()) {
            try {
                mMap.setMyLocationEnabled(true);
            } catch (SecurityException ignored) {
            }
        }
    }

    public void enableLocationListener() {
        if (!isPermissionGranted()) {
            Log.d(TAG, "Permission not granted. :(");
        } else {
            Log.d(TAG, "Permission granted. :)");

            if (isEnabled) return;
            mGoogleApiClient.connect();

        }
    }

    public boolean isLocationListenerEnabled() {
        return isEnabled;
    }

    public void disableLocationListener() {
        if (!isPermissionGranted() || !isEnabled)
            return;
        try {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
            isEnabled = false;
            Log.i(TAG, "LocationManager removed updates");
        } catch (SecurityException se) {
            Log.e(TAG, se);
        } catch (Exception e) {
            Log.e(TAG, e);
        }
    }

    private void showMessageOKCancel(int message, DialogInterface.OnClickListener okListener) {
        new android.support.v7.app.AlertDialog.Builder(mAppCompatActivity)
                .setTitle(R.string.permission_denied)
                .setMessage(message)
                .setPositiveButton(R.string.permission_retry, okListener)
                .setNegativeButton(R.string.permission_im_sure, null)
                .create()
                .show();
    }

    private void showMessage(int message) {
        new android.support.v7.app.AlertDialog.Builder(mAppCompatActivity)
                .setMessage(message)
                .setPositiveButton(R.string.ok, null)
                .create()
                .show();
    }

    @Nullable
    public synchronized Location getLocation() {

        if (!isPermissionGranted()) return null;
        try {
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (location == null) {
                Log.i(TAG, "Location is NULL");
            } else {
                Log.i(TAG, "Location: " + location.toString());
            }
            return location;
        } catch (SecurityException se) {
            Log.i(TAG, se.toString());
            return null;
        } catch (Exception e) {
            Log.i(TAG, e.toString());
            return null;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, location.toString());
        mLocation = location;
        for (OnLocationChangedListener onLocationChangedListener : onLocationChangedListeners) {
            onLocationChangedListener.onLocationChanged(location);
        }
    }

    public void addOnLocationChangedListener(OnLocationChangedListener onLocationChangedListener) {
        Log.i(TAG, "LocationChangedListener added.");
        onLocationChangedListeners.add(onLocationChangedListener);
    }

    public void removeOnLocationChangedListener(OnLocationChangedListener onLocationChangedListener) {
        Log.i(TAG, "LocationChangedListener removed.");
        onLocationChangedListeners.remove(onLocationChangedListener);
    }


    public void removeAllOnLocationChangedListeners() {
        Log.i(TAG, "All LocationChangedListeners removed.");
        onLocationChangedListeners.clear();
    }


    @Override
    public void onConnected(Bundle bundle) {
        try {
            Log.i(TAG, "Fused Location API request location updates");
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            isEnabled = true;
        } catch (SecurityException se) {
            Log.e(TAG, se);
        } catch (Exception e) {
            Log.e(TAG, e);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "Connection Suspended: " + i);
        isEnabled = false;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "Connection Failed: " + connectionResult.getErrorCode() + " -> " + connectionResult.getErrorMessage());
        isEnabled = false;
    }

    public interface OnLocationChangedListener {
        void onLocationChanged(Location location);
    }


}
