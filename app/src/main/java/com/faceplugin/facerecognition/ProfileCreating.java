package com.faceplugin.facerecognition;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.ocp.facesdk.FaceBox;
import com.ocp.facesdk.FaceDetectionParam;
import com.ocp.facesdk.FaceSDK;

import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class ProfileCreating extends AppCompatActivity {
    CircularImageView profileImage;
    private TextView date, gender;
    private EditText name, username;
    Button saveButton;
    ActivityResultLauncher<String> mGetContent;
    FirebaseStorage storage;
    private Uri profileImageUri;
    private DBManager dbManager;
    private TextView textWarning;
    private TextView textEnrolledFace;
    private PersonAdapter personAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        supportRequestWindowFeature(1);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        window.setNavigationBarColor(ContextCompat.getColor(this, R.color.white));
        //for full screen activity and visible status bar icon
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_profile_creating2);
        profileImage = findViewById(R.id.profileImage);
        date = findViewById(R.id.date);
        gender = findViewById(R.id.gender);
        name = findViewById(R.id.name);
        username = findViewById(R.id.username);
        saveButton = findViewById(R.id.saveButton);

        //sdk initialization
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


        // SQLite Database initialization
        dbManager = new DBManager(this);
        dbManager.loadPerson();




        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        assert currentUser != null;
        String AuthUserId = currentUser.getUid();

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGetContent.launch("image/*");
            }
        });
        mGetContent=registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
               profileImageUri = result;
               profileImage.setImageURI(result);
            }
        });
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });
        gender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGenderSelectionDialog();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String Date = date.getText().toString();
                String Gender = gender.getText().toString().toLowerCase();
                String Name = name.getText().toString();
                String Username = username.getText().toString().toLowerCase().replaceAll(" ", "");

                if(profileImageUri == null || TextUtils.isEmpty(Date) || TextUtils.isEmpty(Gender) || TextUtils.isEmpty(Name) || TextUtils.isEmpty(Username)){

                    Toast.makeText(ProfileCreating.this, "All fields are empty!", Toast.LENGTH_SHORT).show();
                }
                else {
                    ProgressDialog pd = new ProgressDialog(ProfileCreating.this);
                    pd.setTitle("Profile creating");
                    pd.setCancelable(false);
                    pd.show();
                    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                    DatabaseReference databaseReference= firebaseDatabase.getReference();

                    databaseReference.child("Users").orderByChild("userName").equalTo(Username).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.getValue() != null) {
                                databaseReference.child("Users").child(AuthUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        String UserName = dataSnapshot.child("userName").getValue(String.class);
                                        if(Objects.equals(UserName,Username)){
                                            storage = FirebaseStorage.getInstance();
                                            StorageReference profileImagePath = storage.getReference().child(AuthUserId + "/ProfileImage").child(profileImageUri.getLastPathSegment());
                                            profileImagePath.putFile(profileImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                    profileImagePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                        @Override
                                                        public void onSuccess(Uri profileImageDownloadUri) {
                                                            new FirebaseImageLoader(new FirebaseImageLoader.OnImageLoadedListener() {
                                                                @Override
                                                                public void onImageLoaded(Bitmap bitmap) {
                                                                    if (bitmap != null) {
                                                                        try {
                                                                            // Perform face detection on the loaded image
                                                                            FaceDetectionParam faceDetectionParam = new FaceDetectionParam();
                                                                            faceDetectionParam.check_liveness = true;
                                                                            faceDetectionParam.check_liveness_level = SettingsActivity.getLivenessLevel(ProfileCreating.this);
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

                                                                                if (maxSimilarity > SettingsActivity.getIdentifyThreshold(ProfileCreating.this)) {
                                                                                    // If the similarity exceeds the threshold, show the recognition result
                                                                                    // You can customize this part according to your UI


                                                                                    //ProfileVerified
                                                                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                                                                    DatabaseReference databaseReference = database.getReference();
                                                                                    databaseReference.child("Users").child(AuthUserId).child("profileImage").setValue(profileImageDownloadUri.toString());
                                                                                    databaseReference.child("Users").child(AuthUserId).child("dateOfBirth").setValue(Date);
                                                                                    databaseReference.child("Users").child(AuthUserId).child("gender").setValue(Gender);
                                                                                    databaseReference.child("Users").child(AuthUserId).child("name").setValue(Name);
                                                                                    databaseReference.child("Users").child(AuthUserId).child("userName").setValue(Username);
                                                                                    databaseReference.child("Users").child(AuthUserId).child("profileVerified").setValue(true);
                                                                                    pd.dismiss();
                                                                                    Toast.makeText(ProfileCreating.this, "Profile created successfully!", Toast.LENGTH_SHORT).show();
                                                                                    startActivity(new Intent(ProfileCreating.this, Welcome.class));
                                                                                    finish();
                                                                                }
                                                                                else{
                                                                                   //Profile not verified
                                                                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                                                                    DatabaseReference databaseReference = database.getReference();
                                                                                    databaseReference.child("Users").child(AuthUserId).child("profileImage").setValue(profileImageDownloadUri.toString());
                                                                                    databaseReference.child("Users").child(AuthUserId).child("dateOfBirth").setValue(Date);
                                                                                    databaseReference.child("Users").child(AuthUserId).child("gender").setValue(Gender);
                                                                                    databaseReference.child("Users").child(AuthUserId).child("name").setValue(Name);
                                                                                    databaseReference.child("Users").child(AuthUserId).child("userName").setValue(Username);
                                                                                    databaseReference.child("Users").child(AuthUserId).child("profileVerified").setValue(false);
                                                                                    pd.dismiss();
                                                                                    Toast.makeText(ProfileCreating.this, "Profile created successfully!", Toast.LENGTH_SHORT).show();
                                                                                    startActivity(new Intent(ProfileCreating.this, Welcome.class));
                                                                                    finish();

                                                                                }
                                                                            }
                                                                            else{
                                                                                //Profile not verified
                                                                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                                                                DatabaseReference databaseReference = database.getReference();
                                                                                databaseReference.child("Users").child(AuthUserId).child("profileImage").setValue(profileImageDownloadUri.toString());
                                                                                databaseReference.child("Users").child(AuthUserId).child("dateOfBirth").setValue(Date);
                                                                                databaseReference.child("Users").child(AuthUserId).child("gender").setValue(Gender);
                                                                                databaseReference.child("Users").child(AuthUserId).child("name").setValue(Name);
                                                                                databaseReference.child("Users").child(AuthUserId).child("userName").setValue(Username);
                                                                                databaseReference.child("Users").child(AuthUserId).child("profileVerified").setValue(false);
                                                                                pd.dismiss();
                                                                                Toast.makeText(ProfileCreating.this, "Profile created successfully!", Toast.LENGTH_SHORT).show();
                                                                                startActivity(new Intent(ProfileCreating.this, Welcome.class));
                                                                                finish();

                                                                            }
                                                                        } catch (Exception e) {
                                                                            e.printStackTrace();
                                                                            Toast.makeText(ProfileCreating.this, "Error, please try again!", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }
                                                                }
                                                            }).execute(profileImageDownloadUri.toString());










                                                        }
                                                    });
                                                }
                                            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                                    float percent = (100 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                                                    pd.setMessage("Please wait: " + (int) percent + "%");
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    pd.dismiss();
                                                    Toast.makeText(ProfileCreating.this, "Error! Please try again", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                        else{
                                            pd.dismiss();
                                            Toast.makeText(ProfileCreating.this, "This username is not available!", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                            else {
                                storage = FirebaseStorage.getInstance();
                                StorageReference profileImagePath = storage.getReference().child(AuthUserId + "/ProfileImage").child(profileImageUri.getLastPathSegment());
                                profileImagePath.putFile(profileImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        profileImagePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri profileImageDownloadUri) {

                                                new FirebaseImageLoader(new FirebaseImageLoader.OnImageLoadedListener() {
                                                    @Override
                                                    public void onImageLoaded(Bitmap bitmap) {
                                                        if (bitmap != null) {
                                                            try {
                                                                // Perform face detection on the loaded image
                                                                FaceDetectionParam faceDetectionParam = new FaceDetectionParam();
                                                                faceDetectionParam.check_liveness = true;
                                                                faceDetectionParam.check_liveness_level = SettingsActivity.getLivenessLevel(ProfileCreating.this);
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

                                                                    if (maxSimilarity > SettingsActivity.getIdentifyThreshold(ProfileCreating.this)) {
                                                                        // If the similarity exceeds the threshold, show the recognition result
                                                                        // You can customize this part according to your UI


                                                                        //ProfileVerified
                                                                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                                                                        DatabaseReference databaseReference = database.getReference();
                                                                        databaseReference.child("Users").child(AuthUserId).child("profileImage").setValue(profileImageDownloadUri.toString());
                                                                        databaseReference.child("Users").child(AuthUserId).child("dateOfBirth").setValue(Date);
                                                                        databaseReference.child("Users").child(AuthUserId).child("gender").setValue(Gender);
                                                                        databaseReference.child("Users").child(AuthUserId).child("name").setValue(Name);
                                                                        databaseReference.child("Users").child(AuthUserId).child("userName").setValue(Username);
                                                                        databaseReference.child("Users").child(AuthUserId).child("profileVerified").setValue(true);
                                                                        pd.dismiss();
                                                                        Toast.makeText(ProfileCreating.this, "Profile created successfully!", Toast.LENGTH_SHORT).show();
                                                                        startActivity(new Intent(ProfileCreating.this, Welcome.class));
                                                                        finish();
                                                                    }
                                                                    else{
                                                                        //Profile not verified
                                                                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                                                                        DatabaseReference databaseReference = database.getReference();
                                                                        databaseReference.child("Users").child(AuthUserId).child("profileImage").setValue(profileImageDownloadUri.toString());
                                                                        databaseReference.child("Users").child(AuthUserId).child("dateOfBirth").setValue(Date);
                                                                        databaseReference.child("Users").child(AuthUserId).child("gender").setValue(Gender);
                                                                        databaseReference.child("Users").child(AuthUserId).child("name").setValue(Name);
                                                                        databaseReference.child("Users").child(AuthUserId).child("userName").setValue(Username);
                                                                        databaseReference.child("Users").child(AuthUserId).child("profileVerified").setValue(false);
                                                                        pd.dismiss();
                                                                        Toast.makeText(ProfileCreating.this, "Profile created successfully!", Toast.LENGTH_SHORT).show();
                                                                        startActivity(new Intent(ProfileCreating.this, Welcome.class));
                                                                        finish();

                                                                    }
                                                                }
                                                                else{
                                                                    //Profile not verified
                                                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                                                    DatabaseReference databaseReference = database.getReference();
                                                                    databaseReference.child("Users").child(AuthUserId).child("profileImage").setValue(profileImageDownloadUri.toString());
                                                                    databaseReference.child("Users").child(AuthUserId).child("dateOfBirth").setValue(Date);
                                                                    databaseReference.child("Users").child(AuthUserId).child("gender").setValue(Gender);
                                                                    databaseReference.child("Users").child(AuthUserId).child("name").setValue(Name);
                                                                    databaseReference.child("Users").child(AuthUserId).child("userName").setValue(Username);
                                                                    databaseReference.child("Users").child(AuthUserId).child("profileVerified").setValue(false);
                                                                    pd.dismiss();
                                                                    Toast.makeText(ProfileCreating.this, "Profile created successfully!", Toast.LENGTH_SHORT).show();
                                                                    startActivity(new Intent(ProfileCreating.this, Welcome.class));
                                                                    finish();

                                                                }
                                                            } catch (Exception e) {
                                                                e.printStackTrace();
                                                                Toast.makeText(ProfileCreating.this, "Error, please try again!", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    }
                                                }).execute(profileImageDownloadUri.toString());

                                            }
                                        });
                                    }
                                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                        float percent = (100 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                                        pd.setMessage("Please wait: " + (int) percent + "%");
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        pd.dismiss();
                                        Toast.makeText(ProfileCreating.this, "Error! Please try again", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                        @Override
                        public void onCancelled (@NonNull DatabaseError error){
                            Toast.makeText(ProfileCreating.this, "Error! Please try again..", Toast.LENGTH_SHORT).show();

                        }
                    });
                }

            }
        });

    }
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String getDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                        date.setText(getDate);
                    }
                },
                year,
                month,
                day
        );

        datePickerDialog.show();
    }
    private void showGenderSelectionDialog() {
        final String[] genders = {"Male", "Female", "Other"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Gender")
                .setItems(genders, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String selectedGender = genders[which];
                        gender.setText(selectedGender);
                    }
                });

        builder.show();
    }

}