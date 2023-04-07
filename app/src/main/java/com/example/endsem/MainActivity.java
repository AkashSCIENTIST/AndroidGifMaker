package com.example.endsem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private static final int PICKFILE_REQUEST_CODE = 100;
    private ArrayList<Bitmap> bitmaps;
    ImageView img;
    TextView page_number;
    Uri imageUri;
    EditText filenamebox;
    int imgCounter = 0;
    int numImages = 1;
    String folderPath;
    int PICTURE_RESULT = 999;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        img= (ImageView)findViewById(R.id.preview_image);
        filenamebox = (EditText)findViewById(R.id.filename);
        page_number = (TextView)findViewById(R.id.page_num);
        page_number.setText("   0/0   ");
        bitmaps = new ArrayList<>();
        String path = String.valueOf(Environment.getExternalStorageDirectory()) + "/your_name_folder";
        try {
            File ruta_sd = new File(path);
            File folder = new File(ruta_sd.getAbsolutePath(), path);
            boolean success = true;
            if (!folder.exists()) {
                success = folder.mkdir();
            }
            if (success) {
                Toast.makeText(MainActivity.this, "Folder Created..", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception ex) {
            Log.e("Exception",ex.toString());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
            else
            {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            img.setImageBitmap(photo);
            bitmaps.add(photo);
            imgCounter = bitmaps.size() - 1;
            page_number.setText("   " + (imgCounter+1) + "/" + bitmaps.size() + "   ");
        }
        if (requestCode == PICKFILE_REQUEST_CODE) {
            folderPath = data.getDataString();
        }
    }





    public File getFilePath(){
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        String imageFileName = filenamebox.getText() + ".gif";
        Toast toast = Toast.makeText(getApplicationContext(), "Gif name " + imageFileName, Toast.LENGTH_SHORT);
        toast.show();
        return new File(dir, imageFileName);
    }

    public void takeNext(View view) {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    public void retake(View view) {
        if(bitmaps.size() == 0) return;
        bitmaps.remove(imgCounter);
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    @SuppressLint("SetTextI18n")
    public void nextImg(View view) {
        if(bitmaps.size() == 0) return;
        if(imgCounter == bitmaps.size()-1) return;
        imgCounter++;
        imgCounter %= bitmaps.size();
        img.setImageBitmap(bitmaps.get(imgCounter));
        page_number.setText("   " + (imgCounter+1) + "/" + bitmaps.size() + "   ");
    }

    @SuppressLint("SetTextI18n")
    public void prevImg(View view) {
        if(bitmaps.size() == 0) return;
        if(imgCounter == 0) return;
        imgCounter--;
        if(imgCounter < 0) imgCounter = bitmaps.size()-1;
        img.setImageBitmap(bitmaps.get(imgCounter));
        page_number.setText("   " + (imgCounter+1) + "/" + bitmaps.size() + "   ");
    }

    public static Bitmap crop(Bitmap bitmap, float newWidth, float newHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        return resizedBitmap;
    }

    public void zoom_out(View v){
        Bitmap bitmap = bitmaps.get(imgCounter);
        bitmaps.set(imgCounter, crop(bitmap, (float)(bitmap.getWidth()*0.75), (float)(bitmap.getHeight()*0.75)));
        img.setImageBitmap(bitmap);
    }

    public void zoom_in(View v){
        Bitmap bitmap = bitmaps.get(imgCounter);
        bitmaps.set(imgCounter, crop(bitmap, (float)(bitmap.getWidth()*1.25), (float)(bitmap.getHeight()*1.25)));
        img.setImageBitmap(bitmap);
    }

    public void createGif(View view) {
        FileOutputStream outStream = null;
        Toast.makeText(this, "Saving as GIF", Toast.LENGTH_LONG).show();
        try{
            outStream = new FileOutputStream(getFilePath());
            outStream.write(generateGIF());
            outStream.close();
            bitmaps = new ArrayList<>();
            img.setImageBitmap(BitmapFactory.decodeResource(view.getContext().getResources(), R.drawable.ic_launcher_background));
            Toast.makeText(this, "Opening gallery or documents folder to see it", Toast.LENGTH_LONG).show();

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public byte[] generateGIF() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        AnimatedGifEncoder encoder = new AnimatedGifEncoder();
        encoder.setDelay(1000);
        encoder.start(bos);
        for (Bitmap bitmap : bitmaps) {
            encoder.addFrame(bitmap);
        }
        encoder.finish();
        return bos.toByteArray();
    }
}