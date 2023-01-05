package com.example.projecto_cm.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projecto_cm.Adapters.Adapter_For_Listing_Trip_Locations;
import com.example.projecto_cm.DAO_helper;
import com.example.projecto_cm.R;
import com.example.projecto_cm.Shared_View_Model;
import com.google.android.gms.tasks.Task;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class Frag_Photo_Album extends Fragment {

    public boolean isFragmentReady = false;
    private String username, trip_title;
    private RecyclerView trip_locations_recyclerView;
    private ArrayList<Bitmap> photo_album = new ArrayList<>();
    private Adapter_For_Listing_Trip_Locations adapter_for_listing_trip_photos;
    private ImageView take_picture;
    private DAO_helper dao;
    private Dialog loading_animation_dialog, interface_hints_dialog;

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

        dao = new DAO_helper(requireActivity());
        trip_title = getArguments().getString("trip title");

        // get username
        Shared_View_Model model = new ViewModelProvider(requireActivity()).get(Shared_View_Model.class);
        model.getData().observe(getViewLifecycleOwner(), item -> {
            username = (String) item;
            dao.getTripPhotos(username, trip_title, this);
        });

        // load login fragment layout
        View view = inflater.inflate(R.layout.frag_trip_photo_album, container, false);

        // load toolbar of this fragment
        Toolbar toolbar = view.findViewById(R.id.photo_album_toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);

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

        // prepare loading animation
        loading_animation_dialog = new Dialog(requireActivity());
        loading_animation_dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        loading_animation_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loading_animation_dialog.setCanceledOnTouchOutside(false);
        loading_animation_dialog.setContentView(R.layout.loading_animation_layout);
        loading_animation_dialog.show();

        // prepare dialog with layout hints
        interface_hints_dialog = new Dialog(requireActivity());
        interface_hints_dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        interface_hints_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        interface_hints_dialog.setCanceledOnTouchOutside(true);
        interface_hints_dialog.setContentView(R.layout.dialog_show_hints_layout);

        // call camera on click in picture
        take_picture = requireActivity().findViewById(R.id.trip_picture);
        take_picture.setOnClickListener(v -> takePicture());

        trip_locations_recyclerView = requireActivity().findViewById(R.id.picture_trip_locations_recycler_view);
    }


    /**
     * app bar menu set up
     * @param menu
     * @param inflater
     */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.list_my_trips_actions, menu);

        // change app bar title
        ActionBar ab = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        assert ab != null;
        ab.setTitle(trip_title + " photo album");

        // hide search button
        MenuItem item = menu.findItem(R.id.search_trip);
        item.setVisible(false);

        super.onCreateOptionsMenu(menu,inflater);
    }

    /**
     * app bar menu actions
     * search trip and help actions
     * help shoe how to delete a trip
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.interface_hints){
            TextView hint = interface_hints_dialog.findViewById(R.id.hint_text);
            hint.setText("\nHint\n\nClick on a photo to view or delete.\n");
            interface_hints_dialog.show();
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * permission handler
     */
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {});

    /**
     * add photo to album and firebase
     */
    private final ActivityResultLauncher<Intent> startCameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {

        // handle the result of the camera intent
        if (result.getResultCode() == Activity.RESULT_OK) {

            // get the image taken with the camera
            Bitmap imageBitmap = (Bitmap) result.getData().getExtras().get("data");

            photo_album.add(imageBitmap);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageInByte = baos.toByteArray();
            String imageEncoded = Base64.encodeToString(imageInByte, Base64.DEFAULT);
            dao.addPhoto(username, trip_title, imageEncoded, this);
        }
    });

    /**
     * request permissions to open camera and take picture
     */
    private void takePicture() {
        if (requireActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {

            // check for camera permissions
            if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

                // open camera to take picture
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startCameraLauncher.launch(cameraIntent);
            }
            else {
                // request camera permission
                requestPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        }
        else {
            // display message if device does not have a camera
            Toast.makeText(getActivity(), "This device does not have a camera", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * get photo album from firebase
     */
    public void setPhotoAlbum(ArrayList<String> album_data){

        // decode bitmap strings to bitmap objects
        if(album_data != null){
            for (String bitmapString : album_data) {
                byte[] decodedString = Base64.decode(bitmapString, Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                photo_album.add(decodedBitmap);
            }
        }

        // set up list of locations
        adapter_for_listing_trip_photos = new Adapter_For_Listing_Trip_Locations(requireActivity(), photo_album, null, "list of locations photos");
        trip_locations_recyclerView.setAdapter(adapter_for_listing_trip_photos);
        trip_locations_recyclerView.setLayoutManager(new GridLayoutManager(requireActivity(), 4));
        loading_animation_dialog.dismiss();
    }

    /**
     * get result of addin image to database
     * @param delete_trip
     */
    public void getDAOResultMessage(Task<Void> delete_trip) {

        // cancel loading animation and go back to home screen
        loading_animation_dialog.dismiss();

        delete_trip.addOnSuccessListener(suc -> {

            // update recycler vie of list of photos
            adapter_for_listing_trip_photos.setLocationsList(photo_album);
            trip_locations_recyclerView.setAdapter(adapter_for_listing_trip_photos);

            Toast.makeText(requireActivity(), "Photo added successfully!",Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(er -> Toast.makeText(requireActivity(), "Error while saving photo in Database!\nTry again later.",Toast.LENGTH_SHORT).show());
    }
}
