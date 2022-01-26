package com.example.minichat.Activity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.method.PasswordTransformationMethod;
import android.transition.Fade;

import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.minichat.R;
import com.example.minichat.databinding.ActivityChangePasswordBinding;
import com.example.minichat.utilities.Constants;
import com.example.minichat.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;


public class ChangePasswordActivity extends AppCompatActivity {

    private ActivityChangePasswordBinding binding;
    AlertDialog alertDialog;
    FirebaseFirestore db;
PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        db = FirebaseFirestore.getInstance();


        setListeners();
    }

    private void setListeners() {
        binding.imgBack.setOnClickListener(v -> {
            onBackPressed();
        });
        binding.checkboxHienMK.setOnClickListener(v -> {
            if (binding.checkboxHienMK.isChecked()) {
                binding.edtoldPass.setTransformationMethod(null);
                binding.edtnewPass.setTransformationMethod(null);
                binding.edtconfirmPass.setTransformationMethod(null);

            } else{
                binding.edtoldPass.setTransformationMethod(new PasswordTransformationMethod());
            binding.edtnewPass.setTransformationMethod(new PasswordTransformationMethod());
            binding.edtconfirmPass.setTransformationMethod(new PasswordTransformationMethod());}
        });

        binding.layoutChangePass.setOnClickListener(v -> {
            View view1 = getCurrentFocus();
            if (view1 != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view1.getWindowToken(), 0);
            }
        });
        binding.tvOk.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("ĐỔI MẬT KHẨU..!!!");
            builder.setIcon(R.drawable.ic_fail_connected);
            builder.setMessage("Bạn đã chắc chắn muốn đổi mật khẩu !!!");
            builder.setCancelable(false);
            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (validate()){
                        db.collection(Constants.KEY_COLLECTION_USERS).document(Constants.KEY_USER_ID).update(Constants.KEY_PASSWORD, binding.edtnewPass.toString());
                        preferenceManager.clear();
                        Intent intent= new Intent(getApplicationContext(),SignInActivity.class);
                        startActivity(intent);
                    }
                }
            });

            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));

                }
            });
            alertDialog = builder.create();
            alertDialog.show();
        });
        binding.tvReset.setOnClickListener(v -> {
            binding.edtconfirmPass.setText("");
            binding.edtnewPass.setText("");
            binding.edtoldPass.setText("");

        });
    }

    private boolean validate(){
        if (binding.edtnewPass.equals("")||binding.edtoldPass.equals("")||binding.edtconfirmPass.equals("")){
            Toast.makeText(getApplicationContext(), "phải nhập đủ thông tin", Toast.LENGTH_SHORT).show();
            return false;
        } else if(binding.edtoldPass.equals(preferenceManager.getString(Constants.KEY_PASSWORD))){
            Toast.makeText(getApplicationContext(), "mật khẩu không đúng", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!binding.edtoldPass.equals(binding.edtconfirmPass)){
            Toast.makeText(getApplicationContext(), "kiểm tra lại password mới", Toast.LENGTH_SHORT).show();
            return false;
        } else return true;
    }


}