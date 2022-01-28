package com.main.wayfinding;

import android.app.Application;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.maps.GeoApiContext;

public class WayfindingApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // create GeoApiContext for later use
        context = new GeoApiContext.Builder()
                .apiKey(getString(R.string.google_maps_key))
                .build();
        // initialise Places API
        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        PlacesClient placesClient = Places.createClient(this);

    }

    public static GeoApiContext getGeoApiContext() {
        return context;
    }
    public static PlacesClient getPlacesClient() { return client; }


    private static GeoApiContext context;
    private static PlacesClient client;
}
