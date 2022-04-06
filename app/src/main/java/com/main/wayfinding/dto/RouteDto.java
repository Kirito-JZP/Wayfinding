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
        private long estimatedTime; // in seconds

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

    private LocationDto startLocation;
    private LocationDto endLocation;
    private TravelMode mode;
    private final List<RouteDto.RouteStep> steps;
    private final List<LocationDto> waypoints;
    private int currentStepIndex;
}
