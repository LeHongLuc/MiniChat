package com.example.minichat.Activity;

import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

import android.os.Bundle;

import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.minichat.UserListener;
import com.example.minichat.adapters.RecentConversionAdapter;
import com.example.minichat.databinding.ActivityMainBinding;
import com.example.minichat.models.ChatMessage;
import com.example.minichat.models.User;
import com.example.minichat.utilities.Constants;
import com.example.minichat.utilities.PreferenceManager;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Fresco.initialize(getApplicationContext());


        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessageList = new ArrayList<>();
        recentConversionAdapter = new RecentConversionAdapter(chatMessageList, this,this);
        binding.rvConversion.setAdapter(recentConversionAdapter);
        firebaseFirestore = FirebaseFirestore.getInstance();

        loadDetails();
        getToken();
        setListeners();
        listenerConversation();



    }

    private void setListeners() {
        binding.imgSignout.setOnClickListener(v -> signOut());
        binding.fab.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), UserActivity.class)));
        binding.imgAvt.setOnClickListener(v -> {


            Intent intent = new Intent(getApplicationContext(), ChangePasswordActivity.class);
            ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this, binding.imgAvt, ViewCompat.getTransitionName(binding.imgAvt));
            startActivity(intent, optionsCompat.toBundle());

        });
    }

    private void loadDetails() {

        binding.tvName.setText(preferenceManager.getString(Constants.KEY_NAME));

        Glide.with(this).load(preferenceManager.getString(Constants.KEY_IMAGE)).into(binding.imgAvt);
    }

    private void showToast(String toast) {
        Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_SHORT).show();
    }

    private void listenerConversation() {
        firebaseFirestore.collection(Constants.KEY_COLLECTION_CONVERSIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        firebaseFirestore.collection(Constants.KEY_COLLECTION_CONVERSIONS)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, er) -> {
        if (er != null) {
            return;
        }

        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
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
                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
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
        builder.setMessage("Do you want to sign out??");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
                DocumentReference documentReference =
                        firebaseFirestore.collection(Constants.KEY_COLLECTION_USERS)
                                .document(preferenceManager.getString(Constants.KEY_USER_ID));

                HashMap<String, Object> updates = new HashMap<>();
                updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
                documentReference.update(updates)
                        .addOnSuccessListener(unused -> {
                            preferenceManager.clear();
                            startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                            finish();
                        })
                        .addOnFailureListener(e -> showToast("Unable to sign out!"));

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



    @Override
    public void onUserClick(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
    }
}