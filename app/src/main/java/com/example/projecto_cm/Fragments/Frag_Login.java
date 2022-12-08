package com.example.projecto_cm.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.projecto_cm.DAO_helper;
import com.example.projecto_cm.Interfaces.Frag_login_interface;
import com.example.projecto_cm.Interfaces.FragmentChangeListener;
import com.example.projecto_cm.Main_Activity;
import com.example.projecto_cm.R;
import com.example.projecto_cm.SharedViewModel;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Frag_Login extends Fragment implements Frag_login_interface {

    private SharedViewModel model;
    private FragmentChangeListener fcl; // to change fragment
    private ExecutorService service;
    private Handler handler;

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

        // create 2 threads just in case - to read and write from database
        service = Executors.newFixedThreadPool(2);
        handler = new Handler(Looper.getMainLooper());

        // load login fragment layout
        View view = inflater.inflate(R.layout.fragment_login_layout, container, false);
        fcl = (Main_Activity) inflater.getContext(); // to change fragments

        return view;
    }

    /**
     * bind login button and create account button
     * get input values to check if username and password are correct
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // get register button view from layout
        Button register_button = requireView().findViewById(R.id.register_new_account_button);
        register_button.setOnClickListener(v -> {

            // load register fragment
            Frag_Register register_frag = new Frag_Register();
            fcl.replaceFragment(register_frag);
        });

        // get login button
        Button login_button = requireActivity().findViewById(R.id.login_button);
        EditText username = requireActivity().findViewById(R.id.editUsername);
        EditText password = requireActivity().findViewById(R.id.editPassword);
        login_button.setOnClickListener(v -> {

            // if username and pass were inserted
            if(!username.getText().toString().equals("") && !password.getText().toString().equals("")){

                // check if username and email are valid
                service.execute(() -> {

                    DAO_helper dao = new DAO_helper();
                    dao.check_credentials(username.getText().toString(), password.getText().toString(), dao, this);
                });

            }
            else {
                Toast.makeText(requireActivity(), "Please insert username and password!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * to get result of credentials verification
     * @param result
     */
    @Override
    public void result(String result) {

        // if credentials are wrong
        if(!Objects.equals(result, "Valid user")){
            Toast.makeText(requireActivity(), result, Toast.LENGTH_SHORT).show();
        }
        else {
            Frag_Home_Screen home_screen = new Frag_Home_Screen();
            fcl.replaceFragment(home_screen);
        }
    }
}
