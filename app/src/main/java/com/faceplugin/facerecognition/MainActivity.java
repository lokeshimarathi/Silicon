package com.faceplugin.facerecognition;


import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.ocp.facesdk.FaceBox;
import com.ocp.facesdk.FaceDetectionParam;
import com.ocp.facesdk.FaceSDK;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final int SELECT_PHOTO_REQUEST_CODE = 1;
    private static final int SELECT_ATTRIBUTE_REQUEST_CODE = 2;

    private DBManager dbManager;
    private TextView textWarning;
    private TextView textEnrolledFace;
    private PersonAdapter personAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textWarning = findViewById(R.id.textWarning);
        textEnrolledFace = findViewById(R.id.tv_enrolledface);
        textEnrolledFace.setVisibility(View.INVISIBLE);

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

        if (ret != FaceSDK.SDK_SUCCESS) {
            textWarning.setVisibility(View.VISIBLE);
            if (ret == FaceSDK.SDK_LICENSE_KEY_ERROR) {
                textWarning.setText("Invalid license!");
            } else if (ret == FaceSDK.SDK_LICENSE_APPID_ERROR) {
                textWarning.setText("Invalid App ID!");
            } else if (ret == FaceSDK.SDK_LICENSE_EXPIRED) {
                textWarning.setText("License expired!");
            } else if (ret == FaceSDK.SDK_NO_ACTIVATED) {
                textWarning.setText("Not activated!");
            } else if (ret == FaceSDK.SDK_INIT_ERROR) {
                textWarning.setText("Initialization error!");
            }
        }

        dbManager = new DBManager(this);
        dbManager.loadPerson();

        personAdapter = new PersonAdapter(this, DBManager.personList, textEnrolledFace);
        ListView listView = findViewById(R.id.listPerson);
        listView.setAdapter(personAdapter);

        LinearLayout enrollLayout = findViewById(R.id.ll_enroll);
        LinearLayout identifyLayout = findViewById(R.id.ll_identify);
        LinearLayout captureLayout = findViewById(R.id.ll_capture);
        LinearLayout attributeLayout = findViewById(R.id.ll_attribute);
        LinearLayout settingsLayout = findViewById(R.id.ll_settings);
        LinearLayout aboutLayout = findViewById(R.id.ll_about);
        LinearLayout brandLayout = findViewById(R.id.lytBrand);

        enrollLayout.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_PICK);
            startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture)), SELECT_PHOTO_REQUEST_CODE);
        });

        identifyLayout.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, CameraActivity.class));
        });

        captureLayout.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, CaptureActivity.class));
        });

        attributeLayout.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_PICK);
            startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture)), SELECT_ATTRIBUTE_REQUEST_CODE);
        });

        settingsLayout.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        });



        brandLayout.setOnClickListener(v -> {
            Intent browse = new Intent(Intent.ACTION_VIEW, Uri.parse("https://faceplugin.com"));
            startActivity(browse);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        personAdapter.notifyDataSetChanged();
        if (personAdapter.getCount() == 0) {
            textEnrolledFace.setVisibility(View.INVISIBLE);
        } else {
            textEnrolledFace.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_PHOTO_REQUEST_CODE && resultCode == RESULT_OK) {

            try {
                Bitmap bitmap = Utils.getCorrectlyOrientedImage(this, data.getData());

                List<FaceBox> faceBoxes = FaceSDK.faceDetection(bitmap, null);

                if (faceBoxes == null || faceBoxes.isEmpty()) {
                    Toast.makeText(this, getString(R.string.no_face_detected), Toast.LENGTH_SHORT).show();
                } else if (faceBoxes.size() > 1) {
                    Toast.makeText(this, getString(R.string.multiple_face_detected), Toast.LENGTH_SHORT).show();
                } else {
                    Bitmap faceImage = Utils.cropFace(bitmap, faceBoxes.get(0));
                    byte[] templates = FaceSDK.templateExtraction(bitmap, faceBoxes.get(0));

                    dbManager.insertPerson("Person" + new Random().nextInt(), faceImage, templates);
                    personAdapter.notifyDataSetChanged();

                    if (personAdapter.getCount() == 0) {
                        textEnrolledFace.setVisibility(View.INVISIBLE);
                    } else {
                        textEnrolledFace.setVisibility(View.VISIBLE);
                    }

                    Toast.makeText(this, getString(R.string.person_enrolled), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (requestCode == SELECT_ATTRIBUTE_REQUEST_CODE && resultCode == RESULT_OK) {
            try {

                Bitmap bitmap = Utils.getCorrectlyOrientedImage(this, data.getData());

                FaceDetectionParam param = new FaceDetectionParam();
                param.check_liveness = true;
                param.check_liveness_level = SettingsActivity.getLivenessLevel(this);
                param.check_eye_closeness = true;
                param.check_face_occlusion = true;
                param.check_mouth_opened = true;
                param.estimate_age_gender = true;

                List<FaceBox> faceBoxes = FaceSDK.faceDetection(bitmap, param);

                if (faceBoxes == null || faceBoxes.isEmpty()) {
                    Toast.makeText(this, getString(R.string.no_face_detected), Toast.LENGTH_SHORT).show();
                } else if (faceBoxes.size() > 1) {
                    Toast.makeText(this, getString(R.string.multiple_face_detected), Toast.LENGTH_SHORT).show();
                } else {
                    Bitmap faceImage = Utils.cropFace(bitmap, faceBoxes.get(0));

                    Intent intent = new Intent(this, AttributeActivity.class);
                    intent.putExtra("face_image", faceImage);
                    intent.putExtra("yaw", faceBoxes.get(0).yaw);
                    intent.putExtra("roll", faceBoxes.get(0).roll);
                    intent.putExtra("pitch", faceBoxes.get(0).pitch);
                    intent.putExtra("face_quality", faceBoxes.get(0).face_quality);
                    intent.putExtra("face_luminance", faceBoxes.get(0).face_luminance);
                    intent.putExtra("liveness", faceBoxes.get(0).liveness);
                    intent.putExtra("left_eye_closed", faceBoxes.get(0).left_eye_closed);
                    intent.putExtra("right_eye_closed", faceBoxes.get(0).right_eye_closed);
                    intent.putExtra("face_occlusion", faceBoxes.get(0).face_occlusion);
                    intent.putExtra("mouth_opened", faceBoxes.get(0).mouth_opened);
                    intent.putExtra("age", faceBoxes.get(0).age);
                    intent.putExtra("gender", faceBoxes.get(0).gender);

                    startActivity(intent);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
