package com.example.catch_up;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.CameraUpdateFactory;

public class ExploreFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener{

	final String TAG = "explore_fragment_log";
	private SupportMapFragment mapFragment; //wrapper and life cycle handler for map view
	private GoogleMap map;

	// Default map UI variables / objects
	public static final LatLng DEFAULT_AUSTRALIA = new LatLng(-25, 135); // default initial lat/log
	private int[] mapPad = new int[]{50,150,50,150}; // map padding in pixels {left, top, right, bottom} - constrains map UI controls
	private boolean hasZoomControl = true;
	private boolean hasCompass = true;
	private boolean zoomGestures = true;
	private boolean rotateGestures = true;


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

}