package com.example.myapplicationooo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private ImageView imgSun, cloud1, cloud2, cloud3;
    private LinearLayout logoContainer;
    private View dot1, dot2, dot3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        imgSun = findViewById(R.id.imgSun);
        cloud1 = findViewById(R.id.cloud1);
        cloud2 = findViewById(R.id.cloud2);
        cloud3 = findViewById(R.id.cloud3);
        logoContainer = findViewById(R.id.logoContainer);
        dot1 = findViewById(R.id.dot1);
        dot2 = findViewById(R.id.dot2);
        dot3 = findViewById(R.id.dot3);

        startAnimations();

        // Chuyển thẳng sang màn hình chính để hỗ trợ Guest Mode
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }, 3000);
    }

    private void startAnimations() {
        RotateAnimation rotate = new RotateAnimation(0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(10000);
        rotate.setRepeatCount(Animation.INFINITE);
        rotate.setInterpolator(new LinearInterpolator());
        if (imgSun != null) {
            imgSun.startAnimation(rotate);
        }

        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        fadeIn.setDuration(1500);
        if (logoContainer != null) {
            logoContainer.startAnimation(fadeIn);
        }

        if (cloud1 != null) animateCloud(cloud1, 20000, 30f);
        if (cloud2 != null) animateCloud(cloud2, 25000, -40f);
        if (cloud3 != null) animateCloud(cloud3, 18000, 50f);

        if (dot1 != null) animateDot(dot1, 0);
        if (dot2 != null) animateDot(dot2, 200);
        if (dot3 != null) animateDot(dot3, 400);
    }

    private void animateCloud(View cloud, int duration, float distance) {
        TranslateAnimation move = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
                Animation.ABSOLUTE, 0, Animation.ABSOLUTE, distance);
        move.setDuration(duration / 4);
        move.setRepeatMode(Animation.REVERSE);
        move.setRepeatCount(Animation.INFINITE);
        cloud.startAnimation(move);
    }

    private void animateDot(View dot, int delay) {
        Animation anim = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        anim.setDuration(600);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        anim.setStartOffset(delay);
        dot.startAnimation(anim);
    }
}
