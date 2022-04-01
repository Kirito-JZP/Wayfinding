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
}
