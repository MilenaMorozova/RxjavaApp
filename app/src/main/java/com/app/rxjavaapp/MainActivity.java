package com.app.rxjavaapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static java.lang.Thread.currentThread;


public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_READ_IMAGES = 0;
    private ImageView imageView;
    private Button startButton;
    private Button cancelButton;
    private String[] imagePaths;
    private Subscription subscription = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.IMAGE_VIEW);
        startButton = findViewById(R.id.StartButton);
        cancelButton = findViewById(R.id.CancelButton);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_IMAGES);
        } else {
            onClicks();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_IMAGES: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    onClicks();
                    Log.d("MY_TAG", "PERMISSION_GRANTED");
                } else {
                    Log.d("MY_TAG", "PERMISSION_DENIED");
                    // permission denied, boo! Disable the functionality that depends on this permission.
                }
                return;
            }
        }
        // other 'case' lines to check for other permissions this app might request
    }

    private void readImagesFromGallery() {
        Cursor imageCursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Images.Media.DATA}, null,
                null, null);
        if (imageCursor == null) {
            return;
        }
        imagePaths = new String[imageCursor.getCount()];
        for (int i = 0; i < imagePaths.length; i++) {
            imageCursor.moveToNext();
            imagePaths[i] = imageCursor.getString(0);
        }
        imageCursor.close();

    }

    private void startLoadImage() {
        if (subscription != null) {
            subscription.unsubscribe();
        }
        Observable<String> observableImagePaths = Observable.from(imagePaths);
        subscription =
                observableImagePaths.subscribeOn(Schedulers.io()).doOnNext(s -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(imagePath -> {
                            Log.d("OBSERVER_SUCCESS", "onNext: " + imagePath);
                            loadImage(imagePath);
                        });
    }



    private void cancelLoadImages(){
        subscription.unsubscribe();
    }

    private void loadImage(String imagePath){
        try {
            Bitmap bit = BitmapFactory.decodeFile(imagePath);
            Log.d("BITMAP", "" + bit);
            Log.d("Image", "" + currentThread().getName());
            imageView.setImageBitmap(bit);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void onClicks(){
        readImagesFromGallery();
        startButton.setOnClickListener(v -> startLoadImage());
        cancelButton.setOnClickListener(v -> cancelLoadImages());
    }
}