package com.example.projecto_cm.Fragments;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.projecto_cm.Interfaces.Interface_On_Trip_Route_Ready;
import com.example.projecto_cm.Map_Route_Helpers.FetchURL;
import com.example.projecto_cm.R;
import com.example.projecto_cm.Shared_View_Model;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import java.util.ArrayList;
import java.util.List;

public class Frag_Show_Trip_Route extends Fragment implements OnMapReadyCallback, Interface_On_Trip_Route_Ready {

    private Shared_View_Model model;
    private String username;
    private MapView mapView;
    private GoogleMap google_Map;
    private ArrayList<Polyline> currentPolylines = new ArrayList<>();
    private CameraUpdate cameraUpdate;
    private ArrayList<MarkerOptions> marker = new ArrayList<>();
    private Dialog loading_animation_dialog;
    private ArrayList<String> all_locations = new ArrayList<>();
    private List<List<String>> all_location_combinations;
    private int combination_iterator = 0;

    /**
     * load fragment to show trip route
     * load model view and app bar menu
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // get activity to get shared view model
        model = new ViewModelProvider(requireActivity()).get(Shared_View_Model.class);

        // get data from shared view model
        model.get_data().observe(getViewLifecycleOwner(), item -> {

            try{
                username = (String) item;
            }

            // if user is coming from list trips fragment
            catch (Exception e) {
                String[] data = (String[]) item;
                username = data[0];

                // get locations and prepare markers
                String[] locations_from_model_view = data[1].split("locations=\\[")[1].split("]")[0].split(", ");
                String location;
                double lat, lon;
                for(String s : locations_from_model_view){

                    all_locations.add(s);

                    location = s.split("_#_")[0];
                    lat  = Double.parseDouble(s.split("_#_")[1]);
                    lon = Double.parseDouble(s.split("_#_")[2]);

                    marker.add(new MarkerOptions().position(new LatLng(lat, lon)).title(location));
                }

                all_location_combinations = getAllCombinations(all_locations);

                // make route calculation star with the biggest combo in the list because i want to include as much locations in the route as possible
                // if it is not possible try the other smaller combinations to get sub routes possible
                combination_iterator = all_location_combinations.size()-1;

                // put username back into model view to be obtainable in other fragments
                // this will trigger the observer callback again but it is ok because it only gets the username again
                model.send_data(username);
            }
        });

        // load login fragment layout
        View view = inflater.inflate(R.layout.fragment_show_trip_route_layout, container, false);

        // load toolbar of this fragment
        Toolbar toolbar = view.findViewById(R.id.view_trip_route_app_bar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);

        return view;
    }

    /**
     * this return a list of lists where each list consist of a different combination of locations
     * the purpose with this is to create routes between all the locations
     * including islands
     * @param list_of_locations
     * @return
     */
    List<List<String>> getAllCombinations(ArrayList<String> list_of_locations) {

        List<List<String>> combinations = new ArrayList<>();
        for (int i = 2; i <= list_of_locations.size(); i++) {
            combinations.addAll(getCombinations(list_of_locations, i));
        }
        return combinations;
    }

    /**
     * this method recursively calculates all the combinations with two or more locations maintaining the order of the original list of locations
     * because it is the order selected by the user
     * @param locations
     * @param size
     * @return
     */
    List<List<String>> getCombinations(ArrayList<String> locations, int size) {
        List<List<String>> combinations = new ArrayList<>();
        if (size == 0) {
            combinations.add(new ArrayList<>());
            return combinations;
        }

        for (int i = 0; i <= locations.size() - size; i++) {

            String location = locations.get(i);
            List<List<String>> subcombinations = getCombinations(new ArrayList<>(locations.subList(i + 1, locations.size())), size - 1);

            for (List<String> subcombination : subcombinations) {
                List<String> newCombination = new ArrayList<>();
                newCombination.add(location);
                newCombination.addAll(subcombination);
                combinations.add(newCombination);
            }
        }

        return combinations;
    }


