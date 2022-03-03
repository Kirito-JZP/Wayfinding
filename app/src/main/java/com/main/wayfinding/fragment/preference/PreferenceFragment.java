package com.main.wayfinding.fragment.preference;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.app.Activity;


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
import com.main.wayfinding.logic.AuthLogic;
import com.main.wayfinding.logic.DB.LocationDBLogic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.List;

/**
 * Define the fragment used for saving user preference(home, work or favourite places)
 *l
 * @author JIA
 * @author Last Modified By Sahil
 * @version Revision: 0
 * Date: 2022/2/10 17:31
 */
public class PreferenceFragment extends Fragment {
    private FirebaseAuth auth;


    private RecyclerView recyclerView;
    private RecyclerView recyclerView_2;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.recyclerview, container, false);
        recyclerView = rootView.findViewById(R.id.rvLocations);
        recyclerView.setAdapter(new preferenceAdapter(new ArrayList<LocationDto>()));
        recyclerView.setLayoutManager(new LinearLayoutManager(rootView.getContext(), LinearLayoutManager.HORIZONTAL, false));
//        locations = RecentlySaved.createLocationList(reload());



        return rootView;

    }




    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();

    }

    @Override
    public void onStart() {
        super.onStart();
        reload();
    }

    public void reload() {
        //example for iterate locations stored in database
        FirebaseUser currentUser = auth.getCurrentUser();

        List<LocationDto> locationDtos = new ArrayList<LocationDto>();

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

                            locationDtos.add(object);
                            //object.getName();
                            //object.getAddress();
                            //object.getCountry();

                        }
                        //example for delete location
                        //new LocationDBLogic().delete("-MvVkRv0fWWEHa1SOqWs");
                        recyclerView.setAdapter(new preferenceAdapter(locationDtos));


                    } else {
                        System.out.println(task.getException());
                    }
                }
            });
        } else {
            System.out.println("Not logged in");
        }


    }
}