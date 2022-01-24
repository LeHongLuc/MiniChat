package com.example.minichat.Activity;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.minichat.R;
import com.example.minichat.UserListener;
import com.example.minichat.adapters.RecentConversionAdapter;
import com.example.minichat.databinding.ActivityMainBinding;
import com.example.minichat.models.ChatMessage;
import com.example.minichat.models.User;
import com.example.minichat.utilities.ChatHeadService;
import com.example.minichat.utilities.Constants;
import com.example.minichat.utilities.PreferenceManager;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends BaseActivity implements UserListener {

    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    List<ChatMessage> chatMessageList;
    RecentConversionAdapter recentConversionAdapter;
    FirebaseFirestore firebaseFirestore;
    AlertDialog alertDialog;

    private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //kiểm tra kết nối mạng
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
        } else {

        }

        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessageList = new ArrayList<>();
        recentConversionAdapter = new RecentConversionAdapter(chatMessageList, this,this);
        binding.rvConversion.setAdapter(recentConversionAdapter);
        firebaseFirestore = FirebaseFirestore.getInstance();

        loadDetails();
        getToken();
        setListeners();
        listenerConversation();

        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
        } else {
            initializeView();
        }
    }
    private void initializeView() {
        binding.tvName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startService(new Intent(MainActivity.this, ChatHeadService.class));
                finish();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CODE_DRAW_OVER_OTHER_APP_PERMISSION) {

            //Check if the permission is granted or not.
            // Settings activity never returns proper value so instead check with following method
            if (Settings.canDrawOverlays(this)) {
                initializeView();
            } else { //Permission is not available
                Toast.makeText(this,
                        "Draw over other app permission not available. Closing the application",
                        Toast.LENGTH_SHORT).show();

                finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
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

    // lắng nghe sự kiện onclick
    private void setListeners() {
        binding.imgSignout.setOnClickListener(v -> signOut());
        binding.fab.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), UserActivity.class)));
        binding.imgAvt.setOnClickListener(v -> {

            Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
            ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this, binding.imgAvt, ViewCompat.getTransitionName(binding.imgAvt));
            startActivity(intent, optionsCompat.toBundle());

        });
    }

    // load ảnh và tên của user mới đăng nhập
    private void loadDetails() {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, preferenceManager.getString(Constants.KEY_EMAIL))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        Glide.with(this).load(documentSnapshot.getString(Constants.KEY_IMAGE)).into(binding.imgAvt);
                        binding.tvName.setText(documentSnapshot.getString(Constants.KEY_NAME));
                    }
                });

    }


// lắng nghe cuộc hội thoại
    private void listenerConversation() {
        firebaseFirestore.collection(Constants.KEY_COLLECTION_CONVERSIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        firebaseFirestore.collection(Constants.KEY_COLLECTION_CONVERSIONS)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    //sự kiện lắng nghe
    private final EventListener<QuerySnapshot> eventListener = (value, er) -> {
        if (er != null) {
            return;
        }

        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {

                // báo nếu firebase có thêm dữ liệu mới thì.
                if (documentChange.getType() == DocumentChange.Type.ADDED) {

                    String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receivedId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);

                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = senderId;
                    chatMessage.receivedId = receivedId;

                    if (preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)) {
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                    } else {
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
                    }
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIME_STAMP);

                    chatMessageList.add(chatMessage);
                } else
                    // nếu không được  thêm mới mà được sửa đổi
                    if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    for (int i = 0; i < chatMessageList.size(); i++) {

                        String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receivedId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        if (chatMessageList.get(i).senderId.equals(senderId) && chatMessageList.get(i).receivedId.equals(receivedId)) {
                            chatMessageList.get(i).message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                            chatMessageList.get(i).dateObject = documentChange.getDocument().getDate(Constants.KEY_TIME_STAMP);
                            break;
                        }
                    }
                }
            }
            Collections.sort(chatMessageList, (obj1, obj2) -> obj2.dateObject.compareTo(obj1.dateObject));
            recentConversionAdapter.notifyDataSetChanged();
            binding.progressBar.setVisibility(View.GONE);
            binding.rvConversion.smoothScrollToPosition(0);
        }
    };

    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token) {
        preferenceManager.putString(Constants.KEY_FCM_TOKEN, token);
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                firebaseFirestore.collection(Constants.KEY_COLLECTION_USERS)
                        .document(preferenceManager.getString(Constants.KEY_USER_ID));

        documentReference.update(Constants.KEY_FCM_TOKEN, token);
    }

    private void signOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("ĐĂNG XUẤT");
        builder.setMessage("Bạn có chắc muốn đăng xuất???");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
                DocumentReference documentReference =
                        firebaseFirestore.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID));

                HashMap<String, Object> updates = new HashMap<>();
                updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
                documentReference.update(updates).addOnSuccessListener(unused -> {
                            preferenceManager.clear();
                            startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                            finish();
                        })
                        .addOnFailureListener(e -> showToast("Không thể đăng xuất!"));

            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create();
        builder.show();
    }


// sự kiện bấm vào từng item chat
    @Override
    public void onUserClick(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
    }

    private void showToast(String toast) {
        Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_SHORT).show();
    }
}