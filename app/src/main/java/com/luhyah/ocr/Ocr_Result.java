package com.luhyah.ocr;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.pdf.PrintedPdfDocument;
import android.util.Log;
import android.view.KeyEvent;

import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Ocr_Result extends AppCompatActivity {


    private ImageView croppedImage;
    private EditText ocrOutput;
    private CardView copiedButton, saveAsPdfButton, share, saveAsTxtButton;
    Uri filePath;
    private String ocrtext;
    private final int SAVE_TXT = 1;
    private final int SAVE_PDF = 2;


    @SuppressLint("ResourceAsColor")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // SAVE TEXT FILE
        if (requestCode == SAVE_TXT && resultCode == RESULT_OK) {
            if (data != null) {
                filePath = data.getData();
                try {
                    OutputStream outputStream = getContentResolver().openOutputStream(data.getData());
                    outputStream.write(ocrtext.getBytes());
                    outputStream.close();
                    Toast.makeText(this, "File Saved", Toast.LENGTH_SHORT).show();
                } catch (FileNotFoundException e) {
                    Log.d("File", "File Not Found");
                } catch (IOException e) {
                    Log.d("File", "Error writing files");
                }
            }
        }
        // SAVE PDF FILE
        if (requestCode == SAVE_PDF && resultCode == RESULT_OK) {
            if (data != null) {
                filePath = data.getData();
                String[] ocrTextArray = ocrtext.split("\n");
                try {
                    OutputStream outputStream = getContentResolver().openOutputStream(data.getData());
                    PrintedPdfDocument pdfDocument = new PrintedPdfDocument(this, new PrintAttributes.Builder()
                            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                            .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                            .setResolution(new PrintAttributes.Resolution("Res", "Resolution", 300, 300))
                            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                            .build());


                    // Set up paint to draw the text
                    Paint paint = new Paint();
                    paint.setColor(R.color.black);
                    int fontSize = 16;
                    paint.setTextSize(fontSize);
                    int pageWidth = 595;
                    int pageHeight = 842;
                    int x = 40;
                    double y = 50;
                    int lineSpacing = 5;
                    int currentPage = 1;
                    int currentLine = 0;
                    int totalLines = ocrTextArray.length;
                    int newPageSize = (int) (pageHeight - (y * 2));
                    double lineHeight = paint.descent() - paint.ascent();
                    int numOfLineOnPage = (int) Math.floor( (double) newPageSize / (fontSize + lineSpacing)) - (int) Math.floor( y / (fontSize + lineSpacing));
                    float maxTextWidth = pageWidth - 2 * x;
                    Log.d("numtotalLines", "" + (numOfLineOnPage*lineHeight + y));

                    PdfDocument.Page page ;
                    while (currentLine < totalLines) {
                        Log.d("Num of times Ran", "" +currentLine);
                        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPage).create();
                        page = pdfDocument.startPage(pageInfo);
                        y = 50;
                        Canvas canvas = page.getCanvas();
                        for (int i =0; i < numOfLineOnPage && currentLine < totalLines; i++,currentLine++ ) {
//                            canvas.drawText(ocrTextArray[currentLine], x, (float)y, paint);
//                             y += lineHeight + lineSpacing;
                            String line = ocrTextArray[currentLine];
                            // Wrap the text if it's too wide for the page
                            while (!line.isEmpty()) {
                                int textCount = paint.breakText(line, true, maxTextWidth, null);
                                String drawText = line.substring(0, textCount);
                                canvas.drawText(drawText, x, (float) y, paint);
                                y += lineHeight + lineSpacing;

                                // Check if page is full
                                if (y + lineHeight > pageHeight) {
                                    break; // Break to move to next page
                                }

                                // Continue with remaining text if it's longer
                                line = line.substring(textCount);
                            }

                            if (!line.isEmpty()) {
                                break; // Break to move to next page
                            }
                        }


                        pdfDocument.finishPage(page);
                        currentPage += 1;
                    }

//                    pdfDocument.finishPage(page);

                    pdfDocument.writeTo(outputStream);
                    outputStream.close();
                    Toast.makeText(this, "PDF saved to " + filePath.getPath(), Toast.LENGTH_SHORT).show();

                    pdfDocument.close();

                } catch (FileNotFoundException e) {
                    //throw new RuntimeException(e);
                } catch (IOException e) {
                   // throw new RuntimeException(e);
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
        share = findViewById(R.id.share);
        saveAsTxtButton = findViewById(R.id.saveAsTxtButton);


        //OCR PROCESSING USING GOOGLE'S ML KIT
        InputImage inputImage = null;
        try {
            inputImage = InputImage.fromFilePath(this, CroppedImageUri());
        } catch (IOException e) {
            Log.d("InputImage", "Failed to get Image from URI");
        }
        TextRecognizer textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        Task<Text> result = textRecognizer.process(inputImage).addOnSuccessListener(new OnSuccessListener<Text>() {
            @Override
            public void onSuccess(Text text) {
                ocrOutput.setText(text.getText());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("ML KIT", "Text Recognition Failed");
            }
        });


        ocrtext = ocrOutput.getText().toString();

        //Set Cropped Image Banner
        try {
            croppedImage.setImageBitmap(BitmapFactory.decodeStream(getContentResolver().openInputStream(CroppedImageUri())));
        } catch (FileNotFoundException e) {
            Log.d("Cropping", "Oopsies, No cropped Image Recieved");
            //Send User Back to Crop Activity with raw image Uri Extra
        }

        //CardView acting as Button for copying Output string to Clipboard
        copiedButton.setOnClickListener(view -> {
            //Code to copy to clipboard
            ClipData clip = ClipData.newPlainText("Ocr text", ocrOutput.getText().toString());
            clipboardManager.setPrimaryClip(clip);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S_V2) {
                Toast.makeText(getApplicationContext(), "TEXT COPIED TO CLIPBOARD", Toast.LENGTH_SHORT).show();
            }
        });

        //CardView acting as Button for saving Output string as pdf file
        saveAsPdfButton.setOnClickListener(view -> {
            //Code to save as pdf
            ocrtext = ocrOutput.getText().toString();
            Intent savePdf = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            savePdf.addCategory(Intent.CATEGORY_OPENABLE);
            savePdf.setType("application/pdf");
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = "Ocr_" + timeStamp + ".pdf";
            savePdf.putExtra(Intent.EXTRA_TITLE, fileName);
            startActivityForResult(savePdf, SAVE_PDF);
        });

        //CardView acting as Button for sharing Output String to other apps
        share.setOnClickListener(view -> {
            //Code to share string
            Intent shareText = new Intent();
            shareText.setAction(Intent.ACTION_SEND);
            shareText.putExtra(Intent.EXTRA_TEXT, ocrOutput.getText().toString());
            shareText.setType("text/plain");
            Intent sendIntent = Intent.createChooser(shareText, null);
            startActivity(sendIntent);


        });
        //CardView acting as Button for saving Output string as text file (.txt)
        saveAsTxtButton.setOnClickListener(view -> {
            //The below "if" block of code is necessary for android 9 and below 'cos write permission is no longer needed for V10 >.
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            } else {
                //Open storage to create ext file
                Intent openDir = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                openDir.addCategory(Intent.CATEGORY_OPENABLE);
                openDir.setType("text/plain"); //Sets file type
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String DefaultFileName = "OCR_" + timeStamp + ".txt";
                openDir.putExtra(Intent.EXTRA_TITLE, DefaultFileName); //Sets file name
                ocrtext = ocrOutput.getText().toString();
                startActivityForResult(openDir, SAVE_TXT);
            }
        });
    }

    //Set the Back Key to go back to CropView with Original Image Uri
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent toCrop = new Intent(this, Crop.class);
            toCrop.putExtra("rawImageUri", RawImageUri());
            startActivity(toCrop);
        }
        return super.onKeyDown(keyCode, event);
    }

    //Get Uri of CroppedImage
    private Uri CroppedImageUri() {
        return getIntent().getParcelableExtra("croppedImageUri");
    }

    //Get Uri of Original Image
    private Uri RawImageUri() {
        return getIntent().getParcelableExtra("rawImageUri");
    }

    private static int numOfLines(String ocrText) {
        String[] array = ocrText.split("");
        return array.length;
    }

    @SuppressLint("ResourceAsColor")
    private void createPDF(String OCRTEXT) {
        PrintedPdfDocument pdfDocument = new PrintedPdfDocument(this, new PrintAttributes.Builder().setMediaSize(PrintAttributes.MediaSize.ISO_A4).setColorMode(PrintAttributes.COLOR_MODE_COLOR).setResolution(new PrintAttributes.Resolution("Res", "Resolution", 300, 300)).setMinMargins(PrintAttributes.Margins.NO_MARGINS).build());
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        Canvas canvas = page.getCanvas();

        // Set up paint to draw the text
        Paint paint = new Paint();
        paint.setColor(R.color.black);
        paint.setTextSize(16);
        canvas.drawText(OCRTEXT, 80, 1000, paint);

        pdfDocument.finishPage(page);


// Save the PDF to a file
        File filePath = new File(getExternalFilesDir(null), "output.pdf");
        try {
            FileOutputStream outputStream = new FileOutputStream(filePath);
            pdfDocument.writeTo(outputStream);
            outputStream.close();
            Toast.makeText(this, "PDF saved to " + filePath.getPath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }

// Close the document
        pdfDocument.close();
    }


}

