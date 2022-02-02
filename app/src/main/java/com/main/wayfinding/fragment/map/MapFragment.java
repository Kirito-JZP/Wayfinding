package com.main.wayfinding.fragment.map;

import static com.main.wayfinding.utility.GeoLocationMsgManager.findLocationGeoMsg;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.model.PlacesSearchResult;
import com.main.wayfinding.R;
import com.main.wayfinding.adapter.LocationAdapter;
import com.main.wayfinding.databinding.FragmentMapBinding;
import com.main.wayfinding.dto.LocationDto;
import com.main.wayfinding.logic.GPSTrackerLogic;
import com.main.wayfinding.logic.NavigationLogic;
import com.main.wayfinding.utility.AutocompleteHandler;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Define the fragment used for displaying map and dynamic Sustainable way-finding
 *
 * @author JIA
 * @author Last Modified By JIA
 * @version Revision: 0
 * Date: 2022/2/2 19:50
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap map;
    private FragmentMapBinding binding;
    private GPSTrackerLogic gps;
    private NavigationLogic navigation;
    private LatLng currentLocation;
    private LocationDto targetLocation;
    private List<LocationDto> locationList;
    private String keyword;
    private AutocompleteHandler autocompleteHandler;
    private Handler UIHandler;
    private int autocompleteDelay = 500;

    // Components
    private EditText departureText;
    private EditText destinationText;
    private ImageView addImage;
    private ImageView exchangeImage;
    private ImageView publicImage;
    private ImageView walkImage;
    private ImageView cyclingImage;
    private ImageView navigate;
    private ImageView position;
    private ListView placesListView;
    private ScrollView autocompleteScrollView;
    private RelativeLayout rootLayout;

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

        // Map
        rootLayout = view.findViewById(R.id.map_root_layout);

        // TextView
        departureText = view.findViewById(R.id.input_start);
        destinationText = view.findViewById(R.id.input_search);

        // ImageView
        addImage = view.findViewById(R.id.add);
        exchangeImage = view.findViewById(R.id.exchange);
        publicImage = view.findViewById(R.id.public_img);
        walkImage = view.findViewById(R.id.walk_img);
        cyclingImage = view.findViewById(R.id.cycling_img);
        navigate = view.findViewById(R.id.navigate);
        position = view.findViewById(R.id.position);

        // ListView
        placesListView = view.findViewById(R.id.places_listview);
        autocompleteScrollView = view.findViewById(R.id.autocomplete_scrollview);

        autocompleteHandler = new AutocompleteHandler();
        autocompleteHandler.setFragment(this);
        UIHandler = new Handler();
        locationList = new ArrayList<>();

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO
            }
        });

        exchangeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO
            }
        });

        publicImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO
            }
        });

        walkImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO
            }
        });

        cyclingImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO
            }
        });

        // Get current location after clicking the position button
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
        navigate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO
            }
        });

        // listener for delayed trigger
        destinationText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                keyword = charSequence.toString().trim();
                if (autocompleteHandler.hasMessages(AutocompleteHandler.TRIGGER_MSG)) {
                    autocompleteHandler.removeMessages(AutocompleteHandler.TRIGGER_MSG);
                }
                Message msg = new Message();
                msg.what = AutocompleteHandler.TRIGGER_MSG;
                autocompleteHandler.sendMessageDelayed(msg, autocompleteDelay);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        // click on a place in the candidate places
        placesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                LocationDto location = locationList.get(i);
                navigation.findRoute(currentLocation, new LatLng(location.getLatitude(), location.getLongitude()));
                placesListView.setAdapter(null);
                destinationText.setText(location.getName());
                // hide the soft keyboard after clicking on an item
                InputMethodManager manager = ((InputMethodManager) getContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE));
                if (manager != null)
                    manager.hideSoftInputFromWindow(getView().findFocus().getWindowToken(), 0);
                destinationText.clearFocus();
            }
        });

        // listener to change the visibility of the places list
        destinationText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    placesListView.setAdapter(new LocationAdapter(getContext(),
                            R.layout.autocomplete_location_item, locationList));
                    autocompleteScrollView.setVisibility(View.VISIBLE);
                } else {
                    placesListView.setAdapter(null);
                    autocompleteScrollView.setVisibility(View.INVISIBLE);
                }
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
        map.setOnMapClickListener(latLng -> {
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
;            });

        // Add map marker click listener
        map.setOnMarkerClickListener(marker -> {
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                    new CameraPosition(marker.getPosition(), 15, 0, 0)
            ));
            return true;
        });
    }

    public void queryAutocomplete() {
        new Thread(() -> {
            PlacesSearchResult[] places = navigation.nearbySearchQuery(keyword, currentLocation);
            locationList.clear();
            for (PlacesSearchResult place : places) {
                locationList.add(new LocationDto()
                        .setGmPlaceID(place.placeId)
                        .setName(place.name)
                        .setAddress(place.vicinity)
                        .setLongitude(place.geometry.location.lng)
                        .setLatitude(place.geometry.location.lat)
                );
            }
            // pass the results to original thread so that UI elements can be updated
            UIHandler.post(() -> placesListView.setAdapter(new LocationAdapter(getContext(),
                    R.layout.autocomplete_location_item, locationList)));
        }).start();
    }
}
