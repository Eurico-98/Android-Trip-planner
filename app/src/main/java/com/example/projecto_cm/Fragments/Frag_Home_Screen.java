package com.example.projecto_cm.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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

import com.example.projecto_cm.Interfaces.FragmentChangeListener;
import com.example.projecto_cm.Main_Activity;
import com.example.projecto_cm.R;

public class Frag_Home_Screen extends Fragment {

    private FragmentChangeListener fcl; // to change fragment
    private String username;

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

        // load login fragment layout
        View view = inflater.inflate(R.layout.fragment_home_screen_layout, container, false);
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
            Frag_Create_Trip frag_create_trip = new Frag_Create_Trip();
            fcl.replaceFragment(frag_create_trip, "yes");
        });


        add_friend = view.findViewById(R.id.add_friends);
        //add_friend.setOnClickListener(view1 -> );


        view_my_trips = view.findViewById(R.id.view_trips);
        //view_my_trips.setOnClickListener(view1 -> );


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
}
