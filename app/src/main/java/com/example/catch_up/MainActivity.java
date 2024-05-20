package com.example.catch_up;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.catch_up.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

// utilities
import android.text.TextUtils;
import android.util.Log;

// places api
import com.google.android.libraries.places.api.Places;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "main_activity_log";
    final String[] BASIC_PERMISSIONS = {
            Manifest.permission.INTERNET,
    };
    final String[] LOCATION_PERMISSIONS = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private final ActivityResultLauncher<String[]> multiplePermissionActivityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), isGranted -> {
                Log.d(TAG, "Launcher result: " + isGranted.toString());
                if (isGranted.containsValue(false)) {
                    Log.d(TAG, "At least one of the permissions was not granted");
                }
            });

    FirebaseAuth auth;
    Button button;
    TextView textView;
    FirebaseUser user;
    ActivityMainBinding binding;
    Button settingsButton;

    protected static boolean hasPermissions(Context context, String[] permissions) {
        boolean permissionStatus = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permission granted: " + permission);
            } else {
                Log.d(TAG, "Permission not granted: " + permission);
                permissionStatus = false;
            }
        }
        return permissionStatus;
    }

    private void askPermissions(String[] permissions) {
        if (!hasPermissions(this, permissions)) {
            Log.d(TAG, "Launching multiple contract permission launcher for required permissions");
            multiplePermissionActivityResultLauncher.launch(permissions);
        } else {
            Log.d(TAG, "Required permissions already granted");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

       // Define a variable to hold the Places API key.
       String apiKey = BuildConfig.PLACES_API_KEY;

       // Log an error if apiKey is not set.
       if (TextUtils.isEmpty(apiKey) || apiKey.equals("DEFAULT_API_KEY")) {
          Log.e("Places test", "No api key");
          finish();
          return;
       }

       // Initialize the SDK
       Places.initializeWithNewPlacesApiEnabled(getApplicationContext(), apiKey);


        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        askPermissions(BASIC_PERMISSIONS);

        binding.fab.setOnClickListener(view -> showBottomDialog());

        replaceFragment(new ExploreFragment());
        binding.bottomNavigationView.setBackground(null);

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.explore) {
                Log.d(TAG, "Explore selected");
                replaceFragment(new ExploreFragment());
            } else if (item.getItemId() == R.id.saved) {
                Log.d(TAG, "Saved selected");
                replaceFragment(new SavedFragment());
            }
            return true;
        });

        // Handle settings icon click
        ImageView settingsIcon = findViewById(R.id.settings_icon);
        settingsIcon.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void showBottomDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottomsheet_layout);

        ImageView cancelButton = dialog.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(v -> dialog.dismiss());

        // Find and set click listeners for the new buttons
        Button createGameButton = dialog.findViewById(R.id.create_game_button);
        Button joinGameButton = dialog.findViewById(R.id.join_game_button);

        createGameButton.setOnClickListener(v -> {
            // Handle create game button click
            replaceFragment(new GameTypeFragment());
            dialog.dismiss();
        });

        joinGameButton.setOnClickListener(v -> {
            // Handle join game button click
            dialog.dismiss();
        });

        dialog.show();
        Objects.requireNonNull(dialog.getWindow()).setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }
}
