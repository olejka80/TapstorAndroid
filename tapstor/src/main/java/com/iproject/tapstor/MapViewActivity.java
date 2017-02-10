package com.iproject.tapstor;


import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.iproject.tapstor.helper.TapstorActivity;
import com.iproject.tapstor.helper.TapstorData;
import com.iproject.tapstor.library.FusedLocationAccess;
import com.iproject.tapstor.library.Log;
import com.iproject.tapstor.objects.Store;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class MapViewActivity extends TapstorActivity implements OnMapReadyCallback {

    private static final String TAG = "MapViewActivity";
    private GoogleMap mapView;
    private Marker markerShowingInfoWindow;

    @Override
    protected void onStart() {
        super.onStart();
        FusedLocationAccess.getInstance(this).enableLocationListener();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");


        final ActionBar actionBar = getSupportActionBar();

        ((TextView) findViewById(R.id.header_title)).setText(R.string.map);
        actionBar.setTitle("");
        actionBar.setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.color.white));
        actionBar.setLogo(null);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.arrow_left);
        actionBar.setDisplayHomeAsUpEnabled(true);


        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        fm.getMapAsync(this);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();
        switch (itemId) {

            // go back
            case android.R.id.home:
                onBackPressed();
                break;

        }

        return true;
    }

    /**
     * Animates map to the specified location
     */
    private void animateToCurrentLocation() {
        Location location = FusedLocationAccess.getInstance(this).getLocation();
        if (location == null) {
            try {
                mapView.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(38.503244, 22.827908), 5));
            } catch (Exception e) {
                Log.e(TAG, e);
            }
        } else {
            try {
                mapView.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(Double.parseDouble(TapstorData.getInstance().getSelectedElement().closestStore.lat), Double
                                .parseDouble(TapstorData.getInstance().getSelectedElement().closestStore.lng)),
                        17));
            } catch (Exception e) {
                Log.e(TAG, e);
            }
        }

        loadStoreLocation();

    }

    /**
     * Load the location of the specified store, animates camera and creates
     * marker and info window
     */
    private void loadStoreLocation() {

        try {

            for (Store store : TapstorData.getInstance().getSelectedElement().stores) {

                LatLng pin = new LatLng(Double.parseDouble(store.lat), Double.parseDouble(store.lng));

                mapView.addMarker(new MarkerOptions().position(pin));
            }

            mapView.setInfoWindowAdapter(new MyInfoWindowAdapter());

            /**
             * listener for the info window of the marker starts a map Intent
             */
            mapView.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

                @Override
                public void onInfoWindowClick(Marker marker) {

                    try {
                        openMapIntent(Double.parseDouble(TapstorData
                                        .getInstance().getSelectedEnterprise().lat),
                                Double.parseDouble(TapstorData.getInstance()
                                        .getSelectedEnterprise().lng));
                    } catch (Exception e) {
                        Log.e(TAG, e);
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, e);
        }

    }

    /**
     * Opens a map intent to display directions to the specified location
     *
     * @param latitude  the lat of destination
     * @param longitude the lng of destination
     */
    private void openMapIntent(double latitude, double longitude) {

        String label = TapstorData.getInstance().getSelectedEnterprise().name;
        String uriBegin = "geo:" + latitude + "," + longitude;
        String query = latitude + "," + longitude + "(" + label + ")";
        String encodedQuery = Uri.encode(query);
        String uriString = uriBegin + "?q=" + encodedQuery + "&z=16";
        Uri uri = Uri.parse(uriString);
        Intent mapIntent = new Intent(android.content.Intent.ACTION_VIEW, uri);
        startActivity(mapIntent);

        finish();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mapView = googleMap;
        FusedLocationAccess.getInstance(this).enableLocationEnabled(mapView);
        animateToCurrentLocation();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        FusedLocationAccess.getInstance(this).onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    class MyInfoWindowAdapter implements InfoWindowAdapter {

        private final View myContentsView;
        boolean isImageLoaded = false;

        MyInfoWindowAdapter() {
            myContentsView = getLayoutInflater().inflate(R.layout.inflate_custom_map_info_window, new LinearLayout(MapViewActivity.this), false);
        }

        @Override
        public View getInfoContents(final Marker marker) {

            markerShowingInfoWindow = marker;

            ImageView image = (ImageView) myContentsView.findViewById(R.id.image);

            TextView store_name = (TextView) myContentsView.findViewById(R.id.store_name);


            try {
                Picasso.with(MapViewActivity.this)
                        .load(TapstorData.getInstance().getSelectedEnterprise().avatar)
                        .resize(150, 150).centerInside()
                        .into(image, new Callback() {

                            @Override
                            public void onSuccess() {
                                if (!isImageLoaded) {
                                    isImageLoaded = true;
                                    if (markerShowingInfoWindow != null && markerShowingInfoWindow.isInfoWindowShown()) {
                                        markerShowingInfoWindow.showInfoWindow();
                                    }
                                }
                            }

                            @Override
                            public void onError() {

                            }
                        });

            } catch (Exception e) {
                Log.e(TAG, e);
            }

            store_name.setText(TapstorData.getInstance().getSelectedEnterprise().name);

            return myContentsView;
        }

        @Override
        public View getInfoWindow(Marker marker) {

            return null;
        }

    }

}
