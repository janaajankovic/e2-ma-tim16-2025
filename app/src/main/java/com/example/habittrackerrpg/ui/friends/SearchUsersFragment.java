package com.example.habittrackerrpg.ui.friends;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.example.habittrackerrpg.R;
import com.example.habittrackerrpg.databinding.FragmentSearchUsersBinding;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class SearchUsersFragment extends Fragment {

    private FragmentSearchUsersBinding binding;
    private FriendsViewModel viewModel;
    private UserSearchAdapter adapter;

    private final ActivityResultLauncher<Intent> qrCodeScannerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                IntentResult scanResult = IntentIntegrator.parseActivityResult(result.getResultCode(), result.getData());
                if (scanResult != null && scanResult.getContents() != null) {
                    String scannedUsername = scanResult.getContents();
                    binding.searchView.setQuery(scannedUsername, true);
                } else {
                    Toast.makeText(getContext(), "Scan cancelled", Toast.LENGTH_SHORT).show();
                }
            });

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

        binding.buttonScanQr.setOnClickListener(v -> startQrScanner());
    }

    private void startQrScanner() {
        IntentIntegrator integrator = IntentIntegrator.forSupportFragment(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Scan a friend's QR code");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(true);
        Intent intent = integrator.createScanIntent();
        qrCodeScannerLauncher.launch(intent);
    }

    private void setupRecyclerView() {
        adapter = new UserSearchAdapter();
        binding.recyclerViewSearch.setAdapter(adapter);

        adapter.setOnAddFriendClickListener(user -> {
            viewModel.sendFriendRequest(user.getId());
        });

        adapter.setOnUserClickListener(user -> {
            Bundle bundle = new Bundle();
            bundle.putString("userId", user.getId());
            Navigation.findNavController(requireView()).navigate(R.id.action_friends_to_profile, bundle);
        });
    }

    private void setupSearchView() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query != null && query.length() >= 3) {
                    viewModel.searchUsers(query);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText != null && newText.length() >= 3) {
                    viewModel.searchUsers(newText);
                } else if (newText != null && newText.isEmpty()) {
                    viewModel.searchUsers("");
                }
                return true;
            }
        });
    }

    private void setupObservers() {
        viewModel.getSearchResults().observe(getViewLifecycleOwner(), results -> {
            String TAG = "MyDebug-Fragment";
            if (results != null) {
                Log.d(TAG, "Fragment primio listu od ViewModel-a. Broj rezultata: " + results.size());
                adapter.setResults(results);
            } else {
                Log.w(TAG, "Fragment primio NULL listu od ViewModel-a.");
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}