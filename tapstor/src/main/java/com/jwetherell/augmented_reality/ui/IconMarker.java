package com.jwetherell.augmented_reality.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.DisplayMetrics;

import com.iproject.tapstor.rest.ArResults;
import com.jwetherell.augmented_reality.ui.objects.PaintableIcon;

/**
 * This class extends Marker and draws an icon instead of a circle for it's
 * visual representation.
 *
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public class IconMarker extends Marker {

    private Bitmap bitmap = null;
    private Context context;

    public IconMarker(String name, double latitude, double longitude,
                      double altitude, int color, Bitmap bitmap, ArResults store,
                      Context context) {
        //super(name, latitude, longitude, altitude, color, store);
        super(name, latitude, longitude, 0, color, store);
        this.bitmap = bitmap;
        this.context = context;
    }

    /**
     * converts density pixels to pixels
     *
     * @param dp      density pixels
     * @param context activity context
     * @return pixels in float type
     */
    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void drawIcon(Canvas canvas) {
        if (canvas == null || bitmap == null)
            throw new NullPointerException();

        // gpsSymbol is defined in Marker
        if (gpsSymbol == null)
            gpsSymbol = new PaintableIcon(bitmap, (int) convertDpToPixel(100,
                    context), (int) convertDpToPixel(178, context));
        super.drawIcon(canvas);
    }

}
