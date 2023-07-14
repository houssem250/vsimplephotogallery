package com.example.vsimplephotogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.github.chrisbanes.photoview.PhotoView;

public class FullSizeImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_size_image);

        PhotoView photoView = findViewById(R.id.zoomageView);

        String imagePath = getIntent().getStringExtra("imagePath");
        Bitmap imageBitmap = BitmapFactory.decodeFile(imagePath);
        if (imageBitmap != null) {
            photoView.setImageBitmap(imageBitmap);
        } else {
            photoView.setImageResource(R.drawable.empty);
        }

        photoView.setMaximumScale(10); // Set the maximum zoom scale you desire
    }
}





