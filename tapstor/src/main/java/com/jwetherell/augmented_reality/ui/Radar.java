package com.jwetherell.augmented_reality.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.iproject.tapstor.helper.TapstorData;
import com.jwetherell.augmented_reality.activity.AugmentedReality;
import com.jwetherell.augmented_reality.camera.CameraModel;
import com.jwetherell.augmented_reality.common.Orientation.ORIENTATION;
import com.jwetherell.augmented_reality.data.ARData;
import com.jwetherell.augmented_reality.data.ScreenPosition;
import com.jwetherell.augmented_reality.ui.objects.PaintableCircle;
import com.jwetherell.augmented_reality.ui.objects.PaintableLine;
import com.jwetherell.augmented_reality.ui.objects.PaintablePosition;
import com.jwetherell.augmented_reality.ui.objects.PaintableRadarPoints;
import com.jwetherell.augmented_reality.ui.objects.PaintableText;

/**
 * This class will visually represent a radar screen with a radar radius and
 * blips on the screen in their appropriate locations.
 *
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public class Radar {

    // private static final int LINE_COLOR = Color.argb(150, 0, 0, 220);
    private static final int LINE_COLOR = Color.argb(150, 255, 255, 255);
    private static final int RADAR_COLOR_BLUE = Color.argb(100, 0, 0, 200);
    private static final int RADAR_COLOR_GREEN = Color.argb(100, 0, 255, 0);
    private static final int TEXT_COLOR = Color.argb(100, 255, 255, 255);
    private static final int BLACK_COLOR = Color.argb(100, 0, 0, 0);
    private static final int TEXT_SIZE = 15;
    private static final StringBuilder DIR_TXT = new StringBuilder();
    private static final StringBuilder RADAR_TXT = new StringBuilder();
    private static final StringBuilder DIST_TXT = new StringBuilder();
    private static final StringBuilder DEC_TXT = new StringBuilder();
    // TODO change dp to pixels
    public static float RADIUS;
    // TODO change dp to pixels
    private static float PAD_X;
    // TODO change dp to pixels
    private static float PAD_Y;
    private static ScreenPosition leftRadarLine = null;
    private static ScreenPosition rightRadarLine = null;
    private static PaintablePosition leftLineContainer = null;
    private static PaintablePosition rightLineContainer = null;
    private static PaintablePosition circleContainer = null;

    private static PaintableRadarPoints radarPoints = null;
    private static PaintablePosition pointsContainer = null;

    private static PaintableText paintableText = null;
    private static PaintablePosition paintedContainer = null;
    private int width;
    private int height;
    private int radiousNumber = 0;

    public Radar(Context context) {

        // TODO change dp to pixels
        RADIUS = convertDpToPixel(40, context);

        // TODO change dp to pixels
        PAD_X = convertDpToPixel(10, context);
        // TODO change dp to pixels
        PAD_Y = convertDpToPixel(65, context);

        if (leftRadarLine == null)
            leftRadarLine = new ScreenPosition();
        if (rightRadarLine == null)
            rightRadarLine = new ScreenPosition();
    }

    private static String formatDist(float meters) {
        DIST_TXT.setLength(0);
        if (meters < 1000)
            DIST_TXT.append((int) meters).append("μ");
        else if (meters < 10000)
            DIST_TXT.append(formatDec(meters / 1000f, 1)).append("χλμ");
        else
            DIST_TXT.append((int) (meters / 1000f)).append("χλμ");
        return DIST_TXT.toString();
    }

    private static String formatDec(float val, int dec) {
        DEC_TXT.setLength(0);
        int factor = (int) Math.pow(10, dec);

        int front = (int) (val);
        int back = (int) Math.abs(val * (factor)) % factor;

        DEC_TXT.append(front).append(".").append(back);
        return DEC_TXT.toString();
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
     * Draw the radar on the given Canvas.
     *
     * @param canvas Canvas to draw on.
     * @throws NullPointerException if Canvas is NULL.
     */
    public void draw(Canvas canvas, Context context) {
        if (canvas == null)
            throw new NullPointerException();

        // Adjust upside down to compensate for zoom-bar
        int ui_ud_pad = 80;
        if (AugmentedReality.ui_portrait)
            ui_ud_pad = 55;

        ORIENTATION orient = ORIENTATION.LANDSCAPE;
        // if (AugmentedReality.useRadarAutoOrientate) {
        // orient = ARData.getDeviceOrientation();
        // if (orient == ORIENTATION.PORTRAIT) {
        canvas.save();
        canvas.translate(0, canvas.getHeight());
        canvas.rotate(-90);
        // } else if (orient == ORIENTATION.PORTRAIT_UPSIDE_DOWN) {
        // canvas.save();
        // canvas.translate(canvas.getWidth() - ui_ud_pad, 0);
        // canvas.rotate(90);
        // } else if (orient == ORIENTATION.LANDSCAPE_UPSIDE_DOWN) {
        // canvas.save();
        // canvas.translate(canvas.getWidth() - ui_ud_pad,
        // canvas.getHeight());
        // canvas.rotate(180);
        // } else {
        // // If landscape, do nothing
        // }
        // }

        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;

        // Update the radar graphics and text based upon the new pitch and
        // bearing
        canvas.save();
        canvas.translate(0, 5);
        drawRadarCircle(canvas, context);
        drawRadarPoints(canvas);
        drawRadarLines(canvas);
        drawRadarText(canvas);
        canvas.restore();

        if (orient != ORIENTATION.LANDSCAPE)
            canvas.restore();
    }

    private void drawRadarCircle(Canvas canvas, Context context) {
        if (canvas == null)
            throw new NullPointerException();

        if (circleContainer == null) {

            PaintableCircle paintableCircle = new PaintableCircle(
                    RADAR_COLOR_BLUE, RADIUS, true);

            circleContainer = new PaintablePosition(paintableCircle, height
                    - PAD_X - RADIUS, width - PAD_Y - RADIUS, 0, 1);

        }

        circleContainer.paint(canvas);

        float scale;
        if (radiousNumber == 0) {
            scale = 0.4f;
        } else if (radiousNumber == 10) {
            scale = 0.6f;
        } else if (radiousNumber == 20) {
            scale = 0.8f;
        } else {
            scale = 1f;

        }

        PaintableCircle paintableCircle2 = new PaintableCircle(
                RADAR_COLOR_GREEN, RADIUS, true);

        PaintablePosition circleContainer2 = new PaintablePosition(
                paintableCircle2, height - PAD_X - RADIUS, width - PAD_Y
                - RADIUS, 0, scale);
        circleContainer2.paint(canvas);

    }

    private void drawRadarPoints(Canvas canvas) {

        if (canvas == null)
            throw new NullPointerException();

        if (radarPoints == null)
            radarPoints = new PaintableRadarPoints();

        if (pointsContainer == null)
            pointsContainer = new PaintablePosition(radarPoints, 0, 0, 0, 1);
        else
            pointsContainer.set(radarPoints, 0, 0, 0, 1);

        // Rotate the points to match the azimuth
        canvas.save();
        canvas.translate((height - PAD_X - radarPoints.getWidth() / 2), (width
                - PAD_Y - radarPoints.getHeight() / 2));
        canvas.rotate(-ARData.getAzimuth());
        canvas.scale(1, 1);
        canvas.translate(-(radarPoints.getWidth() / 2),
                -(radarPoints.getHeight() / 2));
        pointsContainer.paint(canvas);
        canvas.restore();

    }

    private void drawRadarLines(Canvas canvas) {

        if (canvas == null)
            throw new NullPointerException();

        // Left line
        if (leftLineContainer == null) {
            leftRadarLine.set(0, -RADIUS);
            leftRadarLine.rotate(-CameraModel.DEFAULT_VIEW_ANGLE / 2);
            leftRadarLine.add(height - PAD_X - RADIUS, width - PAD_Y - RADIUS);

            float leftX = leftRadarLine.getX() - (height - PAD_X - RADIUS);
            float leftY = leftRadarLine.getY() - (width - PAD_Y - RADIUS);
            PaintableLine leftLine = new PaintableLine(LINE_COLOR, leftX, leftY);
            leftLineContainer = new PaintablePosition(leftLine, height - PAD_X
                    - RADIUS, width - PAD_Y - RADIUS, 0, 1);
        }

        leftLineContainer.paint(canvas);

        // Right line
        if (rightLineContainer == null) {

            rightRadarLine.set(0, -RADIUS);
            rightRadarLine.rotate(CameraModel.DEFAULT_VIEW_ANGLE / 2);
            rightRadarLine.add(height - PAD_X - RADIUS, width - PAD_Y - RADIUS);

            float rightX = rightRadarLine.getX() - (height - PAD_X - RADIUS);
            float rightY = rightRadarLine.getY() - (width - PAD_Y - RADIUS);

            PaintableLine rightLine = new PaintableLine(LINE_COLOR, rightX,
                    rightY);
            rightLineContainer = new PaintablePosition(rightLine, height
                    - PAD_X - RADIUS, width - PAD_Y - RADIUS, 0, 1);
        }

        rightLineContainer.paint(canvas);
    }

    private void drawRadarText(Canvas canvas) {

        if (canvas == null)
            throw new NullPointerException();

        // Direction text
        int range = (int) (ARData.getAzimuth() / (360f / 16f));
        DIR_TXT.setLength(0);
        if (range == 15 || range == 0)
            DIR_TXT.append(TapstorData.getInstance().north);
        else if (range == 1 || range == 2)
            DIR_TXT.append(TapstorData.getInstance().northEast);
        else if (range == 3 || range == 4)
            DIR_TXT.append(TapstorData.getInstance().east);
        else if (range == 5 || range == 6)
            DIR_TXT.append(TapstorData.getInstance().southEast);
        else if (range == 7 || range == 8)
            DIR_TXT.append(TapstorData.getInstance().south);
        else if (range == 9 || range == 10)
            DIR_TXT.append(TapstorData.getInstance().southWest);
        else if (range == 11 || range == 12)
            DIR_TXT.append(TapstorData.getInstance().west);
        else if (range == 13 || range == 14)
            DIR_TXT.append(TapstorData.getInstance().northWest);

        int azimuth = (int) ARData.getAzimuth();
        RADAR_TXT.setLength(0);
        RADAR_TXT
                // .append(azimuth).append((char) 176).append(" ")
                .append(DIR_TXT);
        // Azimuth text
        radarText(canvas, RADAR_TXT.toString(), (height - PAD_X - RADIUS),
                (width - PAD_Y - 5), true);

        // Zoom text
        radarText(canvas, formatDist(ARData.getRadius() * 1000), (height
                - PAD_X - RADIUS), (width - PAD_Y - (RADIUS * 2) - 10), false);
    }

    private void radarText(Canvas canvas, String txt, float x, float y,
                           boolean bg) {
        if (canvas == null || txt == null)
            throw new NullPointerException();

        if (paintableText == null)
            paintableText = new PaintableText(txt, TEXT_COLOR, TEXT_SIZE, bg);
        else
            paintableText.set(txt, TEXT_COLOR, TEXT_SIZE, bg);

        if (paintedContainer == null)
            paintedContainer = new PaintablePosition(paintableText, x, y, 0, 1);
        else
            paintedContainer.set(paintableText, x, y, 0, 1);

        paintedContainer.paint(canvas);
    }

    public void updateScaleNumber(int arrayStart) {

        radiousNumber = arrayStart;
    }
}
