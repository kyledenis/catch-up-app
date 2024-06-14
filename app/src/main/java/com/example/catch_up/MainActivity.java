package com.example.catch_up;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.catch_up.databinding.ActivityMainBinding;
import com.google.android.libraries.places.api.Places;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String[] BASIC_PERMISSIONS = {Manifest.permission.INTERNET};
    private static final String[] LOCATION_PERMISSIONS = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), this::onPermissionsResult);
    private ActivityMainBinding binding;
    private FirebaseAuth auth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initFirebaseAuth();
        checkPlacesApiKey();
        initPlacesApi();
        askPermissions(BASIC_PERMISSIONS);

        // Request location permissions early
        askPermissions(LOCATION_PERMISSIONS);

        setupUI();

        // Load the ExploreFragment by default if this is the first time creating the activity
        if (savedInstanceState == null) {
            Log.d(TAG, "Loading ExploreFragment by default");
            replaceFragment(new ExploreFragment());
        }
    }

    private void initFirebaseAuth() {
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if (user == null) {
            navigateToLogin();
        }
    }

    private void checkPlacesApiKey() {
        String apiKey = BuildConfig.PLACES_API_KEY;
        if (TextUtils.isEmpty(apiKey)) {
            Log.e(TAG, "No Places API key found");
            finish();
        }
    }

    private void initPlacesApi() {
        String apiKey = BuildConfig.PLACES_API_KEY;
        Places.initialize(getApplicationContext(), apiKey);
    }

    private void askPermissions(String[] permissions) {
        if (!hasPermissions(this, permissions)) {
            permissionLauncher.launch(permissions);
        } else {
            Log.d(TAG, "All required permissions are already granted");
        }
    }

    private void setupUI() {
        // Floating Action Button (FAB) click listener
        binding.fab.setOnClickListener(view -> showBottomDialog());

        // Set bottom navigation view background to null
        binding.bottomNavigationView.setBackground(null);

        // Bottom navigation item selected listener
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment;
            int itemId = item.getItemId();

            if (itemId == R.id.explore) {
                Log.d(TAG, "Explore selected");
                selectedFragment = new ExploreFragment();
            } else if (itemId == R.id.saved) {
                Log.d(TAG, "Saved selected");
                selectedFragment = new SavedFragment();
            } else {
                return false;
            }

            replaceFragment(selectedFragment);
            return true;
        });

        // Handle settings icon click
        binding.getRoot().findViewById(R.id.settings_icon).setOnClickListener(v -> navigateToSettings());
    }

    private void navigateToLogin() {
        startActivity(new Intent(this, Login.class));
        finish();
    }

    private void navigateToSettings() {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void showBottomDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottomsheet_layout);
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

        ImageView cancelButton = dialog.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(v -> dialog.dismiss());

        dialog.findViewById(R.id.create_game_button).setOnClickListener(v -> {
            replaceFragment(new GameTypeFragment());
            dialog.dismiss();
        });

        dialog.findViewById(R.id.join_game_button).setOnClickListener(v -> {
            // Handle join game button click
            dialog.dismiss();
        });

        dialog.show();
    }

    private void onPermissionsResult(Map<String, Boolean> isGranted) {
        if (isGranted.containsValue(false)) {
            showPermissionDeniedDialog();
        }
    }

    private boolean hasPermissions(Context context, String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void showPermissionDeniedDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Permissions Required")
                .setMessage("Location permissions are required to use this feature. Please enable them in your device settings.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
