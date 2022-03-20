package com.main.wayfinding.fragment.map;

import static com.main.wayfinding.utility.PlaceManagerUtils.findLocationGeoMsg;
import static com.main.wayfinding.utility.LatLngConverterUtils.convert;
import static com.main.wayfinding.utility.PlaceManagerUtils.queryDetail;
import android.content.Context;
import android.content.Intent;
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
import android.widget.ListAdapter;
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
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.maps.model.PlacesSearchResult;
import com.main.wayfinding.ARNavigationActivity;
import com.main.wayfinding.R;
import com.main.wayfinding.adapter.LocationAdapter;
import com.main.wayfinding.databinding.FragmentMapBinding;
import com.main.wayfinding.dto.LocationDto;
import com.main.wayfinding.logic.DB.LocationDBLogic;
import com.main.wayfinding.logic.GPSTrackerLogic;
import com.main.wayfinding.logic.NavigationLogic;
import com.main.wayfinding.utility.AutoCompleteUtils;
import android.widget.FrameLayout;
import android.widget.TextView;
import org.apache.commons.lang3.StringUtils;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.main.wayfinding.utility.PlaceManagerUtils;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javadz.beanutils.BeanUtils;

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
    private LocationDto startLocation;
    private LocationDto targetLocation;
    private List<LocationDto> destLocationList;
    private List<LocationDto> deptLocationList;
    private String destinationKeyword;
    private String departureKeyword;
    private RelativeLayout setDeparture;
    private RelativeLayout setDestination;
    private FrameLayout bottomsheet;
    private AutoCompleteUtils autoCompleteUtils;
    private Handler UIHandler;
    private int autocompleteDelay = 500;
    private String mode = "walking";
    private RelativeLayout publicBtn;
    private RelativeLayout walkBtn;
    private RelativeLayout cycBtn;
    // Components
    private EditText departureText;
    private EditText destinationText;
    private ImageView addImage;
    private ImageView exchangeImage;
    private ImageView arBtn;
    private ImageView navigate;
    private ImageView position;
    private ImageView departureTextClear;
    private ImageView destinationTextClear;
    private ImageView locationImg;
    private TextView selectLocationName;
    private TextView selectLocationDetail;
    private ListView destPlacesListView;
    private ListView deptPlacesListView;
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
        publicBtn = view.findViewById(R.id.public_btn);
        walkBtn = view.findViewById(R.id.walk_btn);
        cycBtn = view.findViewById(R.id.cyc_btn);
        navigate = view.findViewById(R.id.navigate);
        position = view.findViewById(R.id.position);
        departureTextClear = view.findViewById(R.id.clear_departure);
        destinationTextClear = view.findViewById(R.id.clear_destination);
        selectLocationName = view.findViewById(R.id.click_location_name);
        selectLocationDetail = view.findViewById(R.id.click_location_detail);
        setDeparture = view.findViewById(R.id.set_departure_btn);
        setDestination = view.findViewById(R.id.set_destination_btn);
        locationImg = view.findViewById(R.id.location_img);
        arBtn = view.findViewById(R.id.arBtn);
        //bottom sheet
        bottomsheet = view.findViewById(R.id.bottomsheet);

        // ListView
        destPlacesListView = view.findViewById(R.id.dest_places_listview);
        deptPlacesListView = view.findViewById(R.id.dept_places_listview);
