package com.main.wayfinding.fragment.preference;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.annotation.NonNull;
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
    private FragmentPreferenceBinding binding;

    private ListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_preference, container, false);

        listView = (ListView) view.findViewById(R.id.list_view);


        String preferences[] = {"Preference1", "Preference2", "Preference3", "Preference4", "Preference5"};

        listView.setAdapter(new ArrayAdapter<String>(getActivity().getApplicationContext(),
                android.R.layout.preference_category, preferences));

        return view;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
