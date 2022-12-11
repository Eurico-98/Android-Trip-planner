package com.example.projecto_cm.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projecto_cm.Fragments.Frag_Create_Trip;
import com.example.projecto_cm.Interfaces.Card_Search_result_interface;
import com.example.projecto_cm.Interfaces.Card_location_interface;
import com.example.projecto_cm.R;

import java.util.ArrayList;

public class Adapter_Search_results_list extends RecyclerView.Adapter<Adapter_Search_results_list.MyViewHolder> {

    private Context context;
    private ArrayList results;
    Card_Search_result_interface listener;

    // constructor
    public Adapter_Search_results_list(Context context, Frag_Create_Trip listener, ArrayList results){
        this.context = context;
        this.results = results;
        this.listener = listener;
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
        holder.result_checkbox.setOnClickListener(v -> listener.onToggle(position));
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    // to bind views with layout objects
    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView result;
        CheckBox result_checkbox;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            result = itemView.findViewById(R.id.result_title); // bind title
            result_checkbox = itemView.findViewById(R.id.result_checkbox);
        }
    }
}
