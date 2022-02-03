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
        ImageView imageView1 = (ImageView) rootView.findViewById(R.id.imageView7);

        imageView1.setImageResource(R.drawable.cafe);

        TextView recentlySaved = (TextView)rootView.findViewById(R.id.textView3);
        recentlySaved.setText("Recently Saved");

        TextView nearBy = (TextView)rootView.findViewById(R.id.textView4);
        nearBy.setText("Nearby Saved");

        ImageView imageView2 = (ImageView) rootView.findViewById(R.id.imageView8);
        imageView2.setImageResource(R.drawable.guinness);

        return rootView;
    }
}