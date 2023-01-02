package com.example.projecto_cm.Fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.projecto_cm.DAO_helper;
import com.example.projecto_cm.Interfaces.Interface_Frag_Login;
import com.example.projecto_cm.Interfaces.Interface_Frag_Change_Listener;
import com.example.projecto_cm.Main_Activity;
import com.example.projecto_cm.R;
import com.example.projecto_cm.Shared_View_Model;

import java.util.Objects;

public class Frag_Login extends Fragment implements Interface_Frag_Login {

    private Shared_View_Model model;
    private Interface_Frag_Change_Listener fcl; // to change fragment
    private Dialog loading_animation_dialog;

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
        model = new ViewModelProvider(requireActivity()).get(Shared_View_Model.class);

        // load login fragment layout
        View view = inflater.inflate(R.layout.frag_login_layout, container, false);
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

        // prepare loading animation
        loading_animation_dialog = new Dialog(requireActivity());
        loading_animation_dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        loading_animation_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loading_animation_dialog.setCanceledOnTouchOutside(false);
        loading_animation_dialog.setContentView(R.layout.loading_animation_layout);

        // get register button view from layout
        Button register_button = requireView().findViewById(R.id.register_new_account_button);
        register_button.setOnClickListener(v -> {

            // load register fragment
            Frag_Register register_frag = new Frag_Register();
            fcl.replaceFragment(register_frag, "no");
        });

        // get login button
        Button login_button = requireActivity().findViewById(R.id.login_button);
        EditText username = requireActivity().findViewById(R.id.login_username_input);
        EditText password = requireActivity().findViewById(R.id.login_password_input);
        login_button.setOnClickListener(v -> {

            // dismiss keyboard
            username.onEditorAction(EditorInfo.IME_ACTION_DONE);
            password.onEditorAction(EditorInfo.IME_ACTION_DONE);

            // if username and pass were inserted
            if(!username.getText().toString().equals("") && !password.getText().toString().equals("")){

                // hide login layout and show loading animation
                loading_animation_dialog.show();

                // check if username and email are valid
                DAO_helper dao = new DAO_helper();
                dao.check_credentials(username.getText().toString(), password.getText().toString(), this);
            }
            else {
                Toast.makeText(requireActivity(), "Insert username and password!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * to get result of credentials verification
     * @param result
     */
    @Override
    public void result(String result, String username) {

        loading_animation_dialog.dismiss();

        // if credentials are wrong
        if(!Objects.equals(result, "Valid user")){
            Toast.makeText(requireActivity(), result, Toast.LENGTH_SHORT).show();
        }
        else {

            // send username to get it in home screen
            model.send_data(username);

            Frag_Home_Screen home_screen = new Frag_Home_Screen();
            fcl.replaceFragment(home_screen, "no");
        }
    }
}
