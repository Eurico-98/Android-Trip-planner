package com.example.projecto_cm.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projecto_cm.Fragments.Frag_Create_Trip;
import com.example.projecto_cm.Interfaces.Interface_Card_Location;
import com.example.projecto_cm.R;

import java.util.ArrayList;

public class Adapter_Trip_Locations_List extends RecyclerView.Adapter<Adapter_Trip_Locations_List.MyViewHolder> {

    private Context context;
    private ArrayList locations;
    Interface_Card_Location listener;

    // constructor
    public Adapter_Trip_Locations_List(Context context, Frag_Create_Trip listener, ArrayList locations){
        this.context = context;
        this.locations = locations;
        this.listener = listener;
    }

    public void setLocationsList(ArrayList locations) {
        this.locations = locations;
    }

    // inflate layout for the title notes inside recycler view
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.card_location_layout, parent, false);
        return new MyViewHolder(view);
    }

    // to send the note id from the first fragment to the second fragment
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {

        holder.location_title.setText(String.valueOf(locations.get(position)).split("_#_")[0]);
        holder.delete_button.setOnClickListener(view -> listener.onDeleteClick(position));
    }

    @Override
    public int getItemCount() {
        return locations.size();
    }

    // to bind views with layout objects
    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView location_title;
        ConstraintLayout mainLayout;
        ImageView delete_button;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            delete_button = itemView.findViewById(R.id.delete_location);
            location_title = itemView.findViewById(R.id.location_title); // bind title
            mainLayout = itemView.findViewById(R.id.cardLocationLayout); // bind title container
        }
    }
}
