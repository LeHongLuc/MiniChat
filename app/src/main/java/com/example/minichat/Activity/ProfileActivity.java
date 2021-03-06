package com.example.minichat.Activity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.transition.Fade;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.example.minichat.R;
import com.example.minichat.databinding.ActivityChangePasswordBinding;
import com.example.minichat.databinding.ActivityProfileBinding;
import com.example.minichat.utilities.Constants;
import com.example.minichat.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private PreferenceManager preferenceManager;
    ActivityProfileBinding binding;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //chuyển activity(nhận 1 dữ liệu)
        Fade fade = new Fade();
        View decor = getWindow().getDecorView();
        fade.excludeChildren(decor.findViewById(R.id.action_bar_container), true);
        fade.excludeTarget(android.R.id.statusBarBackground, true);
        fade.excludeTarget(android.R.id.navigationBarBackground, true);
        getWindow().setEnterTransition(fade);
        getWindow().setExitTransition(fade);
        preferenceManager = new PreferenceManager(getApplicationContext());

        loadDetails();
        setListeners();

    }

    private void setListeners() {
        binding.imgBack.setOnClickListener(v -> {
            onBackPressed();
        });
        binding.tvChangeProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(ProfileActivity.this, ChangeProfileActivity.class);
                startActivity(intent);
            }
        });
        binding.tvChangePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(ProfileActivity.this, ChangePasswordActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loadDetails() {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, preferenceManager.getString(Constants.KEY_EMAIL))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        Log.e("user ID:" ,documentSnapshot.getString(Constants.KEY_NAME));
                        Glide.with(this).load(documentSnapshot.getString(Constants.KEY_IMAGE)).into(binding.imgAvt);
                        binding.tvName.setText(documentSnapshot.getString(Constants.KEY_NAME));
                        binding.tvEmail.setText(documentSnapshot.getString(Constants.KEY_EMAIL));
                    }
                });
        profileAnimation();
    }

    private void profileAnimation() {
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.right_to_left);
        Animation animation1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.right_to_left);
        Animation animation3 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.right_to_left);
        Animation animation4 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.left_to_right);

        binding.layoutEmail.setVisibility(View.VISIBLE);
        binding.layoutName.setVisibility(View.VISIBLE);
        binding.tvChangeProfile.setVisibility(View.VISIBLE);
        binding.tvChangePass.setVisibility(View.VISIBLE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                binding.layoutEmail.setVisibility(View.VISIBLE);
                binding.layoutEmail.startAnimation(animation);
            }
        }, 300);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                binding.layoutName.setVisibility(View.VISIBLE);
                binding.layoutName.startAnimation(animation1);
            }
        }, 600);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                binding.tvChangeProfile.setVisibility(View.VISIBLE);
                binding.tvChangePass.setVisibility(View.VISIBLE);
                binding.tvChangeProfile.startAnimation(animation3);
                binding.tvChangePass.startAnimation(animation4);
            }
        }, 900);
    }
}