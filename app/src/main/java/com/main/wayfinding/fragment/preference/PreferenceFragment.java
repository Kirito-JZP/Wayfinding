package com.main.wayfinding.fragment.preference;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.GenericTypeIndicator;
import com.main.wayfinding.R;
import com.main.wayfinding.adapter.preferenceAdapter;
import com.main.wayfinding.databinding.FragmentPreferenceBinding;
import com.main.wayfinding.dto.LocationDto;
import com.main.wayfinding.logic.TrackerLogic;
import com.main.wayfinding.logic.db.LocationDBLogic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.List;

/**
 * Define the fragment used for saving user preference(home, work or favourite places)
 * l
 *
 * @author JIA
 * @author Last Modified By Sahil
 * @version Revision: 0
 * Date: 2022/2/10 17:31
 */
public class PreferenceFragment extends Fragment {
    private FragmentPreferenceBinding binding;
    private RecyclerView recentSavedList;
    private preferenceAdapter recentSavedAdapter;
    private RecyclerView nearbyList;
    private preferenceAdapter nearbyAdapter;
    private FirebaseAuth auth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPreferenceBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        auth = FirebaseAuth.getInstance();

        // Recent saved list setting
        recentSavedList = view.findViewById(R.id.recent_loc);
        recentSavedAdapter = new preferenceAdapter();
        recentSavedList.setAdapter(recentSavedAdapter);
        recentSavedList.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false));

        // NearBy List setting
        nearbyList = view.findViewById(R.id.nearby_loc);
        nearbyAdapter = new preferenceAdapter();
        nearbyList.setAdapter(nearbyAdapter);
        nearbyList.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false));

        reload();
    }

    public void reload() {
        //example for iterate locations stored in database
        FirebaseUser currentUser = auth.getCurrentUser();

        List<LocationDto> locationDtoList = new ArrayList<LocationDto>();

        if (currentUser != null) {
            View view = getView();
            new LocationDBLogic().select(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {

                    if (task.isSuccessful()) {
                        HashMap<String, LocationDto> map = task.getResult().getValue(new GenericTypeIndicator<HashMap<String, LocationDto>>() {
                        });
                        //key: locationID
                        //value: LocationDto Object
                        Iterator<Map.Entry<String, LocationDto>> iterator = map.entrySet().iterator();
                        while (iterator.hasNext()) {
                            Map.Entry<String, LocationDto> next = iterator.next();

                            String key = next.getKey();
                            LocationDto object = next.getValue();

                            locationDtoList.add(object);
                        }


                        //example for delete location
                        locationDtoList.sort((o1, o2) -> o2.getDate().compareTo(o1.getDate()));
                        recentSavedAdapter.setLocationList(locationDtoList);
                        recentSavedAdapter.notifyDataSetChanged();
                        TrackerLogic trackerLogic = TrackerLogic.getInstance(getActivity());

                        trackerLogic.requestLastLocation(location -> {
                            setNearByList(location, new ArrayList<LocationDto>(locationDtoList));
                        });

                    } else {
                        System.out.println(task.getException());
                    }
                }
            });
        } else {
            System.out.println("Not logged in");
        }


    }

    private void setNearByList(Location location, ArrayList<LocationDto> locationDtoList) {
        locationDtoList.sort(new Comparator<LocationDto>() {
            @Override
            public int compare(LocationDto loc1, LocationDto loc2) {
                float[] distance1 = new float[1];
                float[] distance2 = new float[1];
                Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                        loc1.getLatitude(), loc1.getLongitude(), distance1);
                Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                        loc2.getLatitude(), loc2.getLongitude(), distance2);
                return Float.compare(distance1[0], distance2[0]);
            }
        });
        for (LocationDto locationDto : locationDtoList) {
            float[] result = new float[1];
            Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                    locationDto.getLatitude(), locationDto.getLongitude(), result);
            System.out.println(result[0]);
        }
        nearbyAdapter.setLocationList(locationDtoList);
        nearbyAdapter.notifyDataSetChanged();
    }

    private void setNearByList(Location location) {

    }
}