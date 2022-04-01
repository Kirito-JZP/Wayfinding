package com.main.wayfinding.logic;

import static com.main.wayfinding.utility.LatLngConverterUtils.convert;

import com.google.android.gms.maps.GoogleMap;
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
    private int registrationNumber;
    private final GoogleMap map;
    private static NavigationLogic instance;

    private NavigationLogic(GoogleMap map) {
        this.map = map;
    }

    public static NavigationLogic getInstance() {
        return instance;
    }

    public static NavigationLogic createInstance(GoogleMap map) {
        if (instance == null) {
            instance = new NavigationLogic(map);
        }
        return instance;
    }

    public void startNavigation(RouteDto route) {
        // register a callback in TrackerLogic
        currentRoute = route;
        registrationNumber = TrackerLogic.registerLocationUpdateCompleteEvent(location -> {
            // generate a location dto
            LocationDto currentLocation = new LocationDto();
            currentLocation.setLatitude(location.getLatitude());
            currentLocation.setLongitude(location.getLongitude());
            // update route state
            route.updateRouteState(currentLocation);
            // update UI
            route.updatePolylinesUI(map);
        });
    }

    public void pauseNavigation() {
        TrackerLogic.unregisterLocationUpdateCompleteEvent(registrationNumber);
    }

    public void resumeNavigation() {
        registrationNumber = TrackerLogic.registerLocationUpdateCompleteEvent(location -> {
            map.clear();
            currentRoute.updatePolylinesUI(map);
        });
    }

    public void stopNavigation() {
        TrackerLogic.unregisterLocationUpdateCompleteEvent(registrationNumber);
        currentRoute = null;
    }

    public void addWayPoint(LocationDto location) {
        // re-search routes based on the current route without changing steps passed previously
        currentRoute.addWaypoint(location);
        currentRoute.updateRouteFromCurrentLocation(location);
    }
}