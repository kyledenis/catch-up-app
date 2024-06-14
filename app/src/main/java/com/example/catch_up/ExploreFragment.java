package com.example.catch_up;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.catch_up.databinding.FragmentExploreBinding;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.net.SearchByTextRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public class ExploreFragment extends Fragment implements
        OnMapReadyCallback,
        OnMyLocationButtonClickListener,
        OnMyLocationClickListener,
        GoogleMap.OnMapClickListener {

    private static final LatLng DEFAULT_AUSTRALIA = new LatLng(-25, 135);
    private static final String TAG = "ExploreFragment";
    private static final String[] LOCATION_PERMISSIONS = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private static final int MAX_SEARCH_RESULT_COUNT = 15;

    private FragmentExploreBinding binding;
    private FragmentActivity associatedActivity;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location currentLocation;
    private final ActivityResultLauncher<String[]> locationRequestActivityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean fineLocationGranted = Boolean.TRUE.equals(result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false));
                boolean coarseLocationGranted = Boolean.TRUE.equals(result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false));

                if (fineLocationGranted && coarseLocationGranted) {
                    showToast("Location permissions enabled");
                    enableMapLocationTools();
                    getLastLocation();
                } else {
                    showPermissionDeniedDialog();
                }
            });
    private PlacesClient placesClient;
    private FirebaseAuth auth;
    private GoogleMap map;
    private FirebaseUser user;
    private CameraPosition lastCameraPosition;
    private boolean locationPermissionsGranted = false;
    private SupportMapFragment mapFragment;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        associatedActivity = requireActivity();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentExploreBinding.inflate(inflater, container, false);
        initFirebase();
        checkUserLoggedIn();
        initLocationProvider();
        initSearchView();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadInstanceState(savedInstanceState);
        initPlacesClient();
        checkGooglePlayServices();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        configureMap();
        if (locationPermissionsGranted) {
            enableMapLocationTools();
            getLastLocation();
        } else {
            requestLocationPermissions();
        }
        moveToInitialPosition();
    }

    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
    }

    private void checkUserLoggedIn() {
        if (user == null) {
            startActivity(new Intent(associatedActivity, Login.class));
            associatedActivity.finish();
        }
    }

    private void initLocationProvider() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(associatedActivity);
    }

    private void initSearchView() {
        binding.searchView.setIconified(true);
        binding.searchView.setOnQueryTextListener(new SearchTextListener());
        binding.searchView.setOnCloseListener(() -> {
            binding.searchView.clearFocus();
            return true;
        });
    }

    private void loadInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            lastCameraPosition = savedInstanceState.getParcelable("lastCameraPosition", CameraPosition.class);
        } else if (savedInstanceState != null) {
            lastCameraPosition = savedInstanceState.getParcelable("lastCameraPosition");
        }
    }

    private void initPlacesClient() {
        placesClient = Places.createClient(associatedActivity);
    }

    private void checkGooglePlayServices() {
        GoogleApiAvailability googleApiAvailability = new GoogleApiAvailability();
        int connectionCode = googleApiAvailability.isGooglePlayServicesAvailable(associatedActivity);
        if (connectionCode != ConnectionResult.SUCCESS) {
            googleApiAvailability.showErrorNotification(associatedActivity, connectionCode);
        } else {
            mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);
            if (mapFragment != null) {
                mapFragment.getMapAsync(this);
            } else {
                Log.e(TAG, "Failed to initialise map fragment");
            }
        }
    }

    private void configureMap() {
        UiSettings mapSettings = map.getUiSettings();
        mapSettings.setZoomControlsEnabled(true);
        mapSettings.setZoomGesturesEnabled(true);
        mapSettings.setRotateGesturesEnabled(false);
        mapSettings.setCompassEnabled(false);
        map.setPadding(5, 150, 5, 150);
        map.setOnMapClickListener(this);
    }

    private void requestLocationPermissions() {
        locationRequestActivityResultLauncher.launch(LOCATION_PERMISSIONS);
    }

    private void enableMapLocationTools() {
        try {
            map.setMyLocationEnabled(true);
            map.setOnMyLocationButtonClickListener(this);
            map.setOnMyLocationClickListener(this);
        } catch (SecurityException e) {
            Log.e(TAG, "Failed to enable location tools: " + e);
        }
    }

    private void getLastLocation() {
        try {
            Task<Location> task = fusedLocationProviderClient.getLastLocation();
            task.addOnSuccessListener(location -> {
                if (location != null) {
                    currentLocation = location;
                    moveToInitialPosition();
                }
            });
        } catch (SecurityException e) {
            Log.e(TAG, "Failed to get last location: " + e);
        }
    }

    private void moveToInitialPosition() {
        CameraUpdate initialCameraUpdate = CameraUpdateFactory.newLatLng(DEFAULT_AUSTRALIA);
        if (lastCameraPosition != null) {
            initialCameraUpdate = CameraUpdateFactory.newCameraPosition(lastCameraPosition);
        } else if (currentLocation != null) {
            initialCameraUpdate = CameraUpdateFactory.newLatLng(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
        }
        map.moveCamera(initialCameraUpdate);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        map.clear();
        map.addMarker(new MarkerOptions().position(latLng).title(latLng.latitude + " : " + latLng.longitude));
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
    }

    @Override
    public boolean onMyLocationButtonClick() {
        showToast("Getting current location...");
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        showToast("Current location:\n" + location);
    }

    private void showPermissionDeniedDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(associatedActivity)
                .setTitle("Permissions Required")
                .setMessage("Location permissions are required to use this feature. Please enable them in your device settings.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void showToast(String message) {
        Toast.makeText(associatedActivity, message, Toast.LENGTH_SHORT).show();
    }

    private class SearchTextListener implements SearchView.OnQueryTextListener {
        @Override
        public boolean onQueryTextSubmit(String query) {
            binding.searchView.clearFocus();
            if (query.trim().isEmpty()) {
                return false;
            }
            performSearch(query.trim());
            return true;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            return false;
        }

        private void performSearch(String query) {
            SearchByTextRequest request = SearchByTextRequest.builder(query, Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS, Place.Field.TYPES))
                    .setMaxResultCount(MAX_SEARCH_RESULT_COUNT)
                    .setLocationRestriction(RectangularBounds.newInstance(map.getProjection().getVisibleRegion().latLngBounds))
                    .setRankPreference(SearchByTextRequest.RankPreference.DISTANCE)
                    .build();

            placesClient.searchByText(request).addOnSuccessListener(response -> {
                List<Place> places = response.getPlaces();
                if (!places.isEmpty()) {
                    map.clear();
                    for (Place place : places) {
                        map.addMarker(new MarkerOptions().title(place.getName()).position(place.getLatLng()).snippet(place.getAddress()));
                    }
                } else {
                    showToast("No places matching your query were found within view.");
                }
            });
        }
    }
}
