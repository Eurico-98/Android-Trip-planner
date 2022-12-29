package com.example.projecto_cm.Fragments;

import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projecto_cm.Adapters.Adapter_For_Listing_Search_Results;
import com.example.projecto_cm.DAO_helper;
import com.example.projecto_cm.Interfaces.Interface_Frag_Change_Listener;
import com.example.projecto_cm.Interfaces.Interface_User_Search_Result;
import com.example.projecto_cm.Main_Activity;
import com.example.projecto_cm.R;
import com.example.projecto_cm.Shared_View_Model;

import java.util.ArrayList;

public class Frag_Home_Screen extends Fragment implements Interface_User_Search_Result {

    private Interface_Frag_Change_Listener fcl; // to change fragment
    private String username;
    private View view;
    private Adapter_For_Listing_Search_Results adapter_for_listing_search_results;
    private Drawable frameDrawable;
    private EditText trip_title_or_username_input;
    private Button execute_trip_or_username_search_button;
    private Dialog loading_animation_dialog, interface_hints_dialog, search_dialog;
    private ViewStub searchResultsStub; // to load dialog item view dynamically according to what is needed in this frag
    private TextView username_search_result;
    private DAO_helper dao = new DAO_helper();

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

        // load login fragment layout
        view = inflater.inflate(R.layout.frag_home_screen_layout, container, false);
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



        Button create_trip, view_my_trips, take_photo;
        ImageButton add_friend;

        create_trip = view.findViewById(R.id.plan_trip);
        create_trip.setOnClickListener(view1 -> {

            // hide home screen layout while next fragment loads to prevent user from clicking in other buttons while next fragment loads
            view.setVisibility(View.GONE);

            Frag_Trip_Planner frag_trip_planner = new Frag_Trip_Planner();
            fcl.replaceFragment(frag_trip_planner, "yes");
        });


        add_friend = view.findViewById(R.id.add_friends);
        // prepare search dialog
        search_dialog = new Dialog(requireActivity());
        search_dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        search_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        search_dialog.setCanceledOnTouchOutside(true);
        search_dialog.setContentView(R.layout.dialog_search_trips_or_users_layout);

        // Inflate the recycler view of search function and add it to the layout
        searchResultsStub = search_dialog.findViewById(R.id.search_results_stub);
        searchResultsStub.setLayoutResource(R.layout.search_users_results_layout);
        username_search_result = (TextView) searchResultsStub.inflate();

        // for frame around result list - just a nice touch
        frameDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.recycler_view_frame);

        // dialog title
        TextView search_trips_or_users_title = search_dialog.findViewById(R.id.search_trips_or_users_title);
        search_trips_or_users_title.setText("Search Friend");

        // title input
        trip_title_or_username_input = search_dialog.findViewById(R.id.trip_title_or_username_input);
        trip_title_or_username_input.setHint("Insert username");

        // search button
        execute_trip_or_username_search_button = search_dialog.findViewById(R.id.execute_trip_or_username_search_button);
        execute_trip_or_username_search_button.setOnClickListener(v -> {

            if(!trip_title_or_username_input.getText().toString().equals("")){


                // dismiss keyboard
                trip_title_or_username_input.onEditorAction(EditorInfo.IME_ACTION_DONE);

                loading_animation_dialog.show();

                //instanciate DAO helper
                dao.seach_friend(trip_title_or_username_input.getText().toString());

            }
            else {
                Toast.makeText(requireActivity(), "Insert username!",Toast.LENGTH_SHORT).show();
            }
        });

        add_friend.setOnClickListener(view1 -> {
            search_dialog.show();
        });

        view_my_trips = view.findViewById(R.id.view_trips);
        view_my_trips.setOnClickListener(view1 -> {
            Frag_List_My_Trips frag_list_my_trips = new Frag_List_My_Trips();
            fcl.replaceFragment(frag_list_my_trips, "yes");
        });


        take_photo = view.findViewById(R.id.take_photo);
        //take_photo.setOnClickListener(view1 -> );
    }

    /**
     * app bar menu set up
     * has logout button and view profile button
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
            // TODO
        }
        else if(item.getItemId() == R.id.log_out){
            Frag_Login frag_login = new Frag_Login();
            fcl.replaceFragment(frag_login, "no");
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * If user exists returns username of the friend search else returns negative message
     * @param username
     */
    @Override
    public void searchResult(String username) {




        if(username == null) {
            username_search_result.setText("Username Not Found");
        }
        else{
            username_search_result.setText(username);
        }

        loading_animation_dialog.dismiss();
    }
}

