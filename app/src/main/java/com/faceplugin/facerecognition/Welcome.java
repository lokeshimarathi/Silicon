package com.faceplugin.facerecognition;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.faceplugin.facerecognition.world.WorldHome;

public class Welcome extends AppCompatActivity {
LinearLayout welcomeText;
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
        setContentView(R.layout.activity_welcome);
          welcomeText = findViewById(R.id.welcomeText);
          welcomeText.startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.fade_in));
          new Handler().postDelayed(new Runnable() {
              @Override
              public void run() {
                  startActivity(new Intent(getApplicationContext(), WorldHome.class));
                  finish();
              }
          },3000);
    }
}