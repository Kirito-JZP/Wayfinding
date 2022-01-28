package com.main.wayfinding.fragment.map;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.main.wayfinding.R;
import com.main.wayfinding.databinding.FragmentMapBinding;
import com.main.wayfinding.logic.NavigationLogic;
import com.main.wayfinding.utility.GPSTracker;

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
    private GPSTracker gps;
    private NavigationLogic navigation;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentMapBinding.inflate(getLayoutInflater());
        View root = binding.getRoot();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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
                // Create GPS object
                assert getParentFragment() != null;
                gps = new GPSTracker(getParentFragment().getContext());
                Location location = gps.getLocation(getActivity());
                // Add a marker in current location and move the camera(for test)
                if (gps.isLocateEnabled()) {
                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
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
                if (gps.isLocateEnabled()) {
                    Location location = gps.getLocation(getActivity());
                    navigation.findRoute(new LatLng(location.getLatitude(), location.getLongitude()), new LatLng(53.3706544, -6.3336711));
                }
            }
        });

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        navigation = new NavigationLogic(googleMap);

        // Add a marker in Dublin and move the camera(for test)
//        LatLng dublin = new LatLng(53, -6);
        LatLng dublin = new LatLng(53, -3);
        map.addMarker(new MarkerOptions().position(dublin).title("Marker in dublin"));
        map.moveCamera(CameraUpdateFactory.newLatLng(dublin));

        // TODO
    }

}
