package com.example.minichat.adapters;

import android.content.Context;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.example.minichat.UserListener;
import com.example.minichat.databinding.ItemUserBinding;
import com.example.minichat.models.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;
    UserListener userListener;
Context context;

    public UserAdapter(List<User> userList, UserListener userListener, Context context) {
        this.userList = userList;
        this.userListener = userListener;
        this.context = context;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUserBinding itemUserBinding = ItemUserBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new UserViewHolder(itemUserBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        holder.binding.tvItemName.setText(user.name);
        holder.binding.tvEmail.setText(user.email);

        Glide.with(context).load(user.image).into(holder.binding.imgProfile);
        holder.binding.getRoot().setOnClickListener(v -> userListener.onUserClick(user));
    }

    @Override
    public int getItemCount() {
        if (userList != null) {
            return userList.size();
        }
        return 0;
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {

        ItemUserBinding binding;

        public UserViewHolder(ItemUserBinding itemUserBinding) {
            super(itemUserBinding.getRoot());
            binding = itemUserBinding;
        }

    }


}
