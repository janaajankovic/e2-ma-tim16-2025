package com.example.habittrackerrpg.ui.friends;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.habittrackerrpg.R;
import com.example.habittrackerrpg.data.model.FriendRequest;
import com.example.habittrackerrpg.logic.AvatarHelper;

import java.util.ArrayList;
import java.util.List;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.RequestViewHolder> {

    private List<FriendRequest> requests = new ArrayList<>();
    private OnRequestInteractionListener listener;

    public interface OnRequestInteractionListener {
        void onAccept(FriendRequest request);
        void onDecline(FriendRequest request);
    }

    public void setListener(OnRequestInteractionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RequestViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_request, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        holder.bind(requests.get(position));
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public void setRequests(List<FriendRequest> requests) {
        this.requests = requests;
        notifyDataSetChanged();
    }

    class RequestViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewAvatar;
        TextView textViewUsername;
        ImageButton buttonAccept, buttonDecline;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewAvatar = itemView.findViewById(R.id.imageViewAvatar);
            textViewUsername = itemView.findViewById(R.id.textViewUsername);
            buttonAccept = itemView.findViewById(R.id.buttonAccept);
            buttonDecline = itemView.findViewById(R.id.buttonDecline);
        }

        void bind(FriendRequest request) {
            textViewUsername.setText(request.getSenderUsername());

            Context context = itemView.getContext();
            int avatarResId = context.getResources().getIdentifier(request.getSenderAvatarId(), "drawable", context.getPackageName());
            imageViewAvatar.setImageResource(AvatarHelper.getAvatarResourceId(request.getSenderAvatarId()));

            buttonAccept.setOnClickListener(v -> listener.onAccept(request));
            buttonDecline.setOnClickListener(v -> listener.onDecline(request));
        }
    }
}