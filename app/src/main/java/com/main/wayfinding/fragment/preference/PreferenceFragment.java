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

import com.main.wayfinding.R;
import com.main.wayfinding.databinding.FragmentPreferenceBinding;

/**
 * Define the fragment used for saving user preference(home, work or favourite places)
 *
 * @author JIA
 * @author Last Modified By JIA
 * @version Revision: 0
 * Date: 2022/1/18 16:06
 */
public class PreferenceFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_preference, container, false);

        TextView recentlySaved = (TextView)rootView.findViewById(R.id.textView4);
        recentlySaved.setText("Recently Saved");

        ImageView imageView1 = (ImageView) rootView.findViewById(R.id.imageView3);

        imageView1.setImageResource(R.drawable.cafe);

        TextView country = (TextView)rootView.findViewById(R.id.textView9);
        country.setText("Country");

        TextView name = (TextView)rootView.findViewById(R.id.textView10);
        name.setText("Name");

        TextView address = (TextView)rootView.findViewById(R.id.textView11);
        address.setText("Address");

        TextView nearbySaved = (TextView)rootView.findViewById(R.id.textView12);
        nearbySaved.setText("Nearby Saved");

        ImageView imageView2 = (ImageView) rootView.findViewById(R.id.imageView4);
        imageView2.setImageResource(R.drawable.guinness);

        TextView country1 = (TextView)rootView.findViewById(R.id.textView11);
        country1.setText("Country");

        TextView name1 = (TextView)rootView.findViewById(R.id.textView11);
        address.setText("Name");

        TextView address1 = (TextView)rootView.findViewById(R.id.textView11);
        address.setText("Address");


        return rootView;
    }
}