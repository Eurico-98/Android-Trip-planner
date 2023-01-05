package com.example.projecto_cm;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;

public class FullScreenImageActivity extends Activity {

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        // get the image and image name from the intent
        Intent intent = getIntent();

        // convert back to bitmap
        byte[] imageByteArray = Base64.decode(intent.getStringExtra("image"), Base64.DEFAULT);
        Bitmap imageBitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);


        // set the image and image name in the image view and action bar
        imageView = findViewById(R.id.full_screen_image);
        imageView.setImageBitmap(imageBitmap);
    }
}
