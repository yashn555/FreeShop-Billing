package com.freeshop.billing.ui.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.freeshop.billing.R;
import com.freeshop.billing.databinding.FragmentRegisterBinding;
import com.freeshop.billing.ui.viewmodel.AuthViewModel;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private AuthViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this, new AuthViewModel.Factory(requireContext())).get(AuthViewModel.class);
        NavController navController = Navigation.findNavController(view);

        binding.topBar.topBar.setTitle(R.string.title_create_shop);
        binding.topBar.topBar.setNavigationIcon(R.drawable.ic_arrow_back);
        binding.topBar.topBar.setNavigationOnClickListener(v -> navController.popBackStack());

        TextWatcher clearErrorWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int count, int after) {
                viewModel.clearError();
            }
            @Override public void afterTextChanged(Editable s) {}
        };

        binding.etShopName.addTextChangedListener(clearErrorWatcher);
        binding.etEmail.addTextChangedListener(clearErrorWatcher);
        binding.etPassword.addTextChangedListener(clearErrorWatcher);
        binding.etConfirmPassword.addTextChangedListener(clearErrorWatcher);

        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            boolean isLoading = Boolean.TRUE.equals(loading);
            binding.loadingOverlay.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnRegister.setEnabled(!isLoading);
            binding.btnRegister.setText(isLoading ? R.string.btn_creating : R.string.btn_create_shop);
        });

        viewModel.getError().observe(getViewLifecycleOwner(), err -> {
            if (err != null && !err.isEmpty()) {
                binding.tvError.setText(err);
                binding.tvError.setVisibility(View.VISIBLE);
            } else {
                binding.tvError.setVisibility(View.GONE);
            }
        });

        binding.btnRegister.setOnClickListener(v -> {
            String name = binding.etShopName.getText() != null ? binding.etShopName.getText().toString() : "";
            String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString() : "";
            String pass = binding.etPassword.getText() != null ? binding.etPassword.getText().toString() : "";
            String confirm = binding.etConfirmPassword.getText() != null ? binding.etConfirmPassword.getText().toString() : "";

            viewModel.register(name, email, pass, confirm, () -> {
                Long shopId = viewModel.getSessionShopId();
                if (shopId != null && shopId > 0) {
                    RegisterFragmentDirections.ActionRegisterFragmentToDashboardFragment action =
                            RegisterFragmentDirections.actionRegisterFragmentToDashboardFragment(shopId);
                    navController.navigate(action);
                }
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
