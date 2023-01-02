package com.example.projecto_cm.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.projecto_cm.DAO_helper;
import com.example.projecto_cm.Interfaces.Interface_Edit_Profile;
import com.example.projecto_cm.Interfaces.Interface_Frag_Change_Listener;
import com.example.projecto_cm.Main_Activity;
import com.example.projecto_cm.R;
import com.example.projecto_cm.Shared_View_Model;
import com.google.android.gms.tasks.Task;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Frag_Home_Screen extends Fragment implements Interface_Edit_Profile {

    private ExecutorService service;
    private Handler handler;
    private Shared_View_Model model;
    private Interface_Frag_Change_Listener fcl; // to change fragment
    private DAO_helper dao;
    private String username, reset_username, original_fullname;
    private Button create_trip, view_my_trips, save_profile_changes, save_password_button, yes_delete_button, no_delete_button;
    private ImageButton add_friend;
    private ImageView profile_pic, edit_profile_pic_button, delete_account_button;
    private Dialog loading_animation_dialog, edit_profile_dialog, change_password_dialog, delete_account_dialog;
    private EditText edit_username_input, edit_pass_input, edit_fullname;
    private TextView change_pass_button;


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
        dao = new DAO_helper(requireActivity());

        // create 1 thread so execute searches with google maps
        service = Executors.newFixedThreadPool(1);
        handler = new Handler(Looper.getMainLooper());

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



        // --------------------------------------------------------------------- prepare edit profile dialog
        edit_profile_dialog = new Dialog(requireActivity());
        edit_profile_dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        edit_profile_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        edit_profile_dialog.setCanceledOnTouchOutside(true);
        edit_profile_dialog.setContentView(R.layout.dialog_edit_profile_layout);

        profile_pic = edit_profile_dialog.findViewById(R.id.profile_image);
        edit_profile_pic_button = edit_profile_dialog.findViewById(R.id.change_profile_picture);
        edit_fullname = edit_profile_dialog.findViewById(R.id.change_user_full_name_input);
        edit_username_input = edit_profile_dialog.findViewById(R.id.change_username_input);
        save_profile_changes = edit_profile_dialog.findViewById(R.id.save_profile_changes);
        change_pass_button = edit_profile_dialog.findViewById(R.id.change_pass_button);
        delete_account_button = edit_profile_dialog.findViewById(R.id.delete_account_button);

        // show button if text was changed
        edit_fullname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                // Show button if there is text in the EditText
                if (s.toString().trim().length() > 0)
                    save_profile_changes.setVisibility(View.VISIBLE);

                // hide button if both username and full name remain the same as their original values
                if(s.toString().trim().equals(original_fullname))
                    save_profile_changes.setVisibility(View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // show button if text was changed
        edit_username_input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                // Show button if there is text in the EditText
                if (s.toString().trim().length() > 0)
                    save_profile_changes.setVisibility(View.VISIBLE);

                // hide button if both username and full name remain the same as their original values
                if(s.toString().trim().equals(username))
                    save_profile_changes.setVisibility(View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // button listener
        save_profile_changes.setOnClickListener(v -> editProfile());

        delete_account_button.setOnClickListener(v -> delete_account_dialog.show());

        edit_profile_pic_button.setOnClickListener(v -> takePicture());
        // --------------------------------------------------------------------- prepare edit profile dialog





        // --------------------------------------------------------------------- prepare delete account dialog
        delete_account_dialog = new Dialog(requireActivity());
        delete_account_dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        delete_account_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        delete_account_dialog.setCanceledOnTouchOutside(true);
        delete_account_dialog.setContentView(R.layout.dialog_confirm_delete_account_layout);

        yes_delete_button = delete_account_dialog.findViewById(R.id.yes_delete_account_button);
        yes_delete_button.setOnClickListener(v -> deleteAccount());

        no_delete_button = delete_account_dialog.findViewById(R.id.no_delete_account_button);
        no_delete_button.setOnClickListener(v -> delete_account_dialog.dismiss());
        // --------------------------------------------------------------------- prepare delete account dialog





        // --------------------------------------------------------------------- prepare change password dialog
        change_password_dialog = new Dialog(requireActivity());
        change_password_dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        change_password_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        change_password_dialog.setCanceledOnTouchOutside(true);
        change_password_dialog.setContentView(R.layout.dialog_edit_password_layout);

        edit_pass_input = change_password_dialog.findViewById(R.id.change_pass_input);
        save_password_button = change_password_dialog.findViewById(R.id.save_password_button);
        save_password_button.setOnClickListener(v -> changePassword());


        change_pass_button.setOnClickListener(v -> change_password_dialog.show());
        // --------------------------------------------------------------------- prepare change password dialog




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



    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {});

    private final ActivityResultLauncher<Intent> startCameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {

        // handle the result of the camera intent
        if (result.getResultCode() == Activity.RESULT_OK) {

            // get the image taken with the camera
            Bitmap imageBitmap = (Bitmap) result.getData().getExtras().get("data");

            // set the image to the image view
            profile_pic.setImageBitmap(imageBitmap);
        }
    });

    private void takePicture() {
        if (requireActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {

            // check for camera permissions
            if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

                // check for external storage write permissions
                if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                    // open camera to take picture
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startCameraLauncher.launch(cameraIntent);
                }
                else {
                    // request external storage write permission
                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
            }
            else {
                // request camera permission
                requestPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        }
        else {
            // display message if device does not have a camera
            Toast.makeText(getActivity(), "This device does not have a camera", Toast.LENGTH_SHORT).show();
        }
    }



    /**
     * delete user account from firebase and sqlite
     */
    private void deleteAccount() {
        delete_account_dialog.dismiss();
        edit_profile_dialog.dismiss();
        loading_animation_dialog.show();
        dao.deleteUserAccount(username, this);
    }

    /**
     * delete account result
     * @param result
     */
    @Override
    public void deleteAccountResult(String result) {
        loading_animation_dialog.dismiss();
        Frag_Login frag_login = new Frag_Login();
        fcl.replaceFragment(frag_login, "no");
        Toast.makeText(requireActivity(), result, Toast.LENGTH_SHORT).show();
    }


    /**
     * function to edit user full name and username
     */
    private void editProfile() {

        loading_animation_dialog.show();

        // get inserted values if any
        String new_fullname = edit_fullname.getText().toString();
        String new_username = edit_username_input.getText().toString();
        reset_username = username;

        edit_username_input.onEditorAction(EditorInfo.IME_ACTION_DONE); // dismiss keyboard

        if(new_username.equals("") || new_fullname.equals("")){
            loading_animation_dialog.dismiss();
            Toast.makeText(requireActivity(), "Fields must not be empty!", Toast.LENGTH_SHORT).show();
            return;
        }
        else if(new_username.contains(" ")){
            loading_animation_dialog.dismiss();
            Toast.makeText(requireActivity(), "Spaces not allowed in username!", Toast.LENGTH_SHORT).show();
            return;
        }

        dao.editUserProfile(username, this, new_username, new_fullname);

        username = new_username; // change username global variable - if new_username is not already registered it will stay in the model view
    }

    /**
     * to change password
     */
    private void changePassword(){

        String new_password = edit_pass_input.getText().toString();

        edit_pass_input.onEditorAction(EditorInfo.IME_ACTION_DONE); // dismiss keyboard

        if(new_password.contains(" ")){
            loading_animation_dialog.dismiss();
            Toast.makeText(requireActivity(), "Pass can't have spaces!", Toast.LENGTH_SHORT).show();
        }
        else if(!new_password.equals("")){
            loading_animation_dialog.show();
            dao.changeUserPassword(username, this, new_password);
        }
    }

    /**
     * show result message after firebase access to edit user data
     * @param result
     */
    @Override
    public void showResultMessage(String result) {

        loading_animation_dialog.dismiss();

        // change username global variable back to original value
        if(result.contains(" already exists!")){
            username = reset_username;
            Toast.makeText(requireActivity(), result,Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(requireActivity(), result, Toast.LENGTH_SHORT).show();
            // change username in shared view model
            model.sendData(username);
            save_profile_changes.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * result of password change
     * @param result
     */
    @Override
    public void changePassResult(String result){
        change_password_dialog.dismiss();
        loading_animation_dialog.dismiss();
        edit_pass_input.setText("");
        Toast.makeText(requireActivity(), result, Toast.LENGTH_SHORT).show();
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
    @SuppressLint("SetTextI18n")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.view_profile){

            // get user profile data from sqlite database
            service.execute(() -> {
                Cursor cursor = dao.getUserData(username);
                while (cursor.moveToNext()){
                    edit_username_input.setText(cursor.getString(1));
                    original_fullname = cursor.getString(3);
                    edit_fullname.setText(original_fullname);
                    //System.out.println(cursor.getString(4)); // photo
                }

                // hide button and only show save changes button if data was changed
                save_profile_changes.setVisibility(View.INVISIBLE);

                handler.post(() -> edit_profile_dialog.show());
            });
        }
        else if(item.getItemId() == R.id.log_out){
            dao.logoutUser(username);
            Frag_Login frag_login = new Frag_Login();
            fcl.replaceFragment(frag_login, "no");
        }

        return super.onOptionsItemSelected(item);
    }
}
