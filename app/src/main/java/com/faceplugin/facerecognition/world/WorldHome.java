package com.faceplugin.facerecognition.world;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

import com.faceplugin.facerecognition.CaptureActivity;
import com.faceplugin.facerecognition.R;
import com.faceplugin.facerecognition.SignUp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import me.ibrahimsn.lib.OnItemSelectedListener;
import me.ibrahimsn.lib.SmoothBottomBar;

public class WorldHome extends AppCompatActivity {
    SmoothBottomBar bottomBar;
    FrameLayout container;
    CircleImageView profileImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(1);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        window.setNavigationBarColor(ContextCompat.getColor(this, R.color.themeblue));
        //for full screen activity and visible status bar icon
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_world_home);
        container = findViewById(R.id.container);
        profileImage = findViewById(R.id.profileImage);
        ProgressDialog pd = new ProgressDialog(WorldHome.this);
        pd.setMessage("Please wait");
        pd.setCancelable(false);
        pd.show();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, new Home());
        transaction.commit();
        FirebaseAuth mAuth =  FirebaseAuth.getInstance();
        FirebaseUser currentUser= mAuth.getCurrentUser();
        assert currentUser != null;
        String AuthUserId = currentUser.getUid();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference();
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String ProfileImage = snapshot.child("Users").child(AuthUserId).child("profileImage").getValue(String.class);
                String UserName = snapshot.child("Users").child(AuthUserId).child("userName").getValue(String.class);
              if(UserName == null){
                  mAuth.signOut();
                  startActivity(new Intent(getApplicationContext(), SignUp.class));
                  finish();
              }
              else {
                  if (ProfileImage != null ){
                      Uri profileImageUri = Uri.parse(ProfileImage);
                      Picasso.get().load(profileImageUri).into(profileImage);
                  }

              }
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                pd.dismiss();
                Toast.makeText(WorldHome.this, "Please try again", Toast.LENGTH_SHORT).show();
            }
        });
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ProfileSettings.class));
            }
        });
        bottomBar = findViewById(R.id.bottomBar);

        bottomBar.setOnItemSelectedListener(new OnItemSelectedListener() {
            @SuppressLint("CommitTransaction")
            @Override
            public boolean onItemSelect(int i) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                switch (i) {
                    case 0:
                        transaction.replace(R.id.container, new Home());
                        break;

                    case 1:
                        transaction.replace(R.id.container, new Upload());
                        break;
                }
                transaction.commit(); // Commit the transaction after replacing the fragment
                return false;
            }
        });


    }
}