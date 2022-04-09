package com.main.wayfinding.utility;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.errors.ApiException;
import com.google.maps.errors.ZeroResultsException;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.SnappedPoint;
import com.google.maps.model.TravelMode;
import com.main.wayfinding.R;
import com.main.wayfinding.WayfindingApp;
import com.main.wayfinding.dto.EmergencyEventDto;
import com.main.wayfinding.dto.LocationDto;
import com.main.wayfinding.dto.RouteDto;
import com.main.wayfinding.logic.EmergencyEventLogic;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Description
 *
 * @author Zehua Guo
 * @author Last Modified By Zehua Guo
 * @version Revision: 0
 * Date: 2022/4/7 1:27
 */
public class NavigationUtils {

    private static final List<PatternItem> walkingPattern = Arrays.asList(new Dot(), new Gap(20));

    public static void updateRouteFromCurrentLocation(RouteDto route, LocationDto currentLocation) {
        // re-arrange the route from the current step while keep the previously passed steps
        List<RouteDto.RouteStep> steps = route.getSteps();
        RouteDto.RouteStep currentStep = steps.get(route.getCurrentStepIndex());
        // modify route from the next route step, otherwise just return (current step is the last
        // step)
        if (route.getCurrentStepIndex() >= route.getSteps().size() - 1) {
            return;
        }
        int indexToReplace = route.getCurrentStepIndex() + 1;
        // filter waypoints, removing those already passed
        List<com.google.maps.model.LatLng> validWaypoints =
                filterValidWaypoints(route, currentLocation);
        List<RouteDto> possibleRoutes = findRoute(currentStep.getEndLocation(),
                route.getEndLocation(),
                route.getMode(), validWaypoints).first;
        long minTime = Long.MAX_VALUE;
        RouteDto bestRoute = possibleRoutes.get(0);
        for (RouteDto r : possibleRoutes) {
            long totalTime = 0;
            for (RouteDto.RouteStep step : r.getSteps()) {
                totalTime += step.getEstimatedTime();
            }
            if (totalTime < minTime) {
                minTime = totalTime;
                bestRoute = r;
            }
        }
        // save new steps
        for (RouteDto.RouteStep step : bestRoute.getSteps()) {
            // replace the step already saved in the route
            replaceOrAddStep(route, indexToReplace, step);
            indexToReplace++;
        }
        // remove the rest steps if there are any
        for (int i = indexToReplace; i < route.getSteps().size(); i++) {
            route.getSteps().remove(indexToReplace);
            indexToReplace++;
        }
    }

    @NonNull
    private static List<com.google.maps.model.LatLng> filterValidWaypoints(RouteDto route,
                                                                           LocationDto currentLocation) {
        // filter only waypoints that haven't been passed
        List<com.google.maps.model.LatLng> results = new ArrayList<>();
        for (LocationDto waypoint : route.getWaypoints()) {
            if (isLocationAheadOfReference(route, waypoint, currentLocation)) {
                results.add(LatLngConverterUtils.convert(LatLngConverterUtils.getLatLngFromDto(waypoint)));
            }
        }
        return results;
    }

