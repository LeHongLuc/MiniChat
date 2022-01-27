package com.example.minichat.adapters;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.minichat.databinding.ItemContainerReceivedMessageBinding;
import com.example.minichat.databinding.ItemContainerSentMessageBinding;
import com.example.minichat.models.ChatMessage;
import com.example.minichat.utilities.Constants;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ChatMessage> chatMessageList;
    private String receivedProfileImage;
    private final String senderId;
    private Context mContext;
    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;


    public void setReceiverImage(String string) {
        receivedProfileImage = string;
    }

    public ChatAdapter(List<ChatMessage> chatMessageList, String receivedProfileImage, String senderId, Context mContext) {
        this.chatMessageList = chatMessageList;
        this.receivedProfileImage = receivedProfileImage;
        this.senderId = senderId;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == VIEW_TYPE_SENT) {
            return new SentMessageViewHolder(ItemContainerSentMessageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        } else {
            return new ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            ((SentMessageViewHolder) holder).setData(chatMessageList.get(position));
        } else {
            ((ReceivedMessageViewHolder) holder).setData(chatMessageList.get(position), receivedProfileImage);
        }
    }

    @Override
    public int getItemCount() {
        if (chatMessageList != null) {
            return chatMessageList.size();
        }
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        if (chatMessageList.get(position).senderId.equals(senderId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    public class SentMessageViewHolder extends RecyclerView.ViewHolder {

        ItemContainerSentMessageBinding binding;

        public SentMessageViewHolder(ItemContainerSentMessageBinding itemContainerSentMessageBinding) {
            super(itemContainerSentMessageBinding.getRoot());
            binding = itemContainerSentMessageBinding;
        }

        void setData(ChatMessage chatMessage) {
            if (chatMessage.classify==null) return;
            if (chatMessage.classify.equals(Constants.CLASSIFY_MESS)) {
                binding.tvMessage.setVisibility(View.VISIBLE);
                binding.tvMessage.setText(chatMessage.message);
                binding.imgMess.setVisibility(View.GONE);
            } else if (chatMessage.classify.equals(Constants.CLASSIFY_IMG)) {
                binding.tvMessage.setVisibility(View.GONE);
                binding.imgMess.setVisibility(View.VISIBLE);
                Glide.with(mContext).load(chatMessage.message).into(binding.imgMess);
            }

            binding.tvDateTime.setText(chatMessage.dateTime);

            binding.itemSentMess.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onClick(View v) {
                    v.getFocusable();
                    if (v != null) {
                        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
            });
        }
    }

    public class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {

        ItemContainerReceivedMessageBinding binding;

        public ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding itemContainerReceivedMessageBinding) {
            super(itemContainerReceivedMessageBinding.getRoot());
            binding = itemContainerReceivedMessageBinding;
        }

        void setData(ChatMessage chatMessage, String bitmap) {
            if (chatMessage.classify.equals(Constants.CLASSIFY_MESS)) {
                binding.tvMessage.setVisibility(View.VISIBLE);
                binding.tvMessage.setText(chatMessage.message);
                binding.imgMess.setVisibility(View.GONE);
            } else if (chatMessage.classify.equals(Constants.CLASSIFY_IMG)) {
                binding.tvMessage.setVisibility(View.GONE);
                binding.imgMess.setVisibility(View.VISIBLE);
                Glide.with(mContext).load(chatMessage.message).into(binding.imgMess);
            }

            binding.tvDateTime.setText(chatMessage.dateTime);
            Glide.with(mContext).load(receivedProfileImage).into(binding.imgAvt);
            binding.itemReceivedMess.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onClick(View v) {
                    v.getFocusable();
                    if (v != null) {
                        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
            });

        }
    }
}
