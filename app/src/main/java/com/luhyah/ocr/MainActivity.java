package com.luhyah.ocr;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

//import net.sourceforge.tess4j.Tesseract;

public class MainActivity extends AppCompatActivity {


    private CardView fromCam;
    private CardView fromGallery;
    private CardView fromLink;
    private ImageView test;

    private static final int REQUEST_CAMERA_PERMISSION = 200;
    static final int REQUEST_IMAGE_CAPTURE = 1;

    private Uri imageUri;


    ActivityResultLauncher<PickVisualMediaRequest> pickMediaLauncher = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
        // Callback is invoked after the user selects a media item or closes the
        // photo picker.
        if (uri != null) {
            Log.d("PhotoPicker", "Selected URI: " + uri);
            cropImage(uri);

        } else {
            Log.d("PhotoPicker", "No media selected");
        }
    });


    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        fromCam = findViewById(R.id.fromCam);
        fromGallery = findViewById(R.id.fromGallery);
        fromLink = findViewById(R.id.fromLink);


        fromGallery.setOnClickListener(view -> {


            if (view != null) {
                pickMediaLauncher.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
            } else {
                Log.d("Testing Album Tab", "View not loaded");
            }
        });

        fromCam.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                Intent accessCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File photoFile = null;
                if (accessCamera.resolveActivity(getPackageManager()) != null) {
                    try {
                        photoFile = createImageFile();

                    } catch (IOException ex) {
                        Log.d("ImageFile", "Error creating Image File");
                    }


                if (photoFile != null) {
                    try {
                        imageUri = FileProvider.getUriForFile(this, "com.example.android.fileprovider", photoFile);

                        accessCamera.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                        startActivityForResult(accessCamera, REQUEST_IMAGE_CAPTURE);

                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(this, "Ooopsies, Camera app didn't work", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            } else {
                Toast.makeText(this, "CAMERA PERMISSION NOT GRANTED", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            }


        });

        fromLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), LoadImageFromInternet.class));
            }
        });
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageName = "OCR_" + "_";
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(imageName, ".png", dir);
        return imageFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {



            cropImage(imageUri);
        }
    }

    private void cropImage(Uri rawImageUri) {

        Intent toCrop = new Intent(this, Crop.class);
        toCrop.putExtra("rawImageUri",rawImageUri);
        startActivity(toCrop);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        return super.onKeyDown(keyCode, event);
    }
}