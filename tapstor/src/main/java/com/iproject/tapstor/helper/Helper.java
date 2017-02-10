package com.iproject.tapstor.helper;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.location.Location;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.provider.Settings.Secure;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.maps.model.LatLng;
import com.iproject.tapstor.AnalyticsApplication;
import com.iproject.tapstor.AugmentedRealityActivity;
import com.iproject.tapstor.BuildConfig;
import com.iproject.tapstor.R;
import com.iproject.tapstor.library.CameraAccess;
import com.iproject.tapstor.library.Log;
import com.iproject.tapstor.library.SharedPreferencesStorage;
import com.iproject.tapstor.objects.Cat;
import com.iproject.tapstor.objects.Element;
import com.iproject.tapstor.objects.Store;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Helper {

    private static final String TAG = "Helper";
    private static final String CONTENT_URL = "http://tapstor.com/";
    private static final String NOTIFICATIONS_KEY = "notifications_key";

    public static void sendGoogleAnalyticsAction(String category, String action, String label, Context context) {
        if (BuildConfig.DEBUG) return;
        try {
            AnalyticsApplication app = (AnalyticsApplication) ((Activity) context).getApplication();
            Tracker t = app.getDefaultTracker();
            t.send(new HitBuilders.EventBuilder().setCategory(category).setAction(action).setLabel(label).build());
        } catch (Exception e) {
            Log.e(TAG, e);
        }
    }

    public static boolean checkProvider(Context context) {
        try {
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (SecurityException se) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static void setLocale(Context context, String language) {
        try {
            Locale locale = new Locale(language);
            Log.i(TAG, locale.getLanguage());
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            Resources resources = context.getResources();
            resources.updateConfiguration(config, resources.getDisplayMetrics());
        } catch (Exception e) {
            Log.e(TAG, e);
        }
    }

    /**
     * Creates or returns if already created a unique String identifier for the
     * device
     *
     * @param context the activity context
     * @return a string with a unique identifier for the device based on WiFi
     * manager
     */
    public static String getUniqueId(Context context) {

        String uniqueID = null;
        String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";

        SharedPreferences sharedPrefs = context.getSharedPreferences(
                PREF_UNIQUE_ID, Context.MODE_PRIVATE);
        uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null);

        Editor editor = sharedPrefs.edit();

        if (uniqueID == null) {
            WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            uniqueID = wm.getConnectionInfo().getMacAddress();

            editor.putString(PREF_UNIQUE_ID, uniqueID);
            editor.commit();

        }

        if (uniqueID == null) {
            try {
                final TelephonyManager tm = (TelephonyManager) context
                        .getSystemService(Context.TELEPHONY_SERVICE);

                final String tmDevice, tmSerial, androidId;
                tmDevice = "" + tm.getDeviceId();
                tmSerial = "" + tm.getSimSerialNumber();
                androidId = ""
                        + android.provider.Settings.Secure.getString(
                        context.getContentResolver(),
                        android.provider.Settings.Secure.ANDROID_ID);

                UUID deviceUuid = new UUID(androidId.hashCode(),
                        ((long) tmDevice.hashCode() << 32)
                                | tmSerial.hashCode());
                uniqueID = deviceUuid.toString();

                editor.putString(PREF_UNIQUE_ID, uniqueID);
                editor.commit();

            } catch (Exception e) {
                Log.e(TAG, e);
            }
        }

        if (uniqueID == null) {

            uniqueID = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);

            editor.putString(PREF_UNIQUE_ID, uniqueID);
            editor.commit();

        }

        Log.d(TAG, "" + uniqueID);
        return uniqueID;

    }

    /**
     * Calculates distance between two points
     *
     * @param pos1 point one
     * @param pos2 point two
     * @return a String with the calculated distance
     */
    public static String distanceBetweenTwoPoints(LatLng pos1, LatLng pos2, Context context) {
        // The computed distance is stored in results[0].
        // If results has length 2 or greater, the initial bearing is stored in
        // results[1].
        // If results has length 3 or greater, the final bearing is stored in
        // results[2].
        try {
            float[] results = new float[1];
            Location.distanceBetween(pos1.latitude, pos1.longitude, pos2.latitude, pos2.longitude, results);
            return String.format(context.getResources().getString(R.string.placeholder_km), results[0] / 1000);
        } catch (Exception e) {
            Log.e(TAG, e);
            return "";
        }
    }

    /*
     * Closes the search grey layout
     */
    public static void closeCategoryMenu(LinearLayout layout, RelativeLayout selection, int topTabSelection) {

        TapstorData.getInstance().setMenuStatusOpen(false);
        layout.getLayoutParams().height = 0;
        layout.requestLayout();
        Log.i(TAG, "CLOSE");
        if (TapstorData.getInstance().getSelectedCategoryId(topTabSelection) >= 0) {
            selection.setVisibility(View.VISIBLE);
        } else {
            selection.setVisibility(View.GONE);
        }

        ((View) layout.getParent()).setVisibility(View.GONE);

    }

    /**
     * checks for sensors availability inside device
     *
     * @param context the Activity context
     * @return true if everything is present false if not
     */
    public static boolean checkForSensors(Context context) {

        PackageManager packageManager = context.getPackageManager();
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // You do not have camera in your device
            Log.i(TAG, "no camera feature");
            return false;
        }

        return true;
    }

    /**
     * Calculates time between post and present and returns a String
     *
     * @param datePost the given timestamp
     * @param context  activity context
     * @return a String of the time difference between now and the post date
     */
    public static String calculateAndDisplayTime(String datePost,
                                                 Context context) {

        if (datePost.equals("now")) {
            return context.getResources().getString(R.string.now);
        }

        // HH converts hour in 24 hours format (0-23), day calculation

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.getDefault());
        DateFormat format2 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss",
                Locale.getDefault());
        Date d1 = null;
        Date d2 = null;
        Date date1 = new Date();
        DateFormat DATE_FORMAT_UTC = new SimpleDateFormat(
                "dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        DATE_FORMAT_UTC.setTimeZone(TimeZone.getTimeZone("UTC"));
        String dateNow = DATE_FORMAT_UTC.format(date1);

        try {

            // fix the date formats
            try {
                d1 = format.parse(datePost);
                d2 = format2.parse(dateNow);
            } catch (ParseException pe) {
                d1 = format2.parse(datePost);
                d2 = format2.parse(dateNow);
            }

            d1 = getServerDateToGMTZero(d1);

            Log.i(TAG, " now date:" + d2.toString() + " post date:" + d1.toString());

            // all diffs in milliseconds
            long diff = d2.getTime() - d1.getTime();
            // long diffSeconds = diff / 1000 % 60;
            long diffMinutes = diff / (60 * 1000) % 60;
            long diffHours = diff / (60 * 60 * 1000) % 24;
            // long diffDays = diff / (24 * 60 * 60 * 1000);
            long diffMonths = (diff / (24 * 60 * 60 * 1000)) / 30;
            long diffYears = ((diff / (24 * 60 * 60 * 1000)) / 30) / 12;

            // create a calendar
            Calendar cal = Calendar.getInstance();
            // Calendar for now date
            cal.setTime(d2);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            int yearNow = cal.get(Calendar.YEAR);
            int dayOfYearNow = cal.get(Calendar.DAY_OF_YEAR);
            // Calendar for post date
            cal.setTime(d1);
            int yearPost = cal.get(Calendar.YEAR);
            int dayOfYearPost = cal.get(Calendar.DAY_OF_YEAR);
            int yearDaysPost = cal.getActualMaximum(Calendar.DAY_OF_YEAR);

            // Difference in days
            int daysDifference = 0;
            if (yearNow - yearPost == 1) {
                daysDifference = (yearDaysPost - dayOfYearPost) + dayOfYearNow;
            } else {
                daysDifference = dayOfYearNow - dayOfYearPost;
            }

            // Diff in years
            if (diffYears != 0) {
                if (diffYears == 1) {
                    return context.getResources().getString(R.string.before)
                            + diffYears
                            + context.getResources().getString(
                            R.string.one_year_ago);

                } else {
                    return context.getResources().getString(R.string.before)
                            + +diffYears
                            + context.getResources().getString(
                            R.string.years_ago);
                }
                // Diff in months
            } else if (diffMonths != 0) {
                if (diffMonths == 1) {
                    return context.getResources().getString(R.string.before)
                            + diffMonths
                            + context.getResources().getString(
                            R.string.one_month_ago);
                } else {
                    return context.getResources().getString(R.string.before)
                            + diffMonths
                            + context.getResources().getString(
                            R.string.months_ago);
                }
            }
            // Diff in days
            else if (daysDifference > 0) {
                if (daysDifference == 1) {
                    return context.getResources().getString(R.string.before)
                            + daysDifference
                            + context.getResources().getString(R.string.day);
                } else {
                    return context.getResources().getString(R.string.before)
                            + daysDifference
                            + context.getResources().getString(
                            R.string.days_ago);
                }
            }
            // Diff in hours or minutes
            else {

                if (diffMinutes < 60) {

                    if (diffMinutes == 1 || diffMinutes == 0) {
                        return context.getResources()
                                .getString(R.string.before)
                                + 1
                                + context.getResources().getString(
                                R.string.minute_ago);
                    } else {
                        return context.getResources()
                                .getString(R.string.before)
                                + diffMinutes
                                + context.getResources().getString(
                                R.string.minutes_ago);
                    }
                } else {

                    if (diffHours == 1) {
                        return context.getResources()
                                .getString(R.string.before)
                                + diffHours
                                + context.getResources().getString(
                                R.string.hour_ago);
                    } else {
                        return context.getResources()
                                .getString(R.string.before)
                                + diffHours
                                + context.getResources().getString(
                                R.string.hours_ago);
                    }

                }

            }
        } catch (Exception e) {
            Log.e(TAG, e);
            return "";
        }

    }

    /**
     * Gets the date from server and changes it to GMT (takes notice of DST)
     *
     * @param parsedDate Date of post obtained from server(Local time set to
     *                   Athens/Greece)
     * @return the Date time to UTC/GMT
     */
    public static Date getServerDateToGMTZero(Date parsedDate) {

        TimeZone tz = TimeZone.getTimeZone("Europe/Athens");
        long tz_hours = TimeUnit.MILLISECONDS.toHours(tz.getRawOffset());
        long savings_minutes = (tz.getDSTSavings() / 1000) / 60;
        long minutesTotal = (tz_hours * 60) + savings_minutes;

        Calendar cal = Calendar.getInstance(); // creates

        cal.setTime(parsedDate); // sets
        cal.add(Calendar.MINUTE, -(int) minutesTotal);
        parsedDate = cal.getTime(); // returns

        return parsedDate;
    }

    /**
     * checks if user has a saved username and password
     *
     * @param context the activity context
     * @return true if username found false if not
     */
    public static boolean checkIfUserHasProfile(Context context) {

        String name, surname = null;
        String USER_PREFS = "USER_PREFS";

        String USER_NAME = "USER_NAME";
        String USER_SURNAME = "USER_SURNAME";

        SharedPreferences sharedPrefs = context.getSharedPreferences(
                USER_PREFS, Context.MODE_PRIVATE);

        name = sharedPrefs.getString(USER_NAME, null);
        surname = sharedPrefs.getString(USER_SURNAME, null);

        return !(name == null && surname == null);

    }

    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics);
    }

    public static int convertDpToPixel(int dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics);
    }

    /**
     * returns a rounded bitmap of the given bitmap
     *
     * @param bitmap the image to process
     * @return a round cornered image
     */
    public static Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        // Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        // return _bmp;
        return output;
    }

    /**
     * rotates an image
     *
     * @param filePath image path
     * @param bmp      the bitmap of the image
     */
    public static Bitmap rotateMediaFile(String filePath, Bitmap bmp) {
        try {
            ExifInterface exif = new ExifInterface(filePath);
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 1);
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                Matrix matrix = new Matrix();
                Log.d(TAG, "rotate 90");
                matrix.postRotate(90);
                bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),
                        bmp.getHeight(), matrix, true);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                Matrix matrix = new Matrix();
                Log.d(TAG, "rotate 180");
                matrix.postRotate(180);
                bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),
                        bmp.getHeight(), matrix, true);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                Matrix matrix = new Matrix();
                Log.d(TAG, "rotate 270");
                matrix.postRotate(270);
                bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),
                        bmp.getHeight(), matrix, true);
            }
            return bmp;
        } catch (IOException e1) {
            Log.e(TAG, e1);
            return null;
        }
    }

    public static Cat getSelectedCategory(int tab) {

        Log.e(TAG, "GET THE CATEGORY");

        int catID = TapstorData.getInstance().getSelectedCategoryId(tab);
        Log.e(TAG, "GET THE CATEGORY with id: " + catID);
        for (Cat category : TapstorData.getInstance().getCloneFeatured()) {
            if (category.id == catID) {
                Log.e(TAG, "return cat");
                return category;
            }

        }
        for (Cat category : TapstorData.getInstance().getCloneAll()) {
            if (category.id == catID) {
                Log.e(TAG, "return cat");
                return category;

            }
        }
        Log.e(TAG, "return null");
        return null;
    }

    /**
     * Scales a bitmap down to the desired height proportionally
     *
     * @param photo     the input bitmap
     * @param newHeight the desired bitmap height
     * @return the scaled bitmap
     */
    public static Bitmap scaleDownBitmap(Bitmap photo, int newHeight,
                                         Context context) {

        try {

            final float densityMultiplier = context.getResources()
                    .getDisplayMetrics().density;

            int h = (int) (newHeight * densityMultiplier);
            int w = (int) (h * photo.getWidth() / ((double) photo.getHeight()));

            photo = Bitmap.createScaledBitmap(photo, w, h, true);

        } catch (Exception e) {
            Log.e(TAG, e);
        }

        return photo;
    }

    public static void findNearestStore(Element store) {

        int i = 0;
        Location user = new Location("USER");
        user.setLatitude(TapstorData.getInstance().getLatitude());
        user.setLongitude(TapstorData.getInstance().getLongitude());
        for (Store s : store.stores) {
            try {
                i++;
                Log.e(TAG, "inside for " + i);
                Location location = new Location("LOCATION");
                location.setLatitude(Double.parseDouble(s.lat));
                location.setLongitude(Double.parseDouble(s.lng));

                float dist = user.distanceTo(location) / 1000;
                s.distance = dist;

                Log.e(TAG, "compare" + dist + " WITH PRODUCT DIST "
                        + store.calculatedDistance);
                if (store.calculatedDistance == 0) {
                    Log.e(TAG, "0 put first dist found" + dist);
                    store.calculatedDistance = dist;
                    store.closestStore = s;
                } else if (store.calculatedDistance > dist) {
                    Log.e(TAG, "DIST " + dist + " smaller than "
                            + store.calculatedDistance + " change!");
                    store.calculatedDistance = dist;
                    store.closestStore = s;
                }
            } catch (Exception e) {
                Log.e(TAG, e);
            }
        }

    }

    public static void storeNotifications(Context context, List<Integer> notifications) {
        Set<String> toStoreNotifications = new HashSet<>();
        for (Integer integer : notifications) {
            toStoreNotifications.add(integer.toString());
        }
        SharedPreferencesStorage.getSharedPreferences(context).set(NOTIFICATIONS_KEY, toStoreNotifications);
    }

    public static ArrayList<Integer> readNotifications(Context context) {
        Set<String> toStoreNotifications = SharedPreferencesStorage.getSharedPreferences(context).get(NOTIFICATIONS_KEY, new HashSet<String>());

        ArrayList<Integer> notifications = new ArrayList<>();

        for (String string : toStoreNotifications) {
            notifications.add(Integer.parseInt(string));
        }
        return notifications;
    }

    public static void shareOption(Context context, String title, String description, String imagePath, int option) {
        if (option == 0) {
            shareFacebook(context, description, imagePath, title);
        } else if (option == 1) {
            shareTwitter(context, title, description, imagePath);
        } else {
            shareOther(context, title, description, imagePath);
        }
    }

    private static void shareTwitter(Context context, String title, String textLarge, String imagePath) {


        PackageManager pm = context.getPackageManager();
        boolean appInstalled = false;
        try {
            pm.getPackageInfo("com.twitter.android", PackageManager.GET_ACTIVITIES);
            appInstalled = true;
        } catch (PackageManager.NameNotFoundException e) {
            appInstalled = false;
        }

        if (appInstalled)// Check android app is installed or not
        {

            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.setType("text/plain");
            //Get List of all activities
            List<ResolveInfo> resolveInfoList = pm.queryIntentActivities(sendIntent, 0);
            for (int j = 0; j < resolveInfoList.size(); j++) {
                ResolveInfo resInfo = resolveInfoList.get(j);
                String packageName = resInfo.activityInfo.packageName;
                //Find twitter app from list
                if (packageName.contains("com.twitter.android")) {
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName(packageName, resInfo.activityInfo.name));//Create Intent with twitter app package
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, title + "\n" + CONTENT_URL + "\n" + textLarge);
                    sendIntent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(imagePath));
                    intent.setPackage(packageName);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    context.startActivity(intent);
                    break;
                }
            }
        } else {
            Toast.makeText(context, R.string.no_twitter_app_found, Toast.LENGTH_LONG).show();
        }

    }

    private static void shareFacebook(Context context, String description, String imageURL, String productName) {
        ShareLinkContent shareLinkContent = new ShareLinkContent.Builder()
                .setContentTitle(productName)
                .setContentDescription(description)
                .setContentUrl(Uri.parse(CONTENT_URL))
                .setImageUrl(Uri.parse(imageURL))
                .build();

        if (ShareDialog.canShow(ShareLinkContent.class)) {
            ShareDialog.show((Activity) context, shareLinkContent);
        } else {
            Toast.makeText(context, R.string.no_facebook_app_found, Toast.LENGTH_LONG).show();
        }
    }

    private static void shareOther(Context context, String title, String textLarge, String imagePath) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, title + "\n" + CONTENT_URL + "\n" + textLarge);

        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(imagePath));
        shareIntent.setType("image/*");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_listing)));
    }

    public static void checkCameraAugmented(AppCompatActivity appCompatActivity, boolean flag) {
        CameraAccess.getInstance(appCompatActivity).checkPermission();
        if (CameraAccess.getInstance(appCompatActivity).isPermissionGranted()) {

            Intent i = new Intent(appCompatActivity, AugmentedRealityActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            AugmentedRealityActivity.navigatedFromStoreView = flag;
            appCompatActivity.startActivity(i);
            appCompatActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        }


    }

    public static String getLanguageToken(Context context) {
        String language = SharedPreferencesStorage.getSharedPreferences(context).get(TapstorActivity.LANGUAGE_KEY, "");
        switch (language) {
            case "el":
                return "gr";
            case "ru":
                return "ru";
            default:
                return "en";
        }
    }

}