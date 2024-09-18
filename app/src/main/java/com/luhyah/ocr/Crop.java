package com.luhyah.ocr;

import android.content.Intent;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.window.OnBackInvokedDispatcher;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;

public class Crop extends AppCompatActivity {

    ActivityResultLauncher<CropImageContractOptions> cropImage = registerForActivityResult(new CropImageContract(), result -> {
        if (result.isSuccessful()) {
            Uri croppedUri = result.getUriContent();
            Intent callOcrActivity = new Intent(getApplicationContext(), Ocr_Result.class);
            callOcrActivity.putExtra("croppedImageUri", croppedUri);
            callOcrActivity.putExtra("rawImageUri", result.getOriginalUri());
            startActivity(callOcrActivity);
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_crop);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Uri rawImageUri = recieveImageUri();

        CropImageOptions cropImageOptions = new CropImageOptions();
        //   cropImageOptions.activityMenuIconColor = R.color.font;
        cropImageOptions.activityBackgroundColor = R.color.Bg;
        //  cropImageOptions.activityMenuTextColor = R.color.font;

        cropImageOptions.borderLineColor = R.color.font;
        cropImageOptions.borderLineThickness = 2f;

        cropImageOptions.activityTitle = "CROP IMAGE";
        CropImageContractOptions cropImageContractOptions = new CropImageContractOptions(rawImageUri, cropImageOptions);

        cropImage.launch(cropImageContractOptions);

        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                            Intent callMainAcivity = new  Intent(getApplicationContext(), MainActivity.class);
            Log.d("BackBtn", "YESS");
            startActivity(callMainAcivity);
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        // The callback can be enabled or disabled here or in handleOnBackPressed()
    }



private Uri recieveImageUri() {
    Intent rawImageIntent = getIntent();
    return rawImageIntent.getParcelableExtra("rawImageUri");
}

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if(keyCode == KeyEvent.KEYCODE_BACK){
//            Intent callMainAcivity = new  Intent(this, MainActivity.class);
//            Log.d("BackBtn", "YESS");
//            startActivity(callMainAcivity);
//        }
//        return super.onKeyDown(keyCode, event);
//    }


}