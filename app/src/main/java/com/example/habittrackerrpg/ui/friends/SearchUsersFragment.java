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
import com.example.habittrackerrpg.databinding.FragmentSearchUsersBinding;

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
        var users = viewModel.getSearchResults().getValue();
        var relatedData = viewModel.getRelatedData().getValue();

        if (users != null && relatedData != null) {
            adapter.setData(users, relatedData.friends, relatedData.sentRequests, relatedData.currentUser.getId());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}