package com.apkzube.mdcomp;

import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.ml.common.FirebaseMLException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.apkzube.mdcomp.barcodescanning.BarcodeScanningProcessor;
import com.apkzube.mdcomp.custommodel.CustomImageClassifierProcessor;
import com.apkzube.mdcomp.facedetection.FaceDetectionProcessor;
import com.apkzube.mdcomp.imagelabeling.ImageLabelingProcessor;
import com.apkzube.mdcomp.textrecognition.TextRecognitionProcessor;

public class LiveDemo extends AppCompatActivity implements
        CompoundButton.OnCheckedChangeListener {


    private static final String FACE_DETECTION = "Face Detection";
    private static final String TEXT_DETECTION = "Text Detection";
    private static final String BARCODE_DETECTION = "Barcode Detection";
    private static final String IMAGE_LABEL_DETECTION = "Label Detection";
    private static final String CLASSIFICATION = "Classification";
    private static final String TAG = "LivePreviewActivity";
    private int mode;
    List<String> parent;
    //Widget
    ToggleButton facingSwitch;
    private AdView adView;


    private CameraSource cameraSource = null;
    private CameraSourcePreview preview;
    private GraphicOverlay graphicOverlay;
    private String selectedModel = FACE_DETECTION;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_demo);
        //code start from here

        //mobile ad init
        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");
        adView=findViewById(R.id.adView);
        AdRequest request=new AdRequest.Builder().addTestDevice("33BE2250B43518CCDA7DE426D04EE231").build();
        adView.loadAd(request);


        //starting actual code
        mode = getIntent().getIntExtra("mode", 0);

        preview = (CameraSourcePreview) findViewById(R.id.firePreview);
        if (preview == null) {
            Log.d(TAG, "Preview is null");
        }
        graphicOverlay = (GraphicOverlay) findViewById(R.id.fireFaceOverlay);
        if (graphicOverlay == null) {
            Log.d(TAG, "graphicOverlay is null");
        }
        allocation();
        setEvent();
    }

    private void allocation() {
        parent = new ArrayList<>();
        parent.add(FACE_DETECTION);
        parent.add(TEXT_DETECTION);
        parent.add(BARCODE_DETECTION);
        parent.add(IMAGE_LABEL_DETECTION);
        parent.add(CLASSIFICATION);
        //switch
        facingSwitch = (ToggleButton) findViewById(R.id.facingswitch);
        //selection mode
        selectedModel = parent.get(mode);
        //method for ML Detection start camera

        createCameraSource(selectedModel);
        startCameraSource();

    }


    private void setEvent() {

        facingSwitch.setOnCheckedChangeListener(this);

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.d(TAG, "Set facing");
        if (cameraSource != null) {
            if (isChecked) {
                cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);
            } else {
                cameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);
            }
        }
        preview.stop();
        startCameraSource();
    }


    private void createCameraSource(String model) {

        //starting camrea model

        if (cameraSource == null) {
            cameraSource = new CameraSource(this, graphicOverlay);
        }

        try {
            switch (model) {
                case CLASSIFICATION:
                    Log.i(TAG, "Using Custom Image Classifier Processor");
                    cameraSource.setMachineLearningFrameProcessor(new CustomImageClassifierProcessor(this));
                    break;
                case TEXT_DETECTION:
                    Log.i(TAG, "Using Text Detector Processor");
                    cameraSource.setMachineLearningFrameProcessor(new TextRecognitionProcessor());
                    break;
                case FACE_DETECTION:
                    Log.i(TAG, "Using Face Detector Processor");
                    cameraSource.setMachineLearningFrameProcessor(new FaceDetectionProcessor());
                    break;
                case BARCODE_DETECTION:
                    Log.i(TAG, "Using Barcode Detector Processor");
                    cameraSource.setMachineLearningFrameProcessor(new BarcodeScanningProcessor());
                    break;
                case IMAGE_LABEL_DETECTION:
                    Log.i(TAG, "Using Image Label Detector Processor");
                    cameraSource.setMachineLearningFrameProcessor(new ImageLabelingProcessor());
                    break;
                default:
                    Log.e(TAG, "Unknown model: " + model);
            }
        } catch (FirebaseMLException e) {
            Log.e(TAG, "can not create camera source: " + model);
        }

    }

    private void startCameraSource() {
        if (cameraSource != null) {
            try {
                if (preview == null) {
                    Log.d(TAG, "resume: Preview is null");
                }
                if (graphicOverlay == null) {
                    Log.d(TAG, "resume: graphOverlay is null");
                }
                preview.start(cameraSource, graphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        startCameraSource();
    }

    // Stops the camera.
    @Override
    protected void onPause() {
        super.onPause();
        preview.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
        }
    }
}
