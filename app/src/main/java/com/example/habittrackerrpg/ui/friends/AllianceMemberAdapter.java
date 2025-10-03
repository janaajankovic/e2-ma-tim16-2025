package com.example.habittrackerrpg.ui.friends;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.habittrackerrpg.R;
import com.example.habittrackerrpg.data.model.AllianceMember;
import com.example.habittrackerrpg.logic.AvatarHelper;
import java.util.ArrayList;
import java.util.List;

public class AllianceMemberAdapter extends RecyclerView.Adapter<AllianceMemberAdapter.MemberViewHolder> {

    private List<AllianceMember> members = new ArrayList<>();
    private String leaderId = "";

    public void setData(List<AllianceMember> members, String leaderId) {
        this.members = members;
        this.leaderId = leaderId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alliance_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        holder.bind(members.get(position));
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    class MemberViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewAvatar;
        TextView textViewUsername, textViewLeader;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewAvatar = itemView.findViewById(R.id.imageViewAvatar);
            textViewUsername = itemView.findViewById(R.id.textViewUsername);
            textViewLeader = itemView.findViewById(R.id.textViewLeader);
        }

        void bind(AllianceMember member) {
            textViewUsername.setText(member.getUsername());
            imageViewAvatar.setImageResource(AvatarHelper.getAvatarResourceId(member.getAvatarId()));

            // Pokaži "(Leader)" oznaku ako je ID člana jednak ID-ju vođe
            if (member.getUserId().equals(leaderId)) {
                textViewLeader.setVisibility(View.VISIBLE);
            } else {
                textViewLeader.setVisibility(View.GONE);
            }
        }
    }
}