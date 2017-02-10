package com.iproject.tapstor;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.util.DisplayMetrics;
import android.view.Window;
import android.widget.Toast;

import com.crittercism.app.Crittercism;
import com.facebook.FacebookSdk;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.iproject.tapstor.helper.Helper;
import com.iproject.tapstor.helper.TapstorActivity;
import com.iproject.tapstor.helper.TapstorData;
import com.iproject.tapstor.library.Log;
import com.iproject.tapstor.objects.Cat;
import com.iproject.tapstor.rest.RestResponse;
import com.iproject.tapstor.rest.RestServices;
import com.iproject.tapstor.rest.Result_do_login_android;
import com.iproject.tapstor.rest.Result_get_categories;
import com.iproject.tapstor.rest.SendLoginData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class SplashTapstorActivity extends TapstorActivity {

    private static final String TAG = "SplashTapstorActivity";
    private static final String PROPERTY_REG_ID = "registration_id";
    // PREF FILES
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private Context context;
    // GCM
    private GoogleCloudMessaging gcm;
    // TODO might be cool to change for their set IDs and google account
    private String SENDER_ID = "977193308962";
    private String regid;
    private boolean firstLoad = true;

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    protected void onResume() {

        if (!firstLoad) {
            LaunhApp();
        } else {
            firstLoad = false;
        }
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);

        setContentView(R.layout.activity_splash_tapstor);

        context = SplashTapstorActivity.this;

        ((AnalyticsApplication) getApplication()).getDefaultTracker();

        TapstorData.resetInstance();
        Crittercism.initialize(getApplicationContext(), "13fb0b8c493b495183c219cf3bab16d000555300");

        FacebookSdk.sdkInitialize(this);
        TapstorData.getInstance().setCompassCharacters(this);
        LaunhApp();

    }

    public void setLocale(String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        Intent refresh = new Intent(this, SplashTapstorActivity.class);
        startActivity(refresh);
        finish();
    }

    private void LaunhApp() {

        if (isNetworkAvailable()) {

            regid = getRegistrationId(context);

            // check for gcm registered ID
            Log.e(TAG, "REGID: " + regid);

            // if not found register and then login
            if (regid.isEmpty()) {
                registerInBackground();
            } else {
                // else login
                new DoLogin().execute();
            }

        } else {

            showMessageDialog(R.string.connection_problem, R.string.no_active_connection_to_the_internet);
        }

    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * displays a message
     *
     * @param message the message to display
     */
    private void showMessage(@StringRes int message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p/>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     * registration ID.
     */
    private String getRegistrationId(Context context) {

        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");

        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }

        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION,
                Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }

        return registrationId;
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences,
        // but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(
                SplashTapstorActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    /**
     * Registers the application with GCM servers asynchronously. Store the
     * registration ID and app versionCode in the application's shared
     * preferences.
     */
    private void registerInBackground() {

        new AsyncTask<Object, Object, Object>() {

            @Override
            protected Object doInBackground(Object... params) {

                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regid);

                    Log.e("regID", regid);

                } catch (IOException ex) {
                    ex.printStackTrace();

                }
                return null;

            }

            @Override
            protected void onPostExecute(Object result) {

                // Register GCM ID is set. LOGIN is now possible
                new DoLogin().execute();

            }

            ;

        }.execute();

    }

    /**
     * Store the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId   registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    public void showMessageDialog(@StringRes int titleResId, @StringRes int messageResId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(titleResId);
        builder.setMessage(messageResId)
                .setCancelable(false)
                .setPositiveButton(R.string.try_again,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                LaunhApp();

                            }

                        })
                .setNegativeButton(R.string.close,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                finish();

                            }

                        }).show();
    }

    /**
     * Login class makes an Asynchronous call to login get web service
     *
     * @author Grassos Konstantinos <grassos.konstantinos@gmail.com>
     */
    private class DoLogin extends AsyncTask<String, Void, String> {

        private RestResponse rest;
        private Result_do_login_android response;

        protected String doInBackground(final String... args) {

            try {

                Gson gson = new Gson();


                String uniqueId = Helper.getUniqueId(context);
                String udId = regid;
                SendLoginData data = new SendLoginData();
                data.udid = udId;
                data.unique_id = uniqueId;

                String reader = RestServices.getInstance().postOperation(data,
                        RestServices.getInstance().NEW_LOGIN_ANDROID);


                rest = gson.fromJson(reader, RestResponse.class);

                response = rest.result_do_login_android;

            } catch (Exception e) {
                Log.e(TAG, e);
            }

            return null;
        }

        protected void onPostExecute(final String result) {

            try {

                if (response.error.equals("")) {

                    TapstorData.getInstance().setUserToken(response.token);

                    TapstorData.getInstance().setTab(1);
                    // Helper.checkForUsersLocation(context, TAG);
                    new GetCats().execute();

                } else {
                    // MESSAGE TO DISPLAY IN CASE OF ERROR
                    showMessage(R.string.connection_error);
                    finish();
                }

            } catch (Exception e) {
                // MESSAGE TO DISPLAY IN CASE OF EXCEPTION
                Log.e(TAG, e);
                showMessage(R.string.connection_error);
                finish();

            }

        }
    }

    /**
     * Web Service to get dynamically all available categories for multiple use
     * inside the app
     *
     * @author Grassos Konstantinos <grassos.konstantinos@gmail.com>
     */
    private class GetCats extends AsyncTask<String, Void, String> {

        private Result_get_categories result_get_categories;

        protected String doInBackground(final String... args) {

            try {
                Gson gson = new Gson();

                String reader = RestServices.getInstance().getOperation(
                        RestServices.getInstance().GET_CATS + "/" + Helper.getLanguageToken(context));
                RestResponse response = gson.fromJson(reader,
                        RestResponse.class);

                result_get_categories = response.result_get_categories;
            } catch (Exception e) {
                Log.e(TAG, e);
            }
            return null;
        }

        protected void onPostExecute(final String result) {

            try {

                if (result_get_categories != null) {
                    if (result_get_categories.error != null) {
                        if (result_get_categories.error.equals("")) {

                            TapstorData.getInstance().setCloneAll(
                                    new ArrayList<Cat>(result_get_categories.companies.all.size()));

                            result_get_categories.companies.featured
                                    .add(new Cat(-1, context.getResources()
                                            .getString(R.string.all_cats)));

                            for (Cat item : result_get_categories.companies.all) {
                                TapstorData.getInstance().getCloneAll()
                                        .add((Cat) item.clone());
                            }

                            TapstorData.getInstance().setCloneFeatured(
                                    new ArrayList<Cat>(result_get_categories.companies.featured.size()));

                            for (Cat item : result_get_categories.companies.featured) {
                                TapstorData.getInstance().getCloneFeatured().add((Cat) item.clone());

                            }

                        }
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, e);

            }
            Intent i = new Intent(SplashTapstorActivity.this,
                    MainViewPagerActivity.class);

            if (getIntent().getExtras() != null) {
                boolean notify = getIntent().getExtras().getBoolean("go_to_notifications", false);

                i.putExtra("go_to_notifications", notify);

            }
            startActivity(i);

            finish();
        }
    }

}