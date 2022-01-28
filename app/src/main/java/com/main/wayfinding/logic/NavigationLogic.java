package com.main.wayfinding.logic;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.errors.ApiException;
import com.google.maps.errors.ZeroResultsException;
import com.google.maps.model.DirectionsResult;
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
            map.addPolyline(new PolylineOptions()
                    .clickable(true)
                    .addAll(LatLngConverter.convert(result.routes[0].overviewPolyline.decodePath())));
            com.google.maps.model.LatLng start = result.routes[0].legs[0].startLocation;
            com.google.maps.model.LatLng end = result.routes[0].legs[0].endLocation;
            LatLng pos = new LatLng((start.lat + end.lat) / 2, (start.lng + end.lng) / 2);
            // TODO: how to auto-fit the camera to the route
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 12));
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
            map.clear();
            map.addPolyline(new PolylineOptions()
                    .clickable(true)
                    .addAll(LatLngConverter.convert(result.routes[0].overviewPolyline.decodePath())));
            com.google.maps.model.LatLng start = result.routes[0].legs[0].startLocation;
            com.google.maps.model.LatLng end = result.routes[0].legs[0].endLocation;
            LatLng pos = new LatLng((start.lat + end.lat) / 2, (start.lng + end.lng) / 2);
            // TODO: how to auto-fit the camera to the route
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 12));
            // TODO: customise line style
        } catch (ZeroResultsException e) {
            // TODO: notify users if there are no routes available
        } catch (InterruptedException | ApiException | IOException e) {
            e.printStackTrace();
        }
    }

    private DirectionsApiRequest getDirections(LatLng orig, LatLng dest) {
        return new DirectionsApiRequest(WayfindingApp.getGeoApiContext()).origin(LatLngConverter.convert(orig)).destination(LatLngConverter.convert(dest));
    }
}