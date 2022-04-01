package com.main.wayfinding.dto;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.SnappedPoint;
import com.google.maps.model.TravelMode;
import com.main.wayfinding.R;
import com.main.wayfinding.WayfindingApp;
import com.main.wayfinding.utility.LatLngConverterUtils;
import com.main.wayfinding.utility.PlaceManagerUtils;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import javadz.collections.list.AbstractListDecorator;

/**
 * Description
 *
 * @author Zehua Guo
 * @author Last Modified By Zehua Guo
 * @version Revision: 0
 * Date: 2022/3/24 14:39
 */


public class RouteDto {
    public static final class RouteStep {
        private PolylineOptions option;
        private Polyline line;
        private LocationDto startLocation;
        private LocationDto endLocation;
        private TravelMode mode;
        private boolean passed;
        private String placeID;
        private boolean isAffected;

        public String getPlaceID() {
            return placeID;
        }

        public void setPlaceID(String placeID) {
            this.placeID = placeID;
        }

        public TravelMode getMode() {
            return mode;
        }

        public void setMode(TravelMode mode) {
            this.mode = mode;
        }

        public boolean isPassed() {
            return passed;
        }

        public void setPassed(boolean passed) {
            this.passed = passed;
            // change the colour of the polyline
            // note that until map.addPolyline is called, the settings won't be applied
            Context context = WayfindingApp.getContext();
            if (passed) {
                option.color(context.getResources().getColor(R.color.polyline_default,
                        context.getTheme()));
            } else {
                switch (mode) {
                    case WALKING:
                        option.color(context.getResources().getColor(R.color.polyline_walking,
                                context.getTheme()));
                        break;
                    case TRANSIT:
                        option.color(context.getResources().getColor(R.color.polyline_transit,
                                context.getTheme()));
                        break;
                    case BICYCLING:
                        option.color(context.getResources().getColor(R.color.polyline_bicycling,
                                context.getTheme()));
                        break;
                    default:
                        option.color(context.getResources().getColor(R.color.polyline_default,
                                context.getTheme()));
                }
            }
        }

        public boolean isAffected() {
            return isAffected;
        }

        public void setAffected(boolean affected) {
            isAffected = affected;
        }

        public PolylineOptions getOption() {
            return option;
        }

        public void setOption(PolylineOptions option) {
            this.option = option;
        }

        public Polyline getLine() {
            return line;
        }

        public void setLine(Polyline line) {
            this.line = line;
        }

        public LocationDto getStartLocation() {
            return startLocation;
        }

        public void setStartLocation(LocationDto startLocation) {
            this.startLocation = startLocation;
        }

        public LocationDto getEndLocation() {
            return endLocation;
        }

        public void setEndLocation(LocationDto endLocation) {
            this.endLocation = endLocation;
        }
    }

    public RouteDto() {
        steps = new ArrayList<>();
        waypoints = new ArrayList<>();
    }

    public LocationDto getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(LocationDto startLocation) {
        this.startLocation = startLocation;
    }

    public LocationDto getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(LocationDto endLocation) {
        this.endLocation = endLocation;
    }

    public List<LocationDto> getWaypoints() {
        return waypoints;
    }

    public void addWaypoint(LocationDto waypoint) {
        waypoints.add(waypoint);
    }

    public List<RouteStep> getSteps() {
        return steps;
    }

    public void addStep(RouteStep step) {
        steps.add(step);
    }

    public TravelMode getMode() {
        return mode;
    }

    public void setMode(TravelMode mode) {
        this.mode = mode;
    }

    public int getCurrentStepIndex() {
        return currentStepIndex;
    }

