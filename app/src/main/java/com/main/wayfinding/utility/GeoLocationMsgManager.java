package com.main.wayfinding.utility;


import android.location.Address;
import android.location.Geocoder;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.model.LatLng;
import com.main.wayfinding.dto.LocationDto;

import java.io.IOException;
import java.util.Locale;

/**
 * Class for location information management.
 *
 * @author JIA
 * @author Last Modified By JIA
 * @version Revision: 0
 * Date: 2022/1/29 1:01
 */
public class GeoLocationMsgManager {

    public static LocationDto findLocationGeoMsg(FragmentActivity activity, LatLng latLng) {
        LocationDto locationDto = new LocationDto();
        Geocoder geocoder = new Geocoder(activity, Locale.getDefault());
        try {
            Address address = geocoder
                    .getFromLocation(latLng.latitude, latLng.longitude, 1).get(0);
            locationDto.setName(address.getFeatureName());
            locationDto.setLatitude(latLng.latitude);
            locationDto.setLongitude(latLng.longitude);
            locationDto.setCity(address.getLocality());
            locationDto.setCountry(address.getCountryName());
            locationDto.setAddress(address.getAddressLine(0));
            locationDto.setPostalCode(address.getPostalCode());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return locationDto;
    }
}
