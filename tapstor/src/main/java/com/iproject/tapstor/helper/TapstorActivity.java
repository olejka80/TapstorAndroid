package com.iproject.tapstor.helper;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.iproject.tapstor.library.SharedPreferencesStorage;

import java.util.Locale;

/**
 * Created by Dimitris Touzloudis (dimitris.touzloudis@gmail.com) on 06/05/16.
 */
public class TapstorActivity extends AppCompatActivity {

    public static final String LANGUAGE_KEY = "LANGUAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        String language = SharedPreferencesStorage.getSharedPreferences(this).get(LANGUAGE_KEY, "");

        if (language.equals("")) {
            if (Locale.getDefault().getDisplayLanguage().equals("el")) {
                SharedPreferencesStorage.getSharedPreferences(this).set(LANGUAGE_KEY, "el");
            } else if (Locale.getDefault().getDisplayLanguage().equals("ru")) {
                SharedPreferencesStorage.getSharedPreferences(this).set(LANGUAGE_KEY, "ru");
            } else {
                SharedPreferencesStorage.getSharedPreferences(this).set(LANGUAGE_KEY, "en");
            }
        }

        Helper.setLocale(this, language);

    }
}
