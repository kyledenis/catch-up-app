package com.example.catch_up;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.catch_up.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.util.Log;
import android.Manifest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.ActivityResultLauncher;
import android.content.Context;



public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    Button button;
    TextView textView;
    FirebaseUser user;
    ActivityMainBinding binding;
    Button settingsButton;

    private static final String TAG = "main_activity_log";


    final String[] BASIC_PERMISSIONS = {
          Manifest.permission.INTERNET,
    };

   
    final String[] LOCATION_PERMISSIONS = {
          Manifest.permission.ACCESS_COARSE_LOCATION,
          Manifest.permission.ACCESS_FINE_LOCATION
    };

    private final ActivityResultLauncher<String[]> multiplePermissionActivityResultLauncher =
          registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), isGranted ->
          {
              Log.d(TAG, "Launcher result: " + isGranted.toString());
              if (isGranted.containsValue(false))
              {
                  Log.d(TAG, "At least one of the permissions was not granted");
              }
          });

    protected static boolean hasPermissions(Context context, String[] permissions)
    {
        boolean permissionStatus = true;
        for (String permission : permissions)
        {
            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED)
            {
                Log.d(TAG, "Permission granted: " + permission);
            }
            else
            {
                Log.d(TAG, "Permission not granted: " + permission);
                permissionStatus = false;
            }
        }
        return permissionStatus;
    }

    private void askPermissions(String[] permissions)
    {
        if (!hasPermissions(permissions))
        {
            Log.d(TAG, "Launching multiple contract permission launcher for required permissions");
            multiplePermissionActivityResultLauncher.launch(permissions);
        }
        else
        {
            Log.d(TAG, "Required permissions already granted");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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