package com.faceplugin.facerecognition;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.faceplugin.facerecognition.world.WorldHome;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashScreen extends AppCompatActivity {
    LinearLayout logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // for full screen activity
        supportRequestWindowFeature(1);
        getWindow().setFlags (WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        //for full screen activity and transparent status bar
        getWindow().setStatusBarColor(Color.WHITE);
        //for full screen activity and visible status bar icon
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_splash_screen);
       FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
         FirebaseApp.initializeApp(/*context=*/ this);



        setContentView(R.layout.activity_splash_screen);
        logo = findViewById(R.id.logo);
        logo.startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.fade_in));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(currentUser!= null){
                    startActivity(new Intent(SplashScreen.this, Welcome.class));
                   finish();
                }

               else{
                    Intent i=new Intent(SplashScreen.this, SignUp.class);
                    startActivity(i);
                    finish();

                }
            }
        }, 3000);


    }
}