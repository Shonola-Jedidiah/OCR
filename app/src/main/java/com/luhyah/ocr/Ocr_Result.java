package com.luhyah.ocr;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Ocr_Result extends AppCompatActivity {


    private ImageView croppedImage;
    private EditText ocrOutput;
    private CardView copiedButton, saveAsPdfButton, saveAsDocxButton, saveAsTxtButton;
    Uri filePath;
    private String ocrtext;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == RESULT_OK){

            if(data != null){
                filePath = data.getData();

                try {
                    OutputStream outputStream = getContentResolver().openOutputStream(data.getData());
                    outputStream.write(ocrtext.getBytes());
                    outputStream.close();
                    Toast.makeText(this,"File Saved",Toast.LENGTH_SHORT).show();
                } catch (FileNotFoundException e) {
                    Log.d("File", "File Not Found");
                } catch (IOException e) {
                    Log.d("File", "Error writing files");
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ocr_result);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        croppedImage = findViewById(R.id.croppedImage);
        ocrOutput = findViewById(R.id.ocrOutput);
        copiedButton = findViewById(R.id.copyButton);
        saveAsPdfButton = findViewById(R.id.saveAsPDFButton);
        saveAsDocxButton = findViewById(R.id.saveAsDocxButton);
        saveAsTxtButton = findViewById(R.id.saveAsTxtButton);
        ocrtext = ocrOutput.getText().toString();

        TextRecognizer textRecognizer = TextRecognition.getClient(TextRecognizerOptions)

        try {
            croppedImage.setImageBitmap(BitmapFactory.decodeStream(getContentResolver().openInputStream(CroppedImageUri())));
        } catch (FileNotFoundException e) {
            Log.d("Cropping", "Oopsies, No cropped Image Recieved");
            //Send User Back to Crop Activity with raw image Uri Extra
        }


        copiedButton.setOnClickListener(view ->{
            //Code to copy to clipboard
            ClipData clip = ClipData.newPlainText("Ocr text",ocrtext);
            clipboardManager.setPrimaryClip(clip);
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.S_V2){
            Toast.makeText(getApplicationContext(),"TEXT COPIED TO CLIPBOARD", Toast.LENGTH_SHORT).show();
            }
        });
        saveAsPdfButton.setOnClickListener(view ->{
            //Code to save as pdf
        });
        saveAsDocxButton.setOnClickListener(view ->{
            //Code to save as docx
        });
        saveAsTxtButton.setOnClickListener(view ->{
            //Code to save as txt
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!=
                        PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT <= Build.VERSION_CODES.P){
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                }else{
                    Intent openDir = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                    openDir.addCategory(Intent.CATEGORY_OPENABLE);
                    openDir.setType("text/plain");
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    String DefaultFileName = "OCR_" + timeStamp +".txt";
                    openDir.putExtra(Intent.EXTRA_TITLE, DefaultFileName);
                    ocrtext = ocrOutput.getText().toString();
                    startActivityForResult(openDir,1);
                }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            Intent toCrop = new Intent(this, Crop.class);
            toCrop.putExtra("rawImageUri",RawImageUri());
            startActivity(toCrop);
        }
        return super.onKeyDown(keyCode, event);
    }

    private Uri CroppedImageUri(){
        return getIntent().getParcelableExtra("croppedImageUri");
    }
    private Uri RawImageUri(){
        return getIntent().getParcelableExtra("rawImageUri");
    }
}