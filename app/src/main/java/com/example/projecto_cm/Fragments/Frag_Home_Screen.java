package com.example.projecto_cm.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.projecto_cm.Interfaces.Interface_Frag_Change_Listener;
import com.example.projecto_cm.Main_Activity;
import com.example.projecto_cm.R;
import com.example.projecto_cm.Shared_View_Model;

public class Frag_Home_Screen extends Fragment {

    private Shared_View_Model model;
    private Interface_Frag_Change_Listener fcl; // to change fragment
    private String username;
    private View view;

    /**
     *  onCreateView of home screen fragment
     *  load layout and toolbar
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // get activity to get shared view model
        model = new ViewModelProvider(requireActivity()).get(Shared_View_Model.class);
        //model.get_username().observe(getViewLifecycleOwner(), item -> username = (String) item);

        // load login fragment layout
        view = inflater.inflate(R.layout.fragment_home_screen_layout, container, false);
        fcl = (Main_Activity) inflater.getContext(); // to change fragments

        // load toolbar of this fragment
        Toolbar toolbar = view.findViewById(R.id.home_screen_app_bar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);

        return view;
    }

    /**
     * bind buttons to listeners
     * crete trip
     * add friend
     * view my trips
     * take photo
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button create_trip, view_my_trips, take_photo;
        ImageButton add_friend;

        create_trip = view.findViewById(R.id.plan_trip);
        create_trip.setOnClickListener(view1 -> {

            // hide home screen layout while next fragment loads to prevent user from clicking in other buttons while next fragment loads
            view.setVisibility(View.GONE);

            Frag_Create_Trip frag_create_trip = new Frag_Create_Trip();
            fcl.replaceFragment(frag_create_trip, "yes");
        });


        add_friend = view.findViewById(R.id.add_friends);
        //add_friend.setOnClickListener(view1 -> );

        view_my_trips = view.findViewById(R.id.view_trips);
        view_my_trips.setOnClickListener(view1 -> {
            Frag_List_My_Trips frag_list_my_trips = new Frag_List_My_Trips();
            fcl.replaceFragment(frag_list_my_trips, "yes");
        });


        take_photo = view.findViewById(R.id.take_photo);
        //take_photo.setOnClickListener(view1 -> );
    }

    /**
     * app bar menu set up
     * has logout button and view profile button
     * @param menu
     * @param inflater
     */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.home_screen_actions, menu);

        // change app bar title
        ActionBar ab = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        assert ab != null;
        ab.setTitle("Home screen");

        super.onCreateOptionsMenu(menu,inflater);
    }

    /**
     * app bar menu actions
     * logout and view profile
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.view_profile){
            // TODO
        }
        else if(item.getItemId() == R.id.log_out){
            Frag_Login frag_login = new Frag_Login();
            fcl.replaceFragment(frag_login, "no");
        }

        return super.onOptionsItemSelected(item);
    }
}
