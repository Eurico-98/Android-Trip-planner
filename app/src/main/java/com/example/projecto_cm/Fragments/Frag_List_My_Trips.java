package com.example.projecto_cm.Fragments;

import android.annotation.SuppressLint;
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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projecto_cm.Adapters.Adapter_For_Listing_Trips;
import com.example.projecto_cm.Adapters.Adapter_For_Listing_Search_Results;
import com.example.projecto_cm.DAO_helper;
import com.example.projecto_cm.Interfaces.Interface_Card_My_Trip_In_Trip_List;
import com.example.projecto_cm.Interfaces.Interface_Card_Search_Result_In_Create_Trip;
import com.example.projecto_cm.Interfaces.Interface_Frag_Change_Listener;
import com.example.projecto_cm.Main_Activity;
import com.example.projecto_cm.R;
import com.example.projecto_cm.Shared_View_Model;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Locale;

public class Frag_List_My_Trips extends Fragment implements Interface_Card_My_Trip_In_Trip_List, Interface_Card_Search_Result_In_Create_Trip {

    private Shared_View_Model model;
    private Interface_Frag_Change_Listener fcl; // to change fragment
    private Adapter_For_Listing_Trips adapter_for_listing_trips;
    private Adapter_For_Listing_Search_Results adapter_for_listing_search_results;
    private RecyclerView my_trips_recycler_view, search_trips_results_recycler_view;
    private ArrayList<String> my_trips_list = new ArrayList<>(), search_results = new ArrayList<>();
    private DAO_helper dao;
    private View view;
    private Dialog loading_animation_dialog, interface_hints_dialog, search_dialog;
    private ViewStub searchResultsStub; // to load dialog item view dynamically according to what is needed in this frag
    private String username;
    private Frag_List_My_Trips this_fragment;
    private EditText trip_title_or_username_input;
    private Button execute_trip_or_username_search_button;
    private Drawable frameDrawable;

    /**
     * load shared view model and get username from shared view model, load layout, initialize DAO_helper variable
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // load shared view model to get username
        model = new ViewModelProvider(requireActivity()).get(Shared_View_Model.class);

        dao = new DAO_helper();

        // load login fragment layout
        view = inflater.inflate(R.layout.frag_list_my_trips_layout, container, false);
        fcl = (Main_Activity) inflater.getContext(); // to change fragments

        this_fragment = this;

        // load toolbar of this fragment
        Toolbar toolbar = view.findViewById(R.id.my_trips_list_toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);

        return view;
    }

    /**
     * call function to get list of trips from database, prepare list of trips, dialogs of loading, hints, trip options and search function
     * @param view
     * @param savedInstanceState
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // prepare loading animation
        loading_animation_dialog = new Dialog(requireActivity());
        loading_animation_dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        loading_animation_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loading_animation_dialog.setCanceledOnTouchOutside(false);
        loading_animation_dialog.setContentView(R.layout.loading_animation_layout);
        loading_animation_dialog.show();

        // prepare dialog with layout hints
        interface_hints_dialog = new Dialog(requireActivity());
        interface_hints_dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        interface_hints_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        interface_hints_dialog.setCanceledOnTouchOutside(true);
        interface_hints_dialog.setContentView(R.layout.dialog_show_hints_layout);

        // prepare search dialog
        search_dialog = new Dialog(requireActivity());
        search_dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        search_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        search_dialog.setCanceledOnTouchOutside(true);
        search_dialog.setContentView(R.layout.dialog_search_trips_or_users_layout);

        // Inflate the recycler view of search function and add it to the layout
        searchResultsStub = search_dialog.findViewById(R.id.search_results_stub);
        searchResultsStub.setLayoutResource(R.layout.search_trips_results_layout);
        search_trips_results_recycler_view = (RecyclerView) searchResultsStub.inflate();

        // prepare recycler view for results list inside search dialog
        adapter_for_listing_search_results = new Adapter_For_Listing_Search_Results(requireActivity(), null, this, search_results);
        search_trips_results_recycler_view.setAdapter(adapter_for_listing_search_results);
        search_trips_results_recycler_view.setLayoutManager(new LinearLayoutManager(requireActivity()));

        // for frame around result list - just a nice touch
        frameDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.recycler_view_frame);

        // dialog title
        TextView search_trips_or_users_title = search_dialog.findViewById(R.id.search_trips_or_users_title);
        search_trips_or_users_title.setText("Search Trips");

        // title input
        trip_title_or_username_input = search_dialog.findViewById(R.id.trip_title_or_username_input);
        trip_title_or_username_input.setHint("Insert trip title");

        // search button
        execute_trip_or_username_search_button = search_dialog.findViewById(R.id.execute_trip_or_username_search_button);
        execute_trip_or_username_search_button.setOnClickListener(v -> {

            if(!trip_title_or_username_input.getText().toString().equals("")){

                search_results.clear();
                search_trips_results_recycler_view.setForeground(null); // to clear the frame

                // dismiss keyboard
                trip_title_or_username_input.onEditorAction(EditorInfo.IME_ACTION_DONE);

                loading_animation_dialog.show();

                showSearchResults();
            }
            else {

                search_results.clear();
                search_trips_results_recycler_view.setForeground(null); // to clear the frame

                Toast.makeText(requireActivity(), "Insert a trip title!",Toast.LENGTH_SHORT).show();
            }
        });

        // get username to fetch list of trips from firebase database
        model.getData().observe(getViewLifecycleOwner(), item -> {
            username = (String) item;

            // get list of trips
            dao.getUserTrips(username, this);

            my_trips_recycler_view = view.findViewById(R.id.recyclerView_my_trips_list);
        });
    }

    /**
     * show search results of trips or usernames
     */
    private void showSearchResults() {

        String inserted_string = trip_title_or_username_input.getText().toString();

        String parsed_trip_title = "";

        // search list of trips to find possible matches or similar stings between the inserted string and the trip titles inside the title list
        for(String trip_title : my_trips_list){

            parsed_trip_title = trip_title.split("title=")[1].split(",")[0];

            // if search is an exact match clear list and return single result
            if(parsed_trip_title.equalsIgnoreCase(inserted_string)){
                search_results.clear();
                search_results.add(parsed_trip_title);
                break;
            }

            // if the trip title contains one of the words inserted
            else if(parsed_trip_title.toUpperCase(Locale.ROOT).contains(inserted_string.toUpperCase(Locale.ROOT))){
                search_results.add(parsed_trip_title);
            }
        }

        if(search_results.size() > 0){

            Toast.makeText(requireActivity(), "Found " + search_results.size() + " results!",Toast.LENGTH_SHORT).show();

            // show frame after list is ready just a nice touch
            search_trips_results_recycler_view.setForeground(frameDrawable);

            // update recycler view of list of results
            adapter_for_listing_search_results.setResultsList(search_results);
            search_trips_results_recycler_view.setAdapter(adapter_for_listing_search_results);

            // clear input field
            trip_title_or_username_input.setText("");
        }
        else {
            Toast.makeText(requireActivity(), "No results found!",Toast.LENGTH_SHORT).show();
        }

        loading_animation_dialog.dismiss();
    }

