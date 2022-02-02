package com.main.wayfinding;

import android.app.Application;

import com.google.maps.GeoApiContext;

public class WayfindingApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // create GeoApiContext for later use
        context = new GeoApiContext.Builder()
                .apiKey(getString(R.string.google_maps_key))
                .build();
        key = getString(R.string.google_maps_key);
    }

    public static GeoApiContext getGeoApiContext() {
        return context;
    }

    public static float getDpWidth() {
        return dpWidth;
    }

    public static void setDpWidth(float dpWidth) {
        WayfindingApp.dpWidth = dpWidth;
    }


    public static float getDpHeight() {
        return dpHeight;
    }

    public static void setDpHeight(float dpHeight) {
        WayfindingApp.dpHeight = dpHeight;
    }

    public static String getKey() {
        return key;
    }

    public static void setKey(String key) {
        WayfindingApp.key = key;
    }

    private static GeoApiContext context;
    private static float dpHeight;
    private static float dpWidth;
    private static String key;
}
