package com.iproject.tapstor;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.iproject.tapstor.helper.DistanceComparator;
import com.iproject.tapstor.helper.Helper;
import com.iproject.tapstor.helper.TapstorData;
import com.iproject.tapstor.library.Log;
import com.iproject.tapstor.objects.Results;
import com.iproject.tapstor.objects.Store;
import com.iproject.tapstor.rest.ArResults;
import com.iproject.tapstor.rest.RestResponse;
import com.iproject.tapstor.rest.RestServices;
import com.iproject.tapstor.rest.SendValueAR;
import com.jwetherell.augmented_reality.activity.AugmentedReality;
import com.jwetherell.augmented_reality.data.ARData;
import com.jwetherell.augmented_reality.ui.IconMarker;
import com.jwetherell.augmented_reality.ui.Marker;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * This class extends the AugmentedReality and is designed to be an example on
 * how to extends the AugmentedReality class to show multiple data sources.
 *
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public class AugmentedRealityActivity extends AugmentedReality {

    private static final String TAG = "AugmentedRealityActivit";

    public static int arrayStart = 0;
    public static boolean navigatedFromStoreView;
    boolean pressed = false;
    boolean activityIsFinished = false;

    private Marker selectedMarker;
    private Context context;
    private List<ArResults> arResults = new ArrayList<>();
    private Bitmap bitmap = null;
    private Bitmap b;
    private AsyncTask<Object, Object, Object> mTask;

    @Override
    public void onBackPressed() {
        activityIsFinished = true;

        if (mTask != null) {
            try {
                mTask.cancel(true);
            } catch (Exception e) {
                Log.e(TAG, e);
            }
        }

        super.onBackPressed();

    }

    @Override
    public void finish() {
        activityIsFinished = true;

        if (mTask != null) {
            try {
                mTask.cancel(true);
            } catch (Exception e) {
                Log.e(TAG, e);
            }
        }

        super.finish();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = AugmentedRealityActivity.this;

        Log.e(TAG, "Create DATA");
        // loadStoresInBackground();

    }


    @Override
    public void onStart() {
        super.onStart();

        loadStoresInBackground();

    }


    @Override
    public void onLocationChanged(Location location) {
        super.onLocationChanged(location);

        try {
            if (location != null) {
                Log.e(TAG, "LOCATION CHANGED");

                loadStoresInBackground();
            }

        } catch (Exception e) {
            Log.e(TAG, e);
        }

    }

    @Override
    protected void markerTouched(Marker marker) {

        Helper.sendGoogleAnalyticsAction("AR Marker Click", marker.getStore().title, marker.getStore().id, context);

        populateTopView(marker.getStore());
        selectedMarker = marker;
        myRelativeLayoutTop.setVisibility(View.VISIBLE);

    }

    @Override
    protected void radarTouched() {
        if (!navigatedFromStoreView) {
            // TODO load next 10 results highlight the second layer maybe change the radar max zoom in kilometers
            Log.e(TAG, "radar clicked change radar");
            if (arrayStart < 30) {
                arrayStart = arrayStart + 10;
            } else {
                arrayStart = 0;
            }

            super.radarTouched();

            addMyMarkers(arrayStart);

        }

    }

    /**
     * open a new activity on top info layout touched
     */
    @Override
    protected synchronized void topLayoutTouched() {

        if (!pressed) {
            pressed = true;

            activityIsFinished = true;
            if (!navigatedFromStoreView) {
                Results results = new Results();
                results.setResultFromArResult(selectedMarker.getStore());
                TapstorData.getInstance().setSelectedEnterprise(results);

                // send tracker.
                Helper.sendGoogleAnalyticsAction("Company From AR", TapstorData.getInstance().getSelectedEnterprise().company,
                        TapstorData.getInstance().getSelectedEnterprise().id, context);

                startActivity(new Intent(context, DetailedListingActivity.class));
                finish();
            } else {
                onBackPressed();
            }

        }
    }


    @Override
    protected void updateDataOnZoom() {
        super.updateDataOnZoom();


        loadStoresInBackground();

    }

    /**
     * Adds the markers in the AR view in tens
     *
     * @param start the first position
     */
    private void addMyMarkers(int start) {

        List<Marker> markers = new ArrayList<Marker>();

        for (int i = start; i < (start + 10); i++) {

            bitmap = null;

            try {

                bitmap = Helper.scaleDownBitmap(Ion.with(getApplication().getBaseContext())
                        .load(arResults.get(i).avatar).asBitmap().get(), 100, context);

                b = loadBitmapFromXmlLayout(bitmap, arResults.get(i).title,
                        arResults.get(i).distance, arResults.get(i).news, arResults.get(i).has_offers);

            } catch (InterruptedException | ExecutionException e) {

                Log.e(TAG, e);
            }

            Marker marker = new IconMarker(arResults.get(i).title,
                    Double.parseDouble(arResults.get(i).lat),
                    Double.parseDouble(arResults.get(i).lng),
                    arResults.get(i).elevation, Color.parseColor("#BBFFFF"), b,
                    arResults.get(i), context);

            markers.add(marker);

        }

        Location myLocation = new Location("Loc");
        myLocation.setLatitude(TapstorData.getInstance().getLatitude());
        myLocation.setLongitude(TapstorData.getInstance().getLongitude());

        double lat = Double.parseDouble(markers.get(markers.size() - 1).getStore().lat);
        double lng = Double.parseDouble(markers.get(markers.size() - 1).getStore().lng);

        Location location = new Location("Store");
        location.setLatitude(lat);
        location.setLongitude(lng);

        int dist = (int) myLocation.distanceTo(location) / 1000;

        Log.e(TAG, "distance " + dist);

        dist = dist + 1;

        if (dist < 1) {
            dist = 1;
        }

        changeZoomLevel(dist);
        ARData.addMarkers(markers);


    }

    /**
     * Creates a bitmap from an xml designed layout
     *
     * @param bm   the bitmap to use
     * @param name the name of the company
     * @return the layout in bitmap form
     */
    private Bitmap loadBitmapFromXmlLayout(Bitmap bm, String name, float distance, int news, int offers) {

        View view = getLayoutInflater().inflate(R.layout.pin_store_augmented_layout, new LinearLayout(this), false);

        ((ImageView) view.findViewById(R.id.grid_item_image)).setImageBitmap(bm);

        ((TextView) view.findViewById(R.id.text)).setText(name);

        String distText = "";
        if (distance >= 1.0) {
            distText = String.format(getResources().getString(R.string.placeholder_km), distance);
        } else {
            distText = String.format(getResources().getString(R.string.placeholder_m), distance * 1000);
        }

        ((TextView) view.findViewById(R.id.text_distance)).setText(distText);

        TextView newsCount = (TextView) view.findViewById(R.id.news_count);


        if (news != 0) {
            newsCount.setText(String.format(getResources().getString(R.string.placeholder_string), news));
            newsCount.setVisibility(View.VISIBLE);
        } else {
            newsCount.setVisibility(View.INVISIBLE);
        }

        if (offers != 0) {
            view.findViewById(R.id.yellow_ribbon).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.yellow_ribbon).setVisibility(View.INVISIBLE);
        }


        view.findViewById(R.id.frame_view).setBackgroundColor(randomColor(128, 64));
        view.setDrawingCacheEnabled(true);
        view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        view.buildDrawingCache(true);
        final Bitmap bitmapView = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);

        return bitmapView;

    }

    private int randomColor(int sd, int mean) {
        return Color.rgb((int) Math.floor(Math.random() * sd) + mean,
                (int) Math.floor(Math.random() * sd) + mean,
                (int) Math.floor(Math.random() * sd) + mean);
    }

    private void addFourMarkers() throws NullPointerException {


        List<Marker> markers = new ArrayList<>();

        bitmap = null;

        DistanceComparator dcomp = new DistanceComparator();
        Collections.sort(TapstorData.getInstance().getSelectedElement().stores, dcomp);

        Store farthestStore = null;

        int max = 4;

        if (TapstorData.getInstance().getSelectedElement().stores.size() < 4) {

            max = TapstorData.getInstance().getSelectedElement().stores.size();

        }

        for (int j = 0; j < max; j++) {

            Store store = TapstorData.getInstance().getSelectedElement().stores
                    .get(j);

            try {

                Log.e(TAG, " 1:" + store.lat + " 2:" + store.lng + " 3:" + store.distance);

                if (bitmap == null) {
                    bitmap = Helper.scaleDownBitmap(Ion.with(getApplication().getBaseContext())
                            .load(TapstorData.getInstance().getSelectedElement().avatar)
                                    .asBitmap().get(), 100, context);

                }

                b = loadBitmapFromXmlLayout(bitmap, TapstorData.getInstance().getSelectedElement().title, store.distance, 0, 0);

            } catch (InterruptedException | ExecutionException e) {

                Log.e(TAG, e);

            }
            ArResults arStore = new ArResults();
            arStore.createfromStore(store, TapstorData.getInstance()
                    .getSelectedElement().avatar);
            Marker marker = new IconMarker(TapstorData.getInstance()
                    .getSelectedElement().title + " " + j,
                    Double.parseDouble(store.lat),
                    Double.parseDouble(store.lng), store.elevation,
                    Color.parseColor("#BBFFFF"), b, arStore, context);

            markers.add(marker);

            farthestStore = store;

        }

        try {
            Location myLocation = new Location("Loc");
            myLocation.setLatitude(TapstorData.getInstance().getLatitude());
            myLocation.setLongitude(TapstorData.getInstance().getLongitude());

            double lat = Double.parseDouble(farthestStore.lat);
            double lng = Double.parseDouble(farthestStore.lng);

            Location location = new Location("Store");
            location.setLatitude(lat);
            location.setLongitude(lng);

            int dist = (int) myLocation.distanceTo(location) / 1000;

            Log.e(TAG, "distance " + dist);

            dist = dist + 1;

            if (dist < 1) {
                dist = 1;
            }

            changeZoomLevel(dist);

            ARData.addMarkers(markers);
        } catch (NullPointerException e) {
            Log.e(TAG, e);
        }
    }

    /*
     * Load stores in background thread
     */
    private void loadStoresInBackground() {


        Log.e("***********", "LOAD STORES");
        if (!activityIsFinished) {
            if (navigatedFromStoreView) {
                addFourMarkers();
            } else {
                if (mTask == null) {
                    mTask = new AsyncTask<Object, Object, Object>() {
                        RestResponse response;

                        @Override
                        protected Object doInBackground(Object... params) {

                            try {

                                Gson gson = new Gson();

                                SendValueAR sendValue = new SendValueAR();

                                sendValue.setLat(TapstorData.getInstance().getLatitude());

                                sendValue.setLng(TapstorData.getInstance().getLongitude());

                                sendValue.setToken(TapstorData.getInstance().getUserToken());

                                String reader = RestServices.getInstance().postOperation(sendValue,
                                        RestServices.getInstance().AR_STORES);

                                response = gson.fromJson(reader, RestResponse.class);

                            } catch (Exception ex) {
                                ex.printStackTrace();

                            }
                            return null;

                        }

                        @Override
                        protected void onPostExecute(Object result) {
                            try {

                                if (response.result_a_reality.error.equals("")) {

                                    DistanceComparator distanceComparator = new DistanceComparator();
                                    Collections.sort(response.result_a_reality.results, distanceComparator);

                                    arResults = response.result_a_reality.results;
                                    addMyMarkers(arrayStart);

                                }
                            } catch (Exception e) {
                                Log.e(TAG, e);
                            }

                            mTask = null;
                        }

                    };
                    mTask.execute();
                }

            }
        }
    }
}
