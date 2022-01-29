package com.main.wayfinding.fragment.map;

import static com.main.wayfinding.utility.GeoLocationMsgManager.findLocationGeoMsg;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.PlacesApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.AutocompletePrediction;
import com.main.wayfinding.R;
import com.main.wayfinding.WayfindingApp;
import com.main.wayfinding.databinding.FragmentMapBinding;
import com.main.wayfinding.dto.LocationDto;
import com.main.wayfinding.logic.GPSTrackerLogic;
import com.main.wayfinding.logic.NavigationLogic;

import java.io.IOException;

/**
 * Define the fragment used for displaying map and dynamic Sustainable way-finding
 *
 * @author JIA
 * @author Last Modified By JIA
 * @version Revision: 0
 * Date: 2022/1/26 19:50
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap map;
    private FragmentMapBinding binding;
    private GPSTrackerLogic gps;
    private NavigationLogic navigation;
    private LatLng currentLocation;
    private LocationDto targetLocation;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentMapBinding.inflate(getLayoutInflater());
        View root = binding.getRoot();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        
        //bottom sheet
        //BottomSheetBehavior.from().apply
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get current location after clicking the position button
        ImageView position = (ImageView) view.findViewById(R.id.position);
        position.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Location location = gps.getLocation(getActivity());
                // Add a marker in current location and move the camera(for test)
                if (gps.isLocateEnabled()) {
                    currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    map.clear();
                    map.addMarker(new MarkerOptions().position(currentLocation).title("current location"));
                    map.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
                }

            }
        });

        // Do way finding after clicking the navigate button
        ImageView navigate = (ImageView) view.findViewById(R.id.navigate);
        navigate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLocation != null) {
                    navigation.findRoute(currentLocation, new LatLng(53.3706544, -6.3336711));
                }
            }
        });

        EditText searchBox = (EditText) view.findViewById(R.id.input_search);
        searchBox.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                    Location location = gps.getLocation(getActivity());
                    try {
                        AutocompletePrediction[] predictions = PlacesApi.queryAutocomplete(WayfindingApp.getGeoApiContext(), searchBox.getText().toString())
                                .location(new com.google.maps.model.LatLng(location.getLatitude(), location.getLongitude()))
                                .radius(50000)
                                .await();
                        for (AutocompletePrediction prediction : predictions) {
                            // TODO: UI
                        }
                    } catch (ApiException | IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        navigation = new NavigationLogic(googleMap);

        // Create GPS object
        gps = new GPSTrackerLogic(getParentFragment().getContext());
        Location location = gps.getLocation(getActivity());
        // Add a marker in current location and move the camera(for test)
        if (gps.isLocateEnabled()) {
            currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        } else {
            // Add a marker in Dublin and move the camera(for test)
            // LatLng dublin = new LatLng(53, -6);
            currentLocation = new LatLng(53, -6);
        }

        map.addMarker(new MarkerOptions().position(currentLocation).title("current location"));
        map.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));

        // Add map click listener
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                map.clear();
                map.addMarker(new MarkerOptions().position(latLng));
                targetLocation = findLocationGeoMsg(getActivity(), latLng);
                new AlertDialog.Builder(getActivity())
                        .setTitle("Target Place")
                        .setMessage(targetLocation.getName() + "\n" + targetLocation.getAddress())
                        .setPositiveButton("Set", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //Do nothing
                            }
                        })
                        .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //Do nothing
                            }
                        })
                        .show();
;            }
        });

        // Add map marker click listener
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                        new CameraPosition(marker.getPosition(), 15, 0, 0)
                ));
                return true;
            }
        });
    }

}
