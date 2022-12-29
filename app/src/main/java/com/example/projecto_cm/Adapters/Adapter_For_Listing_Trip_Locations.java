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
    private String type_of_list;
    private FragmentActivity requireActivity;

    // constructor
    public Adapter_For_Listing_Trip_Locations(Context context, ArrayList locations, String type_of_list, FragmentActivity requireActivity){
        this.context = context;
        this.locations = locations;
        this.type_of_list = type_of_list;
        this.requireActivity = requireActivity;
    }

    public void setLocationsList(ArrayList locations) {
        this.locations = locations;
    }

    // inflate layout for the title notes inside recycler view or weather forecast
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

    // to send the note id from the first fragment to the second fragment
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {

        // set location title
        if(Objects.equals(type_of_list, "list of locations")) {
            holder.location_title.setText(String.valueOf(locations.get(position)).split("_#_")[0]);
        }

        // set location list of weather forecast for the maximum number of days possible
        else {
            holder.location_title_weather_forecast.setText(String.valueOf(locations.get(position)).split("_#_")[0]);
            //holder.recyclerView_weather_forecast.setAdapter(aqui passo o adpater que tera de vir numa lista de adapters do fragmento);
            holder.recyclerView_weather_forecast.setLayoutManager(new LinearLayoutManager(requireActivity));
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
