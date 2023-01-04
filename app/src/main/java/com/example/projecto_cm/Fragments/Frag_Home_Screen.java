package com.example.projecto_cm.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
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
import android.view.ViewStub;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.projecto_cm.DAO_helper;
import com.example.projecto_cm.Interfaces.Interface_Edit_Profile;
import com.example.projecto_cm.Interfaces.Interface_Frag_Change_Listener;
import com.example.projecto_cm.MQTT_Helper;
import com.example.projecto_cm.Main_Activity;
import com.example.projecto_cm.R;
import com.example.projecto_cm.Shared_View_Model;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Frag_Home_Screen extends Fragment implements Interface_Edit_Profile {

    public boolean isFragmentReady = false;
    private ExecutorService service;
    private Handler handler;
    private Shared_View_Model model;
    private Interface_Frag_Change_Listener fcl; // to change fragment
    private DAO_helper dao;
    private String username, reset_username, original_fullname;
    private Button save_profile_changes, save_password_button, yes_delete_button, no_delete_button, execute_trip_or_username_search_button;
    private ImageButton create_trip, view_my_trips, add_friend;
    private ImageView profile_pic, edit_profile_pic_button, delete_account_button;
    private Dialog loading_animation_dialog, edit_profile_dialog, change_password_dialog, delete_account_dialog;
    private EditText edit_username_input, edit_pass_input, edit_fullname, trip_title_or_username_input;
    private TextView change_pass_button;

    private Dialog search_dialog;
    private ViewStub searchResultsStub; // to load dialog item view dynamically according to what is needed in this frag
    private TextView username_search_result;
    private Button send_friend_request;
    private ConstraintLayout send_request_cLayout;
    private MQTT_Helper helper;


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

        model = new ViewModelProvider(requireActivity()).get(Shared_View_Model.class);

        // get username and start mqtt
        model.getData().observe(getViewLifecycleOwner(), item ->{
            username = (String) item;
            model.getActivityInstance().observe(getViewLifecycleOwner(), item_2 -> setUpTopics(username, item_2));
        });
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

            loading_animation_dialog.show();
            service.execute(() -> {

                Frag_Trip_Planner frag_trip_planner = new Frag_Trip_Planner();
                fcl.replaceFragment(frag_trip_planner, "yes");

                handler.post(() -> {
                    // wait for the Frag_Trip_Planner fragment to be fully initialized before dismissing the loading animation
                    while (!frag_trip_planner.isFragmentReady) {
                    }
                    loading_animation_dialog.dismiss();
                });
            });
        });



        //--------------------------------------------------------------------------------------------------------------------------
        add_friend = view.findViewById(R.id.add_friends);

        // prepare search dialog
        search_dialog = new Dialog(requireActivity());
        search_dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        search_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        search_dialog.setCanceledOnTouchOutside(true);
        search_dialog.setContentView(R.layout.dialog_search_trips_or_users_layout);

        //
        searchResultsStub = search_dialog.findViewById(R.id.search_results_stub);
        searchResultsStub.setLayoutResource(R.layout.search_users_results_layout);
        /*send_request_cLayout = (ConstraintLayout) searchResultsStub.inflate();
        username_search_result = (TextView) send_request_cLayout.findViewById(R.id.search_results_view);
        send_friend_request = send_request_cLayout.findViewById(R.id.send_friend_request_button);
        send_friend_request.setVisibility(View.INVISIBLE);*/
        username_search_result = (TextView) searchResultsStub.inflate();

        // dialog title
        TextView search_trips_or_users_title = search_dialog.findViewById(R.id.search_trips_or_users_title);
        search_trips_or_users_title.setText("Search Friend");

        // title input
        trip_title_or_username_input = search_dialog.findViewById(R.id.trip_title_or_username_input);
        trip_title_or_username_input.setHint("Insert username");

        // search button
        execute_trip_or_username_search_button = search_dialog.findViewById(R.id.execute_trip_or_username_search_button);
        execute_trip_or_username_search_button.setOnClickListener(v -> {

            if(trip_title_or_username_input.getText().toString().equals(username)){
                Toast.makeText(requireActivity(), "Can't send friend request to yourself!",Toast.LENGTH_SHORT).show();
                username_search_result.setText("Can't send friend request to yourself!");
            }
            else if(!trip_title_or_username_input.getText().toString().equals("")){


                // dismiss keyboard
                trip_title_or_username_input.onEditorAction(EditorInfo.IME_ACTION_DONE);

                loading_animation_dialog.show();

                //instanciate DAO helper
                dao.search_friend(trip_title_or_username_input.getText().toString(), this);

                search_dialog.dismiss();
            }
            else {
                Toast.makeText(requireActivity(), "Insert username!",Toast.LENGTH_SHORT).show();
            }
        });


        add_friend.setOnClickListener(view1 -> search_dialog.show());


        view_my_trips = view.findViewById(R.id.view_trips);
        view_my_trips.setOnClickListener(view1 -> {
            Frag_List_My_Trips frag_list_my_trips = new Frag_List_My_Trips();
            fcl.replaceFragment(frag_list_my_trips, "yes");
        });

        isFragmentReady = true; // to dismiss loading animation that was initialized in the login fragment
    }


    /**
     * permission handler
     */
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {});

    /**
     * set profile image to the picture taken and store bitmap image in sqlite storage
     */
    private final ActivityResultLauncher<Intent> startCameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {

        // handle the result of the camera intent
        if (result.getResultCode() == Activity.RESULT_OK) {

            // get the image taken with the camera
            Bitmap imageBitmap = (Bitmap) result.getData().getExtras().get("data");

            // set the image to the image view
            roundPictureCorners(imageBitmap);

            // save picture do sqlite and firebase databases
            dao.savePicture(username, imageBitmap, "profile");
        }
    });


    /**
     * request permissions to open camera and to write to external storage
     */
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
     * do nothing when android back button is pressed
     */
    @Override
    public void onResume() {
        super.onResume();
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {}
        });
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

                    // set profile picture
                    try {
                        roundPictureCorners(BitmapFactory.decodeByteArray(cursor.getBlob(4), 0, cursor.getBlob(4).length));
                    }
                    // if user does not have a profile picture use the default one
                    catch (Exception e){
                        e.printStackTrace();
                        profile_pic.setImageResource(R.drawable.profile_pic_template);
                    }
                }

                // hide button and only show save changes button if data was changed
                save_profile_changes.setVisibility(View.INVISIBLE);

                handler.post(() -> edit_profile_dialog.show());
            });
        }
        else if(item.getItemId() == R.id.log_out){
            try {
                helper.unSubscribeToTopic(username);
            } catch (MqttException e) {
                e.printStackTrace();
            }
            dao.logoutUser(username);
            Frag_Login frag_login = new Frag_Login();
            fcl.replaceFragment(frag_login, "no");
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * round picture corners and make image fit the frame
     */
    private void roundPictureCorners(Bitmap imageBitmap) {

        // Create a new Bitmap object from the given Bitmap image
        Bitmap roundedBitmap = Bitmap.createBitmap(imageBitmap.getWidth(), imageBitmap.getHeight(), imageBitmap.getConfig());

        // Set the corner radius to the desired value
        float cornerRadius = 15.0f;

        // Create a new Canvas object to draw on the new Bitmap
        Canvas canvas = new Canvas(roundedBitmap);

        // Create a new Paint object for drawing shapes
        Paint paint = new Paint();

        // Set the paint to use anti-aliasing for smooth edges
        paint.setAntiAlias(true);

        // set the image to the canvas
        paint.setShader(new BitmapShader(imageBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));

        // Create a new RectF object for the rounded rectangle
        RectF rect = new RectF(0, 0, imageBitmap.getWidth(), imageBitmap.getHeight());

        // Draw the rounded rectangle on the canvas using the paint and the RectF object
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint);

        // Set the ImageView's image to the rounded Bitmap and resize image frame to the image size
        ViewGroup.LayoutParams layoutParams = profile_pic.getLayoutParams();
        layoutParams.width = 280; // set the width in pixels
        layoutParams.height = 380; // set the height in pixels
        profile_pic.setLayoutParams(layoutParams);
        profile_pic.setImageBitmap(roundedBitmap);
    }

    /**
     * Responsible for handling the result of the username search, if exists sends friend request, if not notifies user for unexistent username
     * @param friendsUsername
     */
    public void searchResult(String friendsUsername) throws MqttException, IOException {

        loading_animation_dialog.dismiss();

        if (friendsUsername.equals("no result found")) {
            username_search_result.setText("User does not exist");
            Toast.makeText(requireActivity(), "User does not exist!", Toast.LENGTH_SHORT).show();
        }
        else if (friendsUsername.equals("Already Friends")){
            username_search_result.setText("Already Friends");
            Toast.makeText(requireActivity(), "Already Friends!", Toast.LENGTH_SHORT).show();
        }
        else{
            username_search_result.setText(friendsUsername);
            Toast.makeText(requireActivity(), "Request sent!",Toast.LENGTH_SHORT).show();
            helper.addFriendRequest(username, friendsUsername);
            search_dialog.dismiss();
        }
    }

    /**
     * subscribes to topics when app is initialized
     * @param user
     * @param activity
     */
    void setUpTopics(String user, Main_Activity activity){

        // already runs in background
        // create connection to MQTT broker in the background thread
        try {
            helper = new MQTT_Helper(getContext(), activity);
        } catch (MqttException ignored) {}

        // subscribe only if connection is OK to the user topic
        if(helper != null){
            try {
                helper.subscribeToTopic(user);
            } catch (MqttException ignored){}
        }
        else {
            handler.post(() -> Toast.makeText(requireActivity(), "Unable to start notifications system due to network error!", Toast.LENGTH_SHORT).show());
        }
    }
}
