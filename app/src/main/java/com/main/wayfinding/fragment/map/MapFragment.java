package com.main.wayfinding.fragment.map;

import static com.main.wayfinding.utility.GeoLocationMsgManager.findLocationGeoMsg;
import static com.main.wayfinding.utility.LatLngConverter.convert;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.maps.model.PlacesSearchResult;
import com.main.wayfinding.R;
import com.main.wayfinding.adapter.LocationAdapter;
import com.main.wayfinding.databinding.FragmentMapBinding;
import com.main.wayfinding.dto.LocationDto;
import com.main.wayfinding.dto.UserDto;
import com.main.wayfinding.logic.DB.LocationDBLogic;
import com.main.wayfinding.logic.DB.UserDBLogic;
import com.main.wayfinding.logic.GPSTrackerLogic;
import com.main.wayfinding.logic.NavigationLogic;
import com.main.wayfinding.utility.AutocompleteHandler;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Define the fragment used for displaying map and dynamic Sustainable way-finding
 *
 * @author JIA
 * @author Last Modified By JIA
 * @version Revision: 0
 * Date: 2022/2/2 19:50
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

    /**
     * Constant string
     */
    private static final String YOUR_LOCATION = "Your location";

    private GoogleMap map;
    private FragmentMapBinding binding;
    private GPSTrackerLogic gps;
    private NavigationLogic navigation;
    private LocationDto currentLocation;
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
                LocationDto location = new LocationDto();
                location.setName("Test");
                location.setLatitude(253.678);
                location.setLongitude(345.879);
                location.setCity("Dublin");
                location.setCountry("Ireland");
                location.setAddress("19 Robinhood Rd, Robinhood, Dublin, Ireland");
                location.setPostalCode("D22 HW63");
                location.setDate(new Date());
                location.setGmPlaceID("ChIJ3Y7HLZsOZ0gRZ2FxjA3-ACc");
                location.setGmImgUrl("https://maps.googleapis.com/maps/api/place/photo?photo_reference=Aap_uED4R2CIRg3z3FfzI0JXC_hT9_8fUSMeXu6cI7rL3qsYV8tJOJfrEGTxx3xnvRam_SAvzIkgdukmcQcrV3j_DmNfzRkX3VVIPHOmeYVjiWDn_Xc89L69AKC-f4sFch6BQlXYGSJM2wZpFErQnndYTo5JyQwM7aZAMr1WHF3p2OJE1XTz&maxheight=500&maxwidth=500&key=AIzaSyCw22dPUG1-s666qK4gTyemXQXnWEIoqic");

                new LocationDBLogic().insert(location);

            }
        });

        exchangeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Exchange the departure and the destination
                if (currentLocation != null || targetLocation != null) {
                    LocationDto temp = currentLocation;
                    currentLocation = targetLocation;
                    targetLocation = temp;
                    departureText.setText(currentLocation.getName());
                    destinationText.setText(targetLocation.getName());
                }
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
                resetCurrentPosition(location);
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
                navigation.findRoute(convert(currentLocation), convert(location));
                placesListView.setAdapter(null);
                targetLocation = location;
                destinationText.setText(targetLocation.getName());
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
                for (int i = 0; i < locationList.size(); i++) {
                    System.out.println("+++++++++++++++++++" + locationList.get(i).getAddress() + "+++++++++++++++++++++");
                }

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
        resetCurrentPosition(location);

        // Add map click listener
        map.setOnMapClickListener(latLng -> {
            LocationDto locationDto = findLocationGeoMsg(latLng);
            // Only when the location exists in the map, change the maker.
            if (StringUtils.isNotEmpty(locationDto.getName())) {
                map.clear();
                map.addMarker(new MarkerOptions().position(latLng));
                new AlertDialog.Builder(getActivity())
                        .setTitle("Target Place")
                        .setMessage(locationDto.getName() + "\n" + locationDto.getAddress())
                        .setPositiveButton("Set as departure", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                currentLocation = locationDto;
                                departureText.setText(currentLocation.getName());
                            }
                        })
                        .setNegativeButton("Set as destination", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                targetLocation = locationDto;
                                destinationText.setText(targetLocation.getName());
                            }
                        })
                        .setNeutralButton("Close", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //Do nothing
                            }
                        })
                        .show();
            }
        });

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
            PlacesSearchResult[] places = navigation.nearbySearchQuery(keyword, convert(currentLocation));
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

    /**
     * Method for getting and setting current position
     *
     * @param location
     */
    private void resetCurrentPosition(Location location) {
        // Add a marker in current location and move the camera
        currentLocation = new LocationDto();
        currentLocation.setName(YOUR_LOCATION);
        if (gps.isLocateEnabled()) {
            // reset Dto
            currentLocation.setLatitude(location.getLatitude());
            currentLocation.setLongitude(location.getLongitude());
            // reset view text
            departureText.setText(currentLocation.getName());
        } else {
            // reset Dto
            currentLocation.setLatitude(53);
            currentLocation.setLongitude(-6);
        }
        // reset map
        map.clear();
        map.addMarker(new MarkerOptions().position(convert(currentLocation))
                .title("current location"));
        map.moveCamera(CameraUpdateFactory.newLatLng(convert(currentLocation)));
    }

}
