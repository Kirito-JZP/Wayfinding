package com.main.wayfinding.dto;

import android.content.Context;

import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.model.TravelMode;
import com.main.wayfinding.R;
import com.main.wayfinding.WayfindingApp;

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
        private boolean isAffected;
        private long estimatedTime; // in seconds

        public TravelMode getMode() {
            return mode;
        }

        public void setMode(TravelMode mode) {
            this.mode = mode;
        }

        public long getEstimatedTime() {
            return estimatedTime;
        }

        public void setEstimatedTime(long estimatedTime) {
            this.estimatedTime = estimatedTime;
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

    public void setCurrentStepIndex(int currentStepIndex) {
        this.currentStepIndex = currentStepIndex;
    }

    public PolylineOptions getPassedPolylineOptions() {
        return passedPolylineOptions;
    }

    public void setPassedPolylineOptions(PolylineOptions passedPolylineOptions) {
        this.passedPolylineOptions = passedPolylineOptions;
    }

    public Polyline getPassedPolyline() {
        return passedPolyline;
    }

    public void setPassedPolyline(Polyline passedPolyline) {
        this.passedPolyline = passedPolyline;
    }

    public PolylineOptions getRestPolylineOptions() {
        return restPolylineOptions;
    }

    public void setRestPolylineOptions(PolylineOptions restPolylineOptions) {
        this.restPolylineOptions = restPolylineOptions;
    }

    public Polyline getRestPolyline() {
        return restPolyline;
    }

    public void setRestPolyline(Polyline restPolyline) {
        this.restPolyline = restPolyline;
    }

    private LocationDto startLocation;
    private LocationDto endLocation;
    private TravelMode mode;
    private final List<RouteDto.RouteStep> steps;
    private final List<LocationDto> waypoints;
    private int currentStepIndex;
    private PolylineOptions passedPolylineOptions;
    private Polyline passedPolyline;
    private PolylineOptions restPolylineOptions;
    private Polyline restPolyline;
}
