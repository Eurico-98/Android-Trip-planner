package com.example.projecto_cm.Fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projecto_cm.Adapters.Adapter_For_Listing_Trip_Locations;
import com.example.projecto_cm.R;
import com.example.projecto_cm.Shared_View_Model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Frag_Photo_Album extends Fragment {

    public boolean isFragmentReady = false;
    private String username;
    private RecyclerView trip_locations_recyclerView;
    private ArrayList<String> locations = new ArrayList<>();
    private Adapter_For_Listing_Trip_Locations adapter_for_listing_trip_locations;
    private ImageView trip_picture, delete_trip_picture;
    private TextView photo_album_page_title;


    /**
     *  onCreateView of create trip fragment
     *  load layout and toolbar and get username from shared model view
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // get username
        Shared_View_Model model = new ViewModelProvider(requireActivity()).get(Shared_View_Model.class);
        model.getData().observe(getViewLifecycleOwner(), item -> username = (String) item);

        // load login fragment layout
        View view = inflater.inflate(R.layout.frag_picture_in_trip_or_location, container, false);

        return view;
    }

    /**
     *
     * @param view
     * @param savedInstanceState
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // get locations
        locations.addAll(Arrays.asList(getArguments().getString("locations").split("locations=\\[")[1].split("]")[0].split(", ")));

        trip_picture = requireActivity().findViewById(R.id.trip_picture);
        trip_picture.setImageResource(R.drawable.ic_photo_placehoder);

        delete_trip_picture = requireActivity().findViewById(R.id.delete_trip_picture);

        photo_album_page_title = requireActivity().findViewById(R.id.photo_album_page_title);
        photo_album_page_title.setText(getArguments().getString("trip title") + " photo album");

        //TODO: a propria imagem e o listener que chama a funcao de tirar foto
        //TODO: aqui ir buscar a imagem da trip de exitir a firebase para mostrar - e preciso meter uma imagem template lá
        //TODO: nao esquecer de meter a visibilidade do botao de delete em funcao do tipo de imagem
        //TODO: é preciso tambem ir buscar a lista de fotos de cada local que e preciso meter primeiro la BD, depois passa-se essa lista aqui ao adapter


        // set up list of locations
        trip_locations_recyclerView = requireActivity().findViewById(R.id.picture_trip_locations_recycler_view); // to show list of locations selected
        adapter_for_listing_trip_locations = new Adapter_For_Listing_Trip_Locations(requireActivity(), locations, null, "list of locations photos");
        trip_locations_recyclerView.setAdapter(adapter_for_listing_trip_locations);
        trip_locations_recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));

    }

}
