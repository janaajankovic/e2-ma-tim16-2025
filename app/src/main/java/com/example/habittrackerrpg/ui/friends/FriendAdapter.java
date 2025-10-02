package com.example.habittrackerrpg.ui.friends;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.habittrackerrpg.R;
import com.example.habittrackerrpg.data.model.Friend;
import com.example.habittrackerrpg.logic.AvatarHelper;

import java.util.ArrayList;
import java.util.List;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {

    private List<Friend> friends = new ArrayList<>();

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FriendViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        holder.bind(friends.get(position));
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    public void setFriends(List<Friend> friends) {
        this.friends = friends;
        notifyDataSetChanged();
    }

    class FriendViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewAvatar;
        TextView textViewUsername;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewAvatar = itemView.findViewById(R.id.imageViewAvatar);
            textViewUsername = itemView.findViewById(R.id.textViewUsername);
        }

        void bind(Friend friend) {
            textViewUsername.setText(friend.getUsername());

            Context context = itemView.getContext();
            int avatarResId = context.getResources().getIdentifier(friend.getAvatarId(), "drawable", context.getPackageName());
            imageViewAvatar.setImageResource(AvatarHelper.getAvatarResourceId(friend.getAvatarId()));
        }
    }
}