//        destScrollView = view.findViewById(R.id.dest_scrollview);
//        deptScrollView = view.findViewById(R.id.dept_scrollview);

        autoCompleteUtils = new AutoCompleteUtils();
        autoCompleteUtils.setFragment(this);
        UIHandler = new Handler();
        destLocationList = new ArrayList<>();
        deptLocationList = new ArrayList<>();

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                targetLocation.setDate(new Date());
                new LocationDBLogic().insert(targetLocation);
            }
        });
        arBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getActivity(), ARNavigationActivity.class);
                startActivity(intent);
            }
        });
        exchangeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Exchange the departure and the destination
                if (startLocation != null || targetLocation != null) {
                    // Exchange content in dto
                    LocationDto temp = startLocation;
                    startLocation = targetLocation;
                    targetLocation = temp;

                    // Exchange message in TextView
                    if (startLocation != null) {
                        departureText.setText(startLocation.getName());
                    } else {
                        departureText.setText("");
                    }
                    if (targetLocation != null) {
                        destinationText.setText(targetLocation.getName());
                    } else {
                        destinationText.setText("");
                    }
                }
                if (startLocation != null && targetLocation != null && StringUtils.isNotEmpty(mode)) {
                    PlaceManagerUtils.findRoute(convert(startLocation), convert(targetLocation),
                            mode);
                }
            }
        });

        departureTextClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLocation = new LocationDto();
                ;
                departureText.setText("");
            }
        });

        destinationTextClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                targetLocation = new LocationDto();
                destinationText.setText("");
            }
        });

        publicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mode = "transit";
                if (startLocation != null && targetLocation != null) {
                    PlaceManagerUtils.findRoute(
                            convert(startLocation),
                            convert(targetLocation),
                            mode.equals("") ? "walking" : mode
                    );
                }
            }
        });

        walkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mode = "walking";
                if (startLocation != null && targetLocation != null) {
                    PlaceManagerUtils.findRoute(
                            convert(startLocation),
                            convert(targetLocation),
                            mode.equals("") ? "walking" : mode
                    );
                }
            }
        });

        cycBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mode = "bicycling";
                if (startLocation != null && targetLocation != null) {
                    PlaceManagerUtils.findRoute(
                            convert(startLocation),
                            convert(targetLocation),
                            mode.equals("") ? "walking" : mode
                    );
                }
            }
        });

        // Get current location after clicking the position button
        position.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Location location = gps.getLocation(getActivity());
                resetCurrentPosition(location);
                if (startLocation != null && targetLocation != null && StringUtils.isNotEmpty(mode)) {
                    PlaceManagerUtils.findRoute(convert(startLocation), convert(targetLocation),
                            mode);
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
                if (getView().findFocus() == destinationText) {
                    destinationKeyword = charSequence.toString().trim();
                    if (autoCompleteUtils.hasMessages(AutoCompleteUtils.TRIGGER_DEST_MSG)) {
                        autoCompleteUtils.removeMessages(AutoCompleteUtils.TRIGGER_DEST_MSG);
                    }
                    Message msg = new Message();
                    msg.what = AutoCompleteUtils.TRIGGER_DEST_MSG;
                    autoCompleteUtils.sendMessageDelayed(msg, autocompleteDelay);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        departureText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (getView().findFocus() == departureText) {
                    departureKeyword = charSequence.toString().trim();
                    if (autoCompleteUtils.hasMessages(AutoCompleteUtils.TRIGGER_DEPT_MSG)) {
                        autoCompleteUtils.removeMessages(AutoCompleteUtils.TRIGGER_DEPT_MSG);
                    }
                    Message msg = new Message();
                    msg.what = AutoCompleteUtils.TRIGGER_DEPT_MSG;
                    autoCompleteUtils.sendMessageDelayed(msg, autocompleteDelay);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        // click on a place in the candidate places
        destPlacesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                LocationDto location = destLocationList.get(i);
                destPlacesListView.setAdapter(null);
                targetLocation = location;
                destinationText.setText(targetLocation.getName());
                // hide the soft keyboard after clicking on an item
                InputMethodManager manager = ((InputMethodManager) getContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE));
                if (manager != null)
                    manager.hideSoftInputFromWindow(getView().findFocus().getWindowToken(), 0);
                destinationText.clearFocus();
                if (startLocation != null && targetLocation != null && StringUtils.isNotEmpty(mode)) {
                    PlaceManagerUtils.findRoute(convert(startLocation), convert(targetLocation),
                            mode);
                }
            }
        });

        deptPlacesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                LocationDto location = deptLocationList.get(i);
                deptPlacesListView.setAdapter(null);
                startLocation = location;
                departureText.setText(startLocation.getName());
                // hide the soft keyboard after clicking on an item
                InputMethodManager manager = ((InputMethodManager) getContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE));
                if (manager != null)
                    manager.hideSoftInputFromWindow(getView().findFocus().getWindowToken(), 0);
                departureText.clearFocus();
                if (startLocation != null && targetLocation != null && StringUtils.isNotEmpty(mode)) {
                    PlaceManagerUtils.findRoute(convert(startLocation), convert(targetLocation),
                            mode);
                }

            }
        });

        // listener to change the visibility of the places list
        destinationText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    destPlacesListView.setAdapter(new LocationAdapter(getContext(),
                            R.layout.autocomplete_location_item, destLocationList));
                    destPlacesListView.setVisibility(View.VISIBLE);
                } else {
                    destPlacesListView.setAdapter(null);
                    destPlacesListView.setVisibility(View.INVISIBLE);
                }
            }
        });

        departureText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    deptPlacesListView.setAdapter(new LocationAdapter(getContext(),
                            R.layout.autocomplete_location_item, deptLocationList));
                    deptPlacesListView.setVisibility(View.VISIBLE);
                } else {
                    deptPlacesListView.setAdapter(null);
                    deptPlacesListView.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        PlaceManagerUtils.SetMap(googleMap);
        map.setOnPoiClickListener(new GoogleMap.OnPoiClickListener() {
            @Override
            public void onPoiClick(@NonNull PointOfInterest pointOfInterest) {
                LocationDto location = queryDetail(pointOfInterest.placeId);
                if (location != null) {
                    showPlaceDetail(location);
                }
            }
        });

        // Create GPS object
        gps = new GPSTrackerLogic(getContext());
        Location location = gps.getLocation(getActivity());
        resetCurrentPosition(location);

        // Add map click listener
        map.setOnMapClickListener(latLng -> {
            LocationDto locationDto = findLocationGeoMsg(latLng);
            // Only when the location exists in the map, change the maker.
            showPlaceDetail(locationDto);
        });

        // Add map marker click listener
        map.setOnMarkerClickListener(marker -> {
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                    new CameraPosition(marker.getPosition(), 15, 0, 0)
            ));
            return true;
        });
    }

    public void queryAutocomplete(AutoCompleteUtils.AutocompleteType type) {
        new Thread(() -> {
            PlacesSearchResult[] places;
            if (type == AutoCompleteUtils.AutocompleteType.DEST) {
                places = PlaceManagerUtils.nearbySearchQuery(destinationKeyword,
                        convert(startLocation != null ? startLocation : currentLocation));
                destLocationList.clear();
            } else {
                places = PlaceManagerUtils.nearbySearchQuery(departureKeyword,
                        convert(targetLocation != null ? targetLocation : currentLocation));
                deptLocationList.clear();
            }
            for (PlacesSearchResult place : places) {
                LocationDto location = PlaceManagerUtils.queryDetail(place.placeId);
                if (type == AutoCompleteUtils.AutocompleteType.DEST) {
                    destLocationList.add(location);
                } else {
                    deptLocationList.add(location);
                }
            }
            // pass the results to original thread so that UI elements can be updated
            if (type == AutoCompleteUtils.AutocompleteType.DEST) {
                UIHandler.post(() -> destPlacesListView.setAdapter(new LocationAdapter(getContext(),
                        R.layout.autocomplete_location_item, destLocationList)));
            } else {
                UIHandler.post(() -> deptPlacesListView.setAdapter(new LocationAdapter(getContext(),
                        R.layout.autocomplete_location_item, deptLocationList)));
            }
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
        currentLocation.setDate(new Date());
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
        try {
            startLocation = new LocationDto();
            BeanUtils.copyProperties(startLocation, currentLocation);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error occurs while doing dto copy");
        }
        // reset map
        map.clear();
        map.addMarker(new MarkerOptions().position(convert(currentLocation))
                .title("current location"));
        map.moveCamera(CameraUpdateFactory.newLatLng(convert(currentLocation)));
    }

    private void showPlaceDetail(LocationDto location) {
        if (StringUtils.isNotEmpty(location.getName())) {
            map.clear();
            map.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(),
                    location.getLongitude())));
            BottomSheetBehavior<FrameLayout> sheetBehavior = BottomSheetBehavior.from(bottomsheet);

            if (sheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }

            if (StringUtils.isNotEmpty(location.getGmImgUrl())) {
                new Thread(() -> {
                    try {
                        // resolving the string into url
                        URL url = new URL(location.getGmImgUrl());
                        // Open the input stream
                        InputStream inputStream = url.openStream();
                        // Convert the online source to bitmap picture
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        UIHandler.post(() -> {
                            locationImg.setImageBitmap(bitmap);
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            } else {
                locationImg.setImageResource(R.drawable.ic_location_unavaliable);
            }
            selectLocationName.setText(location.getName());
            selectLocationDetail.setText(location.getAddress());

            setDeparture.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    startLocation = location;
                    departureText.setText(startLocation.getName());
                    if (startLocation != null && targetLocation != null && StringUtils.isNotEmpty(mode)) {
                        PlaceManagerUtils.findRoute(convert(startLocation),
                                convert(targetLocation), mode);
                    }
                }
            });
            setDestination.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    targetLocation = location;
                    destinationText.setText(targetLocation.getName());
                    if (startLocation != null && targetLocation != null && StringUtils.isNotEmpty(mode)) {
                        PlaceManagerUtils.findRoute(convert(startLocation),
                                convert(targetLocation), mode);
                    }
                }
            });
            new Thread(() -> {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                UIHandler.post(() -> {
                    sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                });
            }).start();
        }
    }
}
