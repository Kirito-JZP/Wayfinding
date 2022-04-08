package com.main.wayfinding.utility;

import com.google.android.gms.maps.model.LatLng;
import com.main.wayfinding.dto.LocationDto;

import java.util.ArrayList;
import java.util.List;

/**
 * Define the fragment used for displaying map and dynamic Sustainable way-finding
 *
 * @author Zehua Guo
 * @author Last Modified By Zehua Guo
 * @version Revision: 0
 * Date: 2022/1/26 19:50
 */
public class LatLngConverterUtils {
    public static LatLng convert(com.google.maps.model.LatLng ll) {
        return new LatLng(ll.lat, ll.lng);
    }

    public static List<LatLng> convertMap2GMS(List<com.google.maps.model.LatLng> lls) {
        List<LatLng> results = new ArrayList<>();
        for (com.google.maps.model.LatLng ll : lls) {
            results.add(new LatLng(ll.lat, ll.lng));
        }
        return results;
    }

    public static List<com.google.maps.model.LatLng> convertGMS2Map(List<LatLng> lls) {
        List<com.google.maps.model.LatLng> results = new ArrayList<>();
        for (LatLng ll : lls) {
            results.add(new com.google.maps.model.LatLng(ll.latitude, ll.longitude));
        }
        return results;
    }

    public static com.google.maps.model.LatLng convert(LatLng ll) {
        return new com.google.maps.model.LatLng(ll.latitude, ll.longitude);
    }

    public static LatLng getLatLngFromDto(LocationDto locationDto) {
        return new LatLng(locationDto.getLatitude(), locationDto.getLongitude());
    }

    public static double getDistanceInMetersAlongLatitude(double deltaInDegrees,
                                                          double currentLat) {
        // https://en.wikipedia.org/wiki/Geographic_coordinate_system
        return deltaInDegrees * (111132.92 - 559.82 * Math.cos(2 * currentLat) + 1.175 * Math.cos(4 * currentLat) - 0.0023 * Math.cos(6 * currentLat));
    }

    public static double getDistanceInMetersAlongLongitude(double deltaInDegrees,
                                                           double currentLng) {
        // https://en.wikipedia.org/wiki/Geographic_coordinate_system
        return deltaInDegrees * (111412.84 * Math.cos(currentLng) - 93.5 * Math.cos(3 * currentLng) + 0.118 * Math.cos(5 * currentLng));
    }

    public static double getLatitudeFromDistance(double distanceInMeters) {
        // https://gis.stackexchange.com/questions/2951/algorithm-for-offsetting-a-latitude-longitude-by-some-amount-of-meters
        return Math.toDegrees(distanceInMeters / (6371.393 * 1000));
    }

    public static double getLongitudeFromDistance(double distanceInMeters,
                                                              double currentLat) {
        // https://gis.stackexchange.com/questions/2951/algorithm-for-offsetting-a-latitude-longitude-by-some-amount-of-meters
        return Math.toDegrees(distanceInMeters / (6371.393 * 1000 * Math.cos(Math.toRadians(currentLat))));
    }
}
