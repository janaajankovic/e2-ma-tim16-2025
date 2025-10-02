package com.example.habittrackerrpg.ui.friends;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.habittrackerrpg.R;
import com.example.habittrackerrpg.data.model.Friend;
import com.example.habittrackerrpg.data.model.FriendRequest;
import com.example.habittrackerrpg.data.model.User;
import com.example.habittrackerrpg.logic.AvatarHelper;

import java.util.ArrayList;
import java.util.List;

public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.SearchViewHolder> {

    private List<User> users = new ArrayList<>();
    private List<String> friendIds = new ArrayList<>();
    private List<String> sentRequestReceiverIds = new ArrayList<>();
    private String currentUserId;
    private OnUserClickListener userClickListener;

    private OnAddFriendClickListener listener;
    public interface OnAddFriendClickListener {
        void onAddFriendClick(User user);
    }

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    public void setUserClickListener(OnUserClickListener listener) {
        this.userClickListener = listener;
    }
    public void setListener(OnAddFriendClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SearchViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_search_result, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        holder.bind(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void setUsers(List<User> users) {
        this.users = users;
        notifyDataSetChanged();
    }
    public void setData(List<User> users, List<Friend> friends, List<FriendRequest> sentRequests, String currentUserId) {
        this.users = users;
        this.currentUserId = currentUserId;
        this.friendIds.clear();
        for (Friend friend : friends) {
            this.friendIds.add(friend.getUserId());
        }

        this.sentRequestReceiverIds.clear();
        for (FriendRequest request : sentRequests) {
            this.sentRequestReceiverIds.add(request.getReceiverId());
        }

        notifyDataSetChanged();
    }

    class SearchViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewAvatar;
        TextView textViewUsername;
        Button buttonAddFriend;

        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewAvatar = itemView.findViewById(R.id.imageViewAvatar);
            textViewUsername = itemView.findViewById(R.id.textViewUsername);
            buttonAddFriend = itemView.findViewById(R.id.buttonAddFriend);
            itemView.setOnClickListener(v -> {
                if (userClickListener != null) {
                    userClickListener.onUserClick(users.get(getAdapterPosition()));
                }
            });
        }

        void bind(User user) {
            textViewUsername.setText(user.getUsername());

            Context context = itemView.getContext();
            int avatarResId = context.getResources().getIdentifier(user.getAvatarId(), "drawable", context.getPackageName());
            imageViewAvatar.setImageResource(AvatarHelper.getAvatarResourceId(user.getAvatarId()));
            if (user.getId().equals(currentUserId)) {
                // Ako je korisnik u listi zapravo trenutno ulogovani korisnik
                buttonAddFriend.setVisibility(View.GONE); // Sakrij dugme
            } else if (friendIds.contains(user.getId())) {
                buttonAddFriend.setVisibility(View.VISIBLE);
                buttonAddFriend.setText("Friends");
                buttonAddFriend.setEnabled(false);
            } else if (sentRequestReceiverIds.contains(user.getId())) {
                buttonAddFriend.setVisibility(View.VISIBLE);
                buttonAddFriend.setText("Request Sent");
                buttonAddFriend.setEnabled(false);
            } else {
                buttonAddFriend.setText("Add");
                buttonAddFriend.setEnabled(true);
                buttonAddFriend.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onAddFriendClick(user);
                        buttonAddFriend.setText("Request Sent");
                        buttonAddFriend.setEnabled(false);
                    }
                });
            }
        }
    }
}