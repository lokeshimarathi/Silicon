package com.faceplugin.facerecognition.world.profile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.faceplugin.facerecognition.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import java.util.Objects;
import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends AppCompatActivity {
    CircleImageView profileImage,verifiedMark;
    TextView name, username,date, gender;
    Button verifyButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(1);
        // for full screen activity
        supportRequestWindowFeature(1);
        getWindow().setFlags (WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        //for full screen activity and transparent status bar
        setContentView(R.layout.activity_profile);
        username = findViewById(R.id.userName);
        name = findViewById(R.id.name);
        verifyButton = findViewById(R.id.verifyButton);
        profileImage = findViewById(R.id.profileImage);
        date = findViewById(R.id.dateOfBirth);
        gender = findViewById(R.id.gender);
        verifiedMark = findViewById(R.id.verifiedMark);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        String AuthUserId = user.getUid();
        String UsersAuthUserId = getIntent().getStringExtra("usersAuthUserId");
        ProgressDialog pd = new ProgressDialog(Profile.this);
        pd.setMessage("Please wait");
        pd.setCancelable(false);
        pd.show();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference();
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String Name = snapshot.child("Users").child(UsersAuthUserId).child("name").getValue(String.class);
                name.setText(Name);
                String ProfileImage = snapshot.child("Users").child(UsersAuthUserId).child("profileImage").getValue(String.class);
                if (ProfileImage!=null){
                    Uri profileImageUri = Uri.parse(ProfileImage);
                    Picasso.get().load(profileImageUri).into(profileImage);
                }
                else {
                    profileImage.setImageResource(R.drawable.faceemoji);
                }
                String Username = snapshot.child("Users").child(UsersAuthUserId).child("userName").getValue(String.class);
                username.setText(Username);
                String DateOfBirth = snapshot.child("Users").child(UsersAuthUserId).child("dateOfBirth").getValue(String.class);
                date .setText(DateOfBirth);
                String Gender = snapshot.child("Users").child(UsersAuthUserId).child("gender").getValue(String.class);
                gender.setText(Gender);
                boolean ProfileVerified = snapshot.child("Users").child(UsersAuthUserId).child("profileVerified").getValue(Boolean.class);
                if(ProfileVerified){
                    verifiedMark.setVisibility(View.VISIBLE);
                }
                else{
                    verifiedMark.setVisibility(View.GONE);
                }
                pd.dismiss();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                pd.dismiss();
            }
        });
     verifyButton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        Intent intent = new Intent(Profile.this, ProfileVerify.class);
        intent.putExtra("userAuthUserId",UsersAuthUserId );
        startActivity(intent);
    }
});
    }
}