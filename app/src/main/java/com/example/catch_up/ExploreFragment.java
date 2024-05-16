package com.example.catch_up;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ExploreFragment
        extends Fragment
        implements
        OnRequestPermissionsResultCallback,
        OnMapReadyCallback,
        OnMyLocationButtonClickListener,
        OnMyLocationClickListener,
        GoogleMap.OnMapClickListener {
    // Default map UI variables / objects
    public static final LatLng DEFAULT_AUSTRALIA = new LatLng(-25, 135); // default initial lat/log
    private static final String[] LOCATION_PERMISSIONS = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
    private static final String TAG = "explore_fragment_log";
    private final int[] mapPad = new int[]{50, 150, 50, 150}; // map padding in pixels {left, top, right, bottom} - constrains map UI controls

    FirebaseAuth auth;
    FirebaseUser user;

    private GoogleMap map;

    @SuppressLint("MissingPermission")
    private final ActivityResultLauncher<String[]> multiplePermissionActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), isGranted -> {
        Log.d(TAG, "Launcher result: " + isGranted.toString());
        if (isGranted.containsValue(false)) {
            Toast.makeText(this.getContext(), "Location permissions currently disabled", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this.getContext(), "Location permissions enabled", Toast.LENGTH_SHORT).show();
            map.setMyLocationEnabled(true);
            map.setOnMyLocationButtonClickListener(this);
            map.setOnMyLocationClickListener(this);
        }
    });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_explore, container, false);

        // Initialize Firebase authentication
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        // Check if the user is logged in
        if (user == null) {
            Intent intent = new Intent(requireActivity().getApplicationContext(), Login.class);
            startActivity(intent);
            requireActivity().finish();
        }

        // Initialize the map fragment
        // Wrapper and life cycle handler for map view
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        // Return the view
        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Log.d(TAG, "onMapReady() called");
        map = googleMap;

        UiSettings mapSet = map.getUiSettings();
        mapSet.setZoomControlsEnabled(true);
        mapSet.setZoomGesturesEnabled(true);
        mapSet.setRotateGesturesEnabled(false); // Disabled for UX simplicity
        mapSet.setCompassEnabled(false); // Not needed if rotate gestures are disabled

        enableLocationPermissions();

        map.setPadding(mapPad[0], mapPad[1], mapPad[2], mapPad[3]);

        map.moveCamera(CameraUpdateFactory.newLatLng(DEFAULT_AUSTRALIA));
        map.setOnMapClickListener(this);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        String clickCoords = latLng.latitude + " : " + latLng.longitude;
        Log.d(TAG, "Map click registered | " + clickCoords);

        // Place marker at clicked coordinates
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title(clickCoords);
        map.clear();
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
        map.addMarker(markerOptions);
    }

    @SuppressLint("MissingPermission")
    public void enableLocationPermissions() {
        boolean hasPermissions = true;
        for (String permission : LOCATION_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this.requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permission granted: " + permission);
            } else {
                Log.d(TAG, "Permission not granted: " + permission);
                hasPermissions = false;
            }
        }

        if (!hasPermissions) {
            multiplePermissionActivityResultLauncher.launch(LOCATION_PERMISSIONS);
        } else {
            map.setMyLocationEnabled(true);
            map.setOnMyLocationButtonClickListener(this);
            map.setOnMyLocationClickListener(this);
        }
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this.getActivity(), "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this.getActivity(), "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }
}