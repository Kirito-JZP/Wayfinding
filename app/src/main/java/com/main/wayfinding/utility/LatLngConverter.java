package com.main.wayfinding.utility;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class LatLngConverter {
    public static LatLng convert(com.google.maps.model.LatLng ll) {
        return new LatLng(ll.lat, ll.lng);
    }

    public static List<LatLng> convert(List<com.google.maps.model.LatLng> lls) {
        List<LatLng> results = new ArrayList<>();
        for (com.google.maps.model.LatLng ll : lls) {
            results.add(new LatLng(ll.lat, ll.lng));
        }
        return results;
    }

    public static com.google.maps.model.LatLng convert(LatLng ll) {
        return new com.google.maps.model.LatLng(ll.latitude, ll.longitude);
    }
}
