package com.main.wayfinding.logic;

import static com.main.wayfinding.utility.LatLngConverterUtils.convert;

import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.main.wayfinding.dto.LocationDto;
import com.main.wayfinding.dto.RouteDto;

/**
 * Logic of Navigation
 *
 * @author jia72
 * @author Last Modified By jia72
 * @version Revision: 0
 * Date: 2022/1/28 17:18
 */
public class NavigationLogic {

    private RouteDto currentRoute;
    private final GoogleMap map;
    private final TrackerLogic trackerLogic;
    private static NavigationLogic instance;

    private NavigationLogic(GoogleMap map) {
        trackerLogic = TrackerLogic.getInstance();
        this.map = map;
    }

    public static NavigationLogic getInstance() {
        return instance;
    }

    public static void createInstance(GoogleMap map) {
        instance = new NavigationLogic(map);
    }

    public void startNavigation(RouteDto route) {
        // register a callback in TrackerLogic
        currentRoute = route;
        trackerLogic.registerLocationUpdateCompleteEvent(location -> {
            map.clear();
            map.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(),
                    location.getLongitude()))
                    .title("current location"));
        });
    }

    public void pauseNavigation() {

    }

    public void resumeNavigation() {

    }

    public void stopNavigation() {

    }

    public void modifyRoute() {

    }
}