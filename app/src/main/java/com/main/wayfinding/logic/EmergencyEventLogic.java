package com.main.wayfinding.logic;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.main.wayfinding.dto.EmergencyEventDto;
import com.main.wayfinding.dto.LocationDto;
import com.main.wayfinding.dto.RouteDto;
import com.main.wayfinding.utility.EmergencyEventUtils;
import com.main.wayfinding.utility.LatLngConverterUtils;
import com.main.wayfinding.utility.NavigationUtils;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
        void onEmergencyEventBegin(EmergencyEventDto event);

        void onEmergencyEventEnd(EmergencyEventDto event);
    }

    private int emergencyEventCallbackNum = 0;
    private final Map<Integer, EmergencyEventCallback> emergencyEventCallbacks =
            new HashMap<>();
    private Map<String, EmergencyEventDto> events;
    private Map<String, List<LatLng>> waypoints;    // waypoints added due to emergency events
    private Map<String, Boolean> triggered;
    private int locationUpdateCallbackIndex = -1;
    private boolean firstTime = true;

    public EmergencyEventLogic() {
        events = new HashMap<>();
        waypoints = new HashMap<>();
        triggered = new HashMap<>();
    }

    public void addEvent(EmergencyEventDto event) {
        LocalTime endTime =
                EmergencyEventUtils.convertToLocalTime(event.getEndTime());
        LocalTime now = LocalTime.now();
        // ignore this event if it has already passed when added
        if (now.isAfter(endTime)) {
            return;
        }
        this.events.put(event.getCode(), event);
        if (locationUpdateCallbackIndex == -1) {
            locationUpdateCallbackIndex =
                    TrackerLogic.registerLocationUpdateCompleteEvent(new TrackerLogic.LocationUpdateCompleteCallback() {
                        @Override
                        public void onLocationUpdateComplete(Location location) {
                            // compare time and determine whether to end an event
                            List<String> eventsToRemove = new ArrayList<>();
                            for (EmergencyEventDto event : events.values()) {
                                LocalTime startTime =
                                        EmergencyEventUtils.convertToLocalTime(event.getStartTime());
                                LocalTime endTime =
                                        EmergencyEventUtils.convertToLocalTime(event.getEndTime());
                                LocalTime now = LocalTime.now();
                                if (now.isAfter(endTime)) {
                                    broadcastEmergencyEventEnd(event);
                                    eventsToRemove.add(event.getCode());
                                } else if (now.isBefore(endTime) && now.isAfter(startTime) && !triggered.containsKey(event.getCode())) {
                                    broadcastEmergencyEventStart(event);
                                    triggered.put(event.getCode(), true);
                                }
                            }
                            // remove ended events
                            for (String eventToRemove : eventsToRemove) {
                                events.remove(eventToRemove);
                                waypoints.remove(eventToRemove);
                                triggered.remove(eventToRemove);
                            }
                        }
                    });
        }
    }

    public int registerEmergencyEvent(EmergencyEventCallback callback) {
        emergencyEventCallbacks.put(emergencyEventCallbackNum, callback);
        emergencyEventCallbackNum += 1;
        return emergencyEventCallbackNum - 1;
    }

    public void unregisterEmergencyEvent(int callbackNumber) {
        emergencyEventCallbacks.remove(callbackNumber);
        emergencyEventCallbackNum -= 1;
    }

    public void broadcastEmergencyEventStart(EmergencyEventDto event) {
        for (EmergencyEventCallback callback : emergencyEventCallbacks.values()) {
            callback.onEmergencyEventBegin(event);
        }
    }

    public void broadcastEmergencyEventEnd(EmergencyEventDto event) {
        for (EmergencyEventCallback callback : emergencyEventCallbacks.values()) {
            callback.onEmergencyEventEnd(event);
        }
    }

    public void processEmergencyEventStart(EmergencyEventDto event, LocationDto currentLocation,
                                           RouteDto currentRoute) {
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
                double r0 = LatLngConverterUtils.getLatitudeFromDistance(event.getRadius() * 2);
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
                List<LatLng> tempWaypoints = new ArrayList<>();
                tempWaypoints.add(LatLngConverterUtils.getLatLngFromDto(waypoint));
                waypoints.put(event.getCode(), tempWaypoints);
                // re-search routes from the current position with the added waypoints
                NavigationUtils.updateRouteFromCurrentLocation(currentRoute, currentLocation);
            }
        }
    }

    public void processEmergencyEvenEnd(EmergencyEventDto event, LocationDto currentLocation,
                                        RouteDto currentRoute) {
        List<LatLng> tempWaypoints = waypoints.get(event.getCode());
        if (tempWaypoints != null) {
            List<LocationDto> allWaypoints = currentRoute.getWaypoints();
            for (LatLng tw : tempWaypoints) {
                // if the two waypoints are the same, or the one stored in the current route has
                // been passed, then remove it
                allWaypoints.removeIf(ta -> tw.latitude == ta.getLatitude() && tw.longitude == ta.getLongitude() ||
                        !NavigationUtils.isLocationAheadOfReference(currentRoute, ta,
                                currentLocation));
            }

            // re-search routes from the current position with event-related waypoints removed
            NavigationUtils.updateRouteFromCurrentLocation(currentRoute, currentLocation);
        }
    }
}
