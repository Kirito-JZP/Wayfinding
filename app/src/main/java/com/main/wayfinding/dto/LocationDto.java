package com.main.wayfinding.dto;

import com.google.android.gms.maps.model.LatLngBounds;

/**
 * Entity for storing loaction information
 *
 * @author jia72
 * @author Last Modified By jia72
 * @version Revision: 0
 * Date: 2022/1/28 22:10
 */
public class LocationDto {

    private String name;
    private double latitude;
    private double longitude;
    private String address;

    // the following properties only apply to google maps apis
    private String gmPlaceID;
    private LatLngBounds gmViewPort;

    public LatLngBounds getGmViewPort() {
        return gmViewPort;
    }

    public LocationDto setGmViewPort(LatLngBounds gmViewPort) {
        this.gmViewPort = gmViewPort;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public LocationDto setAddress(String address) {
        this.address = address;
        return this;
    }

    public String getGmPlaceID() {
        return gmPlaceID;
    }

    public LocationDto setGmPlaceID(String gmPlaceID) {
        this.gmPlaceID = gmPlaceID;
        return this;
    }


    public String getName() {
        return name;
    }

    public LocationDto setName(String name) {
        this.name = name;
        return this;
    }

    public double getLatitude() {
        return latitude;
    }

    public LocationDto setLatitude(double latitude) {
        this.latitude = latitude;
        return this;
    }

    public double getLongitude() {
        return longitude;
    }

    public LocationDto setLongitude(double longitude) {
        this.longitude = longitude;
        return this;
    }
}
