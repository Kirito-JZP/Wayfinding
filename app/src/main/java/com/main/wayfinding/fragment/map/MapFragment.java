package com.main.wayfinding.fragment.map;

import static com.main.wayfinding.utility.AlertDialogUtils.createAlertDialog;
import static com.main.wayfinding.utility.PlaceManagerUtils.findLocationGeoMsg;
import static com.main.wayfinding.utility.LatLngConverterUtils.convert;
import static com.main.wayfinding.utility.PlaceManagerUtils.queryDetail;
import static com.main.wayfinding.utility.PlaceManagerUtils.queryLatLng;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;
import com.main.wayfinding.ARNavigationActivity;
import com.main.wayfinding.R;
import com.main.wayfinding.adapter.LocationAdapter;
import com.main.wayfinding.databinding.FragmentMapBinding;
import com.main.wayfinding.dto.LocationDto;
import com.main.wayfinding.dto.RouteDto;
import com.main.wayfinding.logic.db.LocationDBLogic;
import com.main.wayfinding.logic.TrackerLogic;
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
import java.util.Timer;
import java.util.TimerTask;

import javadz.beanutils.BeanUtils;

/**
 * Define the fragment used for displaying map and dynamic Sustainable way-finding
 *
 * @author JIA
 * @author Last Modified By JIA
 * @version Revision: 1
 * Date: 2022/2/2 19:50
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {


    private int autocompleteDelay = 500;
    private String mode = "walking";
    private FragmentMapBinding binding;
    private String destinationKeyword;
    private String departureKeyword;

    private FrameLayout bottomsheet;
    private Handler UIHandler;

    /********** JAVA DATA Structure ***********/
    // Dto
    private LocationDto currentLocDto;
    private LocationDto startLocDto;
    private LocationDto targetLocDto;
    private RouteDto currentRouteDto;
    private List<RouteDto> possibleRoutes;
    private List<LocationDto> deptLocList;
    private List<LocationDto> destLocList;

    // Logic
    private TrackerLogic gpsLogic;
    private NavigationLogic navigationLogic;

    // Utils
    private AutoCompleteUtils autoCompleteUtils;

    /*************** Components ***************/
    // Map
    private GoogleMap map;

    // EditText/TextView
    private EditText deptTxt;
    private EditText destTxt;
    private TextView btnLocNmTxt;
    private TextView btnLocDtlTxt;

    // ImageView (button)
    private ImageView addBtn;
    private ImageView exchangeBtn;
    private ImageView arBtn;
    private ImageView navigateBtn;
    private ImageView locateBtn;
    private ImageView deptTxtClearBtn;
    private ImageView destTxtClearBtn;
    private ImageView locationImg;
    private RelativeLayout setDeptBtn;
    private RelativeLayout setDestBtn;
    private RelativeLayout publicBtn;
    private RelativeLayout walkBtn;
    private RelativeLayout cycBtn;

    // ListView
    private ListView destPlacesListView;
    private ListView deptPlacesListView;

    // Autocomplete logic
    private Timer autocompleteTimer;
    private enum AutocompleteType {
        DEST, DEPT
    }
    AutocompleteType autocompleteType;

    class AutocompleteTask extends TimerTask {
        @Override
        public void run() {
            switch (autocompleteType){
                case DEST:
                    queryAutocomplete(AutoCompleteUtils.AutocompleteType.DEST);
                    break;
                case DEPT:
                    queryAutocomplete(AutoCompleteUtils.AutocompleteType.DEPT);
                    break;
            }
        }
    }

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

        // TextView
        deptTxt = view.findViewById(R.id.input_start);
        destTxt = view.findViewById(R.id.input_search);

        // ImageView
        addBtn = view.findViewById(R.id.add_btn);
        exchangeBtn = view.findViewById(R.id.exchange_btn);
        publicBtn = view.findViewById(R.id.public_btn);
        walkBtn = view.findViewById(R.id.walk_btn);
        cycBtn = view.findViewById(R.id.cyc_btn);
        navigateBtn = view.findViewById(R.id.navigate);
        locateBtn = view.findViewById(R.id.position);
        deptTxtClearBtn = view.findViewById(R.id.clear_dept_btn);
        destTxtClearBtn = view.findViewById(R.id.clear_dest_btn);
        btnLocNmTxt = view.findViewById(R.id.loc_name_txt);
        btnLocDtlTxt = view.findViewById(R.id.loc_detail_txt);
        setDeptBtn = view.findViewById(R.id.set_dept_btn);
        setDestBtn = view.findViewById(R.id.set_dest_btn);
        locationImg = view.findViewById(R.id.location_img);
        arBtn = view.findViewById(R.id.arBtn);
        //bottom sheet
        bottomsheet = view.findViewById(R.id.bottomsheet);

        // ListView
        destPlacesListView = view.findViewById(R.id.dest_places_list);
        deptPlacesListView = view.findViewById(R.id.dept_places_list);

        autoCompleteUtils = new AutoCompleteUtils();
        autoCompleteUtils.setFragment(this);
        UIHandler = new Handler();
        autocompleteTimer = new Timer();
        deptLocList = new ArrayList<>();
        destLocList = new ArrayList<>();

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (targetLocDto != null && StringUtils.isNotEmpty(targetLocDto.getName())) {
                    targetLocDto.setDate(new Date());
                    new LocationDBLogic().insert(targetLocDto);
                    createAlertDialog(getContext(), getString(R.string.dialog_msg_add));
                } else {
                    createAlertDialog(getContext(), getString(R.string.dialog_msg_no_inputs));
                }

            }
        });
        arBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ARNavigationActivity.class);
                startActivity(intent);
            }
        });
        exchangeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Exchange the departure and the destination
                if (startLocDto != null || targetLocDto != null) {
                    // Exchange content in dto
                    LocationDto temp = startLocDto;
                    startLocDto = targetLocDto;
                    targetLocDto = temp;

                    // Exchange message in TextView
                    if (startLocDto != null) {
                        deptTxt.setText(startLocDto.getName());
                    } else {
                        deptTxt.setText("");
                    }
                    if (targetLocDto != null) {
                        destTxt.setText(targetLocDto.getName());
                    } else {
                        destTxt.setText("");
                    }
                }
                if (startLocDto != null && targetLocDto != null && StringUtils.isNotEmpty(mode)) {
                    PlaceManagerUtils.findRoute(startLocDto.getLatLng(), targetLocDto.getLatLng(),
                            mode);
                }
            }
        });

        deptTxtClearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLocDto = new LocationDto();
                deptPlacesListView.setVisibility(View.INVISIBLE);
                deptTxt.setText("");
            }
        });

        destTxtClearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                targetLocDto = new LocationDto();
                destPlacesListView.setVisibility(View.INVISIBLE);
                destTxt.setText("");
            }
        });

        publicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mode = "transit";
                if (startLocDto != null && targetLocDto != null) {
                    PlaceManagerUtils.findRoute(
                            startLocDto.getLatLng(),
                            targetLocDto.getLatLng(),
                            mode.equals("") ? "walking" : mode
                    );
                }
            }
        });

        walkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mode = "walking";
                if (startLocDto != null && targetLocDto != null) {
                    PlaceManagerUtils.findRoute(
                            startLocDto.getLatLng(),
                            targetLocDto.getLatLng(),
                            mode.equals("") ? "walking" : mode
                    );
                }
            }
        });

        cycBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mode = "bicycling";
                if (startLocDto != null && targetLocDto != null) {
                    PlaceManagerUtils.findRoute(
                            startLocDto.getLatLng(),
                            targetLocDto.getLatLng(),
                            mode.equals("") ? "walking" : mode
                    );
                }
            }
        });

        // Get current location after clicking the position button
        locateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gpsLogic.requestLastLocation(location -> {
                    resetCurrentPosition(location);
                });
                deptPlacesListView.setVisibility(View.INVISIBLE);
                if (startLocDto != null && targetLocDto != null && StringUtils.isNotEmpty(mode)) {
                    PlaceManagerUtils.findRoute(startLocDto.getLatLng(), targetLocDto.getLatLng(),
                            mode);
                }
            }
        });

        // Do way finding after clicking the navigate button
        navigateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigationLogic.startNavigation(currentRouteDto);
            }
        });

        deptTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (getView().findFocus() == deptTxt) {
                    departureKeyword = charSequence.toString().trim();
                    autocompleteTimer.purge();
                    autocompleteTimer.cancel();
                    autocompleteTimer = new Timer();
                    autocompleteType = AutocompleteType.DEPT;
                    autocompleteTimer.schedule(new AutocompleteTask(), autocompleteDelay);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        // listener for delayed trigger
        destTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (getView().findFocus() == destTxt) {
                    destinationKeyword = charSequence.toString().trim();
                    autocompleteTimer.purge();
                    autocompleteTimer.cancel();
                    autocompleteTimer = new Timer();
                    autocompleteType = AutocompleteType.DEST;
                    autocompleteTimer.schedule(new AutocompleteTask(), autocompleteDelay);
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
                LocationDto location = destLocList.get(i);
                destPlacesListView.setAdapter(null);
                // query geological coordinates
                LatLng latlng = queryLatLng(location.getGmPlaceID());
                location.setLatitude(latlng.latitude);
                location.setLongitude(latlng.longitude);
                targetLocDto = location;
                destTxt.setText(targetLocDto.getName());
                // hide the soft keyboard after clicking on an item
                InputMethodManager manager = ((InputMethodManager) getContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE));
                if (manager != null)
                    manager.hideSoftInputFromWindow(getView().findFocus().getWindowToken(), 0);
                destTxt.clearFocus();
                if (startLocDto != null && targetLocDto != null && StringUtils.isNotEmpty(mode)) {
                    Pair<List<RouteDto>, LatLngBounds> result =
                            PlaceManagerUtils.findRoute(startLocDto.getLatLng(),
                                    targetLocDto.getLatLng(),
                            mode);
                    possibleRoutes = result.first;
                    LatLngBounds bounds = result.second;
                    map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
                    // use the first route by default
                    currentRouteDto = possibleRoutes.get(0);
                    map.addPolyline(currentRouteDto.getPolylineOptions());
                }
            }
        });

        deptPlacesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                LocationDto location = deptLocList.get(i);
                deptPlacesListView.setAdapter(null);
                // query geological coordinates
                LatLng latlng = queryLatLng(location.getGmPlaceID());
                location.setLatitude(latlng.latitude);
                location.setLongitude(latlng.longitude);
                startLocDto = location;
                deptTxt.setText(startLocDto.getName());
                // hide the soft keyboard after clicking on an item
                InputMethodManager manager = ((InputMethodManager) getContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE));
                if (manager != null)
                    manager.hideSoftInputFromWindow(getView().findFocus().getWindowToken(), 0);
                deptTxt.clearFocus();
                if (startLocDto != null && targetLocDto != null && StringUtils.isNotEmpty(mode)) {
                    Pair<List<RouteDto>, LatLngBounds> result =
                            PlaceManagerUtils.findRoute(startLocDto.getLatLng(),
                                    targetLocDto.getLatLng(),
                            mode);
                    possibleRoutes = result.first;
                    LatLngBounds bounds = result.second;
                    map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
                    // use the first route by default
                    currentRouteDto = possibleRoutes.get(0);
                    map.addPolyline(currentRouteDto.getPolylineOptions());
                }
            }
        });

        // listener to change the visibility of the places list
        destTxt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    destPlacesListView.setAdapter(new LocationAdapter(getContext(),
                            R.layout.autocomplete_location_item, destLocList));
                    destPlacesListView.setVisibility(View.VISIBLE);
                } else {
                    destPlacesListView.setAdapter(null);
                    destPlacesListView.setVisibility(View.INVISIBLE);
                }
            }
        });

        deptTxt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    deptPlacesListView.setAdapter(new LocationAdapter(getContext(),
                            R.layout.autocomplete_location_item, deptLocList));
                    deptPlacesListView.setVisibility(View.VISIBLE);
                } else {
                    deptPlacesListView.setAdapter(null);
                    deptPlacesListView.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
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
        gpsLogic = TrackerLogic.getInstance(getActivity());
        gpsLogic.requestLastLocation(location -> {
            resetCurrentPosition(location);
        });

        // Create navigation object
        NavigationLogic.createInstance(map);
        navigationLogic = NavigationLogic.getInstance();

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
        List<LocationDto> places;
        if (type == AutoCompleteUtils.AutocompleteType.DEST) {
            destLocList.clear();
            if (StringUtils.isNotEmpty(destinationKeyword)) {
                places = PlaceManagerUtils.autocompletePlaces(destinationKeyword,
                        startLocDto != null ? startLocDto.getLatLng() : currentLocDto.getLatLng());
                destLocList.clear();
                destLocList.addAll(places);
            }
        } else if (type == AutoCompleteUtils.AutocompleteType.DEPT) {
            deptLocList.clear();
            if (StringUtils.isNotEmpty(departureKeyword)) {
                places = PlaceManagerUtils.autocompletePlaces(departureKeyword,
                        targetLocDto != null ? targetLocDto.getLatLng() :
                                currentLocDto.getLatLng());
                deptLocList.addAll(places);
            }
        }
        // pass the results to original thread so that UI elements can be updated
        if (type == AutoCompleteUtils.AutocompleteType.DEST) {
            UIHandler.post(() -> destPlacesListView.setAdapter(new LocationAdapter(getContext(),
                    R.layout.autocomplete_location_item, destLocList)));
        } else {
            UIHandler.post(() -> deptPlacesListView.setAdapter(new LocationAdapter(getContext(),
                    R.layout.autocomplete_location_item, deptLocList)));
        }
    }

    /**
     * Method for getting and setting current position
     *
     * @param location
     */
    private void resetCurrentPosition(Location location) {
        // Add a marker in current location and move the camera
        currentLocDto = new LocationDto();
        currentLocDto.setName(getString(R.string.your_location));
        currentLocDto.setDate(new Date());
        if (location != null) {
            // reset Dto
            currentLocDto.setLatitude(location.getLatitude());
            currentLocDto.setLongitude(location.getLongitude());
            // reset view text
            deptTxt.setText(currentLocDto.getName());
        }
        try {
            startLocDto = new LocationDto();
            BeanUtils.copyProperties(startLocDto, currentLocDto);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error occurs while doing dto copy");
        }
        // reset map
        map.clear();
        map.addMarker(new MarkerOptions().position(currentLocDto.getLatLng())
                .title("current location"));
        map.moveCamera(CameraUpdateFactory.newLatLng(currentLocDto.getLatLng()));
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
                locationImg.setImageResource(R.drawable.ic_loc_img);
            }
            btnLocNmTxt.setText(location.getName());
            btnLocDtlTxt.setText(location.getAddress());

            setDeptBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    startLocDto = location;
                    deptTxt.setText(startLocDto.getName());
                    if (startLocDto != null && targetLocDto != null && StringUtils.isNotEmpty(mode)) {
                        PlaceManagerUtils.findRoute(startLocDto.getLatLng(),
                                targetLocDto.getLatLng(), mode);
                    }
                }
            });
            setDestBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    targetLocDto = location;
                    destTxt.setText(targetLocDto.getName());
                    if (startLocDto != null && targetLocDto != null && StringUtils.isNotEmpty(mode)) {
                        PlaceManagerUtils.findRoute(startLocDto.getLatLng(),
                                targetLocDto.getLatLng(), mode);
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
