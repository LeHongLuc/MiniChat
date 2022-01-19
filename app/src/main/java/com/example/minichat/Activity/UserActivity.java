package com.example.minichat.Activity;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.minichat.UserListener;
import com.example.minichat.adapters.UserAdapter;
import com.example.minichat.databinding.ActivityUserBinding;
import com.example.minichat.models.User;
import com.example.minichat.utilities.Constants;
import com.example.minichat.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserActivity extends BaseActivity implements UserListener {

    private ActivityUserBinding binding;
    private PreferenceManager preferenceManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());

        getUser();
        setListeners();


    }

    private void setListeners() {
        binding.imgBack.setOnClickListener(v -> onBackPressed());
    }

    private void getUser() {
        loading(true);
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<User> userList = new ArrayList<>();

                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if (currentUserId.equals(queryDocumentSnapshot.getId())) {
                                continue;
                            }
                            User user = new User();
                            user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                            user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                            user.id = queryDocumentSnapshot.getId();
                            userList.add(user);
                        }

                        if (userList.size() > 0) {
                            UserAdapter userAdapter = new UserAdapter(userList, this ,this);
                            binding.rvUser.setAdapter(userAdapter);
                        } else {
                            showMessage();
                        }
                    } else {
                        showMessage();
                    }
                });
    }

    private void showMessage() {
        binding.tvError.setText(String.format("%s", "No user available"));
        binding.tvError.setVisibility(View.VISIBLE);
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onUserClick(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER,user);
        startActivity(intent);
        finish();
    }
}