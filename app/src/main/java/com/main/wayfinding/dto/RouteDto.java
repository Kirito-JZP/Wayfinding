package com.main.wayfinding.dto;

import android.graphics.Color;
import android.util.Pair;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.model.TravelMode;

import java.util.ArrayList;
import java.util.List;

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
            option.color(Color.DKGRAY);
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
        // calculate forward direction unit if possible
//        if (endLocation != null) {
//            calculateForwardDirection();
//        }
    }

    public LocationDto getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(LocationDto endLocation) {
        this.endLocation = endLocation;
        // calculate forward direction unit if possible
//        if (startLocation != null) {
//            calculateForwardDirection();
//        }
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

    public void updateUI(GoogleMap map) {
        for (RouteDto.RouteStep step : getSteps()) {
            Polyline line = map.addPolyline(step.getOption());
            step.setLine(line);
        }
    }

    public void updateRouteState(LocationDto currentLocation) {
        boolean hasFoundCurrentStep = false;
        int index = 0;
        for (RouteStep step : steps) {
            if(step.isPassed()) {
                continue;
            }
            LocationDto startLocation = step.getStartLocation();
            LocationDto endLocation = step.getEndLocation();
            Pair<Float, Float> forwardLatLng = calculateForwardDirection(startLocation, endLocation);
            float forwardLat = forwardLatLng.first;
            float forwardLng = forwardLatLng.second;
            // use the symbol of the difference between the "end location" and the "start
            // location" to
            // judge whether the step should be marked as "passed"
            // the "end location" here refers to the start location of a step and the "start
            // location"
            // refers to the parameter currentLocation
            // if the symbol matches the precalculated forwardLng/forwardLat, then the
            // latitude/longitude
            // must be towards the end location of the whole route
            if ((startLocation.getLongitude() - currentLocation.getLongitude()) * forwardLng >= 0 ||
                    (startLocation.getLatitude() - currentLocation.getLatitude()) * forwardLat >= 0) {
                if (!hasFoundCurrentStep) {
                    currentStepIndex = index;
                    hasFoundCurrentStep = true;
                }
                step.setPassed(false);
            } else {
                step.setPassed(true);
            }
            index += 1;
        }
    }

    public boolean replaceOrAddStep(int index, RouteStep step) {
        try {
            if (index < steps.size()) {
                steps.set(index, step);
            } else {
                steps.add(index, step);
            }
            return true;
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
    }

    public boolean judgePassed(LocationDto location, LocationDto reference, float forwardLat, float forwardLng) {
        return false;
    }

    private Pair<Float, Float> calculateForwardDirection(LocationDto startLocation, LocationDto endLocation) {
        float forwardLat = endLocation.getLatitude() - startLocation.getLatitude() > 0 ?
                1.0F : -1.0F;
        float forwardLng = endLocation.getLongitude() - startLocation.getLongitude() > 0
                ? 1.0F : -1.0F;
        return new Pair<Float, Float>(forwardLat, forwardLng);
    }

    private LocationDto startLocation;
    private LocationDto endLocation;
    private TravelMode mode;
    private int currentStepIndex;
    private final List<RouteStep> steps;
    private final List<LocationDto> waypoints;
}
