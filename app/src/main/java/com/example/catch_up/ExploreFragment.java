package com.example.catch_up;

//persistence & context
import android.os.Bundle;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;

//utility
import android.util.Log;
import android.annotation.SuppressLint;
import androidx.annotation.NonNull;
import android.widget.Toast;

//fragments and views
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.SearchView;

//permissions
import android.Manifest;
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.ActivityResultLauncher;

//maps
import android.location.Location;
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

	// utility
	private static final String TAG = "explore_fragment_log";

	// permissions
	private static final String[] LOCATION_PERMISSIONS =
			{
					Manifest.permission.ACCESS_COARSE_LOCATION,
					Manifest.permission.ACCESS_FINE_LOCATION
			};

	// Map objects
	private SupportMapFragment mapFragment; //wrapper and life cycle handler for map view
	private GoogleMap map;

	//widgets
	private SearchView searchView;

	// Default map UI variables / objects
	public static final LatLng DEFAULT_AUSTRALIA = new LatLng(-25, 135); // default initial lat/log
	private final int[] mapPad = new int[]{5,150,5,150}; // map padding in pixels {left, top, right, bottom} - constrains map UI controls
	private final boolean hasZoomControl = true;
	private final boolean hasCompass = true;
	private final boolean zoomGestures = true;
	private final boolean rotateGestures = true;



	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

		  // Initialise view
        View view = inflater.inflate(R.layout.fragment_explore, container, false);

		  // Initialise search view
		  searchView = view.findViewById(R.id.search_view);
		  searchView.setIconified(false);
		  searchView.setOnQueryTextListener(new QueryTextListener());
		  searchView.setOnCloseListener(new CloseListener());

		  // initialise map fragment
		  mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.google_map);
		  mapFragment.getMapAsync(this);
        //return view
        return view;
    }

	 @Override
	 public void onMapReady(@NonNull GoogleMap googleMap)
	 {
		 Log.d(TAG, "onMapReady() called");
		 map = googleMap;

		 // pass in our map and ui settings declared up top
		 map.setPadding(mapPad[0], mapPad[1], mapPad[2], mapPad[3]);


		 setupMapLocationTools();

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

	private void setupMapLocationTools()
	{
		if (hasPermissions(LOCATION_PERMISSIONS))
		{
			enableMapLocationTools(map);
		}
		else
		{
			locationRequestActivityResultLauncher.launch(LOCATION_PERMISSIONS);
		}
	}
	public boolean hasPermissions(String[] permissions)
	{
		boolean hasPermissions = true;
		for (String permission : permissions)
		{
			if (ContextCompat.checkSelfPermission(this.requireContext(), permission) == PackageManager.PERMISSION_GRANTED)
			{
				Log.d(TAG, "Permission granted: " + permission);
			}
			else
			{
				Log.d(TAG, "Permission not granted: " + permission);
				hasPermissions = false;
			}
		}
		return hasPermissions;
	}

	private final ActivityResultLauncher<String[]> locationRequestActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
			result ->
			{
				Boolean fineLocationGranted = Boolean.TRUE.equals(result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false));
				Boolean coarseLocationGranted = Boolean.TRUE.equals(result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION,false));

				if (fineLocationGranted && coarseLocationGranted)
				{
					Toast.makeText(this.getActivity(), "Location permissions enabled", Toast.LENGTH_SHORT).show();
					enableMapLocationTools(map);
				}
				else
				{
					Toast.makeText(this.getActivity(), "Location permissions currently disabled. \nSome functions may not work.", Toast.LENGTH_SHORT).show();
				}
			});

	@SuppressLint("MissingPermission")
	private void enableMapLocationTools(GoogleMap googleMap)
	{
		googleMap.setMyLocationEnabled(true);
		googleMap.setOnMyLocationButtonClickListener(this);
		googleMap.setOnMyLocationClickListener(this);
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

	public class QueryTextListener implements SearchView.OnQueryTextListener
	{
		@Override
		public boolean onQueryTextSubmit(String query)
		{
			searchView.clearFocus();
			Log.d(TAG, "onQueryTextSubmit triggered");
			String queryText = query.trim();
			if (queryText.isEmpty())
			{
				return false;
			}
			Log.d(TAG, "Query text submitted: " + queryText);
			Toast.makeText(getActivity(), "Query text submitted: " + queryText, Toast.LENGTH_SHORT).show();

			return true;
		}

		public boolean onQueryTextChange(String newText)
		{
			return false;
		}
	}

	public class CloseListener implements SearchView.OnCloseListener
	{
		@Override
		public boolean onClose()
		{
			searchView.clearFocus();
			return false;
		}
	}

}