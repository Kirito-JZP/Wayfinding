package com.main.wayfinding;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.google.ar.sceneform.ux.ArFragment;
import com.main.wayfinding.databinding.ActivityArnavigationBinding;

/**
 * Define the activity for AR navigation
 *
 * @author JIA
 * @author Last Modified By JIA
 * @version Revision: 0
 * Date: 2022/2/18 15:43
 */
public class ARNavigationActivity extends AppCompatActivity {

    private ArFragment arFragment;

    private ActivityArnavigationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityArnavigationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
    }

}