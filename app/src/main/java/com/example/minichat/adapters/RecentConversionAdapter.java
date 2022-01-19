package com.example.minichat.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.example.minichat.UserListener;
import com.example.minichat.databinding.ItemContainerRecentConversionBinding;
import com.example.minichat.models.ChatMessage;
import com.example.minichat.models.User;

import java.util.List;


public class RecentConversionAdapter extends RecyclerView.Adapter<RecentConversionAdapter.ConversionViewHolder> {

    List<ChatMessage> chatMessageList;
    UserListener userListener;
    Context mContext;

    public RecentConversionAdapter(List<ChatMessage> chatMessageList, UserListener userListener, Context mContext) {
        this.chatMessageList = chatMessageList;
        this.userListener = userListener;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ConversionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversionViewHolder(ItemContainerRecentConversionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ConversionViewHolder holder, int position) {
        holder.setData(chatMessageList.get(position));
    }

    @Override
    public int getItemCount() {
        if (chatMessageList != null) {
            return chatMessageList.size();
        }
        return 0;
    }

    public class ConversionViewHolder extends RecyclerView.ViewHolder {

        ItemContainerRecentConversionBinding binding;

        public ConversionViewHolder(ItemContainerRecentConversionBinding itemContainerRecentConversionBinding) {
            super(itemContainerRecentConversionBinding.getRoot());
            binding = itemContainerRecentConversionBinding;
        }

        void setData(ChatMessage chatMessage) {
            Glide.with(mContext).load(chatMessage.conversionImage).into(binding.imgProfile);
            binding.tvName.setText(chatMessage.conversionName);
            binding.tvRecentMessage.setText(chatMessage.message);
            binding.getRoot().setOnClickListener(v -> {
                User user = new User();
                user.id = chatMessage.conversionId;
                user.name = chatMessage.conversionName;
                user.image = chatMessage.conversionImage;
                userListener.onUserClick(user);
            });
        }
    }


}
