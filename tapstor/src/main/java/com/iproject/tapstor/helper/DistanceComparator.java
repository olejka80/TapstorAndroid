package com.iproject.tapstor.helper;

import android.location.Location;

import com.iproject.tapstor.library.Log;
import com.iproject.tapstor.objects.Results;
import com.iproject.tapstor.objects.Store;
import com.iproject.tapstor.rest.ArResults;

import java.util.Comparator;

public class DistanceComparator implements Comparator<Object> {

    private static final String TAG = "DistanceComparator";

    @Override
    public int compare(Object arg0, Object arg1) {
        try {
            if (arg0 instanceof ArResults) {

                ArResults ar0 = (ArResults) arg0;
                ArResults ar1 = (ArResults) arg1;

                float dist1 = distanceBetweenTwoPoints(TapstorData
                                .getInstance().getLatitude(), TapstorData.getInstance()
                                .getLongitude(), Double.parseDouble(ar0.lat),
                        Double.parseDouble(ar0.lng));

                float dist2 = distanceBetweenTwoPoints(TapstorData
                                .getInstance().getLatitude(), TapstorData.getInstance()
                                .getLongitude(), Double.parseDouble(ar1.lat),
                        Double.parseDouble(ar1.lng));

                return Float.compare(dist1, dist2);

            } else if (arg0 instanceof Results) {

                Results ar0 = (Results) arg0;
                Results ar1 = (Results) arg1;

                float dist1 = distanceBetweenTwoPoints(TapstorData
                                .getInstance().getLatitude(), TapstorData.getInstance()
                                .getLongitude(), Double.parseDouble(ar0.lat),
                        Double.parseDouble(ar0.lng));

                float dist2 = distanceBetweenTwoPoints(TapstorData
                                .getInstance().getLatitude(), TapstorData.getInstance()
                                .getLongitude(), Double.parseDouble(ar1.lat),
                        Double.parseDouble(ar1.lng));

                return Float.compare(dist1, dist2);
            } else {
                Store ar0 = (Store) arg0;
                Store ar1 = (Store) arg1;

                float dist1 = distanceBetweenTwoPoints(TapstorData
                                .getInstance().getLatitude(), TapstorData.getInstance()
                                .getLongitude(), Double.parseDouble(ar0.lat),
                        Double.parseDouble(ar0.lng));

                float dist2 = distanceBetweenTwoPoints(TapstorData
                                .getInstance().getLatitude(), TapstorData.getInstance()
                                .getLongitude(), Double.parseDouble(ar1.lat),
                        Double.parseDouble(ar1.lng));

                return Float.compare(dist1, dist2);
            }

        } catch (Exception e) {
            return 0;
        }

    }

    public float distanceBetweenTwoPoints(double lat1, double lng1,
                                          double lat2, double lng2) {
        // The computed distance is stored in results[0].
        // If results has length 2 or greater, the initial bearing is stored in
        // results[1].
        // If results has length 3 or greater, the final bearing is stored in
        // results[2].
        try {
            float[] results = new float[1];
            Location.distanceBetween(lat1, lng1, lat2, lng2, results);

            return results[0] / 1000;

        } catch (Exception e) {
            Log.e(TAG, e);
            return 0.0f;
        }
    }
}