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
 * @author Last Modified By JIA
 * @version Revision: 0
 * Date: 2022/1/18 16:06
 */
public class PreferenceFragment extends Fragment {
    private FirebaseAuth auth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_preference, container, false);

        TextView recentlySaved = (TextView) rootView.findViewById(R.id.textView4);
        recentlySaved.setText("Recently Saved");

        ImageView imageView1 = (ImageView) rootView.findViewById(R.id.imageView3);

        imageView1.setImageResource(R.drawable.cafe);

        TextView country = (TextView) rootView.findViewById(R.id.textView9);
        country.setText("Country");

        TextView name = (TextView) rootView.findViewById(R.id.textView10);
        name.setText("Name");

        TextView address = (TextView) rootView.findViewById(R.id.textView11);
        address.setText("Address");

        TextView nearbySaved = (TextView) rootView.findViewById(R.id.textView12);
        nearbySaved.setText("Nearby Saved");

        ImageView imageView2 = (ImageView) rootView.findViewById(R.id.imageView4);
        imageView2.setImageResource(R.drawable.guinness);

        TextView country1 = (TextView) rootView.findViewById(R.id.textView11);
        country1.setText("Country");

        TextView name1 = (TextView) rootView.findViewById(R.id.textView11);
        name1.setText("Name");

        TextView address1 = (TextView) rootView.findViewById(R.id.textView11);
        address.setText("Address");

        ImageView imageView3 = (ImageView) rootView.findViewById(R.id.imageView5);
        imageView3.setImageResource(R.drawable.cafe);

        TextView country2 = (TextView) rootView.findViewById(R.id.textView20);
        country2.setText("Country");

        TextView name2 = (TextView) rootView.findViewById(R.id.textView21);
        name2.setText("Name");

        TextView address2 = (TextView) rootView.findViewById(R.id.textView22);
        address2.setText("Address");

        ImageView imageView4 = (ImageView) rootView.findViewById(R.id.imageView6);
        imageView3.setImageResource(R.drawable.cafe);

        TextView country3 = (TextView) rootView.findViewById(R.id.textView23);
        country3.setText("Country");

        TextView name3 = (TextView) rootView.findViewById(R.id.textView24);
        name3.setText("Name");

        TextView address3 = (TextView) rootView.findViewById(R.id.textView25);
        address3.setText("Address");


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
                            System.out.println(iterator.next().getValue().getDate());
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