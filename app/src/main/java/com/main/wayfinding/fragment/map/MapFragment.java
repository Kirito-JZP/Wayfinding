package com.main.wayfinding.fragment.map;

import static com.main.wayfinding.utility.AlertDialogUtils.createAlertDialog;
import static com.main.wayfinding.utility.PlaceManagerUtils.findLocationGeoMsg;
import static com.main.wayfinding.utility.PlaceManagerUtils.queryDetail;
import static com.main.wayfinding.utility.PlaceManagerUtils.queryLatLng;
import static com.main.wayfinding.utility.StaticStringUtils.*;

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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.maps.model.TravelMode;
import com.main.wayfinding.ARNavigationActivity;
import com.main.wayfinding.R;
import com.main.wayfinding.adapter.LocationAdapter;
import com.main.wayfinding.databinding.FragmentMapBinding;
import com.main.wayfinding.dto.EmergencyEventDto;
import com.main.wayfinding.dto.LocationDto;
import com.main.wayfinding.dto.RouteDto;
import com.main.wayfinding.dto.UserDto;
import com.main.wayfinding.logic.EmergencyEventLogic;
import com.main.wayfinding.logic.db.DisasterDBLogic;
import com.main.wayfinding.logic.db.LocationDBLogic;
import com.main.wayfinding.logic.TrackerLogic;
import com.main.wayfinding.logic.NavigationLogic;

import android.widget.FrameLayout;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.main.wayfinding.utility.LatLngConverterUtils;
import com.main.wayfinding.utility.PlaceManagerUtils;

import java.io.InputStream;
import java.net.URL;
import java.time.LocalTime;
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
public class MapFragment extends Fragment implements OnMapReadyCallback,
        EmergencyEventLogic.EmergencyEventCallback {

    private int autocompleteDelay = 500;
    private TravelMode mode;
    private FragmentMapBinding binding;
    private FrameLayout bottomSheet;
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
    private TrackerLogic trackerLogic;
    private NavigationLogic navigationLogic;

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
    private ImageView accidentBtn;
    private ImageView deptTxtClearBtn;
    private ImageView destTxtClearBtn;
    private ImageView locationImg;
    private RelativeLayout setDeptBtn;
    private RelativeLayout setDestBtn;
    private RelativeLayout addWaypointBtn;
    private RelativeLayout publicBtn;
    private RelativeLayout walkBtn;
    private RelativeLayout cycBtn;

    // ListView
    private ListView destPlacesListView;
    private ListView deptPlacesListView;

    // Autocomplete-related
    private enum AutocompleteType {
        DEST, DEPT
    }

    private Timer autocompleteTimer;

    class AutocompleteTask extends TimerTask {
        private AutocompleteType type;
        private String keyword;

        public AutocompleteTask(AutocompleteType type, String keyword) {
            this.type = type;
            this.keyword = keyword;
        }

        @Override
        public void run() {
            switch (this.type) {
                case DEST:
                    queryAutocomplete(AutocompleteType.DEST, this.keyword);
                    break;
                case DEPT:
                    queryAutocomplete(AutocompleteType.DEPT, this.keyword);
                    break;
            }
        }
    }

    class AutocompleteTextWatcher implements TextWatcher {
        private AutocompleteType type;

        public AutocompleteTextWatcher(AutocompleteType type) {
            this.type = type;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if (getView().findFocus() == deptTxt || getView().findFocus() == destTxt) {
                String keyword = charSequence.toString().trim();
                autocompleteTimer.purge();
                autocompleteTimer.cancel();
                autocompleteTimer = new Timer();
                autocompleteTimer.schedule(new AutocompleteTask(this.type, keyword),
                        autocompleteDelay);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMapBinding.inflate(getLayoutInflater());
        View root = binding.getRoot();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mode = TravelMode.WALKING;
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
        accidentBtn = view.findViewById(R.id.accident);
        btnLocDtlTxt = view.findViewById(R.id.loc_detail_txt);
        setDeptBtn = view.findViewById(R.id.set_dept_btn);
        setDestBtn = view.findViewById(R.id.set_dest_btn);
        addWaypointBtn = view.findViewById(R.id.add_waypoint_btn);
        locationImg = view.findViewById(R.id.location_img);
        arBtn = view.findViewById(R.id.arBtn);
        //bottom sheet
        bottomSheet = view.findViewById(R.id.bottomsheet);

        // ListView
        destPlacesListView = view.findViewById(R.id.dest_places_list);
        deptPlacesListView = view.findViewById(R.id.dept_places_list);

        UIHandler = new Handler();
        autocompleteTimer = new Timer();
        deptLocList = new ArrayList<>();
        destLocList = new ArrayList<>();

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    if (targetLocDto != null && StringUtils.isNotEmpty(targetLocDto.getName())) {
                        targetLocDto.setDate(new Date());
                        new LocationDBLogic().insert(targetLocDto);
                        createAlertDialog(getContext(), ADD_SUCCESS_MSG);
                    } else {
                        createAlertDialog(getContext(), NO_INPUT_MSG);
                    }
                } else {
                    createAlertDialog(getContext(), "Cannot add it without logging in!");
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
                parseRouteData(NavigationLogic.findRoute(startLocDto, targetLocDto, mode, null));
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
                parseRouteData(NavigationLogic.findRoute(startLocDto, targetLocDto,
                        TravelMode.TRANSIT, null));
            }
        });

        walkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parseRouteData(NavigationLogic.findRoute(startLocDto, targetLocDto,
                        TravelMode.WALKING, null));
            }
        });

        cycBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parseRouteData(NavigationLogic.findRoute(startLocDto, targetLocDto,
                        TravelMode.BICYCLING, null));
            }
        });

        //accident button
        accidentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // code use to add disaster even (define disaster code "A" "B" "C" ...)
                EmergencyEventDto emergencyEventDto = new EmergencyEventDto();
                emergencyEventDto.setCode("A");
                emergencyEventDto.setLatitude(53.34153859999999);
                emergencyEventDto.setLongitude(-6.255735299999999);
                emergencyEventDto.setRadius(100);
                emergencyEventDto.setType("Car Accident");
                emergencyEventDto.setStartTime(LocalTime.of(16,0));
                emergencyEventDto.setEndTime(LocalTime.of(19,0));
                new DisasterDBLogic().insert(emergencyEventDto);
                // code use to read disaster even (use disaster code "A" "B" "C" ...)
//                new DisasterDBLogic().select("A", new OnCompleteListener<DataSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<DataSnapshot> task) {
//                        if (task.isSuccessful()) {
//                            EmergencyEventDto event = task.getResult().getValue(EmergencyEventDto.class);
//                            //Todo...
//
//                        } else {
//                            System.out.println(task.getException().getMessage());
//                        }
//                    }
//                });

            }
        });


        // Do way finding after clicking the navigate button
        navigateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigationLogic.startNavigation(currentRouteDto);
            }
        });

        deptTxt.addTextChangedListener(new AutocompleteTextWatcher(AutocompleteType.DEPT));

        // listener for delayed trigger
        destTxt.addTextChangedListener(new AutocompleteTextWatcher(AutocompleteType.DEST));

        // click on a place in the candidate places
        destPlacesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                LocationDto location = destLocList.get(i);
                processItemClick(location, AutocompleteType.DEST);
            }
        });

        deptPlacesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                LocationDto location = deptLocList.get(i);
                processItemClick(location, AutocompleteType.DEPT);
            }
        });

        // listener to change the visibility of the places list
        destTxt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean isFocused) {
                if (isFocused) {
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
            public void onFocusChange(View view, boolean isFocused) {
                if (isFocused) {
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
        trackerLogic = TrackerLogic.createInstance(getActivity());
        // ask for permissions
        trackerLogic.askForLocationPermissions(new TrackerLogic.LocationPermissionRequestCompleteCallback() {
            @Override
            public void onLocationPermissionRequestComplete(boolean isSuccessful) {
                if (isSuccessful) {
                    // Set map
                    map = googleMap;
                    map.setLocationSource(trackerLogic);
                    map.setMyLocationEnabled(true);
                    map.getUiSettings().setCompassEnabled(false);
                    map.getUiSettings().setMyLocationButtonEnabled(false);
                    PlaceManagerUtils.SetMap(map);
                    map.setOnPoiClickListener(new GoogleMap.OnPoiClickListener() {
                        @Override
                        public void onPoiClick(@NonNull PointOfInterest pointOfInterest) {
                            LocationDto location = queryDetail(pointOfInterest.placeId);
                            if (location != null) {
                                showPlaceDetail(location);
                            }
                        }
                    });

                    // Create navigation object
                    NavigationLogic.createInstance(map);
                    navigationLogic = NavigationLogic.getInstance();
                    // Create tracker object
                    trackerLogic = TrackerLogic.createInstance(getActivity());
                    // trackerLogic
                    trackerLogic.requestLastLocation(location -> resetCurrentPosition(location));
                    map.setLocationSource(trackerLogic);    // replace the default location
                    // source with

                    // Create navigation object
                    NavigationLogic.createInstance(map);
                    navigationLogic = NavigationLogic.getInstance();

                    // Add map click listener
                    map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                        @Override
                        public void onMapClick(@NonNull LatLng latLng) {
                            LocationDto locationDto = findLocationGeoMsg(latLng);
                            // Only when the location exists in the map, change the maker.
                            showPlaceDetail(locationDto);
                        }
                    });

                    // Add map marker click listener
                    map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(@NonNull Marker marker) {
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(),
                                    15));
                            return true;
                        }
                    });

                    // Get current location after clicking the position button
                    locateBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            trackerLogic.requestLastLocation(MapFragment.this::resetCurrentPosition);
                            deptPlacesListView.setVisibility(View.INVISIBLE);
                            parseRouteData(NavigationLogic.findRoute(startLocDto, targetLocDto,
                                    mode, null));
                        }
                    });

                    // Add emergency event callback
                    EmergencyEventLogic.registerEmergencyEvent(MapFragment.this);
                } else {
                    trackerLogic.askForLocationPermissions(this);
                }
            }
        });
        //unpack data
        //if accepted data from broadcast, set destination and do corresponding process
        Bundle arguments = this.getArguments();
        if (arguments != null) {
            String keyword = arguments.getString("name");
            destTxt.setText(keyword);

            targetLocDto = PlaceManagerUtils.autocompletePlaces(keyword,
                    startLocDto != null ? LatLngConverterUtils.getLatLngFromDto(startLocDto)
                            : LatLngConverterUtils.getLatLngFromDto(currentLocDto)).get(0);
            LatLng latlng = queryLatLng(targetLocDto.getGmPlaceID());
            targetLocDto.setLatitude(latlng.latitude);
            targetLocDto.setLongitude(latlng.longitude);
            parseRouteData(NavigationLogic.findRoute(startLocDto, targetLocDto, mode, null));
        }
    }

    @Override
    public void onEmergencyEventHappen(EmergencyEventDto event) {
        // find out if the location of the emergency event is ahead of the current location in the
        // current route and if the current route is affected
        LocationDto location = new LocationDto();
        location.setLatitude(event.getLatitude());
        location.setLongitude(event.getLongitude());
        if (NavigationLogic.isLocationAheadOfReference(currentRouteDto, location,
                currentLocDto)) {
            List<RouteDto.RouteStep> affectedSteps =
                    NavigationLogic.findStepsAffectedBy(currentRouteDto, event);
            if (!affectedSteps.isEmpty()) {
                RouteDto.RouteStep backwardBoundary = affectedSteps.get(0);
                RouteDto.RouteStep forwardBoundary = affectedSteps.get(affectedSteps.size() - 1);
                double x1 = backwardBoundary.getStartLocation().getLongitude();
                double y1 = backwardBoundary.getStartLocation().getLatitude();
                double x2 = forwardBoundary.getEndLocation().getLongitude();
                double y2 = forwardBoundary.getEndLocation().getLatitude();
                double x0 = event.getLongitude();
                double y0 = event.getLatitude();
                double r0 = event.getRadius();
                // the intersection points of the straight line
                // perpendicular to the line formed by connecting (x1, y1) and (x2, y2)
                // with the affected range
                // these two points should be the farthest ones away from the center of the event
                // range
                // as well as (x1, y1) and (x2, y2)
                LatLng intersect1, intersect2;
                if (Math.abs(y1 - y2) < 1e-3) {
                    // treat as y1 == y2
                    intersect1 = new LatLng(y0 + r0, x0);
                    intersect2 = new LatLng(y0 - r0, x0);
                } else {
                    double k = (x1 - x2) / (y1 - y2);
                    double term = r0 / Math.sqrt(1 + k * k);
                    double x = x0 + term;
                    double y = -k * x + y0 + k * x0;
                    intersect1 = new LatLng(y, x);
                    x = x0 - term;
                    y = -k * x + y0 + k * x0;
                    intersect2 = new LatLng(y, x);
                }
                // use the point that is the closest to the current position as a waypoint to add
                double p = intersect1.latitude - currentLocDto.getLatitude();
                double q = intersect1.longitude - currentLocDto.getLongitude();
                double distance1 = Math.sqrt(p * p + q * q);
                p = intersect2.latitude - currentLocDto.getLatitude();
                q = intersect2.longitude - currentLocDto.getLongitude();
                double distance2 = Math.sqrt(p * p + q * q);
                LocationDto waypoint = new LocationDto();
                waypoint.setLatitude(distance1 < distance2 ? intersect1.latitude :
                        intersect2.latitude);
                waypoint.setLongitude(distance1 < distance2 ? intersect1.longitude :
                        intersect2.longitude);
                currentRouteDto.addWaypoint(waypoint);
                // re-search routes from the current position with the added waypoints
                NavigationLogic.updateRouteFromCurrentLocation(currentRouteDto, currentLocDto);
            }
        }
    }

    public void queryAutocomplete(AutocompleteType type, String keyword) {
        List<LocationDto> places;
        if (type == AutocompleteType.DEST) {
            destLocList.clear();
            if (StringUtils.isNotEmpty(keyword)) {
                places = PlaceManagerUtils.autocompletePlaces(keyword,
                        startLocDto != null ? LatLngConverterUtils.getLatLngFromDto(startLocDto)
                                : LatLngConverterUtils.getLatLngFromDto(currentLocDto));
                destLocList.clear();
                destLocList.addAll(places);
            }
        } else if (type == AutocompleteType.DEPT) {
            deptLocList.clear();
            if (StringUtils.isNotEmpty(keyword)) {
                places = PlaceManagerUtils.autocompletePlaces(keyword,
                        targetLocDto != null ? LatLngConverterUtils.getLatLngFromDto(targetLocDto)
                                : LatLngConverterUtils.getLatLngFromDto(currentLocDto));
                deptLocList.addAll(places);
            }
        }
        // pass the results to original thread so that UI elements can be updated
        if (type == AutocompleteType.DEST) {
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
        // https://developers.google.com/maps/documentation/android-sdk/views#zoom
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLngConverterUtils
                .getLatLngFromDto(currentLocDto), 16.0F));
    }

    private void showPlaceDetail(LocationDto location) {
        if (StringUtils.isNotEmpty(location.getName())) {
            map.clear();
            map.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(),
                    location.getLongitude())));
            BottomSheetBehavior<FrameLayout> sheetBehavior = BottomSheetBehavior.from(bottomSheet);
            // collapse the bottom sheet
            if (sheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            // obtain preview image if there is one
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
            // add listeners to buttons
            setDeptBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    startLocDto = location;
                    deptTxt.setText(startLocDto.getName());
                    parseRouteData(NavigationLogic.findRoute(startLocDto, targetLocDto, mode,
                            null));
                }
            });
            setDestBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    targetLocDto = location;
                    destTxt.setText(targetLocDto.getName());
                    parseRouteData(NavigationLogic.findRoute(startLocDto, targetLocDto, mode,
                            null));
                }
            });
            addWaypointBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    navigationLogic.addWayPoint(location);
                }
            });
            // re-open the bottom sheet to display a new place and set a delay of 0.1s after folding
            // the bottom sheet
            // the operation is done to avoid some cases where the bottom sheet fails to show up
            // if conducted
            // in the main thread
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

    private void processItemClick(LocationDto location, AutocompleteType type) {
        ListView listView = type == AutocompleteType.DEPT ? deptPlacesListView : destPlacesListView;
        EditText editText = type == AutocompleteType.DEPT ? deptTxt : destTxt;
        listView.setAdapter(null);
        // query geological coordinates
        LatLng latlng = queryLatLng(location.getGmPlaceID());
        location.setLatitude(latlng.latitude);
        location.setLongitude(latlng.longitude);
        switch (type) {
            case DEST:
                targetLocDto = location;
                destTxt.setText(targetLocDto.getName());
                break;
            case DEPT:
                startLocDto = location;
                deptTxt.setText(startLocDto.getName());
                break;
        }
        // hide the soft keyboard after clicking on an item
        InputMethodManager manager = ((InputMethodManager) getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE));
        if (manager != null)
            manager.hideSoftInputFromWindow(getView().findFocus().getWindowToken(), 0);
        editText.clearFocus();
        ;
        // update UI
        parseRouteData(NavigationLogic.findRoute(startLocDto, targetLocDto, mode, null));
    }

    private void parseRouteData(Pair<List<RouteDto>, LatLngBounds> data) {
        BottomSheetBehavior<FrameLayout> sheetBehavior = BottomSheetBehavior.from(bottomSheet);
        // collapse the bottom sheet if it's currently expanded
        if (sheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
        if (data != null) {
            possibleRoutes = data.first;
            LatLngBounds bounds = data.second;
            // use the first route by default
            currentRouteDto = possibleRoutes.get(0);
            map.clear();
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
            NavigationLogic.updatePolylinesUI(currentRouteDto, map);
        }
    }
}
