package com.main.wayfinding.fragment.map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.main.wayfinding.R;
import com.main.wayfinding.databinding.FragmentMapBinding;

/**
 * Define the fragment used for displaying map and dynamic Sustainable way-finding
 *
 * @author JIA
 * @author Last Modified By JIA
 * @version Revision: 0
 * Date: 2022/1/16 13:50
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap map;
    private FragmentMapBinding binding;

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
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;

        // Add a marker in Dublin and move the camera(for test)
        LatLng dublin = new LatLng(53, -6);
        map.addMarker(new MarkerOptions().position(dublin).title("Marker in dublin"));
        map.moveCamera(CameraUpdateFactory.newLatLng(dublin));

        // TODO
    }

}
