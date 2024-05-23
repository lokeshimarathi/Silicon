package com.faceplugin.facerecognition.world.upload;

import static android.graphics.Color.BLACK;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.faceplugin.facerecognition.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class VideoPostUpload extends AppCompatActivity {
    EditText caption;
    VideoView videoPost;
    Button uploadButton;
    CardView videoContainer; // Added CardView for corner radius

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(1);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        window.setNavigationBarColor(ContextCompat.getColor(this, R.color.white));
        // for full screen activity and visible status bar icon
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_video_post_upload);

        caption = findViewById(R.id.caption);
        videoContainer = findViewById(R.id.videoContainer); // Initialize the CardView
        videoPost = findViewById(R.id.videoPost);
        uploadButton = findViewById(R.id.uploadButton);

        String File = getIntent().getStringExtra("File");
        Uri postUri = Uri.parse(File);
        videoPost.setVideoURI(postUri);
        videoPost.start();

        // Set corner radius for the CardView
        videoContainer.setRadius(35);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            videoContainer.setOutlineAmbientShadowColor(BLACK);
            videoContainer.setOutlineSpotShadowColor(BLACK);
        }
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        String AuthUserId = user.getUid();
        ProgressDialog pd = new ProgressDialog(VideoPostUpload.this);
        pd.setTitle("Uploading");
        pd.setMessage("Please wait...");
        pd.setCancelable(false);

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pd.show();
                String Caption = caption.getText().toString().trim();
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference filepath = storage.getReference().child(AuthUserId).child("Posts").child(postUri.getLastPathSegment());
                filepath.putFile(postUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                                DatabaseReference databaseReference = firebaseDatabase.getReference();
                                final String currentTimestamp = String.valueOf(System.currentTimeMillis()).substring(0, 10);
                                String Type = "video";
                                databaseReference.child("Posts").child(currentTimestamp).child("post").setValue(uri.toString());
                                databaseReference.child("Posts").child(currentTimestamp).child("type").setValue(Type);
                                databaseReference.child("Posts").child(currentTimestamp).child("caption").setValue(Caption);
                                databaseReference.child("Posts").child(currentTimestamp).child("authUserId").setValue(AuthUserId);
                                pd.dismiss();
                                Toast.makeText(VideoPostUpload.this, "Uploaded successfully!", Toast.LENGTH_SHORT).show();


                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                pd.dismiss();
                                Toast.makeText(VideoPostUpload.this, "Failed! Please try again", Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(VideoPostUpload.this, "Failed! Please try again", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }
}
