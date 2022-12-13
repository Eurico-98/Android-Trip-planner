package com.example.projecto_cm.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projecto_cm.Fragments.Frag_List_My_Trips;
import com.example.projecto_cm.Interfaces.Interface_Card_My_Trip;
import com.example.projecto_cm.R;

import java.util.ArrayList;

public class Adapter_List_My_Trips extends RecyclerView.Adapter<Adapter_List_My_Trips.MyViewHolder> {

    private Context context;
    private ArrayList my_trips;
    Interface_Card_My_Trip listener;

    // constructor
    public Adapter_List_My_Trips(Context context, Frag_List_My_Trips listener, ArrayList my_trips){
        this.context = context;
        this.my_trips = my_trips;
        this.listener = listener;
    }

    // inflate layout for the title notes inside recycler view
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.element_trip_button_layout, parent, false);
        return new MyViewHolder(view);
    }

    // to send the note id from the first fragment to the second fragment
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {

        // obtain trip title from trip data string
        // string has:
        // star and end date
        // title
        // list of locations where each location has latitude and longitude
        try {
            holder.trip_button.setText(String.valueOf(my_trips.get(position)).split("title=")[1].split(",")[0]);
            holder.trip_button.setOnClickListener(v -> listener.onTripClick(position));
        } catch (Exception e){
            holder.trip_button.setText(String.valueOf(my_trips.get(position))); // in case the user has no trips the split function while not work
        }
    }

    @Override
    public int getItemCount() {
        return my_trips.size();
    }

    // to bind views with layout objects
    public static class MyViewHolder extends RecyclerView.ViewHolder {

        Button trip_button;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            trip_button = itemView.findViewById(R.id.trip_title_button); // bind title
        }
    }
}

