package com.example.habittrackerrpg.ui.friends;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.habittrackerrpg.R;
import com.example.habittrackerrpg.data.model.User;
import com.example.habittrackerrpg.logic.AvatarHelper;
import com.example.habittrackerrpg.logic.UserSearchResult;
import java.util.ArrayList;
import java.util.List;

public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.SearchViewHolder> {

    private List<UserSearchResult> results = new ArrayList<>();
    private OnAddFriendClickListener addFriendListener;
    private OnUserClickListener userClickListener;

    public interface OnAddFriendClickListener { void onAddFriendClick(User user); }
    public interface OnUserClickListener { void onUserClick(User user); }

    public void setOnAddFriendClickListener(OnAddFriendClickListener listener) { this.addFriendListener = listener; }
    public void setOnUserClickListener(OnUserClickListener listener) { this.userClickListener = listener; }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SearchViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_search_result, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        holder.bind(results.get(position));
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    public void setResults(List<UserSearchResult> results) {
        this.results = results;
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
        }

        void bind(UserSearchResult result) {
            textViewUsername.setText(result.user.getUsername());
            imageViewAvatar.setImageResource(AvatarHelper.getAvatarResourceId(result.user.getAvatarId()));

            itemView.setOnClickListener(v -> {
                if (userClickListener != null) userClickListener.onUserClick(result.user);
            });

            switch (result.status) {
                case FRIENDS:
                    buttonAddFriend.setText("Friends");
                    buttonAddFriend.setEnabled(false);
                    break;
                case REQUEST_SENT:
                    buttonAddFriend.setText("Request Sent");
                    buttonAddFriend.setEnabled(false);
                    break;
                case NONE:
                    buttonAddFriend.setText("Add");
                    buttonAddFriend.setEnabled(true);
                    buttonAddFriend.setOnClickListener(v -> {
                        if (addFriendListener != null) {
                            addFriendListener.onAddFriendClick(result.user);
                            buttonAddFriend.setText("Request Sent");
                            buttonAddFriend.setEnabled(false);
                        }
                    });
                    break;
            }
        }
    }
}