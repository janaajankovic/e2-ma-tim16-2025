package com.example.habittrackerrpg.ui.auth;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.habittrackerrpg.MainActivity;
import com.example.habittrackerrpg.R;
import com.example.habittrackerrpg.databinding.FragmentLoginBinding;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private LoginFragmentListener listener;
    private AuthViewModel authViewModel;

    public interface LoginFragmentListener {
        void navigateToRegister();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof LoginFragmentListener) {
            listener = (LoginFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement LoginFragmentListener");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        binding.buttonLogin.setOnClickListener(v -> {
            String email = binding.editTextEmail.getText().toString().trim();
            String password = binding.editTextPassword.getText().toString();

            // resetujemo poruku
            binding.textErrorLogin.setVisibility(View.GONE);

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                binding.textErrorLogin.setText(getString(R.string.error_empty_fields));
                binding.textErrorLogin.setVisibility(View.VISIBLE);
                return;
            }

            authViewModel.loginUser(email, password);

            authViewModel.loginSuccess.observe(getViewLifecycleOwner(), isSuccess -> {
                if (isSuccess) {
                    startActivity(new Intent(getActivity(), MainActivity.class));
                    requireActivity().finish();
                } else {
                    authViewModel.isUnverifiedAccount.observe(getViewLifecycleOwner(), isUnverified -> {
                        if (Boolean.TRUE.equals(isUnverified)) {
                            binding.textErrorLogin.setText(getString(R.string.error_unverified_account));
                        } else {
                            binding.textErrorLogin.setText(getString(R.string.error_invalid_credentials));
                        }
                        binding.textErrorLogin.setVisibility(View.VISIBLE);
                    });

                }
            });
        });

        binding.textViewRegisterPrompt.setOnClickListener(v -> {
            if (listener != null) {
                listener.navigateToRegister();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
