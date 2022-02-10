package com.main.wayfinding.fragment.preference;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.GenericTypeIndicator;
import com.main.wayfinding.R;
import com.main.wayfinding.databinding.FragmentPreferenceBinding;
import com.main.wayfinding.dto.LocationDto;
import com.main.wayfinding.logic.AuthLogic;
import com.main.wayfinding.logic.DB.LocationDBLogic;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Define the fragment used for saving user preference(home, work or favourite places)
 *
 * @author JIA
 * @author Last Modified By Sahil
 * @version Revision: 0
 * Date: 2022/2/10 17:31
 */
public class PreferenceFragment extends Fragment {
    private FirebaseAuth auth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_preference, container, false);

        TextView recentlySaved = (TextView) rootView.findViewById(R.id.textView4);
        recentlySaved.setText("Recently Saved");

        ImageView image1 = (ImageView) rootView.findViewById(R.id.image1);

        TextView textAddress1 = (TextView) rootView.findViewById(R.id.textAddress1);
        TextView textCountry1 = (TextView) rootView.findViewById(R.id.textCountry1);

        ImageView image2 = (ImageView) rootView.findViewById(R.id.image2);
        TextView textName2 = (TextView) rootView.findViewById(R.id.textName2);
        TextView textAddress2 = (TextView) rootView.findViewById(R.id.textAddress2);
        TextView textCountry2 = (TextView) rootView.findViewById(R.id.textCountry2);

        ImageView image3 = (ImageView) rootView.findViewById(R.id.image3);
        TextView textName3 = (TextView) rootView.findViewById(R.id.textName3);
        TextView textAddress3 = (TextView) rootView.findViewById(R.id.textAddress3);
        TextView textCountry3 = (TextView) rootView.findViewById(R.id.textCountry3);

        ImageView image4 = (ImageView) rootView.findViewById(R.id.image4);
        TextView textName4 = (TextView) rootView.findViewById(R.id.textName4);
        TextView textAddress4 = (TextView) rootView.findViewById(R.id.textAddress4);
        TextView textCountry4 = (TextView) rootView.findViewById(R.id.textCountry4);

        ImageView image5 = (ImageView) rootView.findViewById(R.id.image5);
        TextView textName5 = (TextView) rootView.findViewById(R.id.textName5);
        TextView textAddress5 = (TextView) rootView.findViewById(R.id.textAddress5);
        TextView textCountry5 = (TextView) rootView.findViewById(R.id.textCountry5);

        ImageView image6 = (ImageView) rootView.findViewById(R.id.image6);
        TextView textName6 = (TextView) rootView.findViewById(R.id.textName6);
        TextView textAddress6 = (TextView) rootView.findViewById(R.id.textAddress6);
        TextView textCountry6 = (TextView) rootView.findViewById(R.id.textCountry6);




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
        if (currentUser != null) {
            View view = getView();
            new LocationDBLogic().select(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {

                    TextView textName1 = view.findViewById(R.id.textName1);
                    TextView textAddress1 = view.findViewById(R.id.textAddress1);
                    TextView textCountry1 = view.findViewById(R.id.textCountry1);
                    ImageView image1 = view.findViewById(R.id.image1);

                    TextView textName2 = view.findViewById(R.id.textName2);
                    TextView textAddress2 = view.findViewById(R.id.textAddress2);
                    TextView textCountry2 = view.findViewById(R.id.textCountry2);

                    TextView textName3 = view.findViewById(R.id.textName3);
                    TextView textAddress3 = view.findViewById(R.id.textAddress3);
                    TextView textCountry3 = view.findViewById(R.id.textCountry3);

                    TextView textName4 = view.findViewById(R.id.textName4);
                    TextView textAddress4 = view.findViewById(R.id.textAddress4);
                    TextView textCountry4 = view.findViewById(R.id.textCountry4);

                    TextView textName5 = view.findViewById(R.id.textName5);
                    TextView textAddress5 = view.findViewById(R.id.textAddress5);
                    TextView textCountry5 = view.findViewById(R.id.textCountry5);

                    TextView textName6 = view.findViewById(R.id.textName6);
                    TextView textAddress6 = view.findViewById(R.id.textAddress6);
                    TextView textCountry6 = view.findViewById(R.id.textCountry6);


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


                            textName1.setText(object.getName());
                            textAddress1.setText(object.getCity());
                            textCountry1.setText(object.getCountry());




                        }
                        //example for delete location
                        //new LocationDBLogic().delete("-MvVkRv0fWWEHa1SOqWs");



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