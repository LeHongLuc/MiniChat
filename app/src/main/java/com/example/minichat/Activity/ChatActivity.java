package com.example.minichat.Activity;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.minichat.ApiService;
import com.example.minichat.R;
import com.example.minichat.adapters.ChatAdapter;
import com.example.minichat.databinding.ActivityChatBinding;
import com.example.minichat.models.ChatMessage;
import com.example.minichat.models.User;
import com.example.minichat.utilities.Constants;
import com.example.minichat.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;
import gun0912.tedbottompicker.TedBottomPicker;
import gun0912.tedbottompicker.TedBottomSheetDialogFragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseActivity {

    private ActivityChatBinding binding;
    User user;
    ChatAdapter chatAdapter;
    List<ChatMessage> chatMessageList;
    PreferenceManager preferenceManager;
    FirebaseFirestore firebaseFirestore;
    String conversionId = null;
    Boolean isReceiverAvailable = false;
    Uri Imageuri;
//    ConstraintLayout received_map,received_mess,send_map,send_mess;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        setListeners();
        loadReceivedMessage();
        init();
        listenMessage();

    }


    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessageList, user.image, preferenceManager.getString(Constants.KEY_USER_ID), this);
        binding.rcvChat.setAdapter(chatAdapter);
        firebaseFirestore = FirebaseFirestore.getInstance();
    }

    // các sự kiện, nút bấm
    private void setListeners() {
        binding.imgBack.setOnClickListener(v -> onBackPressed());
        binding.imgInfo.setOnClickListener(v -> {
            Intent intent= new Intent(ChatActivity.this,ProfileReceivedActivity.class);
            startActivity(intent);

        });
        binding.layoutSendMess.setOnClickListener(new View.OnClickListener() {
            @Override
                                                      public void onClick(View v) {
                if (binding.edMessage.getText().toString()!=null&&!binding.edMessage.getText().toString().equals("")) {
                    sendMessage(Constants.CLASSIFY_MESS);
                }
                                                      }
                                                  }

        );
        binding.layoutSendMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(Constants.CLASSIFY_MAP);
            }
        });
        binding.layoutSendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(Constants.CLASSIFY_IMG);
            }
        });
        binding.rcvChat.setOnClickListener(v -> {
                    View view1 = getCurrentFocus();
                    if (view1 != null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view1.getWindowToken(), 0);
                    }
                }
        );

    }

    private void sendMessage(String classify) {
        HashMap<String, Object> message = new HashMap<>();

        if (classify == Constants.CLASSIFY_MESS) {
            message.put(Constants.KEY_CLASSIFY, classify);
            Log.e("Send ", "mess");
            message.put(Constants.KEY_MESSAGE, binding.edMessage.getText().toString());
        } else
            if (classify == Constants.CLASSIFY_IMG) {
            message.put(Constants.KEY_CLASSIFY, classify);
            Log.e("Send ", "img");
            RequestPermissions();
            message.put(Constants.KEY_MESSAGE, preferenceManager.getString(Constants.KEY_LINK_IMG));
        }


        message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID, user.id);
        message.put(Constants.KEY_TIME_STAMP, new Date());
        preferenceManager.putString(Constants.KEY_TIME_STAMP, new Date().toString());
        firebaseFirestore.collection(Constants.KEY_COLLECTION_CHAT).add(message);


        if (conversionId != null) {
            if(binding.edMessage.getText().toString()!=null&&!binding.edMessage.getText().toString().equals(""))
            updateConversion(binding.edMessage.getText().toString(),classify);
            else  updateConversion( preferenceManager.getString(Constants.KEY_LINK_IMG),classify);

        } else {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
            hashMap.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
            hashMap.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));

            hashMap.put(Constants.KEY_RECEIVER_ID, user.id);
            hashMap.put(Constants.KEY_RECEIVER_NAME, user.name);
            hashMap.put(Constants.KEY_RECEIVER_IMAGE, user.image);

            hashMap.put(Constants.KEY_TIME_STAMP, new Date());

            if (classify.equals(Constants.CLASSIFY_MESS)) {
                hashMap.put(Constants.KEY_CLASSIFY, Constants.CLASSIFY_MESS);
                hashMap.put(Constants.KEY_LAST_MESSAGE, binding.edMessage.getText().toString());
            } else if (classify == Constants.CLASSIFY_IMG) {
                hashMap.put(Constants.KEY_CLASSIFY, Constants.CLASSIFY_IMG);
                hashMap.put(Constants.KEY_LAST_MESSAGE, preferenceManager.getString(Constants.KEY_LINK_IMG));
            }
            addConversion(hashMap);
        }

        if (!isReceiverAvailable) {
            try {
                JSONArray token = new JSONArray();
                token.put(user.token);

                JSONObject data = new JSONObject();
                data.put(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                data.put(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME));
                data.put(Constants.KEY_FCM_TOKEN, preferenceManager.getString(Constants.KEY_FCM_TOKEN));
                data.put(Constants.KEY_MESSAGE, binding.edMessage.getText().toString());
                data.put(Constants.KEY_CLASSIFY, classify);

                JSONObject body = new JSONObject();
                body.put(Constants.REMOTE_MSG_DATA, data);
                body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, token);

                sendNotification(body.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        binding.edMessage.setText(null);
    }

    private void showToast(String toast) {
        Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_SHORT).show();
    }

    private void sendNotification(String messageBody) {
        ApiService.API_SERVICE.sendMessage(Constants.getRemoteMsgHeader(), messageBody).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    try {
                        if (response.body() != null) {
                            JSONObject jsonObject = new JSONObject(response.body());
                            JSONArray results = jsonObject.getJSONArray("results");
                            if (jsonObject.getInt("failure") == 1) {
                                JSONObject er = (JSONObject) results.get(0);
                                showToast(er.getString("error"));
                                return;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    showToast("Notification sent successfully");
                } else {
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
            }
        });
    }

    private void listenAvailabilityOfReceiver() {
        firebaseFirestore.collection(Constants.KEY_COLLECTION_USERS)
                .document(user.id)
                .addSnapshotListener(ChatActivity.this, (value, er) -> {
                    if (er != null) {
                        return;
                    }
                    if (value != null) {
                        if (value.getLong(Constants.KEY_AVAILABILITY) != null) {
                            int availability = Objects.requireNonNull(value.getLong(Constants.KEY_AVAILABILITY)).intValue();
                            isReceiverAvailable = availability == 1;
                        }
                        user.token = value.getString(Constants.KEY_FCM_TOKEN);
                        if (user.image == null) {
                            user.image = value.getString(Constants.KEY_IMAGE);
                            chatAdapter.setReceiverImage(user.image);
                            chatAdapter.notifyItemRangeInserted(0, chatMessageList.size());
                        }
                    }
                    if (isReceiverAvailable) {
                        binding.tvAvailability.setVisibility(View.VISIBLE);
                    } else {
                        binding.tvAvailability.setVisibility(View.GONE);
                    }
                });
    }

    private void listenMessage() {
        firebaseFirestore.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID, user.id)
                .addSnapshotListener(eventListener);
        firebaseFirestore.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, user.id)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }


    // lấy dữ liệu về cuộc trò chuyện
    private final EventListener<QuerySnapshot> eventListener = (value, er) -> {
        if (er != null) {
            return;
        }
        if (value != null) {
            int count = chatMessageList.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.receivedId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.dateTime = getRealDateTime(documentChange.getDocument().getDate(Constants.KEY_TIME_STAMP));
                    chatMessage.classify = documentChange.getDocument().getString(Constants.KEY_CLASSIFY);
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIME_STAMP);
                    chatMessageList.add(chatMessage);
                }
            }
            Collections.sort(chatMessageList, (obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
            if (count == 0) {
                chatAdapter.notifyDataSetChanged();
            } else {
                chatAdapter.notifyItemRangeInserted(chatMessageList.size(), chatMessageList.size());
                binding.rcvChat.smoothScrollToPosition(chatMessageList.size() - 1);
            }
        }
        binding.progressBar.setVisibility(View.GONE);
        if (conversionId == null) {
            checkForConversion();
        }
    };

    //load tin nhắn của người nhận
    private void loadReceivedMessage() {
        user = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.tvName.setText(user.name);
    }


    //get thời gian gửi tin nhắn
    private String getRealDateTime(Date date) {
        return new SimpleDateFormat("dd MMMM, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    //thêm thông tin nhóm chat
    private void addConversion(HashMap<String, Object> conversion) {
        firebaseFirestore.collection(Constants.KEY_COLLECTION_CONVERSIONS)
                .add(conversion)
                .addOnSuccessListener(documentReference -> conversionId = documentReference.getId());
    }

    //update tin nhắn mới nhất
    private void updateConversion(String message,String classify) {
        DocumentReference documentReference = firebaseFirestore.collection(Constants.KEY_COLLECTION_CONVERSIONS).document(conversionId);
        documentReference.update(Constants.KEY_LAST_MESSAGE, message, Constants.KEY_TIME_STAMP, new Date(),Constants.KEY_CLASSIFY,classify);
    }


    //kiểm tra người gửi - người nhận
    private void checkForConversion() {
        if (chatMessageList.size() != 0) {
            checkForConversionRemotely(preferenceManager.getString(Constants.KEY_USER_ID), user.id);
            checkForConversionRemotely(user.id, preferenceManager.getString(Constants.KEY_USER_ID));
        }
    }


    private void checkForConversionRemotely(String senderId, String receivedId) {
        firebaseFirestore.collection(Constants.KEY_COLLECTION_CONVERSIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receivedId)
                .get()
                .addOnCompleteListener(snapshot);
    }

    private final OnCompleteListener<QuerySnapshot> snapshot = task -> {
        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversionId = documentSnapshot.getId();
        }
    };


    //gửi tin nhắn ảnh
    private void sendImg() {
        SweetAlertDialog pDialog = new SweetAlertDialog(ChatActivity.this, SweetAlertDialog.PROGRESS_TYPE);
       pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.show();
        if (Imageuri != null) {

            FirebaseStorage storage = FirebaseStorage.getInstance();
            String timeStamp = String.valueOf(System.currentTimeMillis());
            StorageReference storageReference = storage.getReference().child("imgChat").child(conversionId).child(timeStamp);
            //  .child(preferenceManager.getString(Constants.KEY_SENDER_ID))
            //  .child(preferenceManager.getString(Constants.KEY_RECEIVER_ID))

            storageReference.putFile(Imageuri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    pDialog.dismiss();
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful()) ;
                    Uri dowloaduri = uriTask.getResult();
                    preferenceManager.putString(Constants.KEY_LINK_IMG, dowloaduri.toString());

                }
            });
        }
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
                Toast.makeText(ChatActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getApplicationContext(), "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }
        };
        TedPermission.create()
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check();
    }

    //chọn ảnh và lấy địa chỉ của ảnh
    private void openImagePicker() {
        TedBottomPicker.with(ChatActivity.this).show(new TedBottomSheetDialogFragment.OnImageSelectedListener() {
            @Override
            public void onImageSelected(Uri uri) {
                Imageuri = uri;
                    sendImg();

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailabilityOfReceiver();
    }
}