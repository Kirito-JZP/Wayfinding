package com.main.wayfinding;

import android.os.Bundle;
import android.util.DisplayMetrics;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.main.wayfinding.databinding.ActivityMainBinding;

/**
 * Define the activity including three fragments with navigation
 *
 * @author JIA
 * @author Last Modified By JIA
 * @version Revision: 0
 * Date: 2022/1/16 1:43
 */

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_map, R.id.navigation_preference, R.id.navigation_account)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        //NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        float dpWidth = dm.widthPixels / (dm.xdpi) * 160;
        float dpHeight = dm.heightPixels / (dm.ydpi) * 160;
        WayfindingApp.setDpWidth(dpWidth);
        WayfindingApp.setDpHeight(dpHeight);
    }
}