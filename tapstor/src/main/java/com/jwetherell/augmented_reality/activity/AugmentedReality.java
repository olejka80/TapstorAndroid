package com.jwetherell.augmented_reality.activity;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.DisplayMetrics;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.iproject.tapstor.AugmentedRealityActivity;
import com.iproject.tapstor.R;
import com.iproject.tapstor.helper.TapstorData;
import com.iproject.tapstor.library.Log;
import com.iproject.tapstor.rest.ArResults;
import com.jwetherell.augmented_reality.data.ARData;
import com.jwetherell.augmented_reality.ui.Marker;
import com.jwetherell.augmented_reality.widget.VerticalSeekBar;
import com.jwetherell.augmented_reality.widget.VerticalTextView;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.List;

/**
 * This class extends the SensorsActivity and is designed tie the AugmentedView
 * and zoom bar together.
 *
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public class AugmentedReality extends SensorsActivity implements OnTouchListener, SurfaceHolder.Callback {

    public static final float MAX_ZOOM = 100; // in KM
    public static final float ONE_PERCENT = MAX_ZOOM / 100f;
    public static final float TEN_PERCENT = 10f * ONE_PERCENT;
    public static final float TWENTY_PERCENT = 2f * TEN_PERCENT;
    public static final float EIGHTY_PERCENTY = 4f * TWENTY_PERCENT;
    private static final String TAG = "AugmentedReality";
    private static final DecimalFormat FORMAT = new DecimalFormat("#.##");
    private static final int ZOOMBAR_BACKGROUND_COLOR = Color.argb(125, 55, 55, 55);
    private static final String END_TEXT = FORMAT.format(AugmentedReality.MAX_ZOOM) + " km";
    private static final int END_TEXT_COLOR = Color.WHITE;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    public static boolean ui_portrait = true; // Defaulted to LANDSCAPE use
    public static boolean showRadar = true;
    public static boolean showZoomBar = false;
    public static boolean useRadarAutoOrientate = false;
    public static boolean useMarkerAutoRotate = true;
    public static boolean useDataSmoothing = true;
    public static boolean useCollisionDetection = true; // defaulted OFF
    protected static WakeLock wakeLock = null;
    // protected static CameraSurface camScreen = null;
    protected static VerticalSeekBar myZoomBar = null;
    protected static VerticalTextView endLabel = null;
    protected static LinearLayout zoomLayout = null;
    protected static AugmentedView augmentedView = null;

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 180);
        ORIENTATIONS.append(Surface.ROTATION_180, 0);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    public RelativeLayout myRelativeLayoutTop;
    int pixelHeight;
    int pixelWidth;
    float dpHeight, dpWidth;
    private Camera camera;
    private SurfaceView surfaceView;
    private SurfaceHolder holder;
    private OnSeekBarChangeListener myZoomBarOnSeekBarChangeListener = new OnSeekBarChangeListener() {

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            updateDataOnZoom();
        }

        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            updateDataOnZoom();
        }
    };

    private static float calcZoomLevel() {
        int myZoomLevel = myZoomBar.getProgress();
        float myout = 0;

        float percent = 0;
        if (myZoomLevel <= 25) {
            percent = myZoomLevel / 25f;
            myout = ONE_PERCENT * percent;
        } else if (myZoomLevel > 25 && myZoomLevel <= 50) {
            percent = (myZoomLevel - 25f) / 25f;
            myout = ONE_PERCENT + (TEN_PERCENT * percent);
        } else if (myZoomLevel > 50 && myZoomLevel <= 75) {
            percent = (myZoomLevel - 50f) / 25f;
            myout = TEN_PERCENT + (TWENTY_PERCENT * percent);
        } else {
            percent = (myZoomLevel - 75f) / 25f;
            myout = TWENTY_PERCENT + (EIGHTY_PERCENTY * percent);
        }

        return myZoomLevel;
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_augmented_camera);
        try {

            surfaceView = (SurfaceView) findViewById(R.id.surfaceView1);
            holder = surfaceView.getHolder();
            holder.addCallback(this);

            holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            pixelWidth = size.x;
            pixelHeight = size.y;
            dpWidth = convertPixelsToDp(pixelWidth, this);
            dpHeight = convertPixelsToDp(pixelHeight, this);

            Log.e(TAG, "width: " + pixelWidth + " : " + " height: " + pixelHeight);

            fixBottomView();
            fixTopView();
            radarClickListener();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        augmentedView = new AugmentedView(this);
        augmentedView.setOnTouchListener(this);
        LayoutParams augLayout = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        addContentView(augmentedView, augLayout);

        zoomLayout = new LinearLayout(this);
        zoomLayout.setVisibility((showZoomBar) ? LinearLayout.VISIBLE
                : LinearLayout.GONE);
        zoomLayout.setOrientation(LinearLayout.VERTICAL);
        zoomLayout.setPadding(5, 5, 5, 5);
        zoomLayout.setBackgroundColor(ZOOMBAR_BACKGROUND_COLOR);

        endLabel = new VerticalTextView(this);
        endLabel.setText(END_TEXT);
        endLabel.setTextColor(END_TEXT_COLOR);
        LinearLayout.LayoutParams zoomTextParams = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        zoomTextParams.gravity = Gravity.CENTER;
        zoomLayout.addView(endLabel, zoomTextParams);

        myZoomBar = new VerticalSeekBar(this);
        myZoomBar.setMax(100);
        myZoomBar.setProgress(50);
        myZoomBar.setOnSeekBarChangeListener(myZoomBarOnSeekBarChangeListener);
        LinearLayout.LayoutParams zoomBarParams = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT);
        zoomBarParams.gravity = Gravity.CENTER_HORIZONTAL;
        zoomLayout.addView(myZoomBar, zoomBarParams);

        FrameLayout.LayoutParams frameLayoutParams = new FrameLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT, Gravity.RIGHT);
        addContentView(zoomLayout, frameLayoutParams);

        updateDataOnZoom();

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNotDimScreen");

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();

        wakeLock.acquire();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPause() {
        super.onPause();

        wakeLock.release();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSensorChanged(SensorEvent evt) {
        super.onSensorChanged(evt);

        if (evt.sensor.getType() == Sensor.TYPE_ACCELEROMETER || evt.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            augmentedView.postInvalidate();
        }
    }

    /**
     * Called when the zoom bar has changed.
     */
    protected void updateDataOnZoom() {
        float zoomLevel = calcZoomLevel();
        ARData.setRadius(zoomLevel);
        ARData.setZoomLevel(FORMAT.format(zoomLevel));
        ARData.setZoomProgress(myZoomBar.getProgress());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onTouch(View view, MotionEvent me) {
        if (me.getAction() != MotionEvent.ACTION_DOWN)
            return false;
        boolean markerClicked = false;
        if (TapstorData.getInstance().centerMarker != null)
            if ((TapstorData.getInstance().centerMarker.handleClick(me.getX(),
                    me.getY()))) {
                markerClicked = true;
                markerTouched(TapstorData.getInstance().centerMarker);
                return true;
            }
        // See if the motion event is on a Marker
        for (Marker marker : ARData.getMarkers()) {
            if (marker.handleClick(me.getX(), me.getY())) {
                markerClicked = true;
                markerTouched(marker);
                return true;
            }
        }

        if (!markerClicked) {

            if (myRelativeLayoutTop.getVisibility() == View.VISIBLE) {
                if (!clickIsOutsideTop(me.getX(), me.getY())) {

                    myRelativeLayoutTop.setVisibility(View.INVISIBLE);

                } else {
                    if (clickIsInsideCallTextView(me.getX(), me.getY())) {

                        makePhoneCall();
                    } else {

                        topLayoutTouched();
                    }
                }

            }
        }

        return super.onTouchEvent(me);
    }

    private void makePhoneCall() {
        try {

            if (!((TextView) myRelativeLayoutTop.findViewById(R.id.textView5)).getText().toString().equals("")) {

                try {

                    Intent dial = new Intent();
                    dial.setAction("android.intent.action.DIAL");
                    dial.setData(Uri.parse("tel:" + ((TextView) myRelativeLayoutTop
                            .findViewById(R.id.textView5)).getText().toString()));
                    startActivity(dial);

                } catch (Exception e) {
                    Log.e(TAG, e);
                }

            }
        } catch (ActivityNotFoundException nfe) {
            Log.e("helloandroid dialing example", "Call failed", nfe);

        } catch (Exception e) {
            Log.e(TAG, e);
        }

    }

    private boolean clickIsInsideCallTextView(float x, float y) {

        TextView call = (TextView) findViewById(R.id.textView4);

        float bottomX = myRelativeLayoutTop.getHeight();
        float bottomY = call.getWidth();

        float topX = convertPixelsToDp(60, this);
        float topY = 0;

        Log.e(TAG, x + " " + topX + " " + x + " " + bottomX + " " + y + " "
                + topY + " " + y + " " + bottomY);
        if (x > topX && x < bottomX && y > topY && y < bottomY) {
            return true;
        }

        return false;
    }

    private boolean clickIsOutsideTop(float x, float y) {
        float bottomX = myRelativeLayoutTop.getHeight();
        float bottomY = myRelativeLayoutTop.getWidth();

        if (x < bottomX && y < bottomY) {
            return true;
        }

        return false;
    }

    protected void markerTouched(Marker marker) {
        Log.w(TAG, "markerTouched() not implemented. marker=" + marker.getName());
    }

    protected void radarTouched() {
        paintRadar(AugmentedRealityActivity.arrayStart);
        Log.w(TAG, "RADAR radarTouched() not implemented");
    }

    private void paintRadar(int arrayStart) {
        augmentedView.repaintRadar(arrayStart);

    }

    protected void topLayoutTouched() {
        Log.w(TAG, "Top Layout topLayoutTouched() not implemented");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (holder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            camera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here
        Camera.Parameters params = camera.getParameters();
        //params.set("orientation", "portrait");

        Size optimalSize = getOptimalPreviewSize(
                params.getSupportedPreviewSizes(), getResources()
                        .getDisplayMetrics().widthPixels, getResources()
                        .getDisplayMetrics().heightPixels);
        params.setPreviewSize(optimalSize.width, optimalSize.height);
        camera.setParameters(params);
        // camera.setDisplayOrientation(getRotation(camera));
        // start preview with new settings
        try {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                camera.setDisplayOrientation(ORIENTATIONS.get(this.getWindowManager().getDefaultDisplay().getRotation()));
            }
            camera.setPreviewDisplay(holder);
            camera.startPreview();

        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        //
        try {
            if (camera != null) {
                try {
                    camera.stopPreview();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                try {
                    camera.release();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                camera = null;
            }

            camera = Camera.open();
            // camera.setDisplayOrientation(90);
            camera.setPreviewDisplay(holder);

        } catch (Exception ex) {
            try {
                if (camera != null) {
                    try {
                        camera.stopPreview();
                    } catch (Exception ex1) {
                        ex.printStackTrace();
                    }
                    try {
                        camera.release();
                    } catch (Exception ex2) {
                        ex.printStackTrace();
                    }
                    camera = null;
                }
            } catch (Exception ex3) {
                ex.printStackTrace();
            }
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        try {
            if (camera != null) {
                try {
                    camera.stopPreview();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                try {
                    camera.release();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                camera = null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.05;
        double targetRatio = (double) w / h;

        if (sizes == null)
            return null;

        Size optimalSize = null;

        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Find size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    /**
     * This method converts device specific pixels to density independent
     * pixels.
     *
     * @param px      A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    private float convertPixelsToDp(float px, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return dp;
    }

    private void fixBottomView() {
        RelativeLayout myRelativeLayout = (RelativeLayout) findViewById(R.id.radar);

        RelativeLayout.LayoutParams params;

        params = new RelativeLayout.LayoutParams(pixelHeight,
                android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        // params.leftMargin =(int) ((pixelWidth/2) - convertDpToPixel(61,
        // this));
        // // params.topMargin = (int) (pixelWidth - convertDpToPixel(30,
        // this));

        myRelativeLayout.setLayoutParams(params);

        final RelativeLayout more = (RelativeLayout) findViewById(R.id.more);
        final LinearLayout moreOptions = (LinearLayout) findViewById(R.id.more_options);
        final TextView exit = (TextView) findViewById(R.id.exit);
        final TextView cancel = (TextView) findViewById(R.id.cancel);

        more.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (moreOptions.getVisibility() == View.GONE) {
                    moreOptions.setVisibility(View.VISIBLE);
                    more.setAlpha(0f);
                }

            }
        });

        exit.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();

            }
        });

        cancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                moreOptions.setVisibility(View.GONE);
                more.setAlpha(1f);

            }
        });

    }

    private void fixTopView() {

        myRelativeLayoutTop = (RelativeLayout) findViewById(R.id.store_info);

        myRelativeLayoutTop.getLayoutParams().width = pixelHeight;
        myRelativeLayoutTop.setPivotX(0);
        myRelativeLayoutTop.setPivotY(0);
        myRelativeLayoutTop.setRotation(-90f);

    }

    private void sendViewToBack(final View child) {
        final ViewGroup parent = (ViewGroup) child.getParent();
        if (null != parent) {
            parent.removeView(child);
            parent.addView(child, 0);
        }
    }

    public void populateTopView(ArResults store) {

        // myRelativeLayoutTop.bringToFront();

        Picasso.with(this).load(store.avatar)
                .into((ImageView) myRelativeLayoutTop.findViewById(R.id.image));

        ((TextView) myRelativeLayoutTop.findViewById(R.id.comp_name))
                .setText(store.title);

        ((TextView) myRelativeLayoutTop.findViewById(R.id.textView3))
                .setText(store.address);

        ((TextView) myRelativeLayoutTop.findViewById(R.id.textView5))
                .setText(store.phone);

        ((TextView) myRelativeLayoutTop.findViewById(R.id.textView5))
                .setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        try {

                            if (!((TextView) myRelativeLayoutTop
                                    .findViewById(R.id.textView5)).getText()
                                    .toString().equals("")) {

                                Intent dial = new Intent();
                                dial.setAction("android.intent.action.DIAL");
                                dial.setData(Uri.parse("tel:" + ((TextView) myRelativeLayoutTop
                                        .findViewById(R.id.textView5)).getText().toString()));
                                startActivity(dial);

                            }

                        } catch (Exception e) {
                            Log.e(TAG, e);
                        }
                    }
                });

        myRelativeLayoutTop.findViewById(R.id.fake_mask).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        topLayoutTouched();

                    }
                });

    }

    private void radarClickListener() {
        LinearLayout radar = (LinearLayout) findViewById(R.id.fakeView);
        radar.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                radarTouched();

            }
        });

    }

    public void changeZoomLevel(int zoomLevel) {
        myZoomBar.setProgress(zoomLevel);

    }

}
