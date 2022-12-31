package com.example.projecto_cm.Fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.projecto_cm.DAO_helper;
import com.example.projecto_cm.Interfaces.Interface_Edit_Profile;
import com.example.projecto_cm.Interfaces.Interface_Frag_Change_Listener;
import com.example.projecto_cm.Main_Activity;
import com.example.projecto_cm.R;
import com.example.projecto_cm.Shared_View_Model;
import com.google.android.gms.tasks.Task;

public class Frag_Home_Screen extends Fragment implements Interface_Edit_Profile {

    private Interface_Frag_Change_Listener fcl; // to change fragment
    private Dialog edit_profile_dialog;
    private Button create_trip, view_my_trips, save_profile_changes;
    private ImageButton add_friend;
    private ImageView edit_profile_pic;
    private EditText edit_username_input, edit_email_input, edit_pass_input, edit_fullname;
    private DAO_helper dao = new DAO_helper();
    private String username;
    private Shared_View_Model model;
    private boolean edited_username = false;
    private Dialog loading_animation_dialog;

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

        // get username
        model = new ViewModelProvider(requireActivity()).get(Shared_View_Model.class);
        model.getData().observe(getViewLifecycleOwner(), item -> username = (String) item);

        // load login fragment layout
        View view = inflater.inflate(R.layout.frag_home_screen_layout, container, false);
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

        // prepare loading animation
        loading_animation_dialog = new Dialog(requireActivity());
        loading_animation_dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        loading_animation_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loading_animation_dialog.setCanceledOnTouchOutside(false);
        loading_animation_dialog.setContentView(R.layout.loading_animation_layout);

        // prepare edit profile dialog
        edit_profile_dialog = new Dialog(requireActivity());
        edit_profile_dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        edit_profile_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        edit_profile_dialog.setCanceledOnTouchOutside(true);
        edit_profile_dialog.setContentView(R.layout.dialog_edit_profile_layout);

        edit_profile_pic = edit_profile_dialog.findViewById(R.id.change_profile_picture);
        edit_fullname = edit_profile_dialog.findViewById(R.id.change_user_full_name_input);
        edit_username_input = edit_profile_dialog.findViewById(R.id.change_username_input);
        edit_email_input = edit_profile_dialog.findViewById(R.id.change_mail_input);
        edit_pass_input = edit_profile_dialog.findViewById(R.id.change_pass_input);
        save_profile_changes = edit_profile_dialog.findViewById(R.id.save_profile_changes);

        save_profile_changes.setOnClickListener(v -> editProfile());

        create_trip = view.findViewById(R.id.plan_trip);
        create_trip.setOnClickListener(view1 -> {

            // hide home screen layout while next fragment loads to prevent user from clicking in other buttons while next fragment loads
            view.setVisibility(View.GONE);

            Frag_Trip_Planner frag_trip_planner = new Frag_Trip_Planner();
            fcl.replaceFragment(frag_trip_planner, "yes");
        });


        add_friend = view.findViewById(R.id.add_friends);
        //add_friend.setOnClickListener(view1 -> );


        view_my_trips = view.findViewById(R.id.view_trips);
        view_my_trips.setOnClickListener(view1 -> {
            Frag_List_My_Trips frag_list_my_trips = new Frag_List_My_Trips();
            fcl.replaceFragment(frag_list_my_trips, "yes");
        });
    }

    /**
     * function to edit user profile
     */
    private void editProfile() {

        loading_animation_dialog.show();

        // get inserted values if any
        String username_inserted = "", email = "", pass = "", fullname = "";

        if(!edit_username_input.getText().toString().equals("") && !edit_username_input.getText().toString().contains(" ")){

            // dismiss keyboard
            edit_username_input.onEditorAction(EditorInfo.IME_ACTION_DONE);
            username_inserted = edit_username_input.getText().toString();
            username = username_inserted;
            edited_username = true;
        }

        if(!edit_email_input.getText().toString().equals("") && !edit_email_input.getText().toString().contains(" ")){

            // dismiss keyboard
            edit_email_input.onEditorAction(EditorInfo.IME_ACTION_DONE);
            email = edit_email_input.getText().toString();
        }

        if(!edit_pass_input.getText().toString().equals("") && !edit_pass_input.getText().toString().contains(" ")){

            // dismiss keyboard
            edit_pass_input.onEditorAction(EditorInfo.IME_ACTION_DONE);
            pass = edit_pass_input.getText().toString();
        }

        if(!edit_fullname.getText().toString().equals("") && !edit_fullname.getText().toString().contains(" ")){

            // dismiss keyboard
            edit_fullname.onEditorAction(EditorInfo.IME_ACTION_DONE);
            fullname = edit_fullname.getText().toString();
        }

        if(username_inserted.equals("") && email.equals("") && pass.equals("") && fullname.equals("")){
            loading_animation_dialog.dismiss();
            Toast.makeText(requireActivity(), "Edit at least one field!", Toast.LENGTH_SHORT).show();
            return;
        }

        dao.editUserProfile(username, this, username_inserted, email, pass, fullname);
    }

    /**
     * show result message after firebase access to edit user data
     * @param result
     */
    @Override
    public void showResultMessage(Object result) {

        loading_animation_dialog.dismiss();

        try {
            // is inserted value is invalid
            String message = (String) result;
            Toast.makeText(requireActivity(), message,Toast.LENGTH_SHORT).show();
        }
        // if inserted value is valid
        catch (Exception e){

            Task<Void> firebase_result = (Task<Void>) result;
            assert firebase_result != null;
            firebase_result.addOnSuccessListener(suc -> {
                Toast.makeText(requireActivity(), "Profile edited Successfully!", Toast.LENGTH_SHORT).show();

                // change username in shared view model
                if(edited_username){
                    model.sendData(username);
                    edited_username = false;
                }

            }).addOnFailureListener(er ->
                    Toast.makeText(requireActivity(), "Failed to edit profile due to network error!", Toast.LENGTH_SHORT).show()
            );
        }
    }

    /**
     * app bar menu set up change title to home screen
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
            edit_profile_dialog.show();
        }
        else if(item.getItemId() == R.id.log_out){
            Frag_Login frag_login = new Frag_Login();
            fcl.replaceFragment(frag_login, "no");
        }

        return super.onOptionsItemSelected(item);
    }
}