    /**
     * when a trip result is selected dismiss search dialog
     * in this fragment this is only necessary to dismiss the search_dialog and because the same adapter used in the trip planner fragment is being reused and the interface method has to be used
     * @param position
     */
    @Override
    public void onSelectResult(int position) {
        search_dialog.dismiss();
    }

    /**
     * app bar menu set up
     * @param menu
     * @param inflater
     */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.list_my_trips_actions, menu);

        // change app bar title
        ActionBar ab = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        assert ab != null;
        ab.setTitle("My Trips");

        super.onCreateOptionsMenu(menu,inflater);
    }

    /**
     * app bar menu actions
     * search trip and help actions
     * help shoe how to delete a trip
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.search_trip){
            if(my_trips_list.size() > 0){
                search_dialog.show();
            }
        }
        else if(item.getItemId() == R.id.interface_hints){
            TextView hint = interface_hints_dialog.findViewById(R.id.hint_text);
            hint.setText("\nHint\n\nSwipe left to delete a trip.\n\nSelect a trip for more options.\n");
            interface_hints_dialog.show();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * get user's trip list from firebase database
     */
    public void getMyTrips(ArrayList<String> my_trips) {

        if(my_trips.size() > 0){
            my_trips_list.addAll(my_trips);
        }
        else {
            my_trips_list.add("You don't have trips created!");
        }

        // set up list of locations
        adapter_for_listing_trips = new Adapter_For_Listing_Trips(requireActivity(), this, my_trips_list);
        my_trips_recycler_view.setAdapter(adapter_for_listing_trips);
        my_trips_recycler_view.setLayoutManager(new LinearLayoutManager(requireActivity()));

        // attach callback for drag and drop to recycler view to reorder and delete locations from trip
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(my_trips_recycler_view);

        loading_animation_dialog.dismiss();
    }

    /**
     * instantiate callback class to execute swipe left to delete a trip
     */
    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) { return false;}

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            if(!my_trips_list.get(0).equals("You don't have trips created!")){

                loading_animation_dialog.show();
                dao.addOrUpdateOrDeleteTrip(username, null, this_fragment, null, "delete", viewHolder.getAdapterPosition());

                my_trips_list.remove(viewHolder.getAdapterPosition());
                adapter_for_listing_trips.setMy_trips(my_trips_list);
                my_trips_recycler_view.setAdapter(adapter_for_listing_trips);
            }
        }
    };

    /**
     * when a trip is selected (clicked on) show dialog with options:
     * view trip route
     * invite friends
     * edit trip data
     * start location of elections
     * @param trip_position_in_list
     */
    @Override
    public void onTripClick(int trip_position_in_list) {

        // if a search was executed first
        if(search_results.size() > 0) {

            // get original position of trip in the list of trips to avoid using the position of the result list
            for(int i = 0; i < my_trips_list.size(); i++){

                if(my_trips_list.get(i).contains(search_results.get(trip_position_in_list))){
                    trip_position_in_list = i;
                    break;
                }
            }

            search_results.clear();
        }

        int finalTrip_position_in_list = trip_position_in_list;

        Dialog show_trip_options_dialog = new Dialog(requireActivity());
        show_trip_options_dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); // make dialog window background transparent

        //Disable the default title
        show_trip_options_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        show_trip_options_dialog.setCanceledOnTouchOutside(true);

        show_trip_options_dialog.setContentView(R.layout.dialog_trip_options_layout);

        // set dialog title to trip title
        TextView trip_options_title = show_trip_options_dialog.findViewById(R.id.trip_options_dialog_title);
        trip_options_title.setText(my_trips_list.get(finalTrip_position_in_list).split("title=")[1].split(",")[0]);


        ImageButton view_trip_route = show_trip_options_dialog.findViewById(R.id.view_trip_route_image_button);
        view_trip_route.setOnClickListener(v -> {

            // pass list of locations to next fragment
            Bundle bundle = new Bundle();
            bundle.putString("locations", my_trips_list.get(finalTrip_position_in_list));

            show_trip_options_dialog.dismiss();

            // hide list trips layout while next create trip fragment loads to prevent user from clicking in other buttons while it loads
            view.setVisibility(View.GONE);

            my_trips_list.clear(); // clear list to avoid getting duplicates when the user return to this fragment

            Frag_Show_Trip_Route frag_show_trip_route = new Frag_Show_Trip_Route();
            frag_show_trip_route.setArguments(bundle);
            fcl.replaceFragment(frag_show_trip_route, "yes");
        });


        ImageButton edit_trip_button = show_trip_options_dialog.findViewById(R.id.edit_trip_image_button);
        edit_trip_button.setOnClickListener(v -> {

            // pass list of locations to next fragment
            Bundle bundle = new Bundle();
            bundle.putString("locations", my_trips_list.get(finalTrip_position_in_list));
            bundle.putString("trip_to_edit", String.valueOf(finalTrip_position_in_list));

            show_trip_options_dialog.dismiss();

            // hide list trips layout while next create trip fragment loads to prevent user from clicking in other buttons while it loads
            view.setVisibility(View.GONE);

            my_trips_list.clear(); // clear list to avoid getting the old trips and the updated trips when the user returns from the create trip fragment

            Frag_Trip_Planner frag_trip_planner = new Frag_Trip_Planner();
            frag_trip_planner.setArguments(bundle);
            fcl.replaceFragment(frag_trip_planner, "yes");
        });


        ImageButton weather_forecast = show_trip_options_dialog.findViewById(R.id.view_weather_image_button);
        weather_forecast.setOnClickListener(v -> {

            // pass list of locations to next fragment
            Bundle bundle = new Bundle();
            bundle.putString("locations", my_trips_list.get(finalTrip_position_in_list));

            show_trip_options_dialog.dismiss();

            // hide list trips layout while next create trip fragment loads to prevent user from clicking in other buttons while it loads
            view.setVisibility(View.GONE);

            my_trips_list.clear(); // clear list to avoid getting duplicates when the user return to this fragment

            Frag_Weather_Forecast frag_show_weather_forecast = new Frag_Weather_Forecast();
            frag_show_weather_forecast.setArguments(bundle);
            fcl.replaceFragment(frag_show_weather_forecast, "yes");
        });

        show_trip_options_dialog.show();
    }

    /**
     * show result message after deleating trip database
     * cancel loading animation and return to home screen
     */
    public void getDAOResultMessage(Task<Void> delete_trip) {

        // cancel loading animation and go back to home screen
        loading_animation_dialog.dismiss();

        delete_trip.addOnSuccessListener(suc -> {
            if(my_trips_list.size() == 0){
                my_trips_list.add("You don't have trips created!");
            }

            // update recycler vie of list of trips
            adapter_for_listing_trips.setMy_trips(my_trips_list);
            my_trips_recycler_view.setAdapter(adapter_for_listing_trips);

            Toast.makeText(requireActivity(), "Trip deleted successfully!",Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(er -> Toast.makeText(requireActivity(), "Error while deleting trip in Database!\nTry again later.",Toast.LENGTH_SHORT).show());
    }
}
