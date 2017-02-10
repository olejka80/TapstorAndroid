package com.iproject.tapstor.library;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

public class SharedPreferencesStorage {

    private static SharedPreferencesStorage mSharedPreferencesStorage = null;
    private SharedPreferences mSharedPreferences;


    private SharedPreferencesStorage(Context context) {
        this.mSharedPreferences = ((Activity) context).getPreferences(Context.MODE_PRIVATE);
    }

    public static synchronized SharedPreferencesStorage getSharedPreferences(Context context) {
        if (mSharedPreferencesStorage == null) {
            mSharedPreferencesStorage = new SharedPreferencesStorage(context);
            Log.i("SharedPreferencesStorage", "SharedPreferences initialized.");
        }
        return mSharedPreferencesStorage;
    }

    public String get(String key, String defaultValue) {
        return mSharedPreferences.getString(key, defaultValue);
    }

    public Set<String> get(String key, Set<String> defaultValue) {
        return mSharedPreferences.getStringSet(key, defaultValue);
    }

    public void set(String key, String value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void set(String key, Set<String> value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putStringSet(key, value);
        editor.apply();
    }
}