    public void updatePolylinesUI(GoogleMap map) {
        for (RouteDto.RouteStep step : steps) {
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

    public void updateRouteState(LocationDto currentLocation) {
        // find the closest step to the current location
        int index = findClosestStep(currentLocation);
        if (index != -1) {
            // save the current step
            currentStepIndex = index;
            // mark all steps at index and after index as not passed
            for (int i = currentStepIndex; i < steps.size(); i++) {
                steps.get(i).setPassed(false);
            }
            // mark all steps before index as passed
            for (int i = currentStepIndex - 1; i >= 0; i--) {
                steps.get(i).setPassed(true);
            }
        }
    }

    public boolean replaceOrAddStep(int index, RouteStep step) {
        try {
            if (index < steps.size()) {
                if(steps.get(index).line != null) {
                    // remove the current polyline
                    steps.get(index).line.setVisible(false);
                    steps.get(index).line.remove();
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

    public boolean isLocationAheadOfReference(LocationDto location, LocationDto reference) {
        // find the closest step for both location and reference
        // and then judge if the former step is after the latter one
        int indexLocation = findClosestStep(location);
        int indexReference = findClosestStep(reference);
        return indexLocation != -1 && indexReference != -1 && indexLocation > indexReference;
    }

    @NonNull
    public List<RouteStep> findStepsAffectedBy(EmergencyEventDto event) {
        // uniformly sample in the range of event and obtain their place ids
        List<LatLng> eventSamples = new ArrayList<>();
        LatLng centerPoint = event.getLocation().getLatLng();
        double radius = event.getRadius();
        double stepLength = radius / 100;
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            double r = i * stepLength;
            double theta = random.nextFloat() * 2 * Math.PI;
            eventSamples.add(new LatLng(Math.cos(theta) * Math.sqrt(r) + centerPoint.latitude,
                    Math.sin(theta) * Math.sqrt(r) + centerPoint.longitude));
        }   // https://blog.csdn.net/weixin_42305901/article/details/114128101
        //List<SnappedPoint> points = PlaceManagerUtils.nearestRoads(eventSamples);
        // obtain the intersection of these two sets and store it in placeIdsInRoute
        // if there is any place id in the current route that is affected, the result set will be
        // non-empty
        List<RouteStep> affectedSteps = new ArrayList<>();
        Set<Integer> affectedIndices = new HashSet<>();
//        for (int i = 0; i < points.size(); i++) {
//            for (int j = 0; j < steps.size(); j++) {
//                if (StringUtils.equals(points.get(i).placeId, steps.get(j).placeID)) {
//                    steps.get(j).isAffected = true;
//                    affectedIndices.add(j);
//                }
//            }
//        }
        for (int index : affectedIndices) {
            affectedSteps.add(steps.get(index));
        }
        return affectedSteps;
    }

    private int findClosestStep(LocationDto location) {
        int index = -1;
        double minDistance = Double.MAX_VALUE;
        double threshold = 1e-3;
        for (int i = 0; i < steps.size(); i++) {
            LatLng start = steps.get(i).startLocation.getLatLng();
            LatLng end = steps.get(i).endLocation.getLatLng();
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

    public static RouteDto.RouteStep generateRouteStep(DirectionsStep step) {
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
        return routeStep;
    }

    private List<com.google.maps.model.LatLng> filterValidWaypoints(LocationDto currentLocation) {
        // filter only waypoints that haven't been passed
        List<com.google.maps.model.LatLng> results = new ArrayList<>();
        for (LocationDto waypoint : waypoints) {
            if (isLocationAheadOfReference(waypoint, currentLocation)) {
                results.add(LatLngConverterUtils.convert(waypoint.getLatLng()));
            }
        }
        return results;
    }

    public void updateRouteFromCurrentLocation(LocationDto currentLocation) {
        // re-arrange the route from the current step while keep the previously passed steps
        try {
            RouteStep currentStep = steps.get(currentStepIndex);
            // split currentStep into two steps according to the current location
            currentStep.setEndLocation(currentLocation);
            currentStep.setPassed(true);
            int indexToReplace = currentStepIndex + 1;
            // filter waypoints, removing those already passed
            List<com.google.maps.model.LatLng> validWaypoints =
                    filterValidWaypoints(currentLocation);
            DirectionsApiRequest request =
                    new DirectionsApiRequest(WayfindingApp.getGeoApiContext());
            request.origin(LatLngConverterUtils.convert(currentLocation.getLatLng()));
            request.destination(LatLngConverterUtils.convert(endLocation.getLatLng()));
            request.mode(mode);
            request.waypoints(validWaypoints.toArray(new com.google.maps.model.LatLng[validWaypoints.size()]));
            DirectionsResult result = request.await();
            // TODO: currently automatically select the one with the minimal estimated arriving time
            //  but should give users options if there are more than one routes available
            long minTime = Long.MAX_VALUE;
            DirectionsRoute bestRoute = result.routes[0];
            for (DirectionsRoute r : result.routes) {
                long totalTime = 0;
                for (DirectionsLeg leg : r.legs) {
                    totalTime += leg.duration.inSeconds;
                }
                if (totalTime < minTime) {
                    minTime = totalTime;
                    bestRoute = r;
                }
            }
            // add start location and end location
            LocationDto startLocation = new LocationDto();
            LocationDto endLocation = new LocationDto();
            startLocation.setLatitude(bestRoute.legs[0].startLocation.lat);
            startLocation.setLongitude(bestRoute.legs[0].startLocation.lng);
            endLocation.setLatitude(bestRoute.legs[bestRoute.legs.length - 1].endLocation.lat);
            endLocation.setLongitude(bestRoute.legs[bestRoute.legs.length - 1].endLocation.lng);
            this.startLocation = startLocation;
            this.endLocation = endLocation;
            // save polyline options
            for (DirectionsLeg leg : bestRoute.legs) {
                for (DirectionsStep step : leg.steps) {
                    RouteStep routeStep = generateRouteStep(step);
                    // replace the step already saved in the route
                    replaceOrAddStep(indexToReplace, routeStep);
                    indexToReplace += 1;
                }
            }
        } catch (ApiException | InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    private LocationDto startLocation;
    private LocationDto endLocation;
    private TravelMode mode;
    private int currentStepIndex;
    private final List<RouteStep> steps;
    private final List<LocationDto> waypoints;
    private static final List<PatternItem> walkingPattern = Arrays.asList(new Dot(), new Gap(20));
}
