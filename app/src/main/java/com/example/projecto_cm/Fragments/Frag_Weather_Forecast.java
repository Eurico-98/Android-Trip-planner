package com.example.projecto_cm.Fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projecto_cm.Adapters.Adapter_For_Listing_Trip_Locations;
import com.example.projecto_cm.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Frag_Weather_Forecast extends Fragment {

    private ExecutorService service;
    private Handler handler;
    private RecyclerView list_of_locations_recycler_view;
    private ArrayList<RecyclerView> locations_weather_recycler_view = new ArrayList<>(); // this list contains the forecast recycler views for each location of the trip
    private ArrayList<String> weather_forecast_per_location = new ArrayList<>(); // keys are locations values are the json result from the api request
    private View view;
    private Dialog loading_animation_dialog;
    private Adapter_For_Listing_Trip_Locations adapter_for_listing_trip_locations;

    /**
     * load shared view model and get username from shared view model, load layout
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
   @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

       // load login fragment layout
       view = inflater.inflate(R.layout.frag_weather_forecast_layout, container, false);

       // create 1 thread to execute requests to openweather api
       service = Executors.newFixedThreadPool(1);
       handler = new Handler(Looper.getMainLooper());

       // load toolbar of this fragment
       Toolbar toolbar = view.findViewById(R.id.locations_weather_list_toolbar);
       ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
       setHasOptionsMenu(true);

       return view;
    }


    /**
     * app bar menu set up
     * @param menu
     * @param inflater
     */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.weather_forecast_actions, menu);

        // change app bar title
        ActionBar ab = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        assert ab != null;
        ab.setTitle("Weather Forecast");

        super.onCreateOptionsMenu(menu,inflater);
    }


    /**
     * app bar menu actions - humidity and temperature options
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.humidity){

        }
        else if(item.getItemId() == R.id.temperature){

        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * prepare recycler views for list of locations and weather forecast recycler views for each location
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

        service.execute(() -> {

            try {
                get_weather_forecast();

                /*
                // set list adapter
                list_of_locations_recycler_view = requireActivity().findViewById(R.id.recyclerView_locations_weather_list); // to show list of locations selected
                adapter_for_listing_trip_locations = new Adapter_For_Listing_Trip_Locations(requireActivity(), weather_forecast_per_location, "list of weather forecasts");
                list_of_locations_recycler_view.setAdapter(adapter_for_listing_trip_locations);
                list_of_locations_recycler_view.setLayoutManager(new LinearLayoutManager(requireActivity()));*/
            } catch (IOException e) {
                e.printStackTrace();
            }

            handler.post(() -> {
                loading_animation_dialog.dismiss();
            });
        });
    }

    /**
     * get locations from bundle, send request to openweather api to get weather forecast for next 5 days starting from the present day if present day is within trip period
     */
    private void get_weather_forecast() throws IOException {

        // send request with the location forecast
        URL url;
        HttpURLConnection connection;
        BufferedReader in;
        StringBuilder response;
        String line;
        DecimalFormat df = new DecimalFormat("#.##");
        for(String location_data : getArguments().getString("locations").split("locations=\\[")[1].split("]")[0].split(", ")){

            try {
                url = new URL("https://api.openweathermap.org/data/2.5/forecast?lat=" + location_data.split("_#_")[1] + "&lon=" + location_data.split("_#_")[2] + "&appid=" + getString(R.string.OpenWeather_API));
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                response = new StringBuilder();
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();
                connection.disconnect();


                JSONObject json = new JSONObject(response.toString());

                // Get list of weather forecasts
                JSONArray forecasts = json.getJSONArray("list");

                // Iterate through list of weather forecasts for 5 days
                for (int i = 0; i < 5; i++) {
                    // Get weather forecast for current day
                    JSONObject forecast = forecasts.getJSONObject(i);

                    ArrayList<String> location_weather_data = new ArrayList<>();


                    // Get temperature, humidity, and probability of rain from weather forecast
                    location_weather_data.add("Weather: " + forecast.getJSONArray("weather").getJSONObject(0).getString("description"));
                    location_weather_data.add("Temperature: " +df.format(forecast.getJSONObject("main").getDouble("temp") - 273.15)); // value comes in kelvin
                    location_weather_data.add(df.format(forecast.getJSONObject("main").getDouble("feels_like") - 273.15));
                    location_weather_data.add(df.format(forecast.getJSONObject("main").getDouble("humidity")));
                    location_weather_data.add(df.format(forecast.getJSONObject("rain").optDouble("3h", 0)));


                    /*System.out.println("Weather: " + description);
                    System.out.println("Temperature: " + df.format(temperature) + "Cº");
                    System.out.println("Feels like: " + df.format(feelsLike) + "Cº");
                    System.out.println("Humidity: " + humidity + "%");
                    System.out.println("Chance of rain:\\n(within 3h) " + rainProbability + "%");


                    // set adapters for weather data of each day
                    Adapter_For_Weather_data_per_day adapter_for_weather_data_per_day = new Adapter_For_Weather_data_per_day(requireActivity(), );*/
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
