package com.main.wayfinding.logic;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.main.wayfinding.R;
import com.main.wayfinding.dto.LocationDto;
import com.main.wayfinding.dto.RouteDto;
import com.main.wayfinding.utility.LatLngConverterUtils;
import com.main.wayfinding.utility.NavigationUtils;
import com.main.wayfinding.utility.NoticeUtils;
import com.main.wayfinding.utility.StaticStringUtils;

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
    private final Context context;
    private boolean isNavigating;
    private boolean isDraggingMap;


    public NavigationLogic(GoogleMap map, Context context) {
        this.map = map;
        this.context = context;
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
            // decide whether to stop navigation
            if (NavigationUtils.calcDistance(currentLocation, currentRoute.getEndLocation()) < 5.0f) {
                stopNavigation(true);
                return;
            }
            // update UI
            if (!isDraggingMap) {
                map.animateCamera(CameraUpdateFactory.newLatLng(LatLngConverterUtils.getLatLngFromDto(currentLocation)), 500, null);
            }
            NavigationUtils.updatePolylinesUI(currentRoute, map);
        });
        // move camera at once for more fluent appearance
        Location location = TrackerLogic.getInstance().getLocation();
        map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(),
                location.getLongitude())), 500, null);
        isNavigating = true;
        // update UI
        NoticeUtils.createToast(context, StaticStringUtils.START_NAVIGATION);
    }

    public void pauseNavigation() {
        TrackerLogic.unregisterLocationUpdateCompleteEvent(registrationNumber);
        isNavigating = false;
        // update UI
        NoticeUtils.createToast(context, StaticStringUtils.PAUSE_NAVIGATION);
    }

    public void resumeNavigation() {
        registrationNumber = TrackerLogic.registerLocationUpdateCompleteEvent(location -> {
            map.clear();
            NavigationUtils.updatePolylinesUI(currentRoute, map);
        });
        isNavigating = true;
        // update UI
        NoticeUtils.createToast(context, StaticStringUtils.RESUME_NAVIGATION);
    }

    public void stopNavigation(boolean arrived) {
        TrackerLogic.unregisterLocationUpdateCompleteEvent(registrationNumber);
        currentRoute = null;
        isNavigating = false;
        // update UI
        if (arrived) {
            NoticeUtils.createToast(context, StaticStringUtils.ARRIVED);
        } else {
            NoticeUtils.createToast(context, StaticStringUtils.STOP_NAVIGATION);
        }
    }

    public void addWayPoint(LocationDto location) {
        // re-search routes based on the current route without changing steps passed previously
        currentRoute.addWaypoint(location);
        LocationDto currentLocation = new LocationDto();
        Location loc = TrackerLogic.getInstance().getLocation();
        currentLocation.setLatitude(loc.getLatitude());
        currentLocation.setLongitude(loc.getLongitude());
        NavigationUtils.updateRouteFromCurrentLocation(currentRoute, currentLocation);
        NavigationUtils.updatePolylinesUI(currentRoute, map);
    }

    public boolean isNavigating() {
        return isNavigating;
    }

    public void setNavigating(boolean navigating) {
        isNavigating = navigating;
    }

    public boolean isDraggingMap() {
        return isDraggingMap;
    }

    public void setDraggingMap(boolean draggingMap) {
        isDraggingMap = draggingMap;
    }
}
