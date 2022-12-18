package com.example.projecto_cm.Fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
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

import java.util.ArrayList;
import java.util.Observer;

public class Frag_List_My_Trips extends Fragment implements Interface_Card_My_Trip {

    private Shared_View_Model model;
    private Interface_Frag_Change_Listener fcl; // to change fragment
    private Adapter_List_My_Trips adapter_list_my_trips;
    private RecyclerView my_trips_recycler_view;
    private ArrayList<String > my_trips_list = new ArrayList<>();
    private DAO_helper dao;
    private View view;
    private Dialog loading_animation_dialog;
    private String username;

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
        view = inflater.inflate(R.layout.fragment_list_my_trips_layout, container, false);
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

        // prepare loading animation
        loading_animation_dialog = new Dialog(requireActivity());
        loading_animation_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loading_animation_dialog.setCanceledOnTouchOutside(false);
        loading_animation_dialog.setContentView(R.layout.loading_animation_layout);

        loading_animation_dialog.show();

        model.get_data().observe(getViewLifecycleOwner(), item -> {

            // this fucking observer will catch the second callback when the user clicks on one of the options of a trip and that will cause an error this way it won't
            try {
                username = (String) item;

                // get list of trips
                dao.get_user_trips(username, this);

                my_trips_recycler_view = view.findViewById(R.id.recyclerView_my_trips_list);
            } catch (Exception ignored){}
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

        loading_animation_dialog.dismiss();
    }

    /**
     * when a trip is selected (clicked on) show dialog with options:
     * view trip route
     * invite friends
     * edit trip data
     * start location of elections
     * @param trip_position_in_list
     */
    @Override
    public void onTripClick(int trip_position_in_list) {

        Dialog show_trip_options_dialog = new Dialog(requireActivity());
        show_trip_options_dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); // make dialog window background transparent

        //Disable the default title
        show_trip_options_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        show_trip_options_dialog.setCanceledOnTouchOutside(true);

        show_trip_options_dialog.setContentView(R.layout.dialog_trip_options_layout);

        // set dialog title to trip title
        TextView trip_options_title = show_trip_options_dialog.findViewById(R.id.trip_options_dialog_title);
        trip_options_title.setText(my_trips_list.get(trip_position_in_list).split("title=")[1].split(",")[0]);

        ImageButton edit_trip_button = show_trip_options_dialog.findViewById(R.id.edit_trip_image_button);
        edit_trip_button.setOnClickListener(v -> {

            // call create trip method but pass selected trip and username
            model.send_data(new String[]{username, my_trips_list.get(trip_position_in_list), String.valueOf(trip_position_in_list)});

            show_trip_options_dialog.dismiss();

            // hide list trips layout while next create trip fragment loads to prevent user from clicking in other buttons while it loads
            view.setVisibility(View.GONE);

            my_trips_list.clear(); // clear list to avoid getting the old trips and the updated trips when the user returns from the create trip fragment

            Frag_Create_Trip frag_create_trip = new Frag_Create_Trip();
            fcl.replaceFragment(frag_create_trip, "yes");
        });

        show_trip_options_dialog.show();
    }
}
