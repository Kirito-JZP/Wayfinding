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
    }

    public static GeoApiContext getGeoApiContext() {
        return context;
    }


    private static GeoApiContext context;
}
