package com.main.wayfinding.utility;


import android.util.Log;
import android.util.Pair;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.FindPlaceFromTextRequest;
import com.google.maps.GeocodingApi;
import com.google.maps.GeocodingApiRequest;
import com.google.maps.NearbySearchRequest;
import com.google.maps.PlaceAutocompleteRequest;
import com.google.maps.PlacesApi;
import com.google.maps.errors.ApiException;
import com.google.maps.errors.ZeroResultsException;
import com.google.maps.model.AddressComponent;
import com.google.maps.model.AutocompletePrediction;
import com.google.maps.model.ComponentFilter;
import com.google.maps.model.DirectionsLeg;
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
import com.main.wayfinding.dto.RouteDto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
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

    private static PlaceAutocompleteRequest.SessionToken autocompleteSessionToken;
    private static boolean needNewSession = true;

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
        request.latlng(LatLngConverterUtils.convert(latlng));
        return request;
    }

    public static List<LocationDto> autocompletePlaces(String keyword, LatLng location) {
        Log.d("[TEST]", "Here!");
        List<LocationDto> locations = new ArrayList<>();
        try {
            if (needNewSession) {
                autocompleteSessionToken = new PlaceAutocompleteRequest.SessionToken();
                needNewSession = false;
            }
            PlaceAutocompleteRequest request =
                    PlacesApi.placeAutocomplete(WayfindingApp.getGeoApiContext(), keyword,
                            autocompleteSessionToken);
            request.components(ComponentFilter.country("ie"))
                    .origin(LatLngConverterUtils.convert(location))
                    .location(LatLngConverterUtils.convert(location));
            List<AutocompletePrediction> predictions = Arrays.asList(request.await());
            Comparator<AutocompletePrediction> compareByDistance =
                    Comparator.comparingInt(p -> p.distanceMeters != null ? p.distanceMeters : 0);  // some predictions have no distance data, in such cases, use 0 for comparison
            Comparator<AutocompletePrediction> compareByMatchedLength =
                    Comparator.comparingInt(p -> p.matchedSubstrings[0].length);
            predictions.sort(compareByMatchedLength.thenComparing(compareByDistance));
            for (AutocompletePrediction prediction : predictions) {
                LocationDto loc = new LocationDto();
                loc.setGmPlaceID(prediction.placeId);
                loc.setName(prediction.structuredFormatting.mainText);
                loc.setAddress(prediction.description);
                locations.add(loc);
            }
            return locations;
        } catch (Exception e) {
            e.printStackTrace();
            return locations;
        }
    }

    public static Pair<List<RouteDto>, LatLngBounds> findRoute(
            LocationDto startLocDto, LocationDto targetLocDto, String mode) {
        if (startLocDto != null && targetLocDto != null) {
            LatLng orig = startLocDto.getLatLng();
            LatLng dest = targetLocDto.getLatLng();
            if (orig != null && dest != null) {
                List<RouteDto> routes = new ArrayList<>();
                RouteDto route = new RouteDto();
                try {
                    PlaceManagerUtils.map.clear();
                    DirectionsResult result = getDirections(orig, dest, mode).await();
                    double max_lat = result.routes[0].bounds.northeast.lat;
                    double min_lat = result.routes[0].bounds.southwest.lat;
                    double max_lng = result.routes[0].bounds.northeast.lng;
                    double min_lng = result.routes[0].bounds.southwest.lng;
                    for (DirectionsRoute r : result.routes) {
                        max_lat = Math.max(max_lat, r.bounds.northeast.lat);
                        min_lat = Math.min(min_lat, r.bounds.southwest.lat);
                        max_lng = Math.max(max_lng, r.bounds.northeast.lng);
                        min_lng = Math.min(min_lng, r.bounds.southwest.lng);
                        // add start location and end location
                        LocationDto startLocation = new LocationDto();
                        LocationDto endLocation = new LocationDto();
                        startLocation.setLatitude(r.legs[0].startLocation.lat);
                        startLocation.setLongitude(r.legs[0].startLocation.lng);
                        endLocation.setLatitude(r.legs[r.legs.length - 1].endLocation.lat);
                        endLocation.setLongitude(r.legs[r.legs.length - 1].endLocation.lng);
                        route.setStartLocation(startLocation);
                        route.setEndLocation(endLocation);
                        // save polyline options
                        route.setPolylineOptions(new PolylineOptions()
                                .clickable(true)
                                .addAll(LatLngConverterUtils.convert(r.overviewPolyline.decodePath())));
                        routes.add(route);
                    }
                    LatLngBounds bounds = new LatLngBounds(new LatLng(min_lat, min_lng),
                            new LatLng(max_lat, max_lng));
                    // TODO: customise line style
                    return new Pair<>(routes, bounds);
                } catch (ZeroResultsException e) {
                    // TODO: notify users if there are no routes available
                    return null;
                } catch (InterruptedException | ApiException | IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }
        return null;
    }

    public static LatLng queryLatLng(String placeID) {
        try {
            GeocodingApiRequest request = new GeocodingApiRequest(WayfindingApp.getGeoApiContext());
            request.place(placeID);
            GeocodingResult[] results = request.await();
            return LatLngConverterUtils.convert(results[0].geometry.location);
        } catch (ApiException | InterruptedException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static LocationDto queryDetail(String placeID) {
        // end the autocomplete session otherwise it produces extra cost
        if (!needNewSession) {
            needNewSession = true;
            autocompleteSessionToken = null;
        }
        try {
            PlaceDetails details = PlacesApi.placeDetails(WayfindingApp.getGeoApiContext(),
                    placeID).await();
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
        request.location(LatLngConverterUtils.convert(location));
        request.radius(5000);
        try {
            return request.await().results;
        } catch (ApiException | InterruptedException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static PlacesSearchResponse nearbySearchQuery(String keyword, LatLng location,
                                                         int radius) {
        NearbySearchRequest request = new NearbySearchRequest(WayfindingApp.getGeoApiContext());
        request.keyword(keyword);
        request.location(LatLngConverterUtils.convert(location));
        request.radius(radius);
        try {
            return request.await();
        } catch (ApiException | InterruptedException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static DirectionsApiRequest getDirections(LatLng orig, LatLng dest) {
        return new DirectionsApiRequest(WayfindingApp.getGeoApiContext()).origin(LatLngConverterUtils.convert(orig)).destination(LatLngConverterUtils.convert(dest));
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
        return new DirectionsApiRequest(WayfindingApp.getGeoApiContext()).origin(LatLngConverterUtils.convert(orig)).destination(LatLngConverterUtils.convert(dest)).mode(travelMode);
    }
}
