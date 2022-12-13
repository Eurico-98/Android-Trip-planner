package com.example.projecto_cm.Fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.example.projecto_cm.DAO_helper;
import com.example.projecto_cm.DB_entities.Trip;
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
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
    private ImageButton search_button, add_location_button;

    private String location;
    private LatLng latLng;
    private MarkerOptions markerOptions;
    private List<Address> listAddress; // list with search results ass addresses
    private RecyclerView trip_locations_recyclerView;
    private Button save_trip_button, start_date_input, end_date_input, create_trip_button;
    private EditText location_input;
    private ArrayList<String> trip_locations_list = new ArrayList<>(); // this is what will stay in the database
    private Adapter_Trip_locations_list adapter_trip_locations_list;

    private ArrayList<String> search_results_list = new ArrayList<>(); // for searches with multiple results - show list in dialog to select only one of them to add to trip
    private Adapter_Search_results_list adapter_search_results_list;
    private Dialog select_location_dialog; // dialog to select one result when a search returns several results

    private Dialog complete_trip_data_dialog;
    private DatePickerDialog datePickerDialog;
    private String type_of_date;
    private Date start_date_for_comparison;
    private EditText trip_title_input;

    private Dialog loading_animation_dialog;

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

        // prepare loading animation
        loading_animation_dialog = new Dialog(requireActivity());
        loading_animation_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loading_animation_dialog.setCanceledOnTouchOutside(false);
        loading_animation_dialog.setContentView(R.layout.loading_animation_layout);

        mapView = requireActivity().findViewById(R.id.map_view); // bind map view
        trip_locations_recyclerView = view.findViewById(R.id.trip_location_list);
        location_input = requireActivity().findViewById(R.id.location_input_field);
        markerOptions = new MarkerOptions(); // to mark locations on the map


        // set up list of locations
        adapter_trip_locations_list = new Adapter_Trip_locations_list(requireActivity(), this, trip_locations_list);
        trip_locations_recyclerView.setAdapter(adapter_trip_locations_list);
        trip_locations_recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));


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
        add_location_button = requireActivity().findViewById(R.id.add_location);
        add_location_button.setOnClickListener(v -> {

            // only add location if search was successful
            if(listAddress != null && listAddress.size() > 0){
                checkResults();
            }
            else {
                Toast.makeText(requireActivity(), "Search locations first!", Toast.LENGTH_SHORT).show();
            }
        });

        // confirm trip button
        save_trip_button = requireActivity().findViewById(R.id.save_trip_button);
        save_trip_button.setOnClickListener(v -> {
            if(trip_locations_list.size() > 0){
                trip_Title_Start_End_date();
            }
            else {
                Toast.makeText(requireActivity(), "Add locations first!", Toast.LENGTH_SHORT).show();
            }
        });
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
     * simple map view and satellite map view
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

        if (result == ConnectionResult.SUCCESS) {
            return true;
        } else if (googleApiAvailability.isUserResolvableError(result)) {
            Dialog dialog = googleApiAvailability.getErrorDialog(requireActivity(), result, 201, dialog1 ->
                    Toast.makeText(requireActivity(), "Canceled!", Toast.LENGTH_SHORT).show());
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
                        Toast.makeText(requireActivity(), "Found 1 location!", Toast.LENGTH_SHORT).show();
                        cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10);
                        google_Map.animateCamera(cameraUpdate);
                    }

                    // zoom in only to level 5 in case of multiple results
                    else if(i == listAddress.size()-1){
                        Toast.makeText(requireActivity(), "Found "+ listAddress.size() + " locations!", Toast.LENGTH_SHORT).show();
                        cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 3);
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
                search_results_list.add(listAddress.get(i).getAddressLine(0));
            }

            // set up list of locations
            adapter_search_results_list = new Adapter_Search_results_list(requireActivity(), this, search_results_list);
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
        trip_locations_list.remove(position);
        adapter_trip_locations_list.setLocationsList(trip_locations_list);
        trip_locations_recyclerView.setAdapter(adapter_trip_locations_list);
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
        String temp;
        try{
            temp = location_input.getText().toString().substring(0, 1).toUpperCase()
                    + location_input.getText().toString().substring(1).toLowerCase();

        }catch (Exception e){
            temp = location_input.getText().toString();
        }

        // if type == 2 add locality to name to distinguish from other results
        if(type == 2){

            // add locality if possible and if locality is not the same has inserted text to avoid repeated names
            if(listAddress.get(pos).getLocality() != null && !temp.equalsIgnoreCase(listAddress.get(pos).getLocality())){
                temp += " " + listAddress.get(pos).getLocality();
            }
            else {
                temp = listAddress.get(pos).getAddressLine(0);
            }
        }

        temp += "_#_" + listAddress.get(pos).getLatitude() + "_#_" +  listAddress.get(pos).getLongitude();

        trip_locations_list.add(temp);
        adapter_trip_locations_list.setLocationsList(trip_locations_list);
        trip_locations_recyclerView.setAdapter(adapter_trip_locations_list);

        // clear input, list of address, list of results of search thar returned several results, and clear markers from map
        search_results_list.clear();
        listAddress.clear();
        location_input.setText("");
        google_Map.clear();
    }

    /**
     * show dialog to insert trip title and insert star and end dates
     */
    private void trip_Title_Start_End_date() {

        complete_trip_data_dialog = new Dialog(requireActivity());

        //Disable the default title
        complete_trip_data_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        complete_trip_data_dialog.setCanceledOnTouchOutside(true);

        //Ability to cancel the dialog by clicking outside the dialog
        complete_trip_data_dialog.setContentView(R.layout.dialog_get_trip_title_star_end_dates_layout);

        // prepare date picker
        initDatePicker();

        // initialize start_date_for_comparison to compare to end date in case the user doesn't change the star date input field
        start_date_for_comparison = getDate(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));

        //Binding dialog elements
        start_date_input = complete_trip_data_dialog.findViewById(R.id.start_date_input);
        start_date_input.setText(getTodaysDate());
        start_date_input.setOnClickListener(v -> {
            type_of_date = "start";
            datePickerDialog.show();
        });

        end_date_input = complete_trip_data_dialog.findViewById(R.id.end_date_input);
        end_date_input.setText(getTodaysDate());
        end_date_input.setOnClickListener(v -> {
            type_of_date = "end";
            datePickerDialog.show();
        });

        trip_title_input = complete_trip_data_dialog.findViewById(R.id.trip_title_input);

        create_trip_button = complete_trip_data_dialog.findViewById(R.id.create_trip_button);
        create_trip_button.setOnClickListener(v -> saveTripToFireBase());

        complete_trip_data_dialog.show();
    }

    /**
     * to get current day date
     * @return date in as string
     */
    private String getTodaysDate() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        month = month + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return makeDateString(day, month, year);
    }

    /**
     * to get any date
     * @param year
     * @param month
     * @param day
     * @return date as Date object
     */
    private Date getDate(int year,int month,int day){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        return cal.getTime();
    }

    /**
     * shows dialog to select date with a spinner
     */
    private void initDatePicker() {

        DatePickerDialog.OnDateSetListener dateSetListener = (datePicker, year, month, day) -> {
            month = month + 1;
            String date = makeDateString(day, month, year);

            // get inserted date as date object
            Date inserted_date = getDate(year, month, day);

            if(Objects.equals(type_of_date, "start")){

                // get today's date as date object
                Date current_date = getDate(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));

                // get star date as date object to compare to end date
                start_date_for_comparison = inserted_date;

                // if inserted date is older than today
                if(inserted_date.compareTo(current_date) > -1){
                    start_date_input.setText(date);
                    end_date_input.setText(date); // always set end date equal to star date to facilitate verifications
                }
                else {
                    Toast.makeText(requireActivity(), "Invalid start date!",Toast.LENGTH_SHORT).show();
                }
            }
            else {

                // if start date is not older than end date
                if(inserted_date.compareTo(start_date_for_comparison) > -1){
                    end_date_input.setText(date);
                }
                else {
                    Toast.makeText(requireActivity(), "Invalid end date!",Toast.LENGTH_SHORT).show();
                }
            }
        };

        datePickerDialog = new DatePickerDialog(getContext(),
                AlertDialog.THEME_HOLO_LIGHT, dateSetListener,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
    }

    /**
     * converts date to string
     * @param day
     * @param month
     * @param year
     * @return date as string
     */
    private String makeDateString(int day, int month, int year) { return getMonthFormat(month) + " " + day + " " + year; }

    /**
     * converts month from int to string like 1 = JAN
     * @param month
     * @return
     */
    private String getMonthFormat(int month) {
        if(month == 1)
            return "JAN";
        if(month == 2)
            return "FEB";
        if(month == 3)
            return "MAR";
        if(month == 4)
            return "APR";
        if(month == 5)
            return "MAY";
        if(month == 6)
            return "JUN";
        if(month == 7)
            return "JUL";
        if(month == 8)
            return "AUG";
        if(month == 9)
            return "SEP";
        if(month == 10)
            return "OCT";
        if(month == 11)
            return "NOV";
        if(month == 12)
            return "DEC";

        //default should never happen
        return "JAN";
    }

    /**
     * save trip to Firebase database and return to home screen
     */
    public void saveTripToFireBase() {

        if(!trip_title_input.getText().toString().equals("")){

            // hide login layout and show loading animation
            loading_animation_dialog.show();
            complete_trip_data_dialog.dismiss();

            Trip new_trip = new Trip(trip_title_input.getText().toString(), start_date_input.getText().toString(), end_date_input.getText().toString(), trip_locations_list);
            DAO_helper dao = new DAO_helper();
            dao.add_trips(username, this, new_trip);
        }
        else {
            Toast.makeText(requireActivity(), "Insert Trip title!",Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * show result message after saving newly created trip database
     * cancel loading animation and return to home screen
     */
    public void getDAOResultMessage(Task<Void> add_trip) {

        // cancel loading animation and go back to home screen
        loading_animation_dialog.dismiss();

        add_trip.addOnSuccessListener(suc -> {

            Toast.makeText(requireActivity(), "Trip saved successfully!",Toast.LENGTH_SHORT).show();

            // don't keep Frag_create_trip in back stack
            Frag_Home_Screen frag_home_screen = new Frag_Home_Screen();
            fcl.replaceFragment(frag_home_screen, "no");
        }).addOnFailureListener(er ->{

            Toast.makeText(requireActivity(), "Error while saving Trip in Database!",Toast.LENGTH_SHORT).show();
            Toast.makeText(requireActivity(), "Try again later!",Toast.LENGTH_SHORT).show();

            // cancel loading animation and go back to home screen

        });
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
