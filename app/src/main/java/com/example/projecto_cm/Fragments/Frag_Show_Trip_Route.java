package com.example.projecto_cm.Fragments;

import android.app.Dialog;
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

import com.example.projecto_cm.Interfaces.Interface_Frag_Change_Listener;
import com.example.projecto_cm.Interfaces.Interface_On_Trip_Route_Ready;
import com.example.projecto_cm.Main_Activity;
import com.example.projecto_cm.Map_Route_Helpers.FetchURL;
import com.example.projecto_cm.R;
import com.example.projecto_cm.Shared_View_Model;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class Frag_Show_Trip_Route extends Fragment implements OnMapReadyCallback, Interface_On_Trip_Route_Ready {

    private Shared_View_Model model;
    private Interface_Frag_Change_Listener fcl; // to change fragment
    private String username;
    private MapView mapView;
    private GoogleMap google_Map;
    private Polyline currentPolyline;
    private CameraUpdate cameraUpdate;
    ArrayList<MarkerOptions> marker = new ArrayList<>();
    private Dialog loading_animation_dialog;

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

                    location = s.split("_#_")[0];
                    lat  = Double.parseDouble(s.split("_#_")[1]);
                    lon = Double.parseDouble(s.split("_#_")[2]);

                    marker.add(new MarkerOptions().position(new LatLng(lat, lon)).title(location));
                }

                // put username back into model view to be obtainable in other fragments
                // this will trigger the observer callback again but it is ok because it only gets the username again
                model.send_data(username);
            }
        });


        // load login fragment layout
        View view = inflater.inflate(R.layout.fragment_show_trip_route_layout, container, false);
        fcl = (Main_Activity) inflater.getContext(); // to change fragments

        // load toolbar of this fragment
        Toolbar toolbar = view.findViewById(R.id.view_trip_route_app_bar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);

        return view;
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
        loading_animation_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loading_animation_dialog.setCanceledOnTouchOutside(false);
        loading_animation_dialog.setContentView(R.layout.loading_animation_layout);

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
     * when map is ready enable zoom functionality from google map
     * @param googleMap
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        google_Map =  googleMap;

        // enable zoom controls
        google_Map.getUiSettings().setZoomControlsEnabled(true);

        set_location_markers_in_map();
    }

    /**
     * to preparer route
     */
    private void set_location_markers_in_map() {

        // Origin and destination of route
        String origin = "";
        String dest = "";
        String waypoints = "";

        // transportation Mode
        String mode = "mode=driving";

        // Building the parameters to the web service
        String parameters = "";

        for(int i = 0; i < marker.size(); i++){

            if(i == 0){
                origin = "origin=" + marker.get(i).getPosition().latitude + "," + marker.get(i).getPosition().longitude;
            }
            else if(i == marker.size()-1){
                dest = "destination=" + marker.get(i).getPosition().latitude + "," + marker.get(i).getPosition().longitude;

                parameters += origin + "&" + dest;

                if(marker.size() > 2){
                    waypoints = waypoints.substring(0, waypoints.length() - 1);  // remove last comma
                    parameters += "&waypoints=[" + waypoints + "]";
                }

                parameters += "&" + mode;
            }

            // add waypoints
            else if(marker.size() > 2){
                waypoints += "{location=" + marker.get(i).getPosition().latitude + "," + marker.get(i).getPosition().longitude + "},";
            }

            google_Map.addMarker(marker.get(i));
        }

        // prepare URL
        new FetchURL(requireActivity()).execute("https://maps.googleapis.com/maps/api/directions/json?" + parameters + "&key=" + getString(R.string.Directions_API), "driving");
    }

    /**
     * show route after polyline is ready
     * @param values
     */
    @Override
    public void show_route(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = google_Map.addPolyline((PolylineOptions) values[0]);
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
