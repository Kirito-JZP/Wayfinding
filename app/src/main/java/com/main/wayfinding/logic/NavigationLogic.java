package com.main.wayfinding.logic;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.NearbySearchRequest;
import com.google.maps.PlacesApi;
import com.google.maps.errors.ApiException;
import com.google.maps.errors.ZeroResultsException;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PlacesSearchResult;
import com.main.wayfinding.WayfindingApp;
import com.main.wayfinding.utility.LatLngConverter;

import java.io.IOException;

/**
 * Logic of Navigation
 *
 * @author jia72
 * @author Last Modified By jia72
 * @version Revision: 0
 * Date: 2022/1/28 17:18
 */
public class NavigationLogic {
    private final GoogleMap map;

    public NavigationLogic(GoogleMap map) {
        this.map = map;
    }

    public void findRoute(String orig, String dest) {
        try {
            DirectionsResult result = DirectionsApi.getDirections(WayfindingApp.getGeoApiContext(), orig, dest).await();
            // TODO: there can be more than one route
            double max_lat = result.routes[0].bounds.northeast.lat;
            double min_lat = result.routes[0].bounds.southwest.lat;
            double max_lng = result.routes[0].bounds.northeast.lng;
            double min_lng = result.routes[0].bounds.southwest.lng;
            for (DirectionsRoute route : result.routes) {
                map.addPolyline(new PolylineOptions()
                        .clickable(true)
                        .addAll(LatLngConverter.convert(route.overviewPolyline.decodePath())));
                max_lat = Math.max(max_lat, route.bounds.northeast.lat);
                min_lat = Math.min(min_lat, route.bounds.southwest.lat);
                max_lng = Math.max(max_lng, route.bounds.northeast.lng);
                min_lng = Math.min(min_lng, route.bounds.southwest.lng);
            }
            LatLngBounds bounds = new LatLngBounds(new LatLng(min_lat, min_lng), new LatLng(max_lat, max_lng));
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
            // TODO: customise line style
        } catch (ZeroResultsException e) {
            // TODO: notify users if there are no routes available
        } catch (InterruptedException | ApiException | IOException e) {
            e.printStackTrace();
        }
    }

    public void findRoute(LatLng orig, LatLng dest) {
        try {
            DirectionsResult result = getDirections(orig, dest).await();
            // TODO: there can be more than one route
            double max_lat = result.routes[0].bounds.northeast.lat;
            double min_lat = result.routes[0].bounds.southwest.lat;
            double max_lng = result.routes[0].bounds.northeast.lng;
            double min_lng = result.routes[0].bounds.southwest.lng;
            for (DirectionsRoute route : result.routes) {
                map.addPolyline(new PolylineOptions()
                        .clickable(true)
                        .addAll(LatLngConverter.convert(route.overviewPolyline.decodePath())));
                max_lat = Math.max(max_lat, route.bounds.northeast.lat);
                min_lat = Math.min(min_lat, route.bounds.southwest.lat);
                max_lng = Math.max(max_lng, route.bounds.northeast.lng);
                min_lng = Math.min(min_lng, route.bounds.southwest.lng);
            }
            LatLngBounds bounds = new LatLngBounds(new LatLng(min_lat, min_lng), new LatLng(max_lat, max_lng));
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
            // TODO: customise line style
        } catch (ZeroResultsException e) {
            // TODO: notify users if there are no routes available
        } catch (InterruptedException | ApiException | IOException e) {
            e.printStackTrace();
        }
    }

    public LatLng queryLatLng(String placeID) {
        try {
            PlaceDetails detail = PlacesApi.placeDetails(WayfindingApp.getGeoApiContext(), placeID).await();
            return LatLngConverter.convert(detail.geometry.location);
        } catch (ApiException | InterruptedException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public PlacesSearchResult[] nearbySearchQuery(String keyword, LatLng location) {
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

    public PlacesSearchResponse nearbySearchQuery(String keyword, LatLng location, int radius) {
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

    private DirectionsApiRequest getDirections(LatLng orig, LatLng dest) {
        return new DirectionsApiRequest(WayfindingApp.getGeoApiContext()).origin(LatLngConverter.convert(orig)).destination(LatLngConverter.convert(dest));
    }
}