package com.example.projecto_cm.Fragments;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projecto_cm.Adapters.Adapter_Search_results_list;
import com.example.projecto_cm.Adapters.Adapter_Trip_locations_list;
import com.example.projecto_cm.Interfaces.Card_Search_result_interface;
import com.example.projecto_cm.Interfaces.Card_location_interface;
import com.example.projecto_cm.Interfaces.FragmentChangeListener;
import com.example.projecto_cm.Main_Activity;
import com.example.projecto_cm.R;
import com.example.projecto_cm.SharedViewModel;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


public class Frag_Create_Trip extends Fragment implements OnMapReadyCallback, Card_location_interface, Card_Search_result_interface {

    private SharedViewModel model;
    private FragmentChangeListener fcl; // to change fragment
    private String username;
    private MapView mapView;
    private GoogleMap google_Map;
    private Geocoder geocoder;
    private CameraUpdate cameraUpdate;
    private ImageButton search_button;

    private String location;
    private LatLng latLng;
    private MarkerOptions markerOptions;
    private List<Address> listAddress;
    private RecyclerView recyclerView;
    private Button add_location, confirm_and_create_trip;
    private EditText location_input;
    private ArrayList<String> trip_locations = new ArrayList<>();
    private Adapter_Trip_locations_list adapter_trip_locations_list;

    private ArrayList<String> search_results = new ArrayList<>();
    private Adapter_Search_results_list adapter_search_results_list;
    private Dialog select_location_dialog;

