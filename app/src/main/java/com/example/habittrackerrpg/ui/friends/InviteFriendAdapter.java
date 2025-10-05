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
import com.example.habittrackerrpg.data.model.Alliance;
import com.example.habittrackerrpg.data.model.AllianceInvite;
import com.example.habittrackerrpg.data.model.Friend;
import com.example.habittrackerrpg.logic.AvatarHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InviteFriendAdapter extends RecyclerView.Adapter<InviteFriendAdapter.InviteViewHolder> {

    private List<Friend> friends = new ArrayList<>();
    private Alliance currentAlliance;
    private List<String> invitedFriendIds = new ArrayList<>();
    private OnInviteClickListener listener;

    public interface OnInviteClickListener {
        void onInviteClick(Friend friend);
    }

    public void setListener(OnInviteClickListener listener) {
        this.listener = listener;
    }

    public void setData(List<Friend> friends, Alliance alliance, List<AllianceInvite> pendingInvites) {
        this.friends = friends != null ? friends : new ArrayList<>();
        this.currentAlliance = alliance;

        if (pendingInvites != null) {
            this.invitedFriendIds = pendingInvites.stream()
                    .map(AllianceInvite::getReceiverId)
                    .collect(Collectors.toList());
        } else {
            this.invitedFriendIds = new ArrayList<>();
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InviteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_invite_friend, parent, false);
        return new InviteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InviteViewHolder holder, int position) {
        holder.bind(friends.get(position));
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    class InviteViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewAvatar;
        TextView textViewUsername;
        Button buttonInvite;

        public InviteViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewAvatar = itemView.findViewById(R.id.imageViewAvatar);
            textViewUsername = itemView.findViewById(R.id.textViewUsername);
            buttonInvite = itemView.findViewById(R.id.buttonInvite);
        }

        void bind(Friend friend) {
            textViewUsername.setText(friend.getUsername());
            imageViewAvatar.setImageResource(AvatarHelper.getAvatarResourceId(friend.getAvatarId()));

            if (currentAlliance != null && currentAlliance.getMembers().containsKey(friend.getUserId())) {
                buttonInvite.setText("Member");
                buttonInvite.setEnabled(false);
            } else if (invitedFriendIds.contains(friend.getUserId())) {
                buttonInvite.setText("Invited");
                buttonInvite.setEnabled(false);
            } else {
                buttonInvite.setText("Invite");
                buttonInvite.setEnabled(true);
                buttonInvite.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onInviteClick(friend);
                        buttonInvite.setText("Invited");
                        buttonInvite.setEnabled(false);
                    }
                });
            }
        }
    }
}