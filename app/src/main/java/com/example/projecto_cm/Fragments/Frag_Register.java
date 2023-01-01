package com.example.projecto_cm.Fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.projecto_cm.DAO_helper;
import com.example.projecto_cm.Interfaces.Interface_Frag_Register;
import com.example.projecto_cm.Interfaces.Interface_Frag_Change_Listener;
import com.example.projecto_cm.Main_Activity;
import com.example.projecto_cm.R;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class Frag_Register extends Fragment implements Interface_Frag_Register {

    private Interface_Frag_Change_Listener fcl; // to change fragment
    private Dialog loading_animation_dialog;

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

        // load login fragment layout
        View view = inflater.inflate(R.layout.frag_register_layout, container, false);
        fcl = (Main_Activity) inflater.getContext(); // to change fragments

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

        // prepare loading animation
        loading_animation_dialog = new Dialog(requireActivity());
        loading_animation_dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        loading_animation_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loading_animation_dialog.setCanceledOnTouchOutside(false);
        loading_animation_dialog.setContentView(R.layout.loading_animation_layout);

        // bind input fields
        EditText username = requireView().findViewById(R.id.register_username_input);
        EditText password = requireView().findViewById(R.id.register_password_input);
        Button register_new_account_button = requireView().findViewById(R.id.confirm_register_button);

        // set on click listener for register button
        register_new_account_button.setOnClickListener(v -> {

            // execute only if all inputs fields have text
            if(!username.getText().toString().equals("") && !password.getText().toString().equals("") && !username.getText().toString().contains(" ") && !password.getText().toString().contains(" ")){

                // hide login layout and show loading animation
                loading_animation_dialog.show();

                // create new DAO instance for database access
                DAO_helper dao = new DAO_helper(requireActivity());

                // check if username and mail already exist
                dao.checkInsertedUsername(username.getText().toString(), password.getText().toString(), dao, this);
            }
            else {
                Toast.makeText(requireActivity(), "Invalid credentials!", Toast.LENGTH_SHORT).show();
            }
        });

        // get register button view from layout
        Button login_button = requireView().findViewById(R.id.login_with_existent_account_button);
        login_button.setOnClickListener(v -> {

            // load login frag
            Frag_Login login_frag = new Frag_Login();
            fcl.replaceFragment(login_frag, "no");
        });
    }

    // method to check if username and mail are already registered and if not -> create new account
    public void createNewAccount(String username, String password, DAO_helper dao, int mail_exists) {

        loading_animation_dialog.dismiss();

        if(mail_exists == 0){

            // this return a task that can have a on-success-listener
            try {
                dao.addNewUserAccount(username, password).addOnSuccessListener(suc -> {

                    Toast.makeText(requireActivity(), "Account created Successfully!", Toast.LENGTH_SHORT).show();

                    // on success load login frag
                    Frag_Login login_frag = new Frag_Login();
                    fcl.replaceFragment(login_frag, "no");

                }).addOnFailureListener(er ->
                        Toast.makeText(requireActivity(), "Failed to create account!", Toast.LENGTH_SHORT).show()
                );

            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                Toast.makeText(requireActivity(), "Failed to create account!", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(requireActivity(), "Username already registered!", Toast.LENGTH_SHORT).show();
        }
    }
}
