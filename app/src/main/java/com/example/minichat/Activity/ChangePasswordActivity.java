package com.example.minichat.Activity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;


import android.os.Build;
import android.os.Bundle;
import android.transition.Fade;

import android.view.View;

import com.bumptech.glide.Glide;
import com.example.minichat.R;
import com.example.minichat.databinding.ActivityChangePasswordBinding;
import com.example.minichat.utilities.Constants;
import com.example.minichat.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;


public class ChangePasswordActivity extends AppCompatActivity {

    private ActivityChangePasswordBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



    }
    private void setListeners(){
        
    }


}