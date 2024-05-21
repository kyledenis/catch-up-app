package com.example.catch_up;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;



//utility
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import androidx.annotation.NonNull;

import android.view.ViewStub;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

//fragments and views
import android.view.LayoutInflater;
import android.view.View;
import android.content.Context;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.SearchView;

//permissions
import android.Manifest;
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.ActivityResultLauncher;
import androidx.fragment.app.FragmentActivity;

//maps
import android.location.Location;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener;

//places
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPhotoResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.net.SearchByTextRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.model.LatLngBounds;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.card.MaterialCardView;


public class ExploreFragment extends Fragment
		implements
		OnRequestPermissionsResultCallback,
		OnMapReadyCallback,
		OnMyLocationButtonClickListener,
		OnMyLocationClickListener,
		GoogleMap.OnMapClickListener,
		GoogleMap.OnMarkerClickListener
{

	// utility
	private static final String TAG = "explore_fragment_log";
	private FragmentActivity associatedActivity;

	// location permissions and location permissions state
	private static final String[] LOCATION_PERMISSIONS =
			{
					Manifest.permission.ACCESS_COARSE_LOCATION,
					Manifest.permission.ACCESS_FINE_LOCATION
			};
	private boolean locationPermissionsGranted = false;

	// location provider and last retrieved location
	FusedLocationProviderClient fusedLocationProviderClient = null;
	Location currentLocation;

	// places client and search defaults
	PlacesClient placesClient;
	private final List<Place.Field> placeFields = Arrays.asList(
			Place.Field.ID,
			Place.Field.NAME,
			Place.Field.LAT_LNG,
			Place.Field.ADDRESS,
			Place.Field.TYPES,
			Place.Field.EDITORIAL_SUMMARY,
			Place.Field.PHOTO_METADATAS);
	private final int MAX_SEARCH_RESULT_COUNT = 10;

	// SearchByTextRequestBuilder only takes 1 setIncludedType
	// - we need to make multiple SearchByTextRequests to both restrict and include different types
	// - the following types were retrieved  from developers.google.com/maps/documentation/places/android-sdk/place-types (May, 2024)
	// - place types can and should be refined on testing
	private final String[] cultureTypes =
			{
					"art_gallery", "museum", "performing_arts_theater"
			};
	private final String[] entertainmentTypes =
			{
					"amusement_center", "amusement_park", "aquarium", "banquet_hall",
					"bowling_alley", "community_center", "casino", "convention_center",
					"cultural_center", "dog_park", "event_venue", "hiking_area",
					"historical_landmark", "marina", "movie_rental", "movie_theater",
					"national_park", "night_club", "park", "tourist_attraction",
					"visitor_center", "wedding_venue", "zoo"
			};
	private final String[][] includedTypes = {cultureTypes, entertainmentTypes};


	//widgets
	private SearchView searchView;

	// Map objects and defaults
	private SupportMapFragment mapFragment; //wrapper and life cycle handler for map view
	private GoogleMap map;
	private CameraPosition lastCameraPosition;
	public static final LatLng DEFAULT_AUSTRALIA = new LatLng(-25, 135); // default initial lat/log
	private final int[] mapPad = new int[]{5,150,5,150}; // map padding in pixels {left, top, right, bottom} - constrains map UI controls
	private final boolean hasZoomControl = true;
	private final boolean hasCompass = true;
	private final boolean zoomGestures = true;
	private final boolean rotateGestures = true;

	private ViewStub placeCardStub;


	@Override
	public void onSaveInstanceState(@NonNull Bundle outState)
	{
		super.onSaveInstanceState(outState);
		if (map != null)
		{
			outState.putParcelable("lastCameraPosition", map.getCameraPosition());
		}
	}

	@SuppressLint("NewApi")
	public void loadInstanceState(Bundle loadState)
	{
		boolean supportedSDK = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU);
		if (loadState != null && supportedSDK)
		{
			lastCameraPosition = loadState.getParcelable("lastCameraPosition", CameraPosition.class);
		}
		if (loadState != null && !supportedSDK)
		{
			Log.e(TAG, "Build version[" + Build.VERSION.SDK_INT + "] doesn't support .getParcelable()");
		}
	}

	@Override
	public void onAttach(@NonNull Context context)
	{
		super.onAttach(context);
		Log.d(TAG, "Acquiring reference associated activity");
		associatedActivity = requireActivity();
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		Log.d(TAG, "ExploreFragment.onCreate() called");
		// Initialise view
		View view = inflater.inflate(R.layout.fragment_explore, container, false);
		// initialise locationProviderClient - will be needed when "around me" style Explore implemented
		fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(associatedActivity);
		// initialise map fragment
		mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.google_map);
		// Initialise search view
		searchView = view.findViewById(R.id.search_view);
		searchView.setIconified(false);

		placeCardStub = view.findViewById(R.id.placecard_stub);

		//return view
		return view;
    }

	 @Override
	 public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
	 {
		 Log.d(TAG, "ExploreFragment.onViewCreated() called");
		 // reload old instance states if any
		 loadInstanceState(savedInstanceState);
		 // initialise places client
		 placesClient = Places.createClient(associatedActivity);
		 // check google play services is available
		 GoogleApiAvailability googleApiAvailability = new GoogleApiAvailability();
		 int connectionCode = googleApiAvailability.isGooglePlayServicesAvailable(associatedActivity);
		 boolean connectionSuccess = new ConnectionResult(connectionCode).isSuccess();

		 if (mapFragment == null)
		 {
			 Log.e(TAG, "mapFragment failed to initialise");
		 }
		 else if (!connectionSuccess)
		 {
			 String errorString = googleApiAvailability.getErrorString(connectionCode);
			 Log.e(TAG,"GooglePlayServices connection error: " + errorString);
			 googleApiAvailability.showErrorNotification(associatedActivity, connectionCode);
		 }
		 else
		 {
			 // fetch the map from google play services
			 mapFragment.getMapAsync(this);


			 SearchTextListener searchTextListener = new SearchTextListener();
			 searchView.setOnQueryTextListener(searchTextListener);
			 searchView.setOnCloseListener(searchTextListener);
		 }
	 }

	 @Override
	 public void onMapReady(@NonNull GoogleMap googleMap)
	 {
		 Log.d(TAG, "onMapReady() called");

		 // map setup
		 map = googleMap;
		 map.setPadding(mapPad[0], mapPad[1], mapPad[2], mapPad[3]);

		 UiSettings mapSet = map.getUiSettings();
		 mapSet.setZoomControlsEnabled(hasZoomControl);
		 mapSet.setCompassEnabled(hasCompass);
		 mapSet.setZoomGesturesEnabled(zoomGestures);
		 mapSet.setRotateGesturesEnabled(rotateGestures);

		 // check location permissions are enabled
		 locationPermissionsGranted = hasPermissions(LOCATION_PERMISSIONS);
		 if (locationPermissionsGranted)
		 {
			 enableMapLocationTools(map);
			 getLastLocation();
		 }
		 else
		 {
			 // request location permissions
			 locationRequestActivityResultLauncher.launch(LOCATION_PERMISSIONS);
		 }

		 CameraUpdate mapInitCamera = CameraUpdateFactory.newLatLng(DEFAULT_AUSTRALIA);

		 if (lastCameraPosition != null)
		 {
			mapInitCamera = CameraUpdateFactory.newCameraPosition(lastCameraPosition);
		 }
		 else if (currentLocation != null)
		 {
			 LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
			 mapInitCamera = CameraUpdateFactory.newLatLng(currentLatLng);
		 }
		 map.moveCamera(mapInitCamera);
		 map.setOnMapClickListener(this);
		 map.setOnMarkerClickListener(this);

	 }



	 private void getLastLocation()
	 {
		 Log.d(TAG, "getLastLocation() called");
		 try
		 {
			 Task<Location> task = fusedLocationProviderClient.getLastLocation();
			 task.addOnSuccessListener(location -> {
				 if(location !=null)
				 {
					 currentLocation = location;
				 }
			 });
		 }
		 catch (SecurityException e)
		 {
			 Log.e(TAG, e.toString());
		 }
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

	public boolean hasPermissions(String[] permissions)
	{
		boolean hasPermissions = true;
		for (String permission : permissions)
		{
			if (ContextCompat.checkSelfPermission(associatedActivity, permission) == PackageManager.PERMISSION_GRANTED)
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
					Toast.makeText(associatedActivity, "Location permissions enabled", Toast.LENGTH_SHORT).show();
					enableMapLocationTools(map);
					getLastLocation();
				}
				else
				{
					Toast.makeText(associatedActivity, "Location permissions currently disabled. \nSome functions may not work.", Toast.LENGTH_SHORT).show();
				}
			});


	private void enableMapLocationTools(GoogleMap googleMap)
	{
		Log.d(TAG, "enableMapLocationTools() called");
		try
		{
			googleMap.setMyLocationEnabled(true);
			googleMap.setOnMyLocationButtonClickListener(this);
			googleMap.setOnMyLocationClickListener(this);
		}
		catch (SecurityException e)
		{
			Log.e(TAG, e.toString());
		}
	}


	@Override
	public void onMyLocationClick(@NonNull Location location)
	{
		Toast.makeText(associatedActivity, "Current location:\n" + location, Toast.LENGTH_LONG).show();
	}

	@Override
	public boolean onMyLocationButtonClick()
	{
		Toast.makeText(associatedActivity, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
		// Return false so that we don't consume the event and the default behavior still occurs
		// (the camera animates to the user's current position)
		return false;
	}

	public class SearchTextListener implements SearchView.OnQueryTextListener, SearchView.OnCloseListener
	{
		private SearchByTextRequest searchByTextRequest;
		private List<Place> searchResponsePlaces;

		private SearchByTextRequest buildSearchByTextRequest(String queryText)
		{
			Log.d(TAG, "Building search text request");
			return SearchByTextRequest.builder(queryText, placeFields)
					.setMaxResultCount(MAX_SEARCH_RESULT_COUNT)
					.setLocationBias(getMapBounds(map))
					.setRankPreference(SearchByTextRequest.RankPreference.RELEVANCE)
					.build();
		}

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
			else
			{
				Log.d(TAG, "Query text submitted: " + queryText);
				Log.d(TAG, "Building search text request");
				searchByTextRequest = buildSearchByTextRequest(queryText);
				Log.d(TAG, "Initiating search task");
				placesClient.searchByText(searchByTextRequest)
						.addOnSuccessListener(response -> {
							searchResponsePlaces = response.getPlaces();
							Log.d(TAG, "Search task success | " + searchResponsePlaces.size() + " places retrieved");
							if (!searchResponsePlaces.isEmpty())
							{
								map.clear();
								//move camera to first result
								CameraUpdate moveToFirst = CameraUpdateFactory.newLatLng(searchResponsePlaces.get(0).getLatLng());
								map.animateCamera(moveToFirst);

								for (Place place : searchResponsePlaces)
								{
									// add marker from place
									MarkerOptions placeMarkerOptions = new MarkerOptions()
											.title(place.getName())
											.position(place.getLatLng())
											.snippet(place.getAddress());
									Marker placeMarker = map.addMarker(placeMarkerOptions);
									placeMarker.setTag(place);

								}
							}
							else
							{
								Toast.makeText(associatedActivity, "No places matching your query were found within view", Toast.LENGTH_SHORT).show();
							}

						});
			}
			return true;
		}

		public boolean onQueryTextChange(String newText)
		{
			return false;
		}

		@Override
		public boolean onClose()
		{
			searchView.clearFocus();
			return true;
		}
	}

	private RectangularBounds getMapBounds(GoogleMap googleMap) // pass visible region to restrict/bias autocomplete results
	{
		LatLngBounds mapBounds = googleMap.getProjection().getVisibleRegion().latLngBounds;
		return RectangularBounds.newInstance(mapBounds);
	}

	public boolean onMarkerClick(final Marker marker)
	{
		Log.d(TAG, "onMarkerClick() triggered");
		CameraUpdate moveToMarker = CameraUpdateFactory.newLatLng(marker.getPosition());
		map.animateCamera(moveToMarker);

		if (marker.getTag() == null)
		{
			return false;
		}
		else
		{
			String markerTagDesc = marker.getTag().getClass().toString();
			Toast.makeText(associatedActivity, markerTagDesc, Toast.LENGTH_SHORT).show();
			//Log.d(TAG, "Marker contains place object");
			return true;
		}
	}

}