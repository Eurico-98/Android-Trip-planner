package com.example.projecto_cm.Frags_Login_Register;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.projecto_cm.DAO_helper;
import com.example.projecto_cm.Frag_register_interface;
import com.example.projecto_cm.FragmentChangeListener;
import com.example.projecto_cm.Main_Activity;
import com.example.projecto_cm.R;
import com.example.projecto_cm.SharedViewModel;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Frag_Register extends Fragment implements Frag_register_interface {

    private SharedViewModel model;
    private FragmentChangeListener fcl; // to change fragment
    private Button login_button;
    private ExecutorService service;
    private Handler handler;

    /**
     * on create view of register fragment
     * set the toolbar the view model and fragment inflater
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
        View view = inflater.inflate(R.layout.fragment_register_layout, container, false);
        fcl = (Main_Activity) inflater.getContext(); // to change fragments

        // load toolbar of this fragment
        Toolbar register_toolbar = view.findViewById(R.id.register_toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(register_toolbar);
        setHasOptionsMenu(true);

        return view;
    }

    /**
     * onViewCreated of register
     * set log in button listener - goes to login page
     * set listener for register_new_account button
     * bind input fields for username mail and password
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // bind input fields
        EditText username = requireView().findViewById(R.id.editUsername);
        EditText password = requireView().findViewById(R.id.editPassword);
        EditText email = requireView().findViewById(R.id.editEmail);
        Button register_new_account_button = requireView().findViewById(R.id.confirm_register_button);

        // set on click listener for register button
        register_new_account_button.setOnClickListener(v -> {

            // execute only if all inputs fields have text
            if(!username.getText().toString().equals("") && !email.getText().toString().equals("") && !password.getText().toString().equals("")){

                // do this in the background
                service.execute(() -> {

                    // create new DAO instance for database access
                    DAO_helper dao = new DAO_helper();

                    // check if username and mail already exist
                    dao.check_inserted_username(username.getText().toString(), email.getText().toString(), password.getText().toString(), dao, this);
                });
            }
        });

        // get register button view from layout
        login_button = requireView().findViewById(R.id.login_with_existent_account_button);
        login_button.setOnClickListener(v -> {

            // load login frag
            Frag_Login login_frag = new Frag_Login();
            fcl.replaceFragment(login_frag);
        });
    }

    /**
     * inflate app bar menu here
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

    public void check_mail(String username, String email, String password, DAO_helper dao, int username_exists) {

        System.out.println("------------------------------- aqui 3");

        if(username_exists == 1){
            Toast.makeText(requireActivity(), "Username already registered!", Toast.LENGTH_SHORT).show();
        }
        else {
            dao.check_inserted_email(username, email, password, dao, this);
        }
    }

    // method to check if username and mail are already registered and if not -> create new account
    public void create_new_account(String username, String email, String password, DAO_helper dao, int mail_exists) {

        System.out.println("------------------------------- aqui 6");

        if(mail_exists == 0){

            // this return a task that can have a on-success-listener
            try {
                dao.add_new_user_account(username, password, email).addOnSuccessListener(suc -> {

                    Toast.makeText(requireActivity(), "Account created Successfully!", Toast.LENGTH_SHORT).show();

                    // on success load login frag
                    Frag_Login login_frag = new Frag_Login();
                    fcl.replaceFragment(login_frag);

                }).addOnFailureListener(er ->
                        Toast.makeText(requireActivity(), "Failed to create account \n" + er.getMessage(), Toast.LENGTH_SHORT).show()
                );

            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                e.printStackTrace();
            }
        }
        else {
            Toast.makeText(requireActivity(), "Email already registered!", Toast.LENGTH_SHORT).show();
        }
    }
}
