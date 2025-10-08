package com.example.habittrackerrpg.ui.friends;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.habittrackerrpg.data.model.FriendRequest;
import androidx.navigation.Navigation;
import com.example.habittrackerrpg.R;
import com.example.habittrackerrpg.data.model.User;
import com.example.habittrackerrpg.databinding.FragmentMyFriendsBinding;

import java.util.ArrayList;

public class MyFriendsFragment extends Fragment {

    private FragmentMyFriendsBinding binding;
    private FriendsViewModel viewModel;
    private FriendRequestAdapter requestAdapter;
    private FriendAdapter friendAdapter;
    private AllianceMemberAdapter allianceMemberAdapter;

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
        binding.buttonInviteFriends.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_friends_to_invite);
        });
        binding.buttonGoToChat.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.nav_alliance_chat);
        });

        binding.buttonStartSpecialMission.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Start Special Mission?")
                    .setMessage("This will start a 2-week mission for the entire alliance. This action cannot be undone.")
                    .setPositiveButton("Start", (dialog, which) -> viewModel.startSpecialMission())
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        binding.buttonViewSpecialMission.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_friendsHostFragment_to_specialMissionFragment);
        });
    }

    private void setupRecyclerViews() {
        requestAdapter = new FriendRequestAdapter();
        binding.recyclerViewRequests.setAdapter(requestAdapter);

        friendAdapter = new FriendAdapter();
        binding.recyclerViewFriends.setAdapter(friendAdapter);

        allianceMemberAdapter = new AllianceMemberAdapter();
        binding.recyclerViewAllianceMembers.setAdapter(allianceMemberAdapter);


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
        friendAdapter.setOnFriendClickListener(friend -> {
            Bundle bundle = new Bundle();
            bundle.putString("userId", friend.getUserId());

            Navigation.findNavController(requireView()).navigate(R.id.action_friends_to_profile, bundle);
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

        viewModel.getCurrentAlliance().observe(getViewLifecycleOwner(), alliance -> {
            if (alliance == null) {
                binding.layoutInAlliance.setVisibility(View.GONE);
                binding.layoutNoAlliance.setVisibility(View.VISIBLE);
                binding.buttonCreateAlliance.setOnClickListener(v -> showCreateAllianceDialog());
            } else {
                binding.layoutNoAlliance.setVisibility(View.GONE);
                binding.layoutInAlliance.setVisibility(View.VISIBLE);
                binding.textViewAllianceName.setText(alliance.getName());
                allianceMemberAdapter.setData(new ArrayList<>(alliance.getMembers().values()), alliance.getLeaderId());

                User currentUser = viewModel.getCurrentUserData().getValue();

                if (currentUser != null) {
                    binding.buttonAllianceAction.setVisibility(View.VISIBLE);

                    if (alliance.getLeaderId().equals(currentUser.getId())) {
                        binding.buttonAllianceAction.setText("Disband Alliance");
                        binding.buttonAllianceAction.setOnClickListener(v -> {
                            new AlertDialog.Builder(requireContext())
                                    .setTitle("Disband Alliance?")
                                    .setMessage("Are you sure? This will remove all members and permanently delete the alliance.")
                                    .setPositiveButton("Yes, Disband", (dialog, which) -> viewModel.onDisbandAllianceClicked())
                                    .setNegativeButton("Cancel", null)
                                    .show();
                        });
                    } else {
                        binding.buttonAllianceAction.setText("Leave Alliance");
                        binding.buttonAllianceAction.setOnClickListener(v -> {
                            new AlertDialog.Builder(requireContext())
                                    .setTitle("Leave Alliance?")
                                    .setMessage("Are you sure you want to leave this alliance?")
                                    .setPositiveButton("Yes, Leave", (dialog, which) -> viewModel.onLeaveAllianceClicked())
                                    .setNegativeButton("Cancel", null)
                                    .show();
                        });
                    }

                    boolean isLeader = alliance.getLeaderId().equals(currentUser.getId());
                    boolean isMissionActive = alliance.getActiveMissionId() != null;

                    binding.buttonStartSpecialMission.setVisibility(isLeader && !isMissionActive ? View.VISIBLE : View.GONE);
                    binding.buttonViewSpecialMission.setVisibility(isMissionActive ? View.VISIBLE : View.GONE);

                } else {
                    binding.buttonAllianceAction.setVisibility(View.GONE);
                }
            }
        });

        viewModel.getToastMessage().observe(getViewLifecycleOwner(), event -> {
            String message = event.getContentIfNotHandled();
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCreateAllianceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Create New Alliance");

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Enter alliance name");
        builder.setView(input);

        builder.setPositiveButton("Create", (dialog, which) -> {
            String allianceName = input.getText().toString();
            viewModel.createAlliance(allianceName);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}