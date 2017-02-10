package com.jwetherell.augmented_reality.activity;

import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.view.Surface;

import com.iproject.tapstor.helper.TapstorActivity;
import com.iproject.tapstor.library.FusedLocationAccess;
import com.iproject.tapstor.library.Log;
import com.jwetherell.augmented_reality.common.LowPassFilter;
import com.jwetherell.augmented_reality.common.Matrix;
import com.jwetherell.augmented_reality.common.Navigation;
import com.jwetherell.augmented_reality.common.Orientation;
import com.jwetherell.augmented_reality.data.ARData;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class extends Activity and processes sensor data and location data.
 *
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public class SensorsActivity extends TapstorActivity implements SensorEventListener,
        FusedLocationAccess.OnLocationChangedListener {

    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS = 30;
    private static final String TAG = "SensorsActivity";
    private static final AtomicBoolean computing = new AtomicBoolean(false);
    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;

    private static final float temp[] = new float[9]; // Temporary rotation matrix in Android format
    private static final float rotation[] = new float[9]; // Final rotation matrix in Android format
    private static final float grav[] = new float[3]; // Gravity (a.k.a accelerometer data)
    private static final float mag[] = new float[3]; // Magnetic

	/*
     * Using Matrix operations instead. This was way too inaccurate, private
	 * static final float apr[] = new float[3]; //Azimuth, pitch, roll
	 */

    private static final Matrix worldCoord = new Matrix();
    private static final Matrix magneticCompensatedCoord = new Matrix();
    private static final Matrix xAxisRotation = new Matrix();
    private static final Matrix yAxisRotation = new Matrix();
    private static final Matrix mageticNorthCompensation = new Matrix();
    private static GeomagneticField gmf = null;
    private static float smooth[] = new float[3];
    private static SensorManager sensorMgr = null;
    private static List<Sensor> sensors = null;

    private static Sensor sensorGrav = null;
    private static Sensor sensorMag = null;


    @Override
    protected void onPause() {
        super.onPause();
        FusedLocationAccess.getInstance(this).removeOnLocationChangedListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        FusedLocationAccess.getInstance(this).addOnLocationChangedListener(this);
        FusedLocationAccess.getInstance(this).enableLocationListener();
        float neg90rads = (float) Math.toRadians(-90);

        // Counter-clockwise rotation at -90 degrees around the x-axis
        // [ 1, 0, 0 ]
        // [ 0, cos, -sin ]
        // [ 0, sin, cos ]
        xAxisRotation.set(1f, 0f, 0f, 0f, (float) Math.cos(neg90rads),
                -(float) Math.sin(neg90rads), 0f, (float) Math.sin(neg90rads),
                (float) Math.cos(neg90rads));

        // Counter-clockwise rotation at -90 degrees around the y-axis
        // [ cos, 0, sin ]
        // [ 0, 1, 0 ]
        // [ -sin, 0, cos ]
        yAxisRotation.set((float) Math.cos(neg90rads), 0f,
                (float) Math.sin(neg90rads), 0f, 1f, 0f,
                -(float) Math.sin(neg90rads), 0f, (float) Math.cos(neg90rads));

        try {
            sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);

            sensors = sensorMgr.getSensorList(Sensor.TYPE_ACCELEROMETER);
            if (sensors.size() > 0) {
                Log.i(TAG, "more than zero accelerometer " + sensors.size());
                sensorGrav = sensors.get(0);
            }

            sensors = sensorMgr.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
            if (sensors.size() > 0) {
                Log.i(TAG, "more than zero magentic field " + sensors.size());

                sensorMag = sensors.get(0);
            }

            sensorMgr.registerListener(this, sensorGrav, SensorManager.SENSOR_DELAY_FASTEST);
            sensorMgr.registerListener(this, sensorMag, SensorManager.SENSOR_DELAY_FASTEST);

            try {


                gmf = new GeomagneticField((float) ARData.getCurrentLocation()
                        .getLatitude(), (float) ARData.getCurrentLocation()
                        .getLongitude(), (float) ARData.getCurrentLocation()
                        .getAltitude(), System.currentTimeMillis());

                float dec = (float) Math.toRadians(-gmf.getDeclination());

                synchronized (mageticNorthCompensation) {
                    // Identity matrix
                    // [ 1, 0, 0 ]
                    // [ 0, 1, 0 ]
                    // [ 0, 0, 1 ]
                    mageticNorthCompensation.toIdentity();

                    // Counter-clockwise rotation at negative declination around the y-axis
                    // note: declination of the horizontal component of the magnetic field
                    // from true north, in degrees (i.e. positive means the  magnetic
                    // field is rotated east that much from true north).
                    // note2: declination is the difference between true north
                    // and magnetic north
                    // [ cos, 0, sin ]
                    // [ 0, 1, 0 ]
                    // [ -sin, 0, cos ]
                    mageticNorthCompensation.set((float) Math.cos(dec), 0f,
                            (float) Math.sin(dec), 0f, 1f, 0f,
                            -(float) Math.sin(dec), 0f, (float) Math.cos(dec));
                }
            } catch (Exception ex) {
                Log.e(TAG, ex);
            }
        } catch (Exception ex1) {
            ex1.printStackTrace();
            try {
                if (sensorMgr != null) {
                    sensorMgr.unregisterListener(this, sensorGrav);
                    sensorMgr.unregisterListener(this, sensorMag);
                    sensorMgr = null;
                }

            } catch (Exception ex2) {
                Log.e(TAG, ex2);
            }
        }
    }


    @Override
    protected void onStop() {
        super.onStop();

        try {
            Log.e(TAG, "STOP UPDATES");
            try {
                sensorMgr.unregisterListener(this, sensorGrav);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            try {
                sensorMgr.unregisterListener(this, sensorMag);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            sensorMgr = null;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    @Override
    public void onSensorChanged(SensorEvent evt) {
        if (!computing.compareAndSet(false, true))
            return;

        if (evt.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            if (AugmentedReality.useDataSmoothing) {
                smooth = LowPassFilter.filter(0.5f, 1.0f, evt.values, grav);
                grav[0] = smooth[0];
                grav[1] = smooth[1];
                grav[2] = smooth[2];
            } else {
                grav[0] = evt.values[0];
                grav[1] = evt.values[1];
                grav[2] = evt.values[2];
            }
            Orientation.calcOrientation(grav, getWindowManager().getDefaultDisplay().getRotation());
            ARData.setDeviceOrientation(Orientation.getDeviceOrientation());
            ARData.setDeviceOrientationAngle(Orientation.getDeviceAngle());
        } else if (evt.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            if (AugmentedReality.useDataSmoothing) {
                smooth = LowPassFilter.filter(2.0f, 4.0f, evt.values, mag);
                mag[0] = smooth[0];
                mag[1] = smooth[1];
                mag[2] = smooth[2];
            } else {
                mag[0] = evt.values[0];
                mag[1] = evt.values[1];
                mag[2] = evt.values[2];
            }
        }

        // // Find real world position relative to phone location ////
        // Get rotation matrix given the gravity and geomagnetic matrices
        SensorManager.getRotationMatrix(temp, null, grav, mag);

        switch (getWindowManager().getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_0:
                SensorManager.remapCoordinateSystem(temp, SensorManager.AXIS_Z, SensorManager.AXIS_MINUS_Y, rotation);
                break;

            case Surface.ROTATION_90:
                SensorManager.remapCoordinateSystem(temp, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_Z, rotation);
                break;

            case Surface.ROTATION_180:
                SensorManager.remapCoordinateSystem(temp, SensorManager.AXIS_Z, SensorManager.AXIS_MINUS_Y, rotation);
                break;

            case Surface.ROTATION_270:
                SensorManager.remapCoordinateSystem(temp, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_Z, rotation);
                break;
            default:
                break;
        }

		/*
         * Using Matrix operations instead. This was way too inaccurate, //Get
		 * the azimuth, pitch, roll SensorManager.getOrientation(rotation,apr);
		 * float floatAzimuth = (float)Math.toDegrees(apr[0]); if
		 * (floatAzimuth<0) floatAzimuth+=360; ARData.setAzimuth(floatAzimuth);
		 * ARData.setPitch((float)Math.toDegrees(apr[1]));
		 * ARData.setRoll((float)Math.toDegrees(apr[2]));
		 */

        // Convert from float[9] to Matrix
        worldCoord
                .set(rotation[0], rotation[1], rotation[2], rotation[3],
                        rotation[4], rotation[5], rotation[6], rotation[7],
                        rotation[8]);

        // // Find position relative to magnetic north ////
        // Identity matrix
        // [ 1, 0, 0 ]
        // [ 0, 1, 0 ]
        // [ 0, 0, 1 ]
        magneticCompensatedCoord.toIdentity();

        synchronized (mageticNorthCompensation) {
            // Cross product the matrix with the magnetic north compensation
            magneticCompensatedCoord.prod(mageticNorthCompensation);
        }

        // The compass assumes the screen is parallel to the ground with the
        // screen pointing
        // to the sky, rotate to compensate.
        magneticCompensatedCoord.prod(xAxisRotation);

        // Cross product with the world coordinates to get a mag north
        // compensated coords
        magneticCompensatedCoord.prod(worldCoord);

        // Y axis
        magneticCompensatedCoord.prod(yAxisRotation);

        // Invert the matrix since up-down and left-right are reversed in
        // landscape mode
        magneticCompensatedCoord.invert();

        // Set the rotation matrix (used to translate all object from lat/lon to
        // x/y/z)
        ARData.setRotationMatrix(magneticCompensatedCoord);

        // Update the pitch and bearing using the phone's rotation matrix
        Navigation.calcPitchBearing(magneticCompensatedCoord);
        ARData.setAzimuth(Navigation.getAzimuth());

        computing.set(false);
    }


    @Override
    public void onLocationChanged(Location location) {

        ARData.setCurrentLocation(location);
        gmf = new GeomagneticField((float) ARData.getCurrentLocation()
                .getLatitude(), (float) ARData.getCurrentLocation()
                .getLongitude(), (float) ARData.getCurrentLocation()
                .getAltitude(), System.currentTimeMillis());

        float dec = (float) Math.toRadians(-gmf.getDeclination());

        synchronized (mageticNorthCompensation) {
            mageticNorthCompensation.toIdentity();

            mageticNorthCompensation.set((float) Math.cos(dec), 0f,
                    (float) Math.sin(dec), 0f, 1f, 0f, -(float) Math.sin(dec),
                    0f, (float) Math.cos(dec));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (sensor == null)
            throw new NullPointerException();

        if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD
                && accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            Log.e(TAG, "Compass data unreliable");
        }
    }

}
