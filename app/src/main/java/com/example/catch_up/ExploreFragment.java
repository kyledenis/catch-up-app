package com.example.catch_up;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.CameraUpdateFactory;

public class ExploreFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Initialise view
        View view = inflater.inflate(R.layout.fragment_explore, container, false);
        //Initialise map fragment
        SupportMapFragment supportMapFragment = (SupportMapFragment)
              getChildFragmentManager().findFragmentById(R.id.google_map);
        //Async map
        mapBehaviour(supportMapFragment);
        //return view
        return view;
    }

    private void mapBehaviour(SupportMapFragment supportMapFragment)
    {
		 //container method for map behaviour
		 supportMapFragment.getMapAsync(new OnMapReadyCallback()
		 {
			 @Override
			 public void onMapReady(GoogleMap googleMap)
			 {
				 googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener()
				 {
					 @Override
					 public void onMapClick(LatLng latLng)
					 {
						 //When clicked on map
						 //Initialise marker options
						 MarkerOptions markerOptions = new MarkerOptions();
						 //Set position of marker
						 markerOptions.position(latLng);
						 //Set title of marker
						 markerOptions.title(latLng.latitude + " : " + latLng.longitude);
						 //remove old marker
						 googleMap.clear();
						 //animate zoom
						 googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
						 //Add new marker to map
						 googleMap.addMarker(markerOptions);
					 }
				 });
			 }
		 });
    }
}