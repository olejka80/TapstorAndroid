package com.iproject.tapstor.library;

import com.iproject.tapstor.BuildConfig;

public class Log {

    public static void i(String tag, String string) {
        if (BuildConfig.DEBUG) android.util.Log.i(tag, string);
    }

    public static void i(String tag, Throwable throwable) {
        if (BuildConfig.DEBUG) android.util.Log.i(tag, throwable.toString(), throwable);
    }

    public static void e(String tag, String string) {
        if (BuildConfig.DEBUG) android.util.Log.e(tag, string);
    }

    public static void e(String tag, String string, Throwable throwable) {
        if (BuildConfig.DEBUG) android.util.Log.e(tag, string, throwable);
    }

    public static void e(String tag, Throwable throwable) {
        if (BuildConfig.DEBUG) android.util.Log.e(tag, throwable.toString(), throwable);
    }

    public static void d(String tag, String string) {
        if (BuildConfig.DEBUG) android.util.Log.d(tag, string);
    }

    public static void v(String tag, String string) {
        if (BuildConfig.DEBUG) android.util.Log.v(tag, string);
    }

    public static void w(String tag, String string) {
        if (BuildConfig.DEBUG) android.util.Log.w(tag, string);
    }

}