    /**
     * initialize mapview
     * check if google play services are available
     * load map view
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // prepare loading animation
        loading_animation_dialog = new Dialog(requireActivity());
        loading_animation_dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        loading_animation_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loading_animation_dialog.setCanceledOnTouchOutside(false);
        loading_animation_dialog.setContentView(R.layout.loading_animation_layout);
        loading_animation_dialog.show();

        mapView = requireActivity().findViewById(R.id.map_view_route); // bind map view

        // check if google play services are available to use map api
        if(checkGooglePlayServices()){
            mapView.getMapAsync(this);
            mapView.onCreate(savedInstanceState);
        }
        else {
            Toast.makeText(requireActivity(), "Google Play services Not Available!",Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * app bar menu set up
     * @param menu
     * @param inflater
     */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.show_trip_route_actions, menu);

        // change app bar title
        ActionBar ab = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        assert ab != null;
        ab.setTitle("Trip Route");

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
            for (Polyline polyline : currentPolylines) {
                polyline.setColor(Color.BLUE);
            }

            google_Map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
        else if(item.getItemId() == R.id.satellite_map){
            for (Polyline polyline : currentPolylines) {
                polyline.setColor(Color.GREEN);
            }

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
     * when map is ready enable zoom functionality from google map
     * @param googleMap
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        google_Map =  googleMap;

        // enable zoom controls
        google_Map.getUiSettings().setZoomControlsEnabled(true);

        // add markers for every location
        for(int i = 0; i < marker.size(); i++) {
            google_Map.addMarker(marker.get(i));
        }

        Toast.makeText(requireActivity(), "Calculating optimal route",Toast.LENGTH_SHORT).show();

        // clear poly lines from map
        if (currentPolylines.size() > 0) {
            for (Polyline polyline : currentPolylines) {
                polyline.remove();
            }
        }

        set_markers_in_map_and_calculate_route(all_location_combinations.get(combination_iterator));
    }

    /**
     * to preparer route
     */
    private void set_markers_in_map_and_calculate_route(List combo)  {

        String origin = "", dest, waypoints = "", parameters = "", mode = "mode=driving";

        //System.out.println("---------------------------------------------------calculando:\n"+combo);

        for(int i = 0; i < combo.size(); i++){

            String aux = (String) combo.get(i);

            if(i == 0){
                origin = "origin=" + aux.split("_#_")[1] + "," + aux.split("_#_")[2];
            }
            else if(i == combo.size()-1){
                dest = "destination=" + aux.split("_#_")[1] + "," + aux.split("_#_")[2];

                parameters += origin + "&" + dest + "&" + mode;

                if(combo.size() > 2){
                    parameters += "&waypoints=optimize:true" + waypoints;
                }
            }

            waypoints += "|" + aux.split("_#_")[1] + "," + aux.split("_#_")[2];
        }

        new FetchURL(this).execute("https://maps.googleapis.com/maps/api/directions/json?" + parameters + "&key=" + getString(R.string.Directions_API), "driving");
    }

    /**
     * show route after polyline is ready
     * @param values
     */
    @Override
    public void show_route(Object... values) {

        // if route is created
        if(values[0] != null){

            currentPolylines.add(google_Map.addPolyline((PolylineOptions) values[0]));
            currentPolylines.get(currentPolylines.size()-1).setWidth(10);

            // stop calculation if a route with all locations was successfully computed
            // or if a route was computed with only one location not included this is for the case where one island is included for example:
            // lisboa coimbra porto e madeira  a route is possible between lisboa coimbra e porto but it madeira cant be included
            if(combination_iterator == all_location_combinations.size()-1 || combination_iterator == all_location_combinations.size()-2 || all_locations.size() == 3){
                combination_iterator = -1;
            }

            // if the previous two cases are not true and a route with two or more locations less is computed, than remove from the list of all combinations
            // the combinations of locations that include the locations already present in the most recent computed route
            else if(combination_iterator == all_location_combinations.size()-3){

                // Get the locations in the most recent computed route
                List<String> computedRouteLocations = all_location_combinations.get(combination_iterator);

                // Iterate through the list of all combinations
                for (int i = 0; i < combination_iterator; i++) {

                    // Get the current combination
                    List<String> combination = all_location_combinations.get(i);

                    // Check if the current combination includes any of the locations in the computed route
                    boolean includesComputedRouteLocation = false;
                    for (String location : combination) {
                        if (computedRouteLocations.contains(location)) {
                            includesComputedRouteLocation = true;
                            break;
                        }
                    }

                    // If the current combination includes a location in the computed route, remove it from the list of all combinations
                    if (includesComputedRouteLocation) {
                        all_location_combinations.remove(i);
                        i--; // Decrement the counter to account for the removed element
                    }
                }
            }
        }

        combination_iterator--;

        if(combination_iterator >= 0){
            set_markers_in_map_and_calculate_route(all_location_combinations.get(combination_iterator));
        }
        else {
            Toast.makeText(requireActivity(), "Zooming on Start location.",Toast.LENGTH_SHORT).show();

            // zoom camera on first location
            cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentPolylines.get(currentPolylines.size()-1).getPoints().get(0), 8);
            google_Map.animateCamera(cameraUpdate);
            loading_animation_dialog.dismiss();
        }
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
