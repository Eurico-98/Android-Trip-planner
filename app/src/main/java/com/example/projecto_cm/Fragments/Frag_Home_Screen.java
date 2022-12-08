package com.example.projecto_cm.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.projecto_cm.Interfaces.FragmentChangeListener;
import com.example.projecto_cm.Main_Activity;
import com.example.projecto_cm.R;
import com.example.projecto_cm.SharedViewModel;

public class Frag_Home_Screen extends Fragment {

    private SharedViewModel model;
    private FragmentChangeListener fcl; // to change fragment


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // get activity to get shared view model
        model = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

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
     * app bar menu set up
     * just changes the title in the app bar to Login
     * uses empty app bar layout
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
