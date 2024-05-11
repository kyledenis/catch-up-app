package com.example.catch_up;

import android.os.Bundle;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.ActivityResultLauncher;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener;

public class ExploreFragment
		extends Fragment
		implements
		OnRequestPermissionsResultCallback,
		OnMapReadyCallback,
		OnMyLocationButtonClickListener,
		OnMyLocationClickListener,
		GoogleMap.OnMapClickListener
{
	private static final String[] LOCATION_PERMISSIONS = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};

	private static final String TAG = "explore_fragment_log";

	// Map objects
	private SupportMapFragment mapFragment; //wrapper and life cycle handler for map view
	private GoogleMap map;

	// Default map UI variables / objects
	public static final LatLng DEFAULT_AUSTRALIA = new LatLng(-25, 135); // default initial lat/log
	private int[] mapPad = new int[]{50,150,50,150}; // map padding in pixels {left, top, right, bottom} - constrains map UI controls
	private boolean hasZoomControl = true;
	private boolean hasCompass = true;
	private boolean zoomGestures = true;
	private boolean rotateGestures = true;

	@SuppressLint("MissingPermission")
	private ActivityResultLauncher<String[]> multiplePermissionActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), isGranted ->
			{
				Log.d(TAG, "Launcher result: " + isGranted.toString());
				if (isGranted.containsValue(false))
				{
					Toast.makeText(this.getContext(), "Location permissions currently disabled", Toast.LENGTH_SHORT).show();
				}
				else
				{
					Toast.makeText(this.getContext(), "Location permissions enabled", Toast.LENGTH_SHORT).show();
					map.setMyLocationEnabled(true);
					map.setOnMyLocationButtonClickListener(this);
					map.setOnMyLocationClickListener(this);
				}
			});

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Initialise view
        View view = inflater.inflate(R.layout.fragment_explore, container, false);
        // initialise map fragment
		  mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);
		  mapFragment.getMapAsync(this);
        //return view
        return view;
    }

	 @Override
	 public void onMapReady(GoogleMap googleMap)
	 {
		 Log.d(TAG, "onMapReady() called");
		 map = googleMap;

		 // pass in our map and ui settings declared up top
		 map.setPadding(mapPad[0], mapPad[1], mapPad[2], mapPad[3]);


		 enableLocationPermissions();

		 UiSettings mapSet = map.getUiSettings();
		 mapSet.setZoomControlsEnabled(hasZoomControl);
		 mapSet.setCompassEnabled(hasCompass);
		 mapSet.setZoomGesturesEnabled(zoomGestures);
		 mapSet.setRotateGesturesEnabled(rotateGestures);

		 //in future can check if we can get coordinates from device locale or location permission
		 //in the mean time we can default to Australia instead of Africa
		 map.moveCamera(CameraUpdateFactory.newLatLng(DEFAULT_AUSTRALIA));

		 map.setOnMapClickListener(this);
	 }

	@Override
	public void onMapClick(LatLng latLng)
	{
		String clickCoords = latLng.latitude + " : " + latLng.longitude;
		Log.d(TAG, "Map click registered | " + clickCoords);

		//Place marker at clicked coordinates
		MarkerOptions markerOptions = new MarkerOptions();
		markerOptions.position(latLng);
		markerOptions.title(clickCoords);
		map.clear();
		map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
		map.addMarker(markerOptions);
	}
	@SuppressLint("MissingPermission")
	public void enableLocationPermissions()
	{
		boolean hasPermissions = true;
		for (String permission : LOCATION_PERMISSIONS)
		{
			if (ContextCompat.checkSelfPermission(this.getContext(), permission) == PackageManager.PERMISSION_GRANTED)
			{
				Log.d(TAG, "Permission granted: " + permission);
			}
			else
			{
				Log.d(TAG, "Permission not granted: " + permission);
				hasPermissions = false;
			}
		}
		if (!hasPermissions)
		{
			multiplePermissionActivityResultLauncher.launch(LOCATION_PERMISSIONS);
		}
		else
		{
			map.setMyLocationEnabled(true);
			map.setOnMyLocationButtonClickListener(this);
			map.setOnMyLocationClickListener(this);
		}

	}

	@Override
	public void onMyLocationClick(@NonNull Location location)
	{
		Toast.makeText(this.getActivity(), "Current location:\n" + location, Toast.LENGTH_LONG).show();
	}

	@Override
	public boolean onMyLocationButtonClick()
	{
		Toast.makeText(this.getActivity(), "MyLocation button clicked", Toast.LENGTH_SHORT).show();
		// Return false so that we don't consume the event and the default behavior still occurs
		// (the camera animates to the user's current position).
		return false;
	}

}