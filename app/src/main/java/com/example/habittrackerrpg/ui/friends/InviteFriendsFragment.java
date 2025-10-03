package com.example.habittrackerrpg.ui.friends;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.habittrackerrpg.data.model.Alliance;
import com.example.habittrackerrpg.data.model.AllianceInvite;
import com.example.habittrackerrpg.data.model.Friend;
import com.example.habittrackerrpg.databinding.FragmentInviteFriendsBinding;

import java.util.List;

public class InviteFriendsFragment extends Fragment {

    private FragmentInviteFriendsBinding binding;
    private FriendsViewModel viewModel;
    private InviteFriendAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentInviteFriendsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(FriendsViewModel.class);

        setupRecyclerView();
        setupObservers();
    }

    private void setupRecyclerView() {
        adapter = new InviteFriendAdapter();
        binding.recyclerViewInviteFriends.setAdapter(adapter);

        adapter.setListener(friend -> {
            viewModel.sendAllianceInvite(friend);
        });
    }
    private void setupObservers() {
        viewModel.getFriendsList().observe(getViewLifecycleOwner(), friends -> updateAdapterData());
        viewModel.getCurrentAlliance().observe(getViewLifecycleOwner(), alliance -> updateAdapterData());
        viewModel.getPendingAllianceInvites().observe(getViewLifecycleOwner(), invites -> updateAdapterData());
    }

    private void updateAdapterData() {
        List<Friend> friends = viewModel.getFriendsList().getValue();
        Alliance alliance = viewModel.getCurrentAlliance().getValue();
        List<AllianceInvite> invites = viewModel.getPendingAllianceInvites().getValue();

        if (friends != null && alliance != null && invites != null) {
            adapter.setData(friends, alliance, invites);
        }
    }
}