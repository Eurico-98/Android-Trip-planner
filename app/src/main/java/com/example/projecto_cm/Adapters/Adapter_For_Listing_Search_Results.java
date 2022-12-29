package com.example.projecto_cm.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projecto_cm.Fragments.Frag_List_My_Trips;
import com.example.projecto_cm.Fragments.Frag_Trip_Planner;
import com.example.projecto_cm.Interfaces.Interface_Card_My_Trip_In_Trip_List;
import com.example.projecto_cm.Interfaces.Interface_Card_Search_Result_In_Create_Trip;
import com.example.projecto_cm.R;

import java.util.ArrayList;

public class Adapter_For_Listing_Search_Results extends RecyclerView.Adapter<Adapter_For_Listing_Search_Results.MyViewHolder> {

    private Context context;
    private ArrayList results;
    Interface_Card_Search_Result_In_Create_Trip listener_on_result_click;
    Interface_Card_My_Trip_In_Trip_List listener_trip_list_fg = null;

    // constructor
    public Adapter_For_Listing_Search_Results(Context context, Frag_Trip_Planner listener_fg_planner, Frag_List_My_Trips listener_fg_list, ArrayList results){
        this.context = context;
        this.results = results;

        if(listener_fg_list == null){
            this.listener_on_result_click = listener_fg_planner;
        }
        else {
            this.listener_on_result_click = listener_fg_list;
            this.listener_trip_list_fg = listener_fg_list;
        }
    }

    public void setResultsList(ArrayList results) {
        this.results = results;
    }

    // inflate layout for the title notes inside recycler view
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.card_search_result_layout, parent, false);
        return new MyViewHolder(view);
    }

    // to send the note id from the first fragment to the second fragment
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {

        holder.result.setText(String.valueOf(results.get(position)));
        holder.result.setOnClickListener(v -> {
            listener_on_result_click.onSelectResult(position); // dismiss search dialog

            // only set this listener if user is in frag tro list trips
            if(listener_trip_list_fg != null) {
                listener_trip_list_fg.onTripClick(position); // show trip options
            }
        });
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    // to bind views with layout objects
    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView result;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            result = itemView.findViewById(R.id.result_title); // bind title
        }
    }
}
