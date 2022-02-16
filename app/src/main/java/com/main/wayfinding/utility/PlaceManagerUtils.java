package com.main.wayfinding.utility;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeocodingApiRequest;
import com.google.maps.NearbySearchRequest;
import com.google.maps.PlacesApi;
import com.google.maps.errors.ApiException;
import com.google.maps.errors.ZeroResultsException;
import com.google.maps.model.AddressComponent;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LocationType;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PlacesSearchResult;
import com.google.maps.model.TravelMode;
import com.main.wayfinding.WayfindingApp;
import com.main.wayfinding.dto.LocationDto;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class for location information management.
 *
 * @author JIA
 * @author Last Modified By JIA
 * @version Revision: 0
 * Date: 2022/1/29 1:01
 */
public class PlaceManagerUtils {
    private static GoogleMap map;

    public static void SetMap(GoogleMap map) {
        PlaceManagerUtils.map = map;
    }

    public static LocationDto findLocationGeoMsg(LatLng latLng) {
        LocationDto result = new LocationDto();
        try {
            GeocodingResult[] results = reverseGeocode(latLng).await();
            if (results.length > 0) {
                String placeId = results[0].placeId;
                PlaceDetails details = PlacesApi.placeDetails(
                        WayfindingApp.getGeoApiContext(), placeId
                ).await();
                List<AddressComponent> comps = Arrays.stream(details.addressComponents)
                        .filter(comp -> Arrays.stream(comp.types)
                                .anyMatch(t -> t.toString().equals("administrative_area_level_1"))
                        ).collect(Collectors.toList());
                String city = comps.isEmpty() ? "" : comps.get(0).longName;
                comps = Arrays.stream(details.addressComponents)
                        .filter(comp -> Arrays.stream(comp.types)
                                .anyMatch(t -> t.toString().equals("country")))
                        .collect(Collectors.toList());
                String country = comps.isEmpty() ? "" : comps.get(0).longName;
                comps = Arrays.stream(details.addressComponents)
                        .filter(comp -> Arrays.stream(comp.types)
                                .anyMatch(t -> t.toString().equals("postal_code")))
                        .collect(Collectors.toList());
                String postalCode = comps.isEmpty() ? "" : comps.get(0).longName;
                String imageUrl = (details.photos != null && details.photos.length != 0) ?
                        "https://maps.googleapis.com/maps/api/place/photo?photo_reference="
                                + details.photos[0].photoReference
                                + "&maxheight=500&maxwidth=500&key="
                                + WayfindingApp.getKey()
                        : "";
                result.setName(details.name);
                result.setAddress(details.formattedAddress);
                result.setLatitude(details.geometry.location.lat);
                result.setLongitude(details.geometry.location.lng);
                result.setCity(city);
                result.setCountry(country);
                result.setPostalCode(postalCode);
                result.setGmImgUrl(imageUrl);
                result.setGmPlaceID(details.placeId);
            }
        } catch (IOException | InterruptedException | ApiException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static GeocodingApiRequest reverseGeocode(LatLng latlng) {
        GeocodingApiRequest request = new GeocodingApiRequest(WayfindingApp.getGeoApiContext());
        request.locationType(LocationType.ROOFTOP);
        request.latlng(LatLngConverter.convert(latlng));
        return request;
    }

    public static void findRoute(String orig, String dest) {
        try {
            DirectionsResult result = DirectionsApi.getDirections(WayfindingApp.getGeoApiContext(), orig, dest).await();
            // TODO: there can be more than one route
            double max_lat = result.routes[0].bounds.northeast.lat;
            double min_lat = result.routes[0].bounds.southwest.lat;
            double max_lng = result.routes[0].bounds.northeast.lng;
            double min_lng = result.routes[0].bounds.southwest.lng;
            for (DirectionsRoute route : result.routes) {
                PlaceManagerUtils.map.addPolyline(new PolylineOptions()
                        .clickable(true)
                        .addAll(LatLngConverter.convert(route.overviewPolyline.decodePath())));
                max_lat = Math.max(max_lat, route.bounds.northeast.lat);
                min_lat = Math.min(min_lat, route.bounds.southwest.lat);
                max_lng = Math.max(max_lng, route.bounds.northeast.lng);
                min_lng = Math.min(min_lng, route.bounds.southwest.lng);
            }
            LatLngBounds bounds = new LatLngBounds(new LatLng(min_lat, min_lng), new LatLng(max_lat, max_lng));
            PlaceManagerUtils.map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
            // TODO: customise line style
        } catch (ZeroResultsException e) {
            // TODO: notify users if there are no routes available
        } catch (InterruptedException | ApiException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void findRoute(LatLng orig, LatLng dest) {
        try {
            PlaceManagerUtils.map.clear();
            DirectionsResult result = getDirections(orig, dest).await();
            // TODO: there can be more than one route
            double max_lat = result.routes[0].bounds.northeast.lat;
            double min_lat = result.routes[0].bounds.southwest.lat;
            double max_lng = result.routes[0].bounds.northeast.lng;
            double min_lng = result.routes[0].bounds.southwest.lng;
            for (DirectionsRoute route : result.routes) {
                PlaceManagerUtils.map.addPolyline(new PolylineOptions()
                        .clickable(true)
                        .addAll(LatLngConverter.convert(route.overviewPolyline.decodePath())));
                max_lat = Math.max(max_lat, route.bounds.northeast.lat);
                min_lat = Math.min(min_lat, route.bounds.southwest.lat);
                max_lng = Math.max(max_lng, route.bounds.northeast.lng);
                min_lng = Math.min(min_lng, route.bounds.southwest.lng);
            }
            LatLngBounds bounds = new LatLngBounds(new LatLng(min_lat, min_lng), new LatLng(max_lat, max_lng));
            PlaceManagerUtils.map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
            // TODO: customise line style
        } catch (ZeroResultsException e) {
            // TODO: notify users if there are no routes available
        } catch (InterruptedException | ApiException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void findRoute(LatLng orig, LatLng dest, String mode) {
        try {
            PlaceManagerUtils.map.clear();
            DirectionsResult result = getDirections(orig, dest, mode).await();
            // TODO: there can be more than one route
            double max_lat = result.routes[0].bounds.northeast.lat;
            double min_lat = result.routes[0].bounds.southwest.lat;
            double max_lng = result.routes[0].bounds.northeast.lng;
            double min_lng = result.routes[0].bounds.southwest.lng;
            for (DirectionsRoute route : result.routes) {
                PlaceManagerUtils.map.addPolyline(new PolylineOptions()
                        .clickable(true)
                        .addAll(LatLngConverter.convert(route.overviewPolyline.decodePath())));
                max_lat = Math.max(max_lat, route.bounds.northeast.lat);
                min_lat = Math.min(min_lat, route.bounds.southwest.lat);
                max_lng = Math.max(max_lng, route.bounds.northeast.lng);
                min_lng = Math.min(min_lng, route.bounds.southwest.lng);
            }
            LatLngBounds bounds = new LatLngBounds(new LatLng(min_lat, min_lng), new LatLng(max_lat, max_lng));
            PlaceManagerUtils.map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
            // TODO: customise line style
        } catch (ZeroResultsException e) {
            // TODO: notify users if there are no routes available
        } catch (InterruptedException | ApiException | IOException e) {
            e.printStackTrace();
        }
    }

    public static LatLng queryLatLng(String placeID) {
        try {
            PlaceDetails detail = PlacesApi.placeDetails(WayfindingApp.getGeoApiContext(), placeID).await();
            return LatLngConverter.convert(detail.geometry.location);
        } catch (ApiException | InterruptedException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static LocationDto queryDetail(String placeID) {
        try {
            PlaceDetails details = PlacesApi.placeDetails(WayfindingApp.getGeoApiContext(), placeID).await();
            LocationDto location = new LocationDto();
            List<AddressComponent> comps = Arrays.stream(details.addressComponents)
                    .filter(comp -> Arrays.stream(comp.types)
                            .anyMatch(t -> t.toString().equals("administrative_area_level_1"))
                    ).collect(Collectors.toList());
            String city = comps.isEmpty() ? "" : comps.get(0).longName;
            comps = Arrays.stream(details.addressComponents)
                    .filter(comp -> Arrays.stream(comp.types)
                            .anyMatch(t -> t.toString().equals("country")))
                    .collect(Collectors.toList());
            String country = comps.isEmpty() ? "" : comps.get(0).longName;
            comps = Arrays.stream(details.addressComponents)
                    .filter(comp -> Arrays.stream(comp.types)
                            .anyMatch(t -> t.toString().equals("postal_code")))
                    .collect(Collectors.toList());
            String postalCode = comps.isEmpty() ? "" : comps.get(0).longName;
            String imageUrl = (details.photos != null && details.photos.length != 0) ?
                    "https://maps.googleapis.com/maps/api/place/photo?photo_reference="
                            + details.photos[0].photoReference
                            + "&maxheight=500&maxwidth=500&key="
                            + WayfindingApp.getKey()
                    : "";
            location.setName(details.name);
            location.setAddress(details.formattedAddress);
            location.setLatitude(details.geometry.location.lat);
            location.setLongitude(details.geometry.location.lng);
            location.setCity(city);
            location.setCountry(country);
            location.setPostalCode(postalCode);
            location.setGmImgUrl(imageUrl);
            location.setGmPlaceID(details.placeId);
            return location;
        } catch (ApiException | InterruptedException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static PlacesSearchResult[] nearbySearchQuery(String keyword, LatLng location) {
        NearbySearchRequest request = new NearbySearchRequest(WayfindingApp.getGeoApiContext());
        request.keyword(keyword);
        request.location(LatLngConverter.convert(location));
        request.radius(5000);
        try {
            return request.await().results;
        } catch (ApiException | InterruptedException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static PlacesSearchResponse nearbySearchQuery(String keyword, LatLng location, int radius) {
        NearbySearchRequest request = new NearbySearchRequest(WayfindingApp.getGeoApiContext());
        request.keyword(keyword);
        request.location(LatLngConverter.convert(location));
        request.radius(radius);
        try {
            return request.await();
        } catch (ApiException | InterruptedException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static DirectionsApiRequest getDirections(LatLng orig, LatLng dest) {
        return new DirectionsApiRequest(WayfindingApp.getGeoApiContext()).origin(LatLngConverter.convert(orig)).destination(LatLngConverter.convert(dest));
    }

    private static DirectionsApiRequest getDirections(LatLng orig, LatLng dest, String mode) {
        TravelMode travelMode = TravelMode.UNKNOWN;
        switch (mode) {
            case "walking":
                travelMode = TravelMode.WALKING;
                break;
            case "driving":
                travelMode = TravelMode.DRIVING;
                break;
            case "bicycling":
                travelMode = TravelMode.BICYCLING;
                break;
            case "transit":
                travelMode = TravelMode.TRANSIT;
                break;
            default:
                travelMode = TravelMode.WALKING;
        }
        return new DirectionsApiRequest(WayfindingApp.getGeoApiContext()).origin(LatLngConverter.convert(orig)).destination(LatLngConverter.convert(dest)).mode(travelMode);
    }
}
