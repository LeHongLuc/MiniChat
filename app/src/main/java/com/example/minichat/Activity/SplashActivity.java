package com.example.minichat.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AnimationUtils;

import com.example.minichat.R;
import com.example.minichat.databinding.ActivityMainBinding;
import com.example.minichat.databinding.ActivitySplashBinding;

public class SplashActivity extends AppCompatActivity {

    ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.imgSplash.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom));
        binding.tvName.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.moving_splash));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, SignInActivity.class);
                startActivity(intent);
                finish();
            }
        }, 3000);
    }
}