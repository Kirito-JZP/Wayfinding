package com.main.wayfinding.utility;

import android.location.Location;

import com.main.wayfinding.dto.LocationDto;

import java.util.Comparator;
import java.util.List;

public class LocationSortUtils {
    public static void sortByDate(List<LocationDto> locationList) {
        locationList.sort((o1, o2) -> o2.getDate().compareTo(o1.getDate()));
    }

    public static void sortByDistance(Location currentLocation,List<LocationDto> locationList) {
        locationList.sort(new Comparator<LocationDto>() {
            @Override
            public int compare(LocationDto loc1, LocationDto loc2) {
                float[] distance1 = new float[1];
                float[] distance2 = new float[1];
                Location.distanceBetween(
                        currentLocation.getLatitude(), currentLocation.getLongitude(),
                        loc1.getLatitude(), loc1.getLongitude(), distance1);
                Location.distanceBetween(
                        currentLocation.getLatitude(), currentLocation.getLongitude(),
                        loc2.getLatitude(), loc2.getLongitude(), distance2);
                return Float.compare(distance1[0], distance2[0]);
            }
        });
    }
}
