package com.example.projecto_cm.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projecto_cm.R;

import java.util.ArrayList;

public class Adapter_For_Listing_Weather_Data extends RecyclerView.Adapter<Adapter_For_Listing_Weather_Data.MyViewHolder> {

    private Context context;
    private ArrayList<String> weather_data;

    public Adapter_For_Listing_Weather_Data(Context context, ArrayList<String> weather_data){
        this.context = context;
        this.weather_data = weather_data;
    }

    // inflate layout for the weather fore cast of a day inside the card layout of a location
    @NonNull
    @Override
    public Adapter_For_Listing_Weather_Data.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.card_weather_per_day_layout, parent, false);

        return new Adapter_For_Listing_Weather_Data.MyViewHolder(view);
    }

    // to send the note id from the first fragment to the second fragment
    @Override
    public void onBindViewHolder(@NonNull Adapter_For_Listing_Weather_Data.MyViewHolder holder, @SuppressLint("RecyclerView") int position) {


        holder.weather_forecast_date.setText(weather_data.get(0));
        holder.weather_description.setText(weather_data.get(1));
        holder.temperature.setText(weather_data.get(2));
        holder.feels_like_temperature.setText(weather_data.get(3));
        holder.humidity.setText(weather_data.get(4));
        holder.rain_prob.setText(weather_data.get(5));

        //holder.weather_forecast_image.setText(String.valueOf(locations.get(position)).split("_#_")[0]);
    }

    @Override
    public int getItemCount() { return weather_data.size(); }

    // to bind views with layout objects
    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView weather_forecast_date, weather_description, temperature, feels_like_temperature, humidity, rain_prob;
        ImageView weather_forecast_image;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            weather_forecast_date = itemView.findViewById(R.id.weather_forecast_date);
            weather_description = itemView.findViewById(R.id.weather_description);
            temperature = itemView.findViewById(R.id.temperature);
            feels_like_temperature = itemView.findViewById(R.id.feels_like_temperature);
            humidity = itemView.findViewById(R.id.humidity);
            rain_prob = itemView.findViewById(R.id.rain_prob);
            weather_forecast_image = itemView.findViewById(R.id.weather_forecast_image);
        }
    }
}