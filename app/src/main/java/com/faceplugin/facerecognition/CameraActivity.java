package com.faceplugin.facerecognition;


import android.graphics.Bitmap;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.ocp.facesdk.FaceBox;
import com.ocp.facesdk.FaceDetectionParam;
import com.ocp.facesdk.FaceSDK;
import java.util.List;

public class CameraActivity extends AppCompatActivity {
    private DBManager dbManager;
    private TextView textWarning;
    private TextView textEnrolledFace;
    private PersonAdapter personAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        int ret = FaceSDK.setActivation(
                "fqRL05BKupwTX4/Q2fE8c5wn+HVz7eh7f7pXGxnQbXo8Bxy4OhbcW0NRYQXJpJ+p6fVYxdB6OU6K\n" +
                        "RCDsWhqcImUQ9+fXdD7314NBFOg7tVh0T4GsKdrVS6989gjDSwDQKxhhyZ7RXbV2a0GmHx6eyfLs\n" +
                        "bugMfje6bXfUA8G9qs0yyOomahS/0x2PUIrSPINbk/JhDeRtFzfUvORBjte1lsxAR5SB/h68veUW\n" +
                        "M7jhfT6Gl/uk/ekK7VXvcZeGcWYW9Ig22+y51OPQNBS/vfo8ENj9xJjFG1AXEHCzYxK9EwC2ZZSi\n" +
                        "6gTif7XYJMGwuFej1TyQ4wnZsLSlx5pdJDuRtw=="
        );

        if (ret == FaceSDK.SDK_SUCCESS) {
            ret = FaceSDK.init(getAssets());
        }
        dbManager = new DBManager(this);
        dbManager.loadPerson();
        String imageUrl = "https://cdn.britannica.com/42/91642-050-332E5C66/Keukenhof-Gardens-Lisse-Netherlands.jpg";
        new FirebaseImageLoader(new FirebaseImageLoader.OnImageLoadedListener() {
            @Override
            public void onImageLoaded(Bitmap bitmap) {
                if (bitmap != null) {
                    try {
                        // Perform face detection on the loaded image
                        FaceDetectionParam faceDetectionParam = new FaceDetectionParam();
                        faceDetectionParam.check_liveness = true;
                        faceDetectionParam.check_liveness_level = SettingsActivity.getLivenessLevel(CameraActivity.this);
                        List<FaceBox> faceBoxes = FaceSDK.faceDetection(bitmap, faceDetectionParam);

                        if (faceBoxes != null && !faceBoxes.isEmpty()) {
                            // If a face is detected, perform face recognition
                            FaceBox faceBox = faceBoxes.get(0);
                            byte[] templates = FaceSDK.templateExtraction(bitmap, faceBox);

                            float maxSimilarity = 0;
                            Person identifiedPerson = null;
                            for (Person person : DBManager.personList) {
                                float similarity = FaceSDK.similarityCalculation(templates, person.templates);
                                if (similarity > maxSimilarity) {
                                    maxSimilarity = similarity;
                                    identifiedPerson = person;
                                }
                            }

                            if (maxSimilarity > SettingsActivity.getIdentifyThreshold(CameraActivity.this)) {
                                // If the similarity exceeds the threshold, show the recognition result
                                // You can customize this part according to your UI
                                Bitmap faceImage = Utils.cropFace(bitmap, faceBox);
                                Intent intent = new Intent(CameraActivity.this, ResultActivity.class);
                                intent.putExtra("identified_face", faceImage);
                                intent.putExtra("enrolled_face", identifiedPerson.face);
                                intent.putExtra("identified_name", identifiedPerson.name);
                                intent.putExtra("similarity", maxSimilarity);
                                intent.putExtra("liveness", faceBox.liveness);
                                Toast.makeText(CameraActivity.this, "x", Toast.LENGTH_SHORT).show();

                                startActivity(intent);
                            }
                            else{
                                Toast.makeText(CameraActivity.this, "y", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else{
                            Toast.makeText(CameraActivity.this, "z", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).execute(imageUrl);
    }
}
