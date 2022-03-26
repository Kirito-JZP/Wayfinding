package com.main.wayfinding.dto;

import com.google.android.gms.maps.model.PolylineOptions;

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
    public RouteDto() {
        polylineOptionsList = new ArrayList<>();
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

    public void addPolylineOptions(PolylineOptions options) {
        polylineOptionsList.add(options);
    }

    public List<PolylineOptions> getAllPolylineOptions() {
        return polylineOptionsList;
    }

    private LocationDto startLocation;
    private LocationDto endLocation;
    private List<LocationDto> waypoints;
    private List<PolylineOptions> polylineOptionsList;

}
