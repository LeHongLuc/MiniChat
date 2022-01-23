package com.example.minichat.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.minichat.R;
import com.example.minichat.databinding.ActivityChangeProfileBinding;
import com.example.minichat.databinding.ActivityProfileBinding;
import com.example.minichat.utilities.Constants;
import com.example.minichat.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
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

public class ChangeProfileActivity extends AppCompatActivity {

    private PreferenceManager preferenceManager;
    FirebaseFirestore firebaseFirestore;
    ActivityChangeProfileBinding binding;
    Uri Imageuri;
    AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_profile);
        binding = ActivityChangeProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());

        loadDetails();
        setListeners();
    }

    private void loadDetails() {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, preferenceManager.getString(Constants.KEY_EMAIL))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        Glide.with(this).load(documentSnapshot.getString(Constants.KEY_IMAGE)).into(binding.imgAvt);
                        binding.edtName.setText(documentSnapshot.getString(Constants.KEY_NAME));
                        binding.edtEmail.setText(documentSnapshot.getString(Constants.KEY_EMAIL));
                        binding.edtEmail.setEnabled(false);
                    }
                });
    }

    private void setListeners() {
        binding.imgBack.setOnClickListener(v -> {
            onBackPressed();
        });
        binding.imgAvt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestPermissions();
            }
        });
        binding.tvReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.edtName.setText(preferenceManager.getString(Constants.KEY_NAME));
                binding.edtEmail.setText(preferenceManager.getString(Constants.KEY_EMAIL));
                Glide.with(getApplicationContext()).load(preferenceManager.getString(Constants.KEY_IMAGE)).into(binding.imgAvt);
            }
        });
        binding.tvOk.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("SỬA THÔNG TIN");
            builder.setMessage("Bạn có chắc muốn sửa các thông tin trên ???");
            builder.setCancelable(false);
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    confirm();

                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alertDialog = builder.create();
            alertDialog.show();
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
                Toast.makeText(ChangeProfileActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(ChangeProfileActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }
        };
        TedPermission.create()
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check();
    }

    // CHọn ảnh và set vào giao diện
    private void openImagePicker() {
        TedBottomPicker.with(ChangeProfileActivity.this)
                .show(new TedBottomSheetDialogFragment.OnImageSelectedListener() {
                    @Override
                    public void onImageSelected(Uri uri) {
                        // here is selected image uri
                        binding.imgAvt.setImageURI(uri);
                        Imageuri = uri;
                    }
                });

    }

    // xác nhận muốn thay đổi thông tin
    private void confirm() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();

        StorageReference storageRef = storage.getReference();
        storageRef.child(Constants.KEY_IMG_USER).child(binding.edtEmail.getText().toString()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                StorageReference storageReference = storage.getReference().child(Constants.KEY_IMG_USER)
                        .child(binding.edtEmail.getText().toString());
                storageReference.putFile(Imageuri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        if (preferenceManager.getString(Constants.KEY_IMAGE) == null || preferenceManager.getString(Constants.KEY_IMAGE).equals("")) {
                            Uri dowloaduri=uriTask.getResult();
                            db.collection(Constants.KEY_COLLECTION_USERS).document(Constants.KEY_USER_ID).update(Constants.KEY_IMAGE, dowloaduri.toString());
                            db.collection(Constants.KEY_COLLECTION_USERS).document(Constants.KEY_USER_ID).update(Constants.KEY_EMAIL, binding.edtEmail.getText().toString());
                            db.collection(Constants.KEY_COLLECTION_USERS).document(Constants.KEY_USER_ID).update(Constants.KEY_NAME, binding.edtName.getText().toString());
                            preferenceManager.putString(Constants.KEY_IMAGE, dowloaduri.toString());
                            preferenceManager.putString(Constants.KEY_EMAIL, binding.edtEmail.getText().toString());
                            preferenceManager.putString(Constants.KEY_NAME, binding.edtName.getText().toString());
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        }

                    }
                });
            }
        });

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, preferenceManager.getString(Constants.KEY_EMAIL))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        Log.e("user ID:" ,documentSnapshot.getString(Constants.KEY_NAME));

                    }
                });
    }


    //Toast
    private void showToast(String toast) {
        Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_SHORT).show();
    }
}