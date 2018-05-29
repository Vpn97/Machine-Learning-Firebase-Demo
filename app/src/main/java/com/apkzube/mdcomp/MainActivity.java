package com.apkzube.mdcomp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.bottomappbar.BottomAppBar;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.AppOpsManagerCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;

import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    BottomAppBar bar;
    FloatingActionButton fab;
    Button btnCode;

    //firebase ML Objects for facedetection

    FirebaseVisionFaceDetectorOptions options;

    FirebaseVisionFaceDetector visionFaceDetector;


    String[] permissions = new String[]{Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);
        visitonSetup();
        allocation();
        setEvnets();

    }

    private void visitonSetup() {
        options = new FirebaseVisionFaceDetectorOptions.Builder()
                .setModeType(FirebaseVisionFaceDetectorOptions.FAST_MODE)
                .setLandmarkType(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                .setClassificationType(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                .setMinFaceSize(0.1f)
                .setTrackingEnabled(true)
                .build();
    }

    private void allocation() {
        btnCode=findViewById(R.id.btnCode);
        bar = findViewById(R.id.bar);
        bar.replaceMenu(R.menu.menu);
        fab = findViewById(R.id.fab);
        //imgVision = findViewById(R.id.imgVision);
        visionFaceDetector = FirebaseVision.getInstance().getVisionFaceDetector(options);
    }


    private void setEvnets() {


        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT,
                        "Hey check out my app at ApkZube Firebase ML Demo App: https://play.google.com/store/apps/details?id=" + getPackageName());
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            }
        });

        btnCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               String uri=getResources().getString(R.string.url_githube);
               Intent i=new Intent(Intent.ACTION_VIEW);
               i.setData(Uri.parse(uri));
               startActivity(i);
            }
        });

        bar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {


                switch (item.getItemId()) {

                    case R.id.menu_rateus:
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
                        return true;
                    case R.id.menu_more:
                        String url_more_app = "https://play.google.com/store/apps/developer?id=ApkZube";
                        Intent viewIntent =
                                new Intent("android.intent.action.VIEW",
                                        Uri.parse(url_more_app));
                        startActivity(viewIntent);

                        return true;
                    default:
                        return false;
                }

            }
        });


        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                takePermission();
                fabEvent();
            }
        });

    }

    @SuppressLint("RestrictedApi")
    private void fabEvent() {

        PopupMenu popupMenu = new PopupMenu(MainActivity.this, fab);
        popupMenu.getMenuInflater().inflate(R.menu.ml_kit_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int mode = 0;
                switch (item.getItemId()) {
                    case R.id.menu_faceDetection:
                        mode = 0;
                        break;
                    case R.id.menu_textDetection:
                        mode = 1;
                        break;
                    case R.id.menu_barocdeDetection:
                        mode = 2;
                        break;
                    case R.id.menu_labelDetection:
                        mode = 3;
                        break;
                    case R.id.menu_classification:
                        mode = 4;
                        break;

                }

                Intent intent = new Intent(MainActivity.this, LiveDemo.class);
                Pair<View, String> pair = new Pair<>(findViewById(R.id.cordinatorLayout), "mytran");

                ActivityOptionsCompat compat = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this, pair);
                intent.putExtra("mode", mode);
                startActivity(intent, compat.toBundle());
                return true;
            }
        });

        try{
            Field[] fields=popupMenu.getClass().getDeclaredFields();
            for (Field field:
                    fields) {

                if("mPopup".equals(field.getName()));
                field.setAccessible(true);
                Object menuPopupHelper =field.get(popupMenu);
                Class<?> classPopupHelper =Class.forName(menuPopupHelper.getClass().getName());
                Method setForceIcon=classPopupHelper.getMethod("setForceShowIcon",boolean.class);
                setForceIcon.invoke(menuPopupHelper,true);
                break;
            }
        }catch (Exception e){

        }
        popupMenu.show();

    }

    private void takePermission() {
        //check Bulid version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, permissions[0]) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(MainActivity.this, permissions[1]) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(MainActivity.this, permissions[2]) != PackageManager.PERMISSION_GRANTED ) {
                //permission is not granted need to get permission
                this.requestPermissions(permissions, 99);

            } else {
                //Permition Already Granted
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 99) {
            if (grantResults.length < 3) {
                System.exit(0);
            } else {
                Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
