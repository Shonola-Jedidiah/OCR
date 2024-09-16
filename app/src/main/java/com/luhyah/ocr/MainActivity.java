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

    CameraManager cameraManager;
    Bitmap rawImage, croppedImage;
    private Uri imageUri;


    ActivityResultLauncher<PickVisualMediaRequest> pickMediaLauncher = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
        // Callback is invoked after the user selects a media item or closes the
        // photo picker.
        if (uri != null) {
            Log.d("PhotoPicker", "Selected URI: " + uri);


            try {
                rawImage = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                cropImage(rawImage);
            } catch (FileNotFoundException e) {
                Toast.makeText(this, "Bitmap Parse Error", Toast.LENGTH_SHORT).show();
            }

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
        //test = (ImageView) findViewById(R.id.Test);

        //cameraManager = new CameraManager;

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
                        Log.d("RRRR", "Got here");
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

            }
        });
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageName = "OCR_" + timeStamp + "_";
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(imageName, ".png", dir);
        return imageFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            try {
                rawImage = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
            } catch (FileNotFoundException e) {
                Log.d("CamActivityResult","Failed to interpret Uri");
            }

            cropImage(rawImage);
        }
    }

    private void cropImage(Bitmap rawImage) {

        test.setImageBitmap(rawImage);
        //return croppedImage;
    }


//    private void openCamera() {
//        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
//        try {
//            String cameraId = manager.getCameraIdList()[0];
//            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
//            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
//            Size imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
//
//            // Check permission and open the camera
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
//                manager.openCamera(cameraId, stateCallback, null);
//            }
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }


//    private boolean checkForCamPermission(){
//
//    }
}