package com.example.projecto_cm;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

public class Register extends Fragment {

    private SharedViewModel model;
    private FragmentChangeListener fcl; // to change fragment
    private Button login_button;

    /**
     * on create view of register fragment
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
        View view = inflater.inflate(R.layout.fragment_register_layout, container, false);
        fcl = (Login_Register) inflater.getContext(); // to change fragments

        // load toolbar of this fragment
        Toolbar register_toolbar = view.findViewById(R.id.register_toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(register_toolbar);
        setHasOptionsMenu(true);

        return view;
    }

    /**
     * onViewCreated of register
     * set log in button listener
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // get register button view from layout
        login_button = requireView().findViewById(R.id.login_with_existent_account_button);
        login_button.setOnClickListener(v -> {

            // load login frag
            Login login_frag = new Login();
            fcl.replaceFragment(login_frag);
        });
    }

    /**
     * app bar menu set up
     * just changes the title in the app bar to Register
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
        ab.setTitle("Register");

        super.onCreateOptionsMenu(menu,inflater);
    }
}
