package com.example.minichat.Activity;


import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.minichat.R;
import com.example.minichat.databinding.ActivitySignUpBinding;
import com.example.minichat.utilities.Constants;
import com.example.minichat.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;

import java.util.HashMap;
import java.util.List;

import gun0912.tedbottompicker.TedBottomPicker;
import gun0912.tedbottompicker.TedBottomSheetDialogFragment;


public class SignUpActivity extends AppCompatActivity {
    Uri Imageuri;
    LinearLayout LayoutSignUp;
    ActivitySignUpBinding binding;
    String encodedImage;
    PreferenceManager preferenceManager;

    FirebaseStorage storage;
    StorageReference storageReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

      
        setListeners();
        preferenceManager = new PreferenceManager(getApplicationContext());
        LayoutSignUp = findViewById(R.id.LayoutSignUp);
        LayoutSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view1 = getCurrentFocus();
                if (view1 != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view1.getWindowToken(), 0);
                }
            }
        });
    }

    private final int PICK_IMAGE_REQUEST = 71;
    private Uri filePath;
    private void setListeners() {
        binding.tvSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                Toast.makeText(getApplicationContext(),"Ấdas",Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValiSignUpDetails()) {
                    signUp(Imageuri);
                }
            }
        });

        binding.layoutImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestPermissions();

            }
        });
    }

    // xin quyền truy cập máy ảnh và file để lấy ảnh
    private void RequestPermissions() {
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                //nếu đã cấp quyền thì truy cập vào bộ nhớ để lấy ảnh
                reQuestPermission();
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(SignUpActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }

        };
        TedPermission.create()
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
                .check();
    }

// truy cập bộ nhớ
    private void reQuestPermission() {
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                //chọn ảnh
                openImagePicker();
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(SignUpActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }
        };
        TedPermission.create()
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check();
    }

    //chọn ảnh và set vào img trên giao diện
    private void openImagePicker() {
        TedBottomPicker.with(SignUpActivity.this)
                .show(new TedBottomSheetDialogFragment.OnImageSelectedListener() {
                    @Override
                    public void onImageSelected(Uri uri) {
                        // here is selected image uri
                        binding.imgProfile.setImageURI(uri);
                        Imageuri=uri;
                    }
                });

    }



    private void signUp(Uri url) {
        loading(true);
        FirebaseStorage storage=FirebaseStorage.getInstance();
        StorageReference storageReference=storage.getReference().child("Avatar");
        storageReference.putFile(Imageuri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                Uri dowloaduri=uriTask.getResult();
                FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
                HashMap<String, Object> user = new HashMap<>();
                user.put(Constants.KEY_NAME, binding.edtSignUpName.getText().toString().trim());
                user.put(Constants.KEY_EMAIL, binding.edtSignUpEmail.getText().toString().trim());
                user.put(Constants.KEY_PASSWORD, binding.edtSignUpPassword.getText().toString().trim());
                user.put(Constants.KEY_IMAGE, dowloaduri.toString());

                firebaseFirestore.collection(Constants.KEY_COLLECTION_USERS)
                        .add(user)
                        .addOnSuccessListener(documentReference -> {
                            loading(false);
                            preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                            preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
                            preferenceManager.putString(Constants.KEY_IMAGE, dowloaduri.toString());
                            preferenceManager.putString(Constants.KEY_NAME, binding.edtSignUpName.getText().toString());

                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        })
                        .addOnFailureListener(e -> {
                            loading(false);
                            showToast(e.getMessage());
                        });
            }
        });

    }



    private void showToast(String toast) {
        Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_SHORT).show();
    }

    private Boolean isValiSignUpDetails() {
        if (Imageuri == null) {
            showToast("Select profile image");
            return false;
        } else if (binding.edtSignUpName.getText().toString().trim().isEmpty()) {
            showToast("Enter name");
            return false;
        } else if (binding.edtSignUpEmail.getText().toString().trim().isEmpty()) {
            showToast("Enter email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.edtSignUpEmail.getText().toString()).matches()) {
            showToast("Enter valid email");
            return false;
        } else if (binding.edtSignUpPassword.getText().toString().trim().isEmpty()) {
            showToast("Enter password");
            return false;
        } else if (binding.edtSignUpConfirmPassword.getText().toString().trim().isEmpty()) {
            showToast("Enter confirm password");
            return false;
        } else if (!binding.edtSignUpPassword.getText().toString().equals(binding.edtSignUpConfirmPassword.getText().toString())) {
            showToast("Password and confirm password must be same");
            return false;
        } else {
            return true;
        }
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.btnSignUp.setVisibility(View.INVISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.btnSignUp.setVisibility(View.VISIBLE);
        }
    }

}