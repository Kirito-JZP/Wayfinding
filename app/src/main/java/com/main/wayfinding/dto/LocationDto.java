package com.main.wayfinding.dto;

import com.google.android.gms.maps.model.LatLngBounds;

/**
 * Entity for storing loaction information
 *
 * @author JIA
 * @author Last Modified By JIA
 * @version Revision: 0
 * Date: 2022/1/28 22:10
 */
public class LocationDto {

    private String name;
    private double latitude;
    private double longitude;
    private String city;
    private String country;
    private String address;
    private String postalCode;

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

    public String getCity() {
        return city;
    }

    public LocationDto setCity(String city) {
        this.city = city;
        return this;
    }

    public String getCountry() {
        return country;
    }

    public LocationDto setCountry(String country) {
        this.country = country;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public LocationDto setAddress(String address) {
        this.address = address;
        return this;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public LocationDto setPostalCode(String postalCode) {
        this.postalCode = postalCode;
        return this;
    }
}
