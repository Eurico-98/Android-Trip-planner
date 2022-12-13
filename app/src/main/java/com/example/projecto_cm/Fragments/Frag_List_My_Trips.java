package com.example.projecto_cm.Fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projecto_cm.Adapters.Adapter_List_My_Trips;
import com.example.projecto_cm.DAO_helper;
import com.example.projecto_cm.Interfaces.Interface_Card_My_Trip;
import com.example.projecto_cm.Interfaces.Interface_Frag_Change_Listener;
import com.example.projecto_cm.Main_Activity;
import com.example.projecto_cm.R;
import com.example.projecto_cm.Shared_View_Model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

public class Frag_List_My_Trips extends Fragment implements Interface_Card_My_Trip {

    private Shared_View_Model model;
    private Interface_Frag_Change_Listener fcl; // to change fragment
    private Adapter_List_My_Trips adapter_list_my_trips;
    private RecyclerView my_trips_recycler_view;
    private ArrayList<String > my_trips_list = new ArrayList<>();
    private DAO_helper dao;

    /**
     * load shared view model and get username from shared view model
     * load layout
     * initialize DAO_helper variable
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // get activity to get shared view model
        model = new ViewModelProvider(requireActivity()).get(Shared_View_Model.class);

        dao = new DAO_helper();

        // load login fragment layout
        View view = inflater.inflate(R.layout.fragment_list_my_trips_layout, container, false);
        fcl = (Main_Activity) inflater.getContext(); // to change fragments

        return view;
    }

    /**
     * call function to get list of trips from database
     * set list do adapter
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        model.get_username().observe(getViewLifecycleOwner(), item -> {
            // get list of trips
            dao.get_user_trips((String) item, this);

            my_trips_recycler_view = view.findViewById(R.id.recyclerView_my_trips_list);
        });
    }

    /**
     * get user's trip list from firebase database
     */
    public void getMyTrips(ArrayList<String> my_trips) {

        if(my_trips.size() > 0){
            my_trips_list.addAll(my_trips);
        }
        else {
            my_trips_list.add("You don't have trips created!");
        }

        // set up list of locations
        adapter_list_my_trips = new Adapter_List_My_Trips(requireActivity(), this, my_trips_list);
        my_trips_recycler_view.setAdapter(adapter_list_my_trips);
        my_trips_recycler_view.setLayoutManager(new LinearLayoutManager(requireActivity()));
    }

    /**
     * when a trip is selected (clicked on) show dialog with options
     * @param trip_position_in_list
     */
    @Override
    public void onTripClick(int trip_position_in_list) {

        Dialog show_trip_options_dialog = new Dialog(requireActivity());

        //Disable the default title
        show_trip_options_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        show_trip_options_dialog.setCanceledOnTouchOutside(true);

        //Ability to cancel the dialog by clicking outside the dialog
        //show_trip_options_dialog.setContentView(R.layout.);

        //Binding dialog elements
        /*start_date_input = show_trip_options_dialog.findViewById(R.id.start_date_input);
        start_date_input.setOnClickListener(v -> {
            type_of_date = "start";
            datePickerDialog.show();
        });*/

        show_trip_options_dialog.show();
    }
}
