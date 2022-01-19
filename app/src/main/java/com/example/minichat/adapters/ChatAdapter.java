package com.example.minichat.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.minichat.databinding.ItemContainerReceivedMessageBinding;
import com.example.minichat.databinding.ItemContainerSentMessageBinding;
import com.example.minichat.models.ChatMessage;

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
            binding.tvMessage.setText(chatMessage.message);
            binding.tvDateTime.setText(chatMessage.dateTime);
        }
    }

    public class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {

        ItemContainerReceivedMessageBinding binding;

        public ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding itemContainerReceivedMessageBinding) {
            super(itemContainerReceivedMessageBinding.getRoot());
            binding = itemContainerReceivedMessageBinding;
        }

        void setData(ChatMessage chatMessage, String bitmap) {
            binding.tvMessage.setText(chatMessage.message);
            binding.tvDateTime.setText(chatMessage.dateTime);
            Glide.with(mContext).load(receivedProfileImage).into(binding.imgAvt);


        }
    }
}
