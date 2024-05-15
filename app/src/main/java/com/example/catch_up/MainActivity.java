package com.example.catch_up;

// functional
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.catch_up.databinding.ActivityMainBinding;
import android.content.Context;

// utilities
import android.util.Log;

// places api
import com.google.android.libraries.places.api.Places;



public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;

    private static final String TAG = "main_activity_log";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Places.initializeWithNewPlacesApiEnabled(getApplicationContext(), );

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.fab.setOnClickListener(view -> replaceFragment(new PlayFragment()));

        replaceFragment(new ExploreFragment());
        binding.bottomNavigationView.setBackground(null);

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.explore) {
               Log.d(TAG, "Explore selected");
               replaceFragment(new ExploreFragment());
            } else if (item.getItemId() == R.id.play) {
               Log.d(TAG, "Play selected");
               replaceFragment(new PlayFragment());
            } else if (item.getItemId() == R.id.saved) {
               Log.d(TAG, "Saved selected");
               replaceFragment(new SavedFragment());
            }
            return true;
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
}