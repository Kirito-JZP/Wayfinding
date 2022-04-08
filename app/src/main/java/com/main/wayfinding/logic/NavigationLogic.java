package com.main.wayfinding.logic;

import android.location.Location;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PatternItem;
import com.main.wayfinding.dto.LocationDto;
import com.main.wayfinding.dto.RouteDto;
import com.main.wayfinding.utility.LatLngConverterUtils;
import com.main.wayfinding.utility.NavigationUtils;

import java.util.Arrays;
import java.util.List;

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

    public NavigationLogic(GoogleMap map) {
        this.map = map;
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
            NavigationUtils.updateRouteState(currentRoute, currentLocation);
            // update UI
            map.animateCamera(CameraUpdateFactory.newLatLng(LatLngConverterUtils.getLatLngFromDto(currentLocation)), 500, null);
            NavigationUtils.updatePolylinesUI(currentRoute, map);
        });
        // move camera at once for more fluent appearance
        Location location = TrackerLogic.getInstance().getLocation();
        map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())), 500, null);
    }

    public void pauseNavigation() {
        TrackerLogic.unregisterLocationUpdateCompleteEvent(registrationNumber);
    }

    public void resumeNavigation() {
        registrationNumber = TrackerLogic.registerLocationUpdateCompleteEvent(location -> {
            map.clear();
            NavigationUtils.updatePolylinesUI(currentRoute, map);
        });
    }

    public void stopNavigation() {
        TrackerLogic.unregisterLocationUpdateCompleteEvent(registrationNumber);
        currentRoute = null;
    }

    public void addWayPoint(LocationDto location) {
        // re-search routes based on the current route without changing steps passed previously
        currentRoute.addWaypoint(location);
        NavigationUtils.updateRouteFromCurrentLocation(currentRoute, location);
    }
}