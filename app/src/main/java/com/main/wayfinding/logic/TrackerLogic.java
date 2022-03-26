package com.main.wayfinding.logic;

import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.location.Location;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.android.material.snackbar.Snackbar;
import com.main.wayfinding.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class for GPS tracking
 *
 * @author JIA
 * @author Last Modified By JIA
 * @version Revision: 0
 * Date: 2022/1/29 0:58
 */
public class TrackerLogic implements LocationSource {

    @Override
    public void activate(@NonNull OnLocationChangedListener onLocationChangedListener) {
        mapSourceDataListener = onLocationChangedListener;
    }

    @Override
    public void deactivate() {
        mapSourceDataListener = null;
    }

    // callback for asyncronously getting values
    public interface RequestLocationCompleteCallback {
        void onRequestLocationComplete(Location location);
    }

    public interface LocationUpdateCompleteCallback {
        void onLocationUpdateComplete(Location location);
    }

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 5; // 5 meters
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 2; // 2 seconds
    // Request code for location permission request.
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    // Location
    public static Location location;
    private double latitude;
    private double longitude;

    private final Activity activity;
    private final FusedLocationProviderClient locationClient;
    private final SettingsClient settingsClient;
    private final LocationCallback locationCallback;
    private final LocationRequest locationRequest;
    private final LocationSettingsRequest settingsRequest;
    private static TrackerLogic instance;
    private static int locationUpdateCompleteCallbackNum;
    private final Map<Integer, LocationUpdateCompleteCallback> locationUpdateCompleteCallbackList;
    private OnLocationChangedListener mapSourceDataListener;

    public static TrackerLogic getInstance() {
        return instance;
    }

    public static TrackerLogic getInstance(Activity act) {
        if (instance == null) {
            instance = new TrackerLogic(act);
        }
        return instance;
    }

    private TrackerLogic(Activity act) {
        // store a reference to the activity
        activity = act;

        locationClient = LocationServices.getFusedLocationProviderClient(activity);
        settingsClient = LocationServices.getSettingsClient(activity);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                location = locationResult.getLastLocation();
                // provide data to the my location layer for the Google Map instance
                mapSourceDataListener.onLocationChanged(location);
                // broadcast new location
                for (LocationUpdateCompleteCallback callback :
                        locationUpdateCompleteCallbackList.values()) {
                    callback.onLocationUpdateComplete(location);
                }
            }
        };  // callback for periodically updating the current location

        // initialise delegate lists
        locationUpdateCompleteCallbackList = new HashMap<>();
        locationUpdateCompleteCallbackNum = 0;

        // create a location request
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(MIN_TIME_BW_UPDATES);
        locationRequest.setFastestInterval(1000);
        locationRequest.setSmallestDisplacement(MIN_DISTANCE_CHANGE_FOR_UPDATES);
        locationRequest.setWaitForAccurateLocation(false);
        locationRequest.setPriority(PRIORITY_HIGH_ACCURACY);

        // create a location settings request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        settingsRequest = builder.build();

        // ask for permissions
        askForLocationPermissions();

        // use the last available location
        requestLastLocation(loc -> {
            location = loc;
        });

        // start requesting location updates
        startUpdateLocation();
    }

    public int registerLocationUpdateCompleteEvent(LocationUpdateCompleteCallback callback) {
        locationUpdateCompleteCallbackList.put(locationUpdateCompleteCallbackNum, callback);
        locationUpdateCompleteCallbackNum += 1;
        return locationUpdateCompleteCallbackNum - 1;
    }

    public void unregisterLocationUpdateCompleteEvent(int callbackNumber) {
        locationUpdateCompleteCallbackList.remove(callbackNumber);
        locationUpdateCompleteCallbackNum -= 1;
    }

    /**
     * Function to get location
     */
    @SuppressLint("MissingPermission")
    public void requestLastLocation(RequestLocationCompleteCallback callback) {
        if (location == null) {
            locationClient.getLastLocation().addOnSuccessListener(activity, loc -> {
                location = loc;
                callback.onRequestLocationComplete(location);
            }).addOnFailureListener(activity, exception -> {
                showNoticeUI("Unable to get the latest location");
                exception.printStackTrace();
            });
        } else {
            callback.onRequestLocationComplete(location);
        }
    }

    public Location getLocation() {
        return location;
    }

    /**
     * Function to get permission in real time
     */

    private void startRequestPermission() {
        ActivityCompat.requestPermissions(
                activity,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE
        );
    }

    private void askForLocationPermissions() {
        // Should we show an explanation?
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION)
        ) {
            Snackbar.make(
                    activity.findViewById(R.id.map),
                    "Core functionalities require location permission",
                    Snackbar.LENGTH_SHORT)
                    .setAction(activity.getString(android.R.string.ok),
                            view -> startRequestPermission()
                    ).show();
            // Show an expanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.
        } else {
            // No explanation needed, we can request the permission.
            startRequestPermission();
            // MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        }
    }

    @SuppressLint("MissingPermission")
    private void updateLocationInstantly() {
        locationClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY, new CancellationToken() {
            @NonNull
            @Override
            public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                return this;
            }

            @Override
            public boolean isCancellationRequested() {
                return false;
            }
        }).addOnSuccessListener(activity, loc -> {
            location = loc;
        }).addOnFailureListener(activity, exception -> {
            // UI notice
            showNoticeUI("Unable to get the latest location");
            exception.printStackTrace();
        });
    }

    @SuppressLint("MissingPermission")
    public void startUpdateLocation() {
        settingsClient.checkLocationSettings(settingsRequest).addOnSuccessListener(activity,
                locationSettingsResponse -> {
                    locationClient.requestLocationUpdates(locationRequest, locationCallback,
                            Looper.myLooper());
                }).addOnFailureListener(activity, exception -> {
            // UI notice
            showNoticeUI("Unable to get the latest location");
        });
    }

    private void showNoticeUI(String content) {
        Snackbar.make(
                activity.findViewById(R.id.map),
                content,
                Snackbar.LENGTH_SHORT
        ).show();
    }

    public void stopUpdateLocation() {
        locationClient.removeLocationUpdates(locationCallback).addOnCompleteListener(activity,
                task -> {
                    // TODO: do something
                });
    }

    /**
     * Function to get longitude
     *
     * @return longitude
     */
    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        } else {
            updateLocationInstantly();
        }

        // return longitude
        return longitude;
    }

    /**
     * Function to get latitude
     *
     * @return latitude
     */
    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        } else {
            updateLocationInstantly();
        }

        // return latitude
        return latitude;
    }
}