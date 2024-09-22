package com.luhyah.ocr;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class LoadImageFromInternet extends AppCompatActivity {


    private CardView okButton, cancelButton;
    private EditText url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.loadimagefrominternet);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        okButton= findViewById(R.id.OK);
        cancelButton = findViewById(R.id.CANCEL);
        url = findViewById(R.id.URL);

        File imageFromInternet = new File(getExternalFilesDir(null),"fromInternet.jpg");
        RequestQueue queue = Volley.newRequestQueue(this);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        }

        cancelButton.setOnClickListener(view -> {
      startActivity(new Intent(getApplicationContext(), MainActivity.class));
        });

        okButton.setOnClickListener(view ->{

            ImageRequest imageRequest = new ImageRequest(url.getText().toString(),
                    response -> {
                        try {
                            FileOutputStream fileOutputStream = new FileOutputStream(imageFromInternet);
                            response.compress(Bitmap.CompressFormat.JPEG,100,fileOutputStream);
                            fileOutputStream.flush();
                            Uri uri = Uri.fromFile(imageFromInternet);
                            Intent toCrop = new Intent(this, Crop.class);
                            toCrop.putExtra("rawImageUri",uri);
                            startActivity(toCrop);
                        } catch (FileNotFoundException e) {
                            Log.d("Error1","Error Save FIle");
                        } catch (IOException e) {
//                            throw new RuntimeException(e);
                            Log.d("Error2","IO error");
                        }

                    }, 0, 0, null,
                    error -> Toast.makeText(getApplicationContext(),error.toString(),Toast.LENGTH_SHORT).show());
            queue.add(imageRequest);

        });
    }
}