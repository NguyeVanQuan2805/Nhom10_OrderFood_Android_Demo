package com.example.neworderfood.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neworderfood.R;
import com.example.neworderfood.models.User;

import java.util.ArrayList;
import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.ViewHolder> {
    private List<User> users;
    private OnUserActionListener listener;

    public interface OnUserActionListener {
        void onUserAction(User user);  // Callback cho edit/delete (long click)
    }

    public AdminUserAdapter(List<User> users, OnUserActionListener listener) {
        this.users = users != null ? users : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);
        holder.usernameText.setText(user.getUsername());
        holder.roleText.setText("Vai trò: " + user.getRole());  // Hiển thị role (admin/employee)

        // Long click để edit/delete
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onUserAction(user);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText, roleText;

        ViewHolder(View view) {
            super(view);
            usernameText = view.findViewById(R.id.user_username);
            roleText = view.findViewById(R.id.user_role);
        }
    }

    public void updateUsers(List<User> newUsers) {
        this.users = newUsers != null ? newUsers : new ArrayList<>();
        notifyDataSetChanged();
    }
}