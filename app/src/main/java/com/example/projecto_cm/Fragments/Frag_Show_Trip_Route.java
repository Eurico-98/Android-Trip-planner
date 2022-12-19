package com.example.projecto_cm.Fragments;

import android.app.Dialog;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.projecto_cm.Adapters.Adapter_Trip_Locations_List;
import com.example.projecto_cm.Interfaces.Interface_Frag_Change_Listener;
import com.example.projecto_cm.Main_Activity;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Executors;

public class Frag_Show_Trip_Route extends Fragment implements OnMapReadyCallback {

    private Shared_View_Model model;
    private Interface_Frag_Change_Listener fcl; // to change fragment
    private String username;
    private MapView mapView;
    private GoogleMap google_Map;
    private Geocoder geocoder;
    private CameraUpdate cameraUpdate;
    private LatLng latLng;
    private MarkerOptions markerOptions;
    private ArrayList<String> trip_locations_list = new ArrayList<>();
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
            } catch (Exception ignored){}
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

        mapView = requireActivity().findViewById(R.id.map_view); // bind map view
        markerOptions = new MarkerOptions(); // to mark locations on the map


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
    }
}
