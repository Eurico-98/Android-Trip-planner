package com.example.projecto_cm.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projecto_cm.Fragments.Frag_List_My_Trips;
import com.example.projecto_cm.Interfaces.Interface_Card_My_Trip_In_Trip_List;
import com.example.projecto_cm.R;

import java.util.ArrayList;

public class Adapter_For_Listing_Trips_and_Friends extends RecyclerView.Adapter<Adapter_For_Listing_Trips_and_Friends.MyViewHolder> {

    private Context context;
    private ArrayList my_list;
    Interface_Card_My_Trip_In_Trip_List listener;
    private String type_of_list;

    // constructor
    public Adapter_For_Listing_Trips_and_Friends(Context context, Frag_List_My_Trips listener, ArrayList my_list, String type_of_list){
        this.context = context;
        this.my_list = my_list;
        this.listener = listener;
        this.type_of_list = type_of_list;
    }

    public void setMy_list(ArrayList my_list) { this.my_list = my_list; }

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
        if(type_of_list.equals("my_trips")){
            System.out.println("----------------------------------------------------- 11");
            view = inflater.inflate(R.layout.card_trip_button_layout, parent, false);
        }
        else{
            System.out.println("----------------------------------------------------- 12");
            view = inflater.inflate(R.layout.card_friend_layout, parent, false);
        }

        return new MyViewHolder(view);
    }

    /**
     * obtain trip title from trip data string -
     * string has:
     * start and end date,
     * title,
     * list of locations where each location has latitude and longitude
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {

        if(type_of_list.equals("my_trips")) {
            try {
                System.out.println("----------------------------------------------------- 21");
                holder.trip_button.setText(String.valueOf(my_list.get(position)).split("title=")[1].split(",")[0]);
                holder.trip_button.setOnClickListener(v -> listener.onTripClick(position));
            } catch (Exception e) {
                holder.trip_button.setText(String.valueOf(my_list.get(position))); // in case the user has no trips the split function while not work
            }
        }
        else{
            System.out.println("----------------------------------------------------- 11" + my_list.get(position));
            holder.friend.setText(String.valueOf(my_list.get(position)));
        }
    }

    @Override
    public int getItemCount() {
        return my_list.size();
    }

    // to bind views with layout objects
    public static class MyViewHolder extends RecyclerView.ViewHolder {

        Button trip_button;
        TextView friend;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            friend = itemView.findViewById(R.id.friend_title); // bind title
            trip_button = itemView.findViewById(R.id.trip_title_button); // bind title
        }
    }
}

