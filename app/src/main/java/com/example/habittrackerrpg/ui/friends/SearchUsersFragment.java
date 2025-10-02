package com.example.habittrackerrpg.ui.friends;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.example.habittrackerrpg.R;

import com.example.habittrackerrpg.data.model.User;
import com.example.habittrackerrpg.databinding.FragmentSearchUsersBinding;

import java.util.ArrayList;
import java.util.List;

public class SearchUsersFragment extends Fragment {

    private FragmentSearchUsersBinding binding;
    private FriendsViewModel viewModel;
    private UserSearchAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchUsersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(FriendsViewModel.class);

        setupRecyclerView();
        setupSearchView();
        setupObservers();
    }

    private void setupRecyclerView() {
        adapter = new UserSearchAdapter();
        binding.recyclerViewSearch.setAdapter(adapter);

        adapter.setListener(user -> {
            viewModel.sendFriendRequest(user.getId());
            Toast.makeText(getContext(), "Friend request sent to " + user.getUsername(), Toast.LENGTH_SHORT).show();
        });
        adapter.setUserClickListener(user -> {
            Bundle bundle = new Bundle();
            bundle.putString("userId", user.getId());
            Navigation.findNavController(requireView()).navigate(R.id.action_friends_to_profile, bundle);
        });
    }

    private void setupSearchView() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                viewModel.searchUsers(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty() || newText.length() < 3) {
                    viewModel.searchUsers("");
                }
                return false;
            }
        });
    }

    private void setupObservers() {
        viewModel.getSearchResults().observe(getViewLifecycleOwner(), users -> {
            updateAdapterData();
        });

        viewModel.getRelatedData().observe(getViewLifecycleOwner(), relatedData -> {
            updateAdapterData();
        });
    }

    private void updateAdapterData() {
        List<User> users = viewModel.getSearchResults().getValue();
        var relatedData = viewModel.getRelatedData().getValue();

        if (users != null && relatedData != null) {
            String currentUserId = relatedData.currentUser.getId();
            List<User> filteredUsers = new ArrayList<>();
            for (User user : users) {
                if (!user.getId().equals(currentUserId)) {
                    filteredUsers.add(user);
                }
            }
            adapter.setData(filteredUsers, relatedData.friends, relatedData.sentRequests, currentUserId);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}