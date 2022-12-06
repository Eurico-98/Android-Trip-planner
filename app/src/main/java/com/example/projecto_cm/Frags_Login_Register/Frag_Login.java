package com.example.projecto_cm.Frags_Login_Register;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.projecto_cm.FragmentChangeListener;
import com.example.projecto_cm.Main_Activity;
import com.example.projecto_cm.R;
import com.example.projecto_cm.SharedViewModel;

public class Frag_Login extends Fragment {

    private SharedViewModel model;
    private FragmentChangeListener fcl; // to change fragment
    private Button register_button;

    /**
     * onCreateView of login fragment
     * set register button listener
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // get activity to get shared view model
        model = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // load login fragment layout
        View view = inflater.inflate(R.layout.fragment_login_layout, container, false);
        fcl = (Main_Activity) inflater.getContext(); // to change fragments

        // load toolbar of this fragment
        Toolbar login_toolbar = view.findViewById(R.id.login_toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(login_toolbar);
        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // get register button view from layout
        register_button = requireView().findViewById(R.id.register_new_account_button);
        register_button.setOnClickListener(v -> {

            // load register fragment
            Frag_Register register_frag = new Frag_Register();
            fcl.replaceFragment(register_frag);
        });
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
        inflater.inflate(R.menu.no_actions_appbar, menu);

        // change app bar title
        ActionBar ab = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        assert ab != null;
        ab.setTitle("Login");

        super.onCreateOptionsMenu(menu,inflater);
    }

}
