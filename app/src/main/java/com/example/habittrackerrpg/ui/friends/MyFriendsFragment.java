package com.example.habittrackerrpg.ui.friends;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.habittrackerrpg.data.model.FriendRequest;
import com.example.habittrackerrpg.databinding.FragmentMyFriendsBinding;

public class MyFriendsFragment extends Fragment {

    private FragmentMyFriendsBinding binding;
    private FriendsViewModel viewModel;
    private FriendRequestAdapter requestAdapter;
    private FriendAdapter friendAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMyFriendsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(FriendsViewModel.class);

        setupRecyclerViews();
        setupObservers();
    }

    private void setupRecyclerViews() {
        requestAdapter = new FriendRequestAdapter();
        binding.recyclerViewRequests.setAdapter(requestAdapter);

        friendAdapter = new FriendAdapter();
        binding.recyclerViewFriends.setAdapter(friendAdapter);

        requestAdapter.setListener(new FriendRequestAdapter.OnRequestInteractionListener() {
            @Override
            public void onAccept(FriendRequest request) {
                viewModel.acceptFriendRequest(request);
            }

            @Override
            public void onDecline(FriendRequest request) {
                viewModel.declineFriendRequest(request);
            }
        });
    }

    private void setupObservers() {
        viewModel.getFriendRequests().observe(getViewLifecycleOwner(), requests -> {
            if (requests != null) {
                requestAdapter.setRequests(requests);
            }
        });

        viewModel.getFriendsList().observe(getViewLifecycleOwner(), friends -> {
            if (friends != null) {
                friendAdapter.setFriends(friends);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}