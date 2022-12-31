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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projecto_cm.Adapters.Adapter_For_Listing_Trip_Locations;
import com.example.projecto_cm.Adapters.Adapter_For_Listing_Weather_Data;
import com.example.projecto_cm.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Frag_Weather_Forecast extends Fragment {

    private ExecutorService service;
    private Handler handler;
    private Dialog loading_animation_dialog, interface_hints_dialog;
    private RecyclerView list_of_locations_recycler_view;
    private ArrayList<String> locations = new ArrayList<>();
    private Adapter_For_Listing_Trip_Locations adapter_for_listing_trip_locations;
    private ArrayList<Adapter_For_Listing_Weather_Data> list_of_weather_adapters_of_locations = new ArrayList<>();

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
       View view = inflater.inflate(R.layout.frag_weather_forecast_layout, container, false);

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

        if(item.getItemId() == R.id.interface_hints){
            TextView hint = interface_hints_dialog.findViewById(R.id.hint_text);
            hint.setText("\nInfo\n\nThe weather forecast given is for a 3 hour window starting from the current time.\n");
            interface_hints_dialog.show();
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

        // prepare dialog with layout hints
        interface_hints_dialog = new Dialog(requireActivity());
        interface_hints_dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        interface_hints_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        interface_hints_dialog.setCanceledOnTouchOutside(true);
        interface_hints_dialog.setContentView(R.layout.dialog_show_hints_layout);

        service.execute(() -> {

            String result = "";
            try {
                // get weather forecast and prepare adapters
                locations.addAll(Arrays.asList(getArguments().getString("locations").split("locations=\\[")[1].split("]")[0].split(", ")));
                result = getWeatherForecast();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            String finalResult = result;
            handler.post(() -> {

                // set list adapter of locations list
                if(finalResult.equals("Forecast successful!")){
                    list_of_locations_recycler_view = requireActivity().findViewById(R.id.recyclerView_locations_weather_list); // to show list of locations selected
                    adapter_for_listing_trip_locations = new Adapter_For_Listing_Trip_Locations(requireActivity(), locations, list_of_weather_adapters_of_locations, "list of weather forecasts");
                    list_of_locations_recycler_view.setAdapter(adapter_for_listing_trip_locations);
                    list_of_locations_recycler_view.setLayoutManager(new LinearLayoutManager(requireActivity()));
                }
                else {
                    Toast.makeText(requireActivity(), finalResult,Toast.LENGTH_SHORT).show();
                }

                loading_animation_dialog.dismiss();
            });
        });
    }

    /**
     * send weather forecast request of up to 5 days for each location of the trip, process all that data creating all the adapters necessary - one adapter for the list of locations, and one adapter for the weather data set of each location
     */
    private String getWeatherForecast() throws IOException {

        // request weather forecast for next 5 days for each location of the trip
        URL url;
        HttpURLConnection connection;
        BufferedReader in;
        StringBuilder response;
        String line;
        DecimalFormat df = new DecimalFormat("#");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM");

        for(String location_data : locations){

            try {
                url = new URL("https://api.openweathermap.org/data/2.5/forecast?" +
                        "lat=" + location_data.split("_#_")[1] +
                        "&lon=" + location_data.split("_#_")[2] +
                        "&cnt=" + 40 + // 40 intervals of 3 hours equals to 5 days of weather forecast
                        "&units=metric" +
                        "&appid=" + getString(R.string.OpenWeather_API));
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                response = new StringBuilder();
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();
                connection.disconnect();

                // convert response to json
                JSONObject json = new JSONObject(response.toString());

                // Get list of weather forecasts
                JSONArray forecasts = json.getJSONArray("list");

                // list of weather data for next 5 days for current location
                ArrayList<String> current_location_weather_data = new ArrayList<>();

                // get data for the 5 days
                for (int i = 0; i < forecasts.length(); i+=8) {

                    // Get weather forecast for current day
                    JSONObject day = forecasts.getJSONObject(i);

                    // get the date/time of the forecast
                    Date date = new Date(day.getLong("dt") * 1000L);

                    // rain might be null
                    JSONObject rain = day.optJSONObject("rain");
                    double probabilityOfRain = rain != null ? rain.optDouble("3h") : 0;

                    // Get temperature, humidity, and probability of rain from weather forecast
                    String data = dateFormat.format(date) + "|" +
                            day.getJSONArray("weather").getJSONObject(0).getString("description") + "|" +
                            df.format(day.getJSONObject("main").getDouble("temp")) + "ºC|" +
                            "Feels like: " + df.format(day.getJSONObject("main").getDouble("feels_like")) + "ºC|" +
                            "Humidity: " + df.format(day.getJSONObject("main").getDouble("humidity")) + "%|" +
                            "Chance of rain: " + df.format(probabilityOfRain) + "%";

                    current_location_weather_data.add(data);
                }

                // set adapter for weather data for the 5 days for the current location
                list_of_weather_adapters_of_locations.add(new Adapter_For_Listing_Weather_Data(requireActivity(), current_location_weather_data));
            }
            catch (Exception e) {
                e.printStackTrace();
                return "Network error while getting weather forecast!";
            }
        }

        return "Forecast successful!";
    }
}