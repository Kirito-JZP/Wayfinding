package com.main.wayfinding.logic;

import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.main.wayfinding.dto.EmergencyEventDto;
import com.main.wayfinding.dto.LocationDto;
import com.main.wayfinding.dto.RouteDto;
import com.main.wayfinding.utility.LatLngConverterUtils;
import com.main.wayfinding.utility.NavigationUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description
 *
 * @author Zehua Guo
 * @author Last Modified By Zehua Guo
 * @version Revision: 0
 * Date: 2022/3/31 17:17
 */
public class EmergencyEventLogic {
    public interface EmergencyEventCallback {
        void onEmergencyEventHappen(EmergencyEventDto event);
    }

    private static int emergencyEventCallbackNum = 0;
    private static final Map<Integer, EmergencyEventCallback> emergencyEventCallbacks =
            new HashMap<>();

    public static int registerEmergencyEvent(EmergencyEventCallback callback) {
        emergencyEventCallbacks.put(emergencyEventCallbackNum, callback);
        emergencyEventCallbackNum += 1;
        return emergencyEventCallbackNum - 1;
    }

    public static void unregisterEmergencyEvent(int callbackNumber) {
        emergencyEventCallbacks.remove(callbackNumber);
        emergencyEventCallbackNum -= 1;
    }

    public static void broadcast(EmergencyEventDto event) {
        for (EmergencyEventCallback callback : emergencyEventCallbacks.values()) {
            callback.onEmergencyEventHappen(event);
        }
    }

    public void processEmergencyEvent(EmergencyEventDto event, LocationDto currentLocation,
                                      RouteDto currentRoute, List<RouteDto> possibleRoutes, GoogleMap map) {
        // find out if the location of the emergency event is ahead of the current location in the
        // current route and if the current route is affected
        LocationDto location = new LocationDto();
        location.setLatitude(event.getLatitude());
        location.setLongitude(event.getLongitude());
        if (NavigationUtils.isLocationAheadOfReference(currentRoute, location,
                currentLocation)) {
            List<RouteDto.RouteStep> affectedSteps =
                    NavigationUtils.findStepsAffectedBy(currentRoute, event);
            if (!affectedSteps.isEmpty()) {
                RouteDto.RouteStep backwardBoundary = affectedSteps.get(0);
                RouteDto.RouteStep forwardBoundary = affectedSteps.get(affectedSteps.size() - 1);
                double x1 = backwardBoundary.getStartLocation().getLongitude();
                double y1 = backwardBoundary.getStartLocation().getLatitude();
                double x2 = forwardBoundary.getEndLocation().getLongitude();
                double y2 = forwardBoundary.getEndLocation().getLatitude();
                double x0 = event.getLongitude();
                double y0 = event.getLatitude();

                // use latitude for simplicity
                double r0 = LatLngConverterUtils.getLatitudeFromDistance(event.getRadius());
                // the intersection points of the straight line
                // perpendicular to the line formed by connecting (x1, y1) and (x2, y2)
                // with the affected range
                // these two points should be the farthest ones away from the center of the event
                // range
                // as well as (x1, y1) and (x2, y2)
                LatLng intersect1, intersect2;
                if (Math.abs(y1 - y2) < 1e-3) {
                    // treat as y1 == y2
                    intersect1 = new LatLng(y0 + r0, x0);
                    intersect2 = new LatLng(y0 - r0, x0);
                } else {
                    double k = (x1 - x2) / (y1 - y2);
                    double term = r0 / Math.sqrt(1 + k * k);
                    double x = x0 + term;
                    double y = -k * x + y0 + k * x0;
                    intersect1 = new LatLng(y, x);
                    x = x0 - term;
                    y = -k * x + y0 + k * x0;
                    intersect2 = new LatLng(y, x);
                }
                // use the point that is the closest to the current position as a waypoint to add
                double p = intersect1.latitude - currentLocation.getLatitude();
                double q = intersect1.longitude - currentLocation.getLongitude();
                double distance1 = Math.sqrt(p * p + q * q);
                p = intersect2.latitude - currentLocation.getLatitude();
                q = intersect2.longitude - currentLocation.getLongitude();
                double distance2 = Math.sqrt(p * p + q * q);
                LocationDto waypoint = new LocationDto();
                waypoint.setLatitude(distance1 < distance2 ? intersect1.latitude :
                        intersect2.latitude);
                waypoint.setLongitude(distance1 < distance2 ? intersect1.longitude :
                        intersect2.longitude);
                currentRoute.addWaypoint(waypoint);
                // re-search routes from the current position with the added waypoints
                NavigationUtils.updateRouteFromCurrentLocation(currentRoute, currentLocation);
            }
        }
    }
}
