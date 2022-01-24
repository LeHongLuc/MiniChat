package com.example.minichat.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.example.minichat.R;
import com.example.minichat.databinding.ActivitySignInBinding;

import com.example.minichat.utilities.Constants;
import com.example.minichat.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;
    PreferenceManager preferenceManager;
    AlertDialog alertDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());

        //tự động đăng nhập nếu đã lưu tài khoản
        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }

        //kiểm tra mạng
        if (checkConnect() == false) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Lỗi mạng..!!!");
            builder.setIcon(R.drawable.ic_fail_connected);
            builder.setMessage("Kiểm tra lại đường truyền");
            builder.setCancelable(false);
            builder.setPositiveButton("OUT", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            });

            builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                                }
                            });
            alertDialog = builder.create();
            alertDialog.show();
        }

        setListeners();
        savePass();

    }


    //Toast and vali
    private void showToast(String toast) {
        Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_SHORT).show();
    }

    private Boolean isValiSignUpDetails() {
        if (binding.edtSignInUserEmail.getText().toString().trim().isEmpty()) {
            showToast("Enter email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.edtSignInUserEmail.getText().toString()).matches()) {
            showToast("Enter valid email");
            return false;
        } else if (binding.edtSignInPassword.getText().toString().trim().isEmpty()) {
            showToast("Enter password");
            return false;
        } else {
            return true;
        }
    }


    //kiểm tra kết nối mạng
    private boolean checkConnect() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        } else
            return false;
    }

    //checkbox lưu mật khẩu
    public void savePass() {
        binding.LayoutLogIn.setOnClickListener(v -> {
            View view1 = getCurrentFocus();
            if (view1 != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view1.getWindowToken(), 0);
            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences("USER", MODE_PRIVATE);
        binding.edtSignInUserEmail.setText(sharedPreferences.getString("USERNAME", ""));
        binding.edtSignInPassword.setText(sharedPreferences.getString("PASSWORD", ""));
        Boolean check = sharedPreferences.getBoolean("REMEMBER", false);

        binding.checkboxActLoginLuuMK.setChecked(check);
        binding.checkboxActLoginLuuMK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.checkboxActLoginLuuMK.isChecked()) {
                    rememberUser(binding.edtSignInUserEmail.getText().toString(), binding.edtSignInPassword.getText().toString(), true);
                } else {
                    rememberUser(binding.edtSignInUserEmail.getText().toString(), binding.edtSignInPassword.getText().toString(), false);
                }
            }
        });
    }

    public void rememberUser(String username, String password, boolean status) {
        SharedPreferences sharedPreferences = getSharedPreferences("USER", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (status == false) {
            editor.clear();
        } else {
            editor.putString("USERNAME", username);
            editor.putString("PASSWORD", password);
            editor.putBoolean("REMEMBER", status);
        }
        editor.commit();
    }


    // gọi các nút bấm
    private void setListeners() {
        binding.tvCreateNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
            }
        });

        binding.btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //kiểm tra kết nối mạng
                if (checkConnect()){
                    if (isValiSignUpDetails()) {
                        signIn();
                    }}
                else showToast("Không có Internet !!!");

            }
        });
    }

    // đăng nhập và lấy thông tin user
    private void signIn() {
        loading(true);
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, binding.edtSignInUserEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD, binding.edtSignInPassword.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.clear();
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                        preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                        preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                        preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
                        preferenceManager.putString(Constants.KEY_EMAIL, documentSnapshot.getString(Constants.KEY_EMAIL));
                        preferenceManager.putString(Constants.KEY_PHONE, documentSnapshot.getString(Constants.KEY_PHONE));
                        preferenceManager.putString(Constants.KEY_PASSWORD, documentSnapshot.getString(Constants.KEY_PASSWORD));

                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        loading(false);
                        showToast("Unable to sign in");
                    }
                });

    }


    // set nút loading (progressBar)
    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.btnSignIn.setVisibility(View.INVISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.btnSignIn.setVisibility(View.VISIBLE);
        }
    }

}