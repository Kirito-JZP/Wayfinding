package com.main.wayfinding.logic;

import static com.main.wayfinding.utility.LatLngConverterUtils.convert;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.main.wayfinding.dto.LocationDto;
import com.main.wayfinding.dto.RouteDto;
import com.main.wayfinding.utility.PlaceManagerUtils;

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
    private int registrationNumber;
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
        registrationNumber = trackerLogic.registerLocationUpdateCompleteEvent(location -> {
            map.clear();
            // generate a location dto
            LocationDto currentLocation = new LocationDto();
            currentLocation.setLatitude(location.getLatitude());
            currentLocation.setLongitude(location.getLongitude());
            // update route state
            route.updateRouteState(currentLocation);
            // update UI
            route.updateUI(map);
            map.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(),
                    location.getLongitude()))
                    .title("current location"));
        });
    }

    public void pauseNavigation() {
        trackerLogic.unregisterLocationUpdateCompleteEvent(registrationNumber);
    }

    public void resumeNavigation() {
        registrationNumber = trackerLogic.registerLocationUpdateCompleteEvent(location -> {
            map.clear();
            currentRoute.updateUI(map);
            map.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(),
                    location.getLongitude()))
                    .title("current location"));
        });
    }

    public void stopNavigation() {
        trackerLogic.unregisterLocationUpdateCompleteEvent(registrationNumber);
        currentRoute = null;
    }

    public void addWayPoint(LocationDto location) {
        // re-search routes based on the current route without changing steps passed previously
        currentRoute.addWaypoint(location);
        PlaceManagerUtils.updateRouteFromCurrentLocation(currentRoute, location);
    }
}