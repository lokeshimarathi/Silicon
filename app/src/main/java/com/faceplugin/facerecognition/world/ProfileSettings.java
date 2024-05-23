package com.faceplugin.facerecognition.world;

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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.faceplugin.facerecognition.DBManager;
import com.faceplugin.facerecognition.FirebaseImageLoader;
import com.faceplugin.facerecognition.Person;
import com.faceplugin.facerecognition.PersonAdapter;
import com.faceplugin.facerecognition.ProfileCreating;
import com.faceplugin.facerecognition.R;
import com.faceplugin.facerecognition.SettingsActivity;
import com.faceplugin.facerecognition.SplashScreen;
import com.faceplugin.facerecognition.Welcome;
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
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class ProfileSettings extends AppCompatActivity {
    TextView usernameError, email, date, gender;
    EditText username, name;
    Button saveButton,signOutButton;
    CircularImageView profileImage;
    ActivityResultLauncher<String> mGetContent;
    FirebaseStorage storage;
    ImageView verifiedMark;
    static Uri newProfileImageUri, oldProfileImageUri;

    private DBManager dbManager;
    private TextView textWarning;
    private TextView textEnrolledFace;
    private PersonAdapter personAdapter;
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
        setContentView(R.layout.activity_profile_settings);
        usernameError =findViewById(R.id.usernameError);
        email = findViewById(R.id.email);
        username = findViewById(R.id.username);
        name = findViewById(R.id.name);
        saveButton = findViewById(R.id.saveButton);
        profileImage = findViewById(R.id.profileImage);
        date = findViewById(R.id.date);
        gender = findViewById(R.id.gender);
        signOutButton = findViewById(R.id.signOutButton);
        verifiedMark = findViewById(R.id.verifiedMark);


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
        ProgressDialog pd = new ProgressDialog(ProfileSettings.this);
        pd.setMessage("Please wait");
        pd.setCancelable(false);
        pd.show();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference();
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String Name = snapshot.child("Users").child(AuthUserId).child("name").getValue(String.class);
                name.setText(Name);
                String ProfileImage = snapshot.child("Users").child(AuthUserId).child("profileImage").getValue(String.class);
                if (ProfileImage!=null){
                    Uri profileImageUri = Uri.parse(ProfileImage);
                    Picasso.get().load(profileImageUri).into(profileImage);
                    oldProfileImageUri = profileImageUri;
                }
                else {
                    profileImage.setImageResource(R.drawable.faceemoji);

                }
                String Username = snapshot.child("Users").child(AuthUserId).child("userName").getValue(String.class);
                username.setText(Username);

                String Email = snapshot.child("Users").child(AuthUserId).child("email").getValue(String.class);
                email.setText(Email);
                String DateOfBirth = snapshot.child("Users").child(AuthUserId).child("dateOfBirth").getValue(String.class);
                date .setText(DateOfBirth);
                String Gender = snapshot.child("Users").child(AuthUserId).child("gender").getValue(String.class);
                gender.setText(Gender);
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                pd.dismiss();
                Toast.makeText(ProfileSettings.this, "Please try again", Toast.LENGTH_SHORT).show();
            }
        });
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGetContent.launch("image/*");
            }
        });
        mGetContent=registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                Picasso.get().load(result).into(profileImage);
                newProfileImageUri = result;
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
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                mAuth.signOut();
                startActivity(new Intent(getApplicationContext(), SplashScreen.class));
                finish();
            }
        });
 saveButton.setOnClickListener(new View.OnClickListener() {
     @Override
     public void onClick(View v) {
         usernameError.setVisibility(View.INVISIBLE);
         String UserName = username.getText().toString().toLowerCase().replaceAll(" ", "");
         String Name = name.getText().toString();
         String Date = date.getText().toString();
         String Gender = gender.getText().toString().toLowerCase();
         if(TextUtils.isEmpty(UserName)){
             Toast.makeText(ProfileSettings.this, "Please fill Username", Toast.LENGTH_SHORT).show();

         }
         if(TextUtils.isEmpty(Name)){
             Toast.makeText(ProfileSettings.this, "Please fill Name", Toast.LENGTH_SHORT).show();

         }
        else{
             FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
             DatabaseReference databaseReference = firebaseDatabase.getReference();
             databaseReference.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                 @Override
                 public void onDataChange(@NonNull DataSnapshot snapshot) {
                     if (snapshot.exists()) {
                         for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                             String userKey = userSnapshot.getKey();
                             // Do something with the user key
                             if(!Objects.equals(userKey, AuthUserId)){
                                 assert userKey != null;
                                 databaseReference.child("Users").child(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
                                     @Override
                                     public void onDataChange(@NonNull DataSnapshot snapshot) {
                                         String username = snapshot.child("userName").getValue(String.class);
                                         if (UserName.equals(username)) {
                                             usernameError.setVisibility(View.VISIBLE);
                                         }
                                         else {
                                             usernameError.setVisibility(View.INVISIBLE);
                                             ProgressDialog pd = new ProgressDialog(ProfileSettings.this);
                                             pd.setTitle("Profile updating");
                                             pd.setCancelable(false);
                                             pd.show();
                                             storage = FirebaseStorage.getInstance();
                                             if(newProfileImageUri == null){
                                                 FirebaseDatabase database = FirebaseDatabase.getInstance();
                                                 DatabaseReference databaseReference = database.getReference();
                                                 databaseReference.child("Users").child(AuthUserId).child("dateOfBirth").setValue(Date);
                                                 databaseReference.child("Users").child(AuthUserId).child("gender").setValue(Gender);
                                                 databaseReference.child("Users").child(AuthUserId).child("name").setValue(Name);
                                                 databaseReference.child("Users").child(AuthUserId).child("userName").setValue(UserName);
                                                 pd.dismiss();
                                                 Toast.makeText(ProfileSettings.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                                             }
                                             else{
                                                 StorageReference profileImagePath = storage.getReference().child(AuthUserId + "/ProfileImage").child(newProfileImageUri.getLastPathSegment());
                                                 profileImagePath.putFile(newProfileImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
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
                                                                                 faceDetectionParam.check_liveness_level = SettingsActivity.getLivenessLevel(ProfileSettings.this);
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

                                                                                     if (maxSimilarity > SettingsActivity.getIdentifyThreshold(ProfileSettings.this)) {
                                                                                         //ProfileVerified
                                                                                         FirebaseDatabase database = FirebaseDatabase.getInstance();
                                                                                         DatabaseReference databaseReference = database.getReference();
                                                                                         databaseReference.child("Users").child(AuthUserId).child("profileImage").setValue(profileImageDownloadUri.toString());
                                                                                         databaseReference.child("Users").child(AuthUserId).child("dateOfBirth").setValue(Date);
                                                                                         databaseReference.child("Users").child(AuthUserId).child("gender").setValue(Gender);
                                                                                         databaseReference.child("Users").child(AuthUserId).child("name").setValue(Name);
                                                                                         databaseReference.child("Users").child(AuthUserId).child("userName").setValue(UserName);
                                                                                         databaseReference.child("Users").child(AuthUserId).child("profileVerified").setValue(true);
                                                                                         pd.dismiss();
                                                                                         Toast.makeText(ProfileSettings.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                                                                                     }
                                                                                     else{
                                                                                         //Profile not verified
                                                                                         FirebaseDatabase database = FirebaseDatabase.getInstance();
                                                                                         DatabaseReference databaseReference = database.getReference();
                                                                                         databaseReference.child("Users").child(AuthUserId).child("profileImage").setValue(profileImageDownloadUri.toString());
                                                                                         databaseReference.child("Users").child(AuthUserId).child("dateOfBirth").setValue(Date);
                                                                                         databaseReference.child("Users").child(AuthUserId).child("gender").setValue(Gender);
                                                                                         databaseReference.child("Users").child(AuthUserId).child("name").setValue(Name);
                                                                                         databaseReference.child("Users").child(AuthUserId).child("userName").setValue(UserName);
                                                                                         databaseReference.child("Users").child(AuthUserId).child("profileVerified").setValue(false);
                                                                                         pd.dismiss();
                                                                                         Toast.makeText(ProfileSettings.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();

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
                                                                                     databaseReference.child("Users").child(AuthUserId).child("userName").setValue(UserName);
                                                                                     databaseReference.child("Users").child(AuthUserId).child("profileVerified").setValue(false);
                                                                                     pd.dismiss();
                                                                                     Toast.makeText(ProfileSettings.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();

                                                                                 }
                                                                             } catch (Exception e) {
                                                                                 e.printStackTrace();
                                                                                 Toast.makeText(ProfileSettings.this, "Error, please try again!", Toast.LENGTH_SHORT).show();
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
                                                         Toast.makeText(ProfileSettings.this, ""+e, Toast.LENGTH_SHORT).show();
                                                     }
                                                 });

                                             }

                                         }
                                     }
                                     @Override
                                     public void onCancelled (@NonNull DatabaseError error){
                                         pd.dismiss();
                                         Toast.makeText(ProfileSettings.this, "Error! Please try again..", Toast.LENGTH_SHORT).show();

                                     }
                                 });
                             }
                         }
                     }

                 }

                 @Override
                 public void onCancelled(@NonNull DatabaseError error) {
                     Toast.makeText(ProfileSettings.this, "Error! Please try again..", Toast.LENGTH_SHORT).show();

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