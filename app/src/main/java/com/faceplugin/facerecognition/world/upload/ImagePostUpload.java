package com.faceplugin.facerecognition.world.upload;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

public class ImagePostUpload extends AppCompatActivity {
    RoundedImageView imagePost;
    EditText caption;
    Button uploadButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(1);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        window.setNavigationBarColor(ContextCompat.getColor(this, R.color.white));
        //for full screen activity and visible status bar icon
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_image_post_upload);
        imagePost = findViewById(R.id.imagePost);
        caption = findViewById(R.id.caption);
        uploadButton = findViewById(R.id.uploadButton);
        imagePost.setCornerRadius(25);
        String File = getIntent().getStringExtra("File");
        Uri postUri = Uri.parse(File);
        Picasso.get().load(postUri).into(imagePost);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        String AuthUserId = user.getUid();
        ProgressDialog pd = new ProgressDialog(ImagePostUpload.this);
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
                                String Type = "image";
                                databaseReference.child("Posts").child(currentTimestamp).child("post").setValue(uri.toString());
                                databaseReference.child("Posts").child(currentTimestamp).child("type").setValue(Type);
                                databaseReference.child("Posts").child(currentTimestamp).child("caption").setValue(Caption);
                                databaseReference.child("Posts").child(currentTimestamp).child("authUserId").setValue(AuthUserId);
                                pd.dismiss();
                                Toast.makeText(ImagePostUpload.this, "Uploaded successfully!", Toast.LENGTH_SHORT).show();


                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                pd.dismiss();
                                Toast.makeText(ImagePostUpload.this, "Failed! Please try again", Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(ImagePostUpload.this, "Failed! Please try again", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }
}