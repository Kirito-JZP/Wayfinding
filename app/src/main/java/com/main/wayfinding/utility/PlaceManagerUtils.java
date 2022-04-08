package com.main.wayfinding.utility;

import static com.main.wayfinding.utility.StaticStringUtils.NULL_STRING;

import android.graphics.Color;
import android.location.Location;
import android.util.Log;
import android.util.Pair;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeocodingApiRequest;
import com.google.maps.NearbySearchRequest;
import com.google.maps.PlaceAutocompleteRequest;
import com.google.maps.PlacesApi;
import com.google.maps.RoadsApi;
import com.google.maps.errors.ApiException;
import com.google.maps.errors.ZeroResultsException;
import com.google.maps.model.AddressComponent;
import com.google.maps.model.AutocompletePrediction;
import com.google.maps.model.ComponentFilter;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LocationType;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PlacesSearchResult;
import com.google.maps.model.SnappedPoint;
import com.google.maps.model.TravelMode;
import com.main.wayfinding.WayfindingApp;
import com.main.wayfinding.dto.LocationDto;
import com.main.wayfinding.dto.RouteDto;
import com.main.wayfinding.logic.NavigationLogic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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
                String city = comps.isEmpty() ? NULL_STRING : comps.get(0).longName;
                comps = Arrays.stream(details.addressComponents)
                        .filter(comp -> Arrays.stream(comp.types)
                                .anyMatch(t -> t.toString().equals("country")))
                        .collect(Collectors.toList());
                String country = comps.isEmpty() ? NULL_STRING : comps.get(0).longName;
                comps = Arrays.stream(details.addressComponents)
                        .filter(comp -> Arrays.stream(comp.types)
                                .anyMatch(t -> t.toString().equals("postal_code")))
                        .collect(Collectors.toList());
                String postalCode = comps.isEmpty() ? NULL_STRING : comps.get(0).longName;
                String imageUrl = (details.photos != null && details.photos.length != 0) ?
                        "https://maps.googleapis.com/maps/api/place/photo?photo_reference="
                                + details.photos[0].photoReference
                                + "&maxheight=500&maxwidth=500&key="
                                + WayfindingApp.getKey()
                        : NULL_STRING;
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
            String city = comps.isEmpty() ? NULL_STRING : comps.get(0).longName;
            comps = Arrays.stream(details.addressComponents)
                    .filter(comp -> Arrays.stream(comp.types)
                            .anyMatch(t -> t.toString().equals("country")))
                    .collect(Collectors.toList());
            String country = comps.isEmpty() ? NULL_STRING : comps.get(0).longName;
            comps = Arrays.stream(details.addressComponents)
                    .filter(comp -> Arrays.stream(comp.types)
                            .anyMatch(t -> t.toString().equals("postal_code")))
                    .collect(Collectors.toList());
            String postalCode = comps.isEmpty() ? NULL_STRING : comps.get(0).longName;
            String imageUrl = (details.photos != null && details.photos.length != 0) ?
                    "https://maps.googleapis.com/maps/api/place/photo?photo_reference="
                            + details.photos[0].photoReference
                            + "&maxheight=500&maxwidth=500&key="
                            + WayfindingApp.getKey()
                    : NULL_STRING;
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

    public static ArrayList<LocationDto> getNearby(Location location) {
        ArrayList<LocationDto> rtn = new ArrayList<>();
        try {
            NearbySearchRequest nearbySearchRequest = PlacesApi.nearbySearchQuery(WayfindingApp.getGeoApiContext(),
                    new com.google.maps.model.LatLng(location.getLatitude(), location.getLongitude()));
            nearbySearchRequest.radius(100);
            PlacesSearchResponse placesSearchResponse = nearbySearchRequest.await();
            PlacesSearchResult[] results = placesSearchResponse.results;
            for (PlacesSearchResult result : results) {
                LocationDto locationDto = new LocationDto();
                locationDto.setName(result.name);
                locationDto.setGmPlaceID(result.placeId);
                locationDto.setAddress(result.formattedAddress);
                locationDto.setLatitude(result.geometry.location.lat);
                locationDto.setLongitude(result.geometry.location.lng);
                rtn.add(locationDto);
            }

        } catch (ApiException | InterruptedException | IOException e) {
            e.printStackTrace();
        }

        return rtn;
    }

    public static List<SnappedPoint> nearestRoads(List<LatLng> coordinates) {
        try {
            SnappedPoint[] points = RoadsApi.nearestRoads(WayfindingApp.getGeoApiContext(),
                    LatLngConverterUtils.convertGMS2Map(coordinates).toArray(new com.google.maps.model.LatLng[coordinates.size()])).await();
            return Arrays.asList(points);
        } catch (ApiException | InterruptedException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
