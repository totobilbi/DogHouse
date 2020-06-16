package com.roeico7.dogadopt.logic;


import android.app.ProgressDialog;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;
import com.roeico7.dogadopt.Entry.RegisterComplete;
import com.roeico7.dogadopt.R;
import com.skyfishjy.library.RippleBackground;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static com.roeico7.dogadopt.logic.GeneralStuff.resources;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentMap extends AppCompatDialogFragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlacesClient placesClient;
    private List<AutocompletePrediction> predictionList;

    private Location mLastKnownLocation;
    private LocationCallback locationCallback;
    private SupportMapFragment mapFragment;
    private MaterialSearchBar materialSearchBar;
    private View mapView;
    private Button btn_save;
    private RippleBackground rippleBg;
    private Fragment mFragment;



    private final float DEFAULT_ZOOM = 15;

    private HashMap<String, String> userDetails = new HashMap<>();

    public FragmentMap() {
        // Required empty public constructor
    }

    public FragmentMap(HashMap<String, String> userDetails) {
        this.userDetails = userDetails;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_map, container, false);

        int widthPixels = (int) (getResources().getDisplayMetrics().widthPixels * 0.8);
        int heightPixels = (int) (getResources().getDisplayMetrics().heightPixels * 0.8);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(widthPixels, heightPixels);
        v.setLayoutParams(layoutParams);
        return v;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mFragment = this;
        materialSearchBar = view.findViewById(R.id.searchBar);
        btn_save = view.findViewById(R.id.btn_save);
        rippleBg = view.findViewById(R.id.ripple_bg);

        mapFragment = (SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mapView = mapFragment.getView();

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        Places.initialize(getContext(), getString(R.string.google_maps_key));
        placesClient = Places.createClient(getContext());

        materialSearchBar.setOnSearchActionListener(onSearchActionListener);


        materialSearchBar.addTextChangeListener(textWatcher);


        materialSearchBar.setSuggstionsClickListener(OnItemViewClickListener);


        btn_save.setOnClickListener(v -> saveAddress());

    }





    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        if (mapView != null && mapView.findViewById(Integer.parseInt("1")) != null) {
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 40, 180);
        }

        //check if gps is enabled or not and then request user to enable it
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

        SettingsClient settingsClient = LocationServices.getSettingsClient(getContext());
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

        task.addOnSuccessListener(getActivity(), locationSettingsResponse -> getDeviceLocation());

        task.addOnFailureListener(getActivity(), e -> {
            if (e instanceof ResolvableApiException) {
                ResolvableApiException resolvable = (ResolvableApiException) e;
                try {
                    resolvable.startResolutionForResult(getActivity(), 51);
                } catch (IntentSender.SendIntentException e1) {
                    e1.printStackTrace();
                }
            }
        });

        mMap.setOnMyLocationButtonClickListener(() -> {
            if (materialSearchBar.isSuggestionsVisible())
                materialSearchBar.clearSuggestions();
            if (materialSearchBar.isSearchEnabled())
                materialSearchBar.disableSearch();
            return false;
        });
    }







    private void getDeviceLocation() {
        mFusedLocationProviderClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            mLastKnownLocation = task.getResult();
                            if (mLastKnownLocation != null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            } else {
                                final LocationRequest locationRequest = LocationRequest.create();
                                locationRequest.setInterval(10000);
                                locationRequest.setFastestInterval(5000);
                                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                                locationCallback = new LocationCallback() {
                                    @Override
                                    public void onLocationResult(LocationResult locationResult) {
                                        super.onLocationResult(locationResult);
                                        if (locationResult == null) {
                                            return;
                                        }
                                        mLastKnownLocation = locationResult.getLastLocation();
                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                        mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
                                    }
                                };
                                mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);

                            }
                        } else {
                            Toast.makeText(getContext(), resources.getString(R.string.unable_location), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }





    private void saveAddress() {
        btn_save.setClickable(false);
        LatLng currentMarkerLocation = mMap.getCameraPosition().target;
        rippleBg.startRippleAnimation();
        new Handler().postDelayed(() -> {
            try {
                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(getContext(), Locale.getDefault());

                addresses = geocoder.getFromLocation(currentMarkerLocation.latitude, currentMarkerLocation.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String city = addresses.get(0).getLocality();
                String country = addresses.get(0).getCountryName();

                address = address.substring(0, address.indexOf(',')).replace("St", "").trim();

                if(userDetails.isEmpty()) {
                    HashMap<String, Object> result = new HashMap<>();
                    result.put(resources.getString(R.string.key_streetAddress), address);
                    result.put(resources.getString(R.string.key_city), city);
                    result.put(resources.getString(R.string.key_zipCode), country);
                    result.put(resources.getString(R.string.key_location), "" + currentMarkerLocation.latitude + "," + currentMarkerLocation.longitude);


                    FirebaseDatabase.getInstance().getReference(resources.getString(R.string.key_users))
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child(resources.getString(R.string.key_profileInfo))
                            .updateChildren(result)
                            .addOnSuccessListener(aVoid -> {
                                rippleBg.stopRippleAnimation();
                                getActivity().getSupportFragmentManager().beginTransaction().remove(mapFragment).commit();
                                getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();

                            })
                            .addOnFailureListener(e -> {
                                rippleBg.stopRippleAnimation();
                                getActivity().getSupportFragmentManager().beginTransaction().remove(mapFragment).commit();
                                getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
                            });

                } else {
                    userDetails.put(resources.getString(R.string.key_streetAddress), address);
                    userDetails.put(resources.getString(R.string.key_city), city);
                    userDetails.put(resources.getString(R.string.key_zipCode), country);
                    userDetails.put(resources.getString(R.string.key_location), "" + currentMarkerLocation.latitude + "," + currentMarkerLocation.longitude);

                    rippleBg.stopRippleAnimation();

                    getActivity().getSupportFragmentManager().beginTransaction().remove(mapFragment).commit();
                    getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, new RegisterComplete(userDetails)).commit();


                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }, 3000);
    }





    private SuggestionsAdapter.OnItemViewClickListener OnItemViewClickListener = new SuggestionsAdapter.OnItemViewClickListener() {
        @Override
        public void OnItemClickListener(int position, View v) {
            if (position >= predictionList.size()) {
                return;
            }
            AutocompletePrediction selectedPrediction = predictionList.get(position);
            String suggestion = materialSearchBar.getLastSuggestions().get(position).toString();
            materialSearchBar.setText(suggestion);

            new Handler().postDelayed(() -> materialSearchBar.clearSuggestions(), 1000);

            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null)
                imm.hideSoftInputFromWindow(materialSearchBar.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
            final String placeId = selectedPrediction.getPlaceId();
            List<Place.Field> placeFields = Arrays.asList(Place.Field.LAT_LNG);

            FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build();
            placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener(fetchPlaceResponse -> {
                Place place = fetchPlaceResponse.getPlace();
                Log.i("mytag", "Place found: " + place.getName());
                LatLng latLngOfPlace = place.getLatLng();
                if (latLngOfPlace != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngOfPlace, DEFAULT_ZOOM));
                }
            }).addOnFailureListener(e -> {
                if (e instanceof ApiException) {
                    ApiException apiException = (ApiException) e;
                    apiException.printStackTrace();
                    int statusCode = apiException.getStatusCode();
                    Log.i("mytag", "place not found: " + e.getMessage());
                    Log.i("mytag", "status code: " + statusCode);
                }
            });
        }

        @Override
        public void OnItemDeleteListener(int position, View v) {

        }
    };





    private TextWatcher textWatcher =  new TextWatcher() {
        final AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            FindAutocompletePredictionsRequest predictionsRequest = FindAutocompletePredictionsRequest.builder()
                    .setTypeFilter(TypeFilter.ADDRESS)
                    .setSessionToken(token)
                    .setQuery(s.toString())
                    .build();
            placesClient.findAutocompletePredictions(predictionsRequest).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FindAutocompletePredictionsResponse predictionsResponse = task.getResult();
                    if (predictionsResponse != null) {
                        predictionList = predictionsResponse.getAutocompletePredictions();
                        List<String> suggestionsList = new ArrayList<>();
                        for (int i = 0; i < predictionList.size(); i++) {
                            AutocompletePrediction prediction = predictionList.get(i);
                            suggestionsList.add(prediction.getFullText(null).toString());
                        }
                        materialSearchBar.updateLastSuggestions(suggestionsList);
                        if (!materialSearchBar.isSuggestionsVisible()) {
                            materialSearchBar.showSuggestionsList();
                        }
                    }
                } else {
                    System.out.println("mytag" + "prediction fetching task unsuccessful");
                }
            });
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };






    private MaterialSearchBar.OnSearchActionListener onSearchActionListener =  new MaterialSearchBar.OnSearchActionListener() {
        @Override
        public void onSearchStateChanged(boolean enabled) {

        }

        @Override
        public void onSearchConfirmed(CharSequence text) {
            getActivity().startSearch(text.toString(), true, null, true);
        }

        @Override
        public void onButtonClicked(int buttonCode) {
            if (buttonCode == MaterialSearchBar.BUTTON_NAVIGATION) {
                //opening or closing a navigation drawer
            } else if (buttonCode == MaterialSearchBar.BUTTON_BACK) {
                materialSearchBar.disableSearch();
            }
        }
    };



    @Override
    public void onResume() {
        super.onResume();
        if (!userDetails.isEmpty()) {
            getDialog().setOnKeyListener((dialog, keyCode, event) -> {
                if ((keyCode == android.view.KeyEvent.KEYCODE_BACK)) {
                    //This is the filter
                    if (event.getAction() != KeyEvent.ACTION_DOWN)
                        return true;
                    else {
                        ProgressDialog progressDialog = new ProgressDialog(getContext());
                        GeneralStuff.shared.executeAlert(progressDialog,
                                resources.getString(R.string.ui_cancel_register),
                                resources.getString(R.string.ui_cancel_register_text),
                                false,
                                () -> {
                                    getActivity().getSupportFragmentManager().beginTransaction().remove(mFragment).commit();
                                    getActivity().getSupportFragmentManager().beginTransaction().remove(mapFragment).commit();
                                    getActivity().getSupportFragmentManager().popBackStack();
                                });
                        return true;
                    }
                } else
                    return false;
            });
        }
    }



}
