package com.jwetherell.augmented_reality.activity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.view.Display;
import android.view.View;

import com.iproject.tapstor.helper.TapstorData;
import com.iproject.tapstor.library.Log;
import com.jwetherell.augmented_reality.common.Orientation.ORIENTATION;
import com.jwetherell.augmented_reality.data.ARData;
import com.jwetherell.augmented_reality.ui.Marker;
import com.jwetherell.augmented_reality.ui.Radar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class extends the View class and is designed draw the zoom bar, radar
 * circle, and markers on the View.
 *
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public class AugmentedView extends View {

    private static final String TAG = "AugmentedView";
    private static final AtomicBoolean drawing = new AtomicBoolean(false);
    private static final float[] locationArray = new float[3];
    private static final List<Marker> cache = new ArrayList<>();
    private static final Set<Marker> updated = new HashSet<>();
    static int centerWidth, centerHeight;
    private static Radar radar;
    Canvas canvas;
    private Context context;

    public AugmentedView(Context context) {
        super(context);

        this.context = context;
        Display display = ((Activity) context).getWindowManager()
                .getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        centerWidth = width / 2;
        centerHeight = height / 2;

        Log.v(TAG, "width height          = " + width + " " + height);
        radar = new Radar(context);
        Log.v(TAG, "portrait              = " + AugmentedReality.ui_portrait);
        Log.v(TAG, "useCollisionDetection = "
                + AugmentedReality.useCollisionDetection);
        Log.v(TAG, "useSmoothing          = "
                + AugmentedReality.useDataSmoothing);
        Log.v(TAG, "showRadar             = " + AugmentedReality.showRadar);
        Log.v(TAG, "showZoomBar           = " + AugmentedReality.showZoomBar);
    }

    // TODO FIX Seems a bit buggy may be because of marker rendering
    private static Marker adjustForCollisionsMyOwnImplementation(Canvas canvas,
                                                                 List<Marker> collection) {
        boolean landscape = false;
        if (ARData.getDeviceOrientation() == ORIENTATION.LANDSCAPE
                || ARData.getDeviceOrientation() == ORIENTATION.LANDSCAPE_UPSIDE_DOWN) {
            landscape = true;
            // Log.e(TAG, "ORIENTATION_LANDSCAPE");
        } else {
            landscape = false;
            // Log.e(TAG, "ORIENTATION_Portrait");
        }

        updated.clear();

        Marker closestMarker = null;
        int closestDistance = 0;
        // Log.d(TAG, "CHECK FOR COLLISION DETECTION");
        // Update the AR markers for collisions
        for (int i = 0; i < collection.size(); i++) {
            Marker marker1 = collection.get(i);

            if (!marker1.isInView()) {
                updated.add(marker1);
                // Log.d(TAG, "NOT IN VIEW ADD IT");
                continue;
            }

            if (updated.contains(marker1)) {
                // Log.d(TAG, "ALREADY ADDED");
                continue;
            }

            if (landscape) {
                if (closestDistance == 0) {
                    // Log.d(TAG, "SET FIRST AS CLOSEST");
                    closestMarker = marker1;

                    closestDistance = (int) Math.abs(centerWidth
                            - marker1.getScreenPosition().getX());

                } else if (closestDistance > Math.abs(centerWidth
                        - marker1.getScreenPosition().getX())) {

                    updated.add(closestMarker);
                    closestMarker = marker1;

                    closestDistance = (int) Math.abs(centerWidth
                            - marker1.getScreenPosition().getX());

                } else {
                    // Log.d(TAG, "Further add it now");
                    updated.add(marker1);
                }
            } else {
                if (closestDistance == 0) {
                    // Log.d(TAG, "SET FIRST AS CLOSEST");
                    closestMarker = marker1;

                    closestDistance = (int) Math.abs(centerWidth
                            - marker1.getScreenPosition().getX());

                } else if (closestDistance > Math.abs(centerHeight
                        - marker1.getScreenPosition().getY())) {

                    updated.add(closestMarker);
                    closestMarker = marker1;

                    closestDistance = (int) Math.abs(centerHeight
                            - marker1.getScreenPosition().getY());

                } else {
                    // Log.d(TAG, "Further add it now");
                    updated.add(marker1);
                }
            }

        }
        if (closestMarker != null) {

            // Log.d(TAG, "ADD CLOSEST");
            updated.add(closestMarker);
            closestMarker.draw(canvas);

            TapstorData.getInstance().centerMarker = closestMarker;
        }

        return closestMarker;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onDraw(Canvas canvas) {
        if (canvas == null)
            return;

        this.canvas = canvas;
        if (drawing.compareAndSet(false, true)) {
            // Log.v(TAG, "DIRTY flag found, re-populating the cache.");

            // Get all the markers
            List<Marker> collection = ARData.getMarkers();

            // Prune all the markers that are out of the radar's radius (speeds
            // up drawing and collision detection)
            cache.clear();
            for (Marker m : collection) {
                m.update(canvas, 0, 0);
                if (m.isOnRadar() && m.isInView())
                    cache.add(m);
            }
            collection = cache;

            Marker centerMarker = null;
            if (AugmentedReality.useCollisionDetection) {

                centerMarker = adjustForCollisionsMyOwnImplementation(canvas,
                        collection);

            }
            // Draw AR markers in reverse order since the last drawn should be
            // the closest
            ListIterator<Marker> iter = collection.listIterator(collection
                    .size());

            while (iter.hasPrevious()) {

                Marker marker = iter.previous();
                if (centerMarker == null || !marker.equals(centerMarker)) {
                    marker.draw(canvas);
                }
            }

            if (centerMarker != null) {
                centerMarker.draw(canvas);

            }

            // Radar circle and radar markers
            if (AugmentedReality.showRadar)
                radar.draw(canvas, context);
            drawing.set(false);

        }
    }

    public void repaintRadar(int arrayStart) {
        Log.e(TAG, "REPAINT RADAR " + arrayStart);
        // canvas.drawColor(0, Mode.CLEAR);
        radar.updateScaleNumber(arrayStart);

    }

}
