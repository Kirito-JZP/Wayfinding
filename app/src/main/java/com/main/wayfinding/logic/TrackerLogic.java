package com.main.wayfinding.logic;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.android.material.snackbar.Snackbar;
import com.main.wayfinding.R;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

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
        if (location != null) {
            mapSourceDataListener.onLocationChanged(location);
        }
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

    public interface LocationPermissionRequestCompleteCallback {
        void onLocationPermissionRequestComplete(boolean isSuccessful);
    }

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 5; // 5 meters
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 5; // 5 seconds
    // Request code for location permission request.
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    // Location
    public static Location location;
    private double latitude;
    private double longitude;

    private final Activity activity;
    private final FusedLocationProviderClient locationClient;
    private static TrackerLogic instance;
    private static int locationUpdateCompleteCallbackNum;
    private final Map<Integer, LocationUpdateCompleteCallback> locationUpdateCompleteCallbackList;
    private OnLocationChangedListener mapSourceDataListener;
    private Timer trackerTimer;

    // permission
    LocationPermissionRequestCompleteCallback locationPermissionRequestCompleteCallback;

    public static TrackerLogic getInstance() {
        return instance;
    }

    public static TrackerLogic createInstance(Activity act) {
        if (instance == null) {
            instance = new TrackerLogic(act);
        }
        return instance;
    }

    private TrackerLogic(Activity act) {
        // store a reference to the activity
        activity = act;

        locationClient = LocationServices.getFusedLocationProviderClient(activity);

        // initialise delegate lists
        locationUpdateCompleteCallbackList = new HashMap<>();
        locationUpdateCompleteCallbackNum = 0;

        // use the last available location
        requestLastLocation(loc -> {
            location = loc;
            if (mapSourceDataListener != null) {
                mapSourceDataListener.onLocationChanged(location);
            }
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

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            int index = -1;
            for (int i = 0; i < permissions.length; i++) {
                if (StringUtils.equals(permissions[i], Manifest.permission.ACCESS_FINE_LOCATION)) {
                    index = i;
                    break;
                }
            }
            if (index != -1) {
                locationPermissionRequestCompleteCallback.onLocationPermissionRequestComplete(grantResults[index] == PERMISSION_GRANTED);
            } else {
                locationPermissionRequestCompleteCallback.onLocationPermissionRequestComplete(false);
            }
        }
    }

    public void askForLocationPermissions(LocationPermissionRequestCompleteCallback callback) {
        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION)
        ) {
            locationPermissionRequestCompleteCallback = callback;
            startRequestPermission();
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
            // provide data to the my location layer for the Google Map instance
            if (mapSourceDataListener != null) {
                mapSourceDataListener.onLocationChanged(location);
            }
            // broadcast new location
            for (LocationUpdateCompleteCallback callback :
                    locationUpdateCompleteCallbackList.values()) {
                callback.onLocationUpdateComplete(location);
            }
        }).addOnFailureListener(activity, exception -> {
            // UI notice
            showNoticeUI("Unable to get the latest location");
            exception.printStackTrace();
        });
    }

    @SuppressLint("MissingPermission")
    public void startUpdateLocation() {
        if (trackerTimer != null) {
            stopUpdateLocation();
        }
        trackerTimer = new Timer();
        trackerTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateLocationInstantly();
            }
        }, MIN_TIME_BW_UPDATES, MIN_TIME_BW_UPDATES);
    }

    private void showNoticeUI(String content) {
        Snackbar.make(
                activity.findViewById(R.id.map),
                content,
                Snackbar.LENGTH_SHORT
        ).show();
    }

    public void stopUpdateLocation() {
        trackerTimer.purge();
        trackerTimer.cancel();
        trackerTimer = null;
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