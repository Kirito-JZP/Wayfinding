package com.main.wayfinding.fragment.preference;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.main.wayfinding.databinding.FragmentAccountBinding;
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
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPreferenceBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        return root;
    }
}
