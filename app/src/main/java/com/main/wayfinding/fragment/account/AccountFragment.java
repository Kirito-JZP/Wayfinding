package com.main.wayfinding.fragment.account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.SupportMapFragment;
import com.main.wayfinding.R;
import com.main.wayfinding.databinding.FragmentAccountBinding;
import com.main.wayfinding.databinding.FragmentMapBinding;

/**
 * Define the fragment used for displaying and changing user info
 *
 * @author JIA
 * @author Last Modified By JIA
 * @version Revision: 0
 * Date: 2022/1/18 16:06
 */
public class AccountFragment extends Fragment {
    private FragmentAccountBinding binding;
    private boolean auth;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        return root;
    }



}
