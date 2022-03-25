package com.main.wayfinding.dto;

import com.google.android.gms.maps.model.LatLngBounds;

import java.util.Date;

/**
 * Entity for storing loaction information
 *
 * @author JIA
 * @author Last Modified By JIA
 * @version Revision: 0
 * Date: 2022/1/28 22:10
 */
public class LocationDto {
//    private String locationId;
    private String name;
    private double latitude;
    private double longitude;
    private String city;
    private String country;
    private String address;
    private String postalCode;
    private Date date;

    // the following properties only apply to google maps apis
    private String gmPlaceID;
    private String gmImgUrl;

//    public String getLocationId() {
//        return locationId;
//    }
//
//    public void setLocationId(String locationId) {
//        this.locationId = locationId;
//    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getGmPlaceID() {
        return gmPlaceID;
    }

    public void setGmPlaceID(String gmPlaceID) {
        this.gmPlaceID = gmPlaceID;
    }

    public String getGmImgUrl() {
        return gmImgUrl;
    }

    public void setGmImgUrl(String gmImgUrl) {
        this.gmImgUrl = gmImgUrl;
    }

}
