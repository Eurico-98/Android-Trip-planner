package com.example.projecto_cm.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projecto_cm.R;

import java.util.ArrayList;
import java.util.Locale;

public class Adapter_For_Listing_Weather_Data extends RecyclerView.Adapter<Adapter_For_Listing_Weather_Data.MyViewHolder> {

    private Context context;
    private ArrayList<String> weather_data;

    public Adapter_For_Listing_Weather_Data(Context context, ArrayList<String> weather_data){
        this.context = context;
        this.weather_data = weather_data;
    }

    /**
     * inflate correct card layout
     * @param parent
     * @param viewType
     * @return
     */
    @NonNull
    @Override
    public Adapter_For_Listing_Weather_Data.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.card_weather_per_day_layout, parent, false);

        return new Adapter_For_Listing_Weather_Data.MyViewHolder(view);
    }

    /**
     * put weather data in text views of the card
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull Adapter_For_Listing_Weather_Data.MyViewHolder holder, @SuppressLint("RecyclerView") int position) {

        holder.weather_forecast_date.setText(weather_data.get(position).split("\\|")[0]);
        holder.weather_description.setText(weather_data.get(position).split("\\|")[1]);

        // set icon image according to description
        if(holder.weather_description.getText().toString().toLowerCase(Locale.ROOT).equals("clear sky")){
            holder.weather_forecast_image.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.clear_sky));
        }
        else if(holder.weather_description.getText().toString().toLowerCase(Locale.ROOT).equals("overcast clouds")){
            holder.weather_forecast_image.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.heavy_clouds));
        }
        else if(holder.weather_description.getText().toString().toLowerCase(Locale.ROOT).contains("clouds")){
            holder.weather_forecast_image.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.light_clouds));
        }
        else if(holder.weather_description.getText().toString().toLowerCase(Locale.ROOT).contains("drizzle")){
            holder.weather_forecast_image.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.drizzle));
        }
        else if(holder.weather_description.getText().toString().toLowerCase(Locale.ROOT).equals("light snow")){
            holder.weather_forecast_image.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.light_snow));
        }
        else if(holder.weather_description.getText().toString().toLowerCase(Locale.ROOT).contains("snow")){
            holder.weather_forecast_image.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.heavy_snow));
        }
        else if(holder.weather_description.getText().toString().toLowerCase(Locale.ROOT).equals("light rain")){
            holder.weather_forecast_image.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.light_rain));
        }
        else if(holder.weather_description.getText().toString().toLowerCase(Locale.ROOT).contains("rain")){
            holder.weather_forecast_image.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.heavy_rain));
        }
        else if(holder.weather_description.getText().toString().toLowerCase(Locale.ROOT).contains("thunderstorm")){
            holder.weather_forecast_image.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.thunderstorm));
        }
        else{
            holder.weather_forecast_image.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.weather_forecast));
        }

        holder.temperature.setText(weather_data.get(position).split("\\|")[2]);

        // set temp color according to temp
        int temp = Integer.parseInt(weather_data.get(position).split("\\|")[2].split("ºC")[0]);
        if(temp < 0){
            holder.temperature.setTextColor(Color.parseColor("#FFFFFFFF"));
        }
        else if(temp < 10){
            holder.temperature.setTextColor(Color.parseColor("#1650ff"));
        }
        else if(temp < 15){
            holder.temperature.setTextColor(Color.parseColor("#16cfff"));
        }
        else if(temp < 20){
            holder.temperature.setTextColor(Color.parseColor("#16ffe2"));
        }
        else if(temp < 27){
            holder.temperature.setTextColor(Color.parseColor("#16ff63"));
        }
        else if(temp < 30){
            holder.temperature.setTextColor(Color.parseColor("#fcff16"));
        }
        else if(temp < 33){
            holder.temperature.setTextColor(Color.parseColor("#FFA000"));
        }
        else {
            holder.temperature.setTextColor(Color.parseColor("#FF1616"));
        }

        holder.feels_like_temperature.setText(weather_data.get(position).split("\\|")[3]);
        holder.humidity.setText(weather_data.get(position).split("\\|")[4]);
        holder.rain_prob.setText(weather_data.get(position).split("\\|")[5]);
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