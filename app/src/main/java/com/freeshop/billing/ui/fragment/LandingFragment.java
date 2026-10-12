package com.freeshop.billing.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.freeshop.billing.R;
import com.freeshop.billing.data.SessionStore;
import com.freeshop.billing.databinding.FragmentLandingBinding;

public class LandingFragment extends Fragment {

    private FragmentLandingBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLandingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = Navigation.findNavController(view);
        SessionStore session = new SessionStore(requireContext());
        Long savedShopId = session.getShopId();

        if (savedShopId != null && savedShopId > 0) {
            LandingFragmentDirections.ActionLandingFragmentToDashboardFragment action =
                    LandingFragmentDirections.actionLandingFragmentToDashboardFragment(savedShopId);
            navController.navigate(action);
            return;
        }

        binding.btnCreateShop.setOnClickListener(v ->
                navController.navigate(R.id.action_landingFragment_to_registerFragment));

        binding.btnLoginShop.setOnClickListener(v ->
                navController.navigate(R.id.action_landingFragment_to_loginFragment));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
