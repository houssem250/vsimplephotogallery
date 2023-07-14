package com.example.vsimplephotogallery;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;


import com.example.myapplication.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private static final int GALLERY_REQUEST_CODE = 100;
    private LinearLayout photoContainer;
    private List<Bitmap> selectedImages;
    private DatabaseHelper databaseHelper;

    private ActivityResultLauncher<String> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        photoContainer = findViewById(R.id.photoContainer);
        selectedImages = new ArrayList<>();
        databaseHelper = new DatabaseHelper(this);

        loadImagesFromDatabase();

        if (!selectedImages.isEmpty()) {
            displayImages();
        } else {
            displayEmptyImage();
        }

        Button selectImageButton = findViewById(R.id.selectImageButton);
        selectImageButton.setOnClickListener(v -> openGallery());

        Button clearGallery = findViewById(R.id.clearGallery);
        clearGallery.setOnClickListener(view -> {
            databaseHelper.dropTable();
            selectedImages.clear();
            displayEmptyImage();
        });

        // Initialize the launcher for gallery activity
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                result -> {
                    // Handle the selected image URI here
                    if (result != null) {
                        // Process the selected image
                        // ...
                    }
                });
    }

    private void loadImagesFromDatabase() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = databaseHelper.retrieveImages(db);
        selectedImages.clear();

        int columnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME);
        if (columnIndex >= 0) {
            while (cursor.moveToNext()) {
                byte[] imageByteArray = cursor.getBlob(columnIndex);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);
                selectedImages.add(bitmap);
            }
        }

        cursor.close();
        db.close();
    }

    private void displayImages() {
        photoContainer.removeAllViews();

        for (Bitmap bitmap : selectedImages) {
            ImageView imageView = new ImageView(this);
            imageView.setImageBitmap(bitmap);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            int sizeInDp = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 350, getResources().getDisplayMetrics());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(sizeInDp, sizeInDp);
            imageView.setLayoutParams(layoutParams);
            imageView.setOnClickListener(v -> openFullSizeImage(bitmap));
            photoContainer.addView(imageView);
        }
    }

    private void displayEmptyImage() {
        photoContainer.removeAllViews();
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(R.drawable.empty);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        int sizeInDp = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 350, getResources().getDisplayMetrics());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(sizeInDp, sizeInDp);
        imageView.setLayoutParams(layoutParams);
        photoContainer.addView(imageView);
    }

    private void openFullSizeImage(Bitmap bitmap) {
        // Save the bitmap to a file
        String imagePath = saveBitmapToFile(bitmap);

        Intent intent = new Intent(this, FullSizeImageActivity.class);
        intent.putExtra("imagePath", imagePath);
        startActivity(intent);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent.toUri(Intent.URI_INTENT_SCHEME));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri imageUri = data.getData();
                if (imageUri != null) {
                    try {
                        Bitmap bitmap = getBitmapFromUri(imageUri);
                        selectedImages.add(bitmap);
                        saveImageToDatabase(bitmap);
                        displayImages();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        inputStream.close();
        return bitmap;
    }

    private void saveImageToDatabase(Bitmap bitmap) {
        byte[] imageByteArray = getImageByteArray(bitmap);

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        databaseHelper.insertImage(db, imageByteArray);
        db.close();
    }

    private byte[] getImageByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    private String saveBitmapToFile(Bitmap bitmap) {
        File imagePath = new File(getFilesDir(), "images");
        if (!imagePath.exists()) {
            imagePath.mkdir();
        }

        String fileName = "image_" + System.currentTimeMillis() + ".png";
        File imageFile = new File(imagePath, fileName);

        try (FileOutputStream out = new FileOutputStream(imageFile)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return imageFile.getAbsolutePath();
    }

}