    /**
     *  onCreateView of create trip fragment
     *  load layout and toolbar and get username from shared model view
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // get activity to get shared view model
        model = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        model.get_username().observe(getViewLifecycleOwner(), item -> username = (String) item);

        // load login fragment layout
        View view = inflater.inflate(R.layout.fragment_create_trip_layout, container, false);
        fcl = (Main_Activity) inflater.getContext(); // to change fragments

        // load toolbar of this fragment
        Toolbar toolbar = view.findViewById(R.id.create_trip_app_bar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);

        return view;
    }

    /**
     * initialize mapview
     * check if google play services are available
     * load map view
     * bind search add and confirm trip buttons
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mapView = requireActivity().findViewById(R.id.map_view); // bind map view
        recyclerView = view.findViewById(R.id.trip_location_list);
        location_input = requireActivity().findViewById(R.id.location_input_field);
        markerOptions = new MarkerOptions(); // to mark locations on the map


        // check if google play services are available to use map api
        if(checkGooglePlayServices()){
            mapView.getMapAsync(this);
            mapView.onCreate(savedInstanceState);
        }
        else {
            Toast.makeText(requireActivity(), "Google Play services Not Available!",Toast.LENGTH_SHORT).show();
        }

        // search locations functions
        search_button = requireActivity().findViewById(R.id.search_button);
        search_button.setOnClickListener(v -> showSearchResults());

        // add location button
        add_location = requireActivity().findViewById(R.id.add_location);
        add_location.setOnClickListener(v -> {

            // only add location if search was successful
            if(listAddress != null && listAddress.size() > 0){
                checkResults();
            }
            else {
                Toast.makeText(requireActivity(), "Search locations first!", Toast.LENGTH_SHORT).show();
            }
        });


        // set up list of locations
        adapter_trip_locations_list = new Adapter_Trip_locations_list(requireActivity(), this, trip_locations);
        recyclerView.setAdapter(adapter_trip_locations_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
    }

    /**
     * app bar menu set up
     * @param menu
     * @param inflater
     */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.create_trip_actions, menu);

        // change app bar title
        ActionBar ab = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        assert ab != null;
        ab.setTitle("Create Trip");

        super.onCreateOptionsMenu(menu,inflater);
    }

    /**
     * app bar menu actions
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.normal_map){
            google_Map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
        else if(item.getItemId() == R.id.satellite_map){
            google_Map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * check if google play services are available to use map api
     */
    private boolean checkGooglePlayServices() {

        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int result = googleApiAvailability.isGooglePlayServicesAvailable(requireContext());

        if (result == ConnectionResult.SUCCESS){
            return true;
        }
        else if(googleApiAvailability.isUserResolvableError(result)){
            Dialog dialog = googleApiAvailability.getErrorDialog(requireActivity(), result, 201, dialog1 ->
                    Toast.makeText(requireActivity(), "Canceled!",Toast.LENGTH_SHORT).show());
            assert dialog != null;
            dialog.show();
        }

        return false;
    }

    /**
     * when map is ready enable zoom functionality from google mao
     * @param googleMap
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        google_Map =  googleMap;

        // enable zoom controls
        google_Map.getUiSettings().setZoomControlsEnabled(true);
    }

    /**
     * function to search for locations in google map
     */
    public void showSearchResults() {

        // dismiss keyboard
        location_input.onEditorAction(EditorInfo.IME_ACTION_DONE);

        // clear previous markers
        google_Map.clear();

        // get inserted location
        location = location_input.getText().toString();

        geocoder = new Geocoder(requireActivity(), Locale.getDefault());
        try {

            // get results for the search
            listAddress = geocoder.getFromLocationName(location, 5);

            // if inserted location exists
            if(listAddress.size() > 0){

                // add markers for each result
                for(int i = 0; i < listAddress.size(); i++){

                    // to get coordinates of inserted location
                    latLng = new LatLng(listAddress.get(i).getLatitude(), listAddress.get(i).getLongitude());

                    // put a marker marker on the selected location
                    markerOptions.position(latLng);
                    google_Map.addMarker(markerOptions);

                    // zoom level
                    // level 1 - world
                    // level 5 - continent
                    // level 10 - city
                    // level 15 - street level
                    // lever 20 - building

                    // automatically zoom to the inserted location if there is only one result
                    if(listAddress.size() == 1){
                        cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
                        google_Map.animateCamera(cameraUpdate);
                    }

                    // zoom in only to level 5 in case of multiple results
                    else if(i == listAddress.size()-1){
                        cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 5);
                        google_Map.animateCamera(cameraUpdate);
                    }
                }
            }
            else {
                Toast.makeText(requireActivity(), "Location not found!", Toast.LENGTH_SHORT).show();
            }

        } catch (IOException e) {
            Toast.makeText(requireActivity(), "Could not execute search!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * function to check if search returned one or more results
     * if it returned 1 call function to add to trip
     * if it returned several show dialog that allows to select one location
     */
    private void checkResults() {

        // if there is only one result from the search add it to the list
        if(listAddress.size() == 1){
            addLocationToTrip(0, 1);
        }

        // if the search return more than one location show dialog to allow selection
        else {
            select_location_dialog = new Dialog(requireActivity());

            //Disable the default title
            select_location_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            select_location_dialog.setCanceledOnTouchOutside(false);

            //Ability to cancel the dialog by clicking outside the dialog
            select_location_dialog.setContentView(R.layout.dialog_select_location_result_layout);

            //Binding dialog elements
            RecyclerView results_recycler_view = select_location_dialog.findViewById(R.id.results_recycler_view);

            for(int i = 0; i < listAddress.size(); i++){
                search_results.add(listAddress.get(i).getAddressLine(0));
            }

            // set up list of locations
            adapter_search_results_list = new Adapter_Search_results_list(requireActivity(), this, search_results);
            results_recycler_view.setAdapter(adapter_search_results_list);
            results_recycler_view.setLayoutManager(new LinearLayoutManager(requireActivity()));

            select_location_dialog.show();
        }
    }

    /**
     * delte a location from the trip location list
     * @param position
     */
    @Override
    public void onDeleteClick(int position) {
        trip_locations.remove(position);
        adapter_trip_locations_list.setLocationsList(trip_locations);
        recyclerView.setAdapter(adapter_trip_locations_list);
    }

    /**
     * function to control check boxes in dialog that show results when a search returns several results and only one must be selected
     */
    @Override
    public void onToggle(int position) {
        select_location_dialog.dismiss();
        addLocationToTrip(position, 2);
    }

    /**
     * function to add locations to trip
     */
    private void addLocationToTrip(int pos, int type){

        // process string to save
        String temp = "";
        try{
            temp = location_input.getText().toString().substring(0, 1).toUpperCase()
                    + location_input.getText().toString().substring(1).toLowerCase();

        }catch (Exception e){
            temp = location_input.getText().toString();
        }

        // if type == 2 add locality to name to distinguish from other results
        if(type == 2){
            temp += " " + listAddress.get(pos).getLocality();
        }

        temp += "_#_" + listAddress.get(pos).getLatitude() + "_#_" +  listAddress.get(pos).getLongitude();

        trip_locations.add(temp);
        adapter_trip_locations_list.setLocationsList(trip_locations);
        recyclerView.setAdapter(adapter_trip_locations_list);

        // clear input, list of address, list of results of search thar returned several results, and clear markers from map
        search_results.clear();
        listAddress.clear();
        location_input.setText("");
        google_Map.clear();
    }


    // ------------------------------------------------------------- METHODS REQUIRED FOR MAP VIEW LIFECYCLE --------------------------------------------------------
    /**
     * for mapView
     */
    @Override
    public void onStart(){
        super.onStart();
        mapView.onStart();
    }

    /**
     * for mapView
     */
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * for mapView
     */
    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * for mapView
     */
    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    /**
     * for mapView
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    /**
     * for mapView
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * for mapView
     */
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
    // ------------------------------------------------------------- METHODS REQUIRED FOR MAP VIEW LIFECYCLE --------------------------------------------------------
}