    public static Pair<List<RouteDto>, LatLngBounds> findRoute(
            LocationDto startLocDto, LocationDto targetLocDto, TravelMode mode,
            List<com.google.maps.model.LatLng> waypoints) {
        if (startLocDto != null && targetLocDto != null) {
            LatLng orig = LatLngConverterUtils.getLatLngFromDto(startLocDto);
            LatLng dest = LatLngConverterUtils.getLatLngFromDto(targetLocDto);
            List<RouteDto> routes = new ArrayList<>();
            try {
                DirectionsApiRequest request =
                        new DirectionsApiRequest(WayfindingApp.getGeoApiContext())
                                .origin(LatLngConverterUtils.convert(orig))
                                .destination(LatLngConverterUtils.convert(dest))
                                .alternatives(true)
                                .mode(mode);
                if (waypoints != null && !waypoints.isEmpty()) {
                    request.waypoints(waypoints.toArray(new com.google.maps.model.LatLng[waypoints.size()]));
                }
                DirectionsResult result = request.await();
                double max_lat = result.routes[0].bounds.northeast.lat;
                double min_lat = result.routes[0].bounds.southwest.lat;
                double max_lng = result.routes[0].bounds.northeast.lng;
                double min_lng = result.routes[0].bounds.southwest.lng;
                for (DirectionsRoute r : result.routes) {
                    RouteDto route = new RouteDto();
                    max_lat = Math.max(max_lat, r.bounds.northeast.lat);
                    min_lat = Math.min(min_lat, r.bounds.southwest.lat);
                    max_lng = Math.max(max_lng, r.bounds.northeast.lng);
                    min_lng = Math.min(min_lng, r.bounds.southwest.lng);
                    // set mode
                    route.setMode(mode);
                    // set waypoints
                    for (com.google.maps.model.LatLng waypoint : waypoints) {
                        LocationDto loc = new LocationDto();
                        loc.setLatitude(waypoint.lat);
                        loc.setLongitude(waypoint.lng);
                        route.addWaypoint(loc);
                    }
                    // add start location and end location
                    LocationDto startLocation = new LocationDto();
                    LocationDto endLocation = new LocationDto();
                    startLocation.setLatitude(r.legs[0].startLocation.lat);
                    startLocation.setLongitude(r.legs[0].startLocation.lng);
                    endLocation.setLatitude(r.legs[r.legs.length - 1].endLocation.lat);
                    endLocation.setLongitude(r.legs[r.legs.length - 1].endLocation.lng);
                    route.setStartLocation(startLocation);
                    route.setEndLocation(endLocation);
                    // save polyline options and find place IDs
                    List<com.google.maps.model.LatLng> locations = new ArrayList<>();
                    for (DirectionsLeg leg : r.legs) {
                        for (DirectionsStep step : leg.steps) {
                            route.addStep(generateRouteStepWithoutPlaceID(step));
                            locations.add(step.startLocation);
                            locations.add(step.endLocation);
                        }
                    }
                    // find place IDs
                    int batchNum = locations.size() / 100 + 1;
                    for (int i = 0; i < batchNum; i++) {
                        int startIndex = 100 * i;
                        int endIndex = Math.min(100 * (i + 1), locations.size());
                        for (int j = startIndex; j < endIndex; j++) {
                            List<com.google.maps.model.LatLng> sub =
                                    locations.subList(startIndex, endIndex);
                            List<LatLng> lls = LatLngConverterUtils.convertMap2GMS(sub);
                            List<SnappedPoint> snappedPoints =
                                    PlaceManagerUtils.nearestRoads(lls);
                            if (snappedPoints != null) {
                                for (SnappedPoint p : snappedPoints) {
                                    int stepIndex = p.originalIndex / 2;
                                    if (p.originalIndex % 2 == 0) {
                                        route.getSteps().get(stepIndex).getStartLocation().setGmPlaceID(p.placeId);
                                    } else {
                                        route.getSteps().get(stepIndex).getEndLocation().setGmPlaceID(p.placeId);
                                    }
                                }
                            }
                        }
                    }
                    routes.add(route);
                }
                LatLngBounds bounds = new LatLngBounds(new LatLng(min_lat, min_lng),
                        new LatLng(max_lat, max_lng));
                return new Pair<>(routes, bounds);
            } catch (ZeroResultsException e) {
                return null;
            } catch (InterruptedException | ApiException | IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    @NonNull
    public static RouteDto.RouteStep generateRouteStepWithoutPlaceID(DirectionsStep step) {
        int colour;
        PolylineOptions options = new PolylineOptions();
        Context context = WayfindingApp.getContext();
        switch (step.travelMode) {
            case WALKING:
                colour = context.getResources().getColor(R.color.polyline_walking,
                        context.getTheme());
                break;
            case TRANSIT:
                colour = context.getResources().getColor(R.color.polyline_transit,
                        context.getTheme());
                break;
            case BICYCLING:
                colour = context.getResources().getColor(R.color.polyline_bicycling,
                        context.getTheme());
                break;
            default:
                colour = context.getResources().getColor(R.color.polyline_default,
                        context.getTheme());
        }
        options.addAll(LatLngConverterUtils.convertMap2GMS(step.polyline.decodePath()))
                .color(colour)
                .width(25)
                .startCap(new RoundCap())
                .endCap(new RoundCap())
                .geodesic(true);
        if (step.travelMode == TravelMode.WALKING) {
            options.pattern(walkingPattern);
        }
        // create a RouteStep object
        RouteDto.RouteStep routeStep = new RouteDto.RouteStep();
        LocationDto stepStartLocation = new LocationDto();
        stepStartLocation.setLatitude(step.startLocation.lat);
        stepStartLocation.setLongitude(step.startLocation.lng);
        LocationDto stepEndLocation = new LocationDto();
        stepEndLocation.setLatitude(step.endLocation.lat);
        stepEndLocation.setLongitude(step.endLocation.lng);
        routeStep.setStartLocation(stepStartLocation);
        routeStep.setEndLocation(stepEndLocation);
        routeStep.setOption(options);
        routeStep.setMode(step.travelMode);
        routeStep.setEstimatedTime(step.duration.inSeconds);
        return routeStep;
    }

    private static int findClosestStep(RouteDto route, LocationDto location) {
        int index = -1;
        double minDistance = Double.MAX_VALUE;
        double threshold = 1e-3;
        List<RouteDto.RouteStep> steps = route.getSteps();
        for (int i = 0; i < steps.size(); i++) {
            LatLng start = LatLngConverterUtils.getLatLngFromDto(steps.get(i).getStartLocation());
            LatLng end = LatLngConverterUtils.getLatLngFromDto(steps.get(i).getEndLocation());
            double distance = 0.0;
            if (Math.abs(start.latitude - end.latitude) <= threshold) {
                // same latitude
                distance = Math.abs(start.latitude - location.getLatitude());
            } else if (Math.abs(start.longitude - end.longitude) <= threshold) {
                // same longitude
                distance = Math.abs(start.longitude - location.getLongitude());
            } else {
                // use line equation instead
                double x1 = start.longitude;
                double x2 = end.longitude;
                double y1 = start.latitude;
                double y2 = end.latitude;
                double x = location.getLongitude();
                double y = location.getLatitude();
                double A = y2 - y1;
                double B = x1 - x2;
                double C = x1 * (y1 - y2) + y1 * (x2 - x1);
                double nom = Math.abs(A * x + B * y + C);
                double denom = Math.sqrt(A * A + B * B);
                distance = nom / denom;
            }
            if (distance <= minDistance) {
                index = i;
                minDistance = distance;
            }
        }
        return index;
    }

    @NonNull
    public static List<RouteDto.RouteStep> findStepsAffectedBy(RouteDto route,
                                                               EmergencyEventDto event) {
        // uniformly sample in the range of event and obtain their place ids
        List<LatLng> eventSamples = new ArrayList<>();
        LatLng centerPoint = new LatLng(event.getLatitude(), event.getLongitude());
        double radius = event.getRadius();
        double stepLength = (radius * radius) / 100;
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            double r = i * stepLength;
            double theta = random.nextDouble() * 2 * Math.PI;
            double lat =
                    LatLngConverterUtils.getLatitudeFromDistance(Math.cos(theta) * Math.sqrt(r)) + centerPoint.latitude;
            double lng =
                    LatLngConverterUtils.getLongitudeFromDistance(Math.sin(theta) * Math.sqrt(r),
                            centerPoint.latitude) + centerPoint.longitude;
            LatLng samplePoint = new LatLng(lat, lng);
            eventSamples.add(samplePoint);
        }   // https://blog.csdn.net/weixin_42305901/article/details/114128101
        List<SnappedPoint> points = PlaceManagerUtils.nearestRoads(eventSamples);
        // obtain the intersection of these two sets and store it in placeIdsInRoute
        // if there is any place id in the current route that is affected, the result set will be
        // non-empty
        List<RouteDto.RouteStep> affectedSteps = new ArrayList<>();
        List<RouteDto.RouteStep> steps = route.getSteps();
        Set<Integer> affectedIndices = new HashSet<>();
        int currentIndex = route.getCurrentStepIndex();
        for (int i = 0; i < points.size(); i++) {
            for (int j = 0; j < steps.size(); j++) {
                String pointPlaceID = points.get(i).placeId;
                String startLocationPlaceID = steps.get(j).getStartLocation().getGmPlaceID();
                String endLocationPlaceID = steps.get(j).getEndLocation().getGmPlaceID();
                if (StringUtils.equals(pointPlaceID, startLocationPlaceID) ||
                        StringUtils.equals(pointPlaceID, endLocationPlaceID)) {
                    // only add indices that are ahead of the current index
                    if (j > currentIndex) {
                        steps.get(j).setAffected(true);
                        affectedIndices.add(j);
                    }
                }
            }
        }
        for (int index : affectedIndices) {
            affectedSteps.add(route.getSteps().get(index));
        }
        return affectedSteps;
    }

    public static void updatePolylinesUI(RouteDto route, GoogleMap map) {
        for (RouteDto.RouteStep step : route.getSteps()) {
            if (step.getLine() != null) {
                Polyline line = step.getLine();
                PolylineOptions options = step.getOption();
                // refresh polyline by re-setting each option
                // it seems that there are no direct ways to apply
                // all options using one single method
                // note that directly clearing all polylines and adding
                // new ones will lead to flickering
                line.setColor(options.getColor());
                line.setClickable(options.isClickable());
                line.setStartCap(options.getStartCap());
                line.setEndCap(options.getEndCap());
                line.setGeodesic(options.isGeodesic());
                line.setJointType(options.getJointType());
                line.setPattern(options.getPattern());
                line.setPoints(options.getPoints());
                line.setVisible(options.isVisible());
                line.setWidth(options.getWidth());
                line.setZIndex(options.getZIndex());
            } else {
                step.setLine(map.addPolyline(step.getOption()));
            }
        }
    }

    public static void updateRouteState(RouteDto route, LocationDto currentLocation) {
        // find the closest step to the current location
        int index = findClosestStep(route, currentLocation);
        if (index != -1) {
            // save the current step
            route.setCurrentStepIndex(index);
            List<RouteDto.RouteStep> steps = route.getSteps();
            // mark all steps at index and after index as not passed
            for (int i = index; i < steps.size(); i++) {
                steps.get(i).setPassed(false);
            }
            // mark all steps before index as passed
            for (int i = index - 1; i >= 0; i--) {
                steps.get(i).setPassed(true);
            }
        }
    }

    public static boolean replaceOrAddStep(RouteDto route, int index, RouteDto.RouteStep step) {
        try {
            List<RouteDto.RouteStep> steps = route.getSteps();
            if (index < steps.size()) {
                if (steps.get(index).getLine() != null) {
                    // remove the current polyline
                    steps.get(index).getLine().setVisible(false);
                    steps.get(index).getLine().remove();
                }
                steps.set(index, step);
            } else {
                steps.add(index, step);
            }
            return true;
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
    }

    public static boolean isLocationAheadOfReference(RouteDto route, LocationDto location,
                                                     LocationDto reference) {
        // find the closest step for both location and reference
        // and then judge if the former step is after the latter one
        int indexLocation = findClosestStep(route, location);
        int indexReference = findClosestStep(route, reference);
        return indexLocation != -1 && indexReference != -1 && indexLocation > indexReference;
    }

    @NonNull
    public static List<com.google.maps.model.LatLng> getLatLngFromWaypoints(@Nullable RouteDto currentRoute) {
        if (currentRoute == null || currentRoute.getWaypoints().isEmpty()) {
            return new ArrayList<>();
        } else {
            return currentRoute.getWaypoints()
                    .stream()
                    .map(waypoint ->
                            LatLngConverterUtils.convert(LatLngConverterUtils.getLatLngFromDto(waypoint))
                    ).collect(Collectors.toList());
        }
    }
}
