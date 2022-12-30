package com.example.projecto_cm.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projecto_cm.R;

import java.util.ArrayList;
import java.util.Objects;

public class Adapter_For_Listing_Trip_Locations extends RecyclerView.Adapter<Adapter_For_Listing_Trip_Locations.MyViewHolder> {

    private Context context;
    private ArrayList locations;
    private ArrayList<Adapter_For_Listing_Weather_Data> list_of_weather_adapters_of_locations;
    private String type_of_list;

    // constructor
    public Adapter_For_Listing_Trip_Locations(Context context, ArrayList locations, ArrayList<Adapter_For_Listing_Weather_Data> list_of_weather_adapters_of_locations, String type_of_list){
        this.context = context;
        this.locations = locations;
        this.type_of_list = type_of_list;
        this.list_of_weather_adapters_of_locations = list_of_weather_adapters_of_locations;
    }

    public void setLocationsList(ArrayList locations) {
        this.locations = locations;
    }

    /**
     * inflate correct card layout
     * @param parent
     * @param viewType
     * @return
     */
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);

        View view;

        // show this card view when list of locations selected in Trip planner fragment is being displayed
        if(Objects.equals(type_of_list, "list of locations")){
            view = inflater.inflate(R.layout.card_location_layout, parent, false);
        }

        // show this card when list of locations for the weather fragment is being displayed
        else {
            view = inflater.inflate(R.layout.card_location_weather_layout, parent, false);
        }

        return new MyViewHolder(view);
    }

    /**
     * show list of locations if user creating or editing a trip in trip planner fragment,
     * show list of forecasts for each location of the trip if user is in weather forecast fragment
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {

        // set location title
        if(Objects.equals(type_of_list, "list of locations")) {
            holder.location_title.setText(String.valueOf(locations.get(position)).split("_#_")[0]);
        }

        // set location title and list of weather data for 5 days
        else {
            holder.location_title_weather_forecast.setText(String.valueOf(locations.get(position)).split("_#_")[0]);
            holder.recyclerView_weather_forecast.setAdapter(list_of_weather_adapters_of_locations.get(position));
            holder.recyclerView_weather_forecast.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        }
    }

    @Override
    public int getItemCount() { return locations.size(); }

    // to bind views with layout objects
    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView location_title, location_title_weather_forecast;
        RecyclerView recyclerView_weather_forecast;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            location_title = itemView.findViewById(R.id.location_title);
            location_title_weather_forecast = itemView.findViewById(R.id.location_title_weather_forecast);
            recyclerView_weather_forecast = itemView.findViewById(R.id.recyclerView_weather_forecast);
        }
    }
}
