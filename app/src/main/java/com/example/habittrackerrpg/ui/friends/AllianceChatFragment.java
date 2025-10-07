package com.example.habittrackerrpg.ui.friends;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.habittrackerrpg.databinding.FragmentAllianceChatBinding;

public class AllianceChatFragment extends Fragment {

    private FragmentAllianceChatBinding binding;
    private FriendsViewModel viewModel;
    private ChatAdapter chatAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAllianceChatBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(FriendsViewModel.class);

        setupRecyclerView();
        setupClickListeners();

        viewModel.getChatMessages().observe(getViewLifecycleOwner(), messages -> {
            if (messages != null) {
                chatAdapter.setMessages(messages);
                if (!messages.isEmpty()) {
                    binding.recyclerViewChat.scrollToPosition(messages.size() - 1);
                }
            }
        });
        // viewModel.loadChatMessages();
    }

    private void setupRecyclerView() {
        chatAdapter = new ChatAdapter();
        binding.recyclerViewChat.setAdapter(chatAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        binding.recyclerViewChat.setLayoutManager(layoutManager);
    }

    private void setupClickListeners() {
        binding.buttonSendMessage.setOnClickListener(v -> {
            String messageText = binding.editTextMessage.getText().toString().trim();
            if (!messageText.isEmpty()) {
                viewModel.sendMessage(messageText);
                binding.editTextMessage.setText("");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}