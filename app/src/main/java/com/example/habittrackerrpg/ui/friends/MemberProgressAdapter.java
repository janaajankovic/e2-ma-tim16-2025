package com.example.habittrackerrpg.ui.friends;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habittrackerrpg.R;
import com.example.habittrackerrpg.data.model.SpecialMissionProgress;
import com.example.habittrackerrpg.logic.AvatarHelper;

public class MemberProgressAdapter extends ListAdapter<SpecialMissionProgress, MemberProgressAdapter.ProgressViewHolder> {

    public MemberProgressAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public ProgressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mission_progress, parent, false);
        return new ProgressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProgressViewHolder holder, int position) {
        SpecialMissionProgress currentProgress = getItem(position);
        holder.bind(currentProgress, position);
    }

    static class ProgressViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewRank;
        private final ImageView imageViewAvatar;
        private final TextView textViewUsername;
        private final TextView textViewDamageDealt;

        public ProgressViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewRank = itemView.findViewById(R.id.textViewRank);
            imageViewAvatar = itemView.findViewById(R.id.imageViewAvatar);
            textViewUsername = itemView.findViewById(R.id.textViewUsername);
            textViewDamageDealt = itemView.findViewById(R.id.textViewDamageDealt);
        }

        public void bind(SpecialMissionProgress progress, int position) {
            textViewRank.setText((position + 1) + ".");
            textViewUsername.setText(progress.getUsername());
            textViewDamageDealt.setText(progress.getTotalDamageDealt() + " HP");

            if (progress.getAvatarId() != null) {
                imageViewAvatar.setImageResource(AvatarHelper.getAvatarResourceId(progress.getAvatarId()));
            }
            else{
                imageViewAvatar.setImageResource(R.mipmap.ic_launcher_round);
            }
        }
    }

    private static final DiffUtil.ItemCallback<SpecialMissionProgress> DIFF_CALLBACK = new DiffUtil.ItemCallback<SpecialMissionProgress>() {
        @Override
        public boolean areItemsTheSame(@NonNull SpecialMissionProgress oldItem, @NonNull SpecialMissionProgress newItem) {
            return oldItem.getUserId().equals(newItem.getUserId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull SpecialMissionProgress oldItem, @NonNull SpecialMissionProgress newItem) {
            return oldItem.getTotalDamageDealt() == newItem.getTotalDamageDealt();
        }
    };
}