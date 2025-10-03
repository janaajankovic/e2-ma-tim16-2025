package com.example.habittrackerrpg.ui.friends;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.habittrackerrpg.data.model.Alliance;
import com.example.habittrackerrpg.data.model.AllianceInvite;
import com.example.habittrackerrpg.data.model.Friend;
import com.example.habittrackerrpg.data.model.User; // NOVI import
import com.example.habittrackerrpg.databinding.FragmentInviteFriendsBinding;
import com.example.habittrackerrpg.logic.NotificationSender; // NOVI import
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

        // --- GLAVNA I JEDINA IZMENA JE OVDE ---
        adapter.setListener(friend -> {
            // Umesto da zovemo ViewModel, sada radimo sve direktno odavde

            // 1. Prvo pozivamo ViewModel da samo upiše pozivnicu u bazu
            viewModel.sendAllianceInvite(friend);

            // 2. Odmah nakon toga, DIREKTNO pozivamo NotificationSender
            Alliance alliance = viewModel.getCurrentAlliance().getValue();
            User currentUser = viewModel.getCurrentUserData().getValue(); // Uzimamo trenutnog korisnika

            if (alliance != null && currentUser != null) {
                String title = "Alliance Invitation";
                String message = currentUser.getUsername() + " has invited you to join the alliance '" + alliance.getName() + "'.";

                // Pozivamo NotificationSender sa podacima i context-om iz fragmenta
                NotificationSender.sendNotificationToUser(requireContext(), friend.getUserId(), title, message);
            }
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