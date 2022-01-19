package com.example.minichat.Activity;

import androidx.appcompat.app.AppCompatActivity;



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
    private PreferenceManager preferenceManager;
    FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        //chuyển activity(nhận 1 dữ liệu)
        Fade fade = new Fade();
        View decor = getWindow().getDecorView();
        fade.excludeChildren(decor.findViewById(R.id.action_bar_container), true);
        fade.excludeTarget(android.R.id.statusBarBackground, true);
        fade.excludeTarget(android.R.id.navigationBarBackground, true);
        getWindow().setEnterTransition(fade);
        getWindow().setExitTransition(fade);



        preferenceManager = new PreferenceManager(getApplicationContext());
        firebaseFirestore = FirebaseFirestore.getInstance();
        binding.tvName.setText(preferenceManager.getString(Constants.KEY_NAME));

        loadDetails();
        setListeners();

    }

    private void setListeners() {
        binding.imgAvt.setOnClickListener(v -> {
            onBackPressed();
        });
    }

    private void loadDetails() {

        binding.tvName.setText(preferenceManager.getString(Constants.KEY_NAME));

        Glide.with(this).load(preferenceManager.getString(Constants.KEY_IMAGE)).into(binding.imgAvt);
    }

}