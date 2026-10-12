package com.freeshop.billing.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.freeshop.billing.R;
import com.freeshop.billing.data.db.entity.Shop;
import com.freeshop.billing.databinding.FragmentSettingsBinding;
import com.freeshop.billing.ui.util.ToastHelper;
import com.freeshop.billing.ui.viewmodel.AuthViewModel;
import com.freeshop.billing.ui.viewmodel.SettingsViewModel;

import java.io.File;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private SettingsViewModel viewModel;
    private AuthViewModel authViewModel;
    private long shopId;
    private boolean isUpdatingUi = false;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null && viewModel != null) {
                    viewModel.pickLogo(uri, err -> ToastHelper.show(requireActivity(), err, true));
                }
            });

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            SettingsFragmentArgs args = SettingsFragmentArgs.fromBundle(getArguments());
            shopId = args.getShopId();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = Navigation.findNavController(view);
        viewModel = new ViewModelProvider(this, new SettingsViewModel.Factory(requireContext(), shopId))
                .get(SettingsViewModel.class);
        authViewModel = new ViewModelProvider(this, new AuthViewModel.Factory(requireContext()))
                .get(AuthViewModel.class);

        binding.topBar.topBar.setTitle(R.string.title_settings);
        binding.topBar.topBar.setNavigationIcon(R.drawable.ic_arrow_back);
        binding.topBar.topBar.setNavigationOnClickListener(v -> navController.popBackStack());

        binding.topBar.topBar.inflateMenu(R.menu.menu_settings);
        binding.topBar.topBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_logout) {
                authViewModel.logout(() ->
                        navController.navigate(R.id.action_settingsFragment_to_landingFragment));
                return true;
            }
            return false;
        });

        binding.btnChangeLogo.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        binding.switchTax.setOnCheckedChangeListener((buttonView, isChecked) ->
                binding.layoutTaxDetails.setVisibility(isChecked ? View.VISIBLE : View.GONE));

        binding.btnSaveSettings.setOnClickListener(v -> saveSettings());

        viewModel.getShop().observe(getViewLifecycleOwner(), this::renderShop);

        viewModel.getSaving().observe(getViewLifecycleOwner(), saving -> {
            boolean isSaving = Boolean.TRUE.equals(saving);
            binding.btnSaveSettings.setEnabled(!isSaving);
            binding.btnSaveSettings.setText(isSaving ? R.string.btn_saving_settings : R.string.btn_save_settings);
        });
    }

    private void renderShop(Shop shop) {
        if (shop == null) return;
        isUpdatingUi = true;

        if (shop.getLogoPath() != null && !shop.getLogoPath().isEmpty()) {
            File file = new File(shop.getLogoPath());
            if (file.exists()) {
                Glide.with(this).load(file).into(binding.ivLogo);
            } else {
                binding.ivLogo.setImageResource(R.drawable.ic_store);
            }
        } else {
            binding.ivLogo.setImageResource(R.drawable.ic_store);
        }

        setTextIfNotEqual(binding.etShopName, shop.getName());
        setTextIfNotEqual(binding.etPhone, shop.getPhone());
        setTextIfNotEqual(binding.etGstin, shop.getGstin());
        setTextIfNotEqual(binding.etAddress, shop.getAddress());
        setTextIfNotEqual(binding.etCity, shop.getCity());
        setTextIfNotEqual(binding.etState, shop.getState());
        setTextIfNotEqual(binding.etPincode, shop.getPincode());
        setTextIfNotEqual(binding.etPrefix, shop.getInvoicePrefix());

        binding.tvNextInvoiceNo.setText(getString(R.string.label_next_invoice_no, shop.getNextInvoiceNo()));

        binding.switchTax.setChecked(shop.isTaxEnabled());
        binding.layoutTaxDetails.setVisibility(shop.isTaxEnabled() ? View.VISIBLE : View.GONE);
        setTextIfNotEqual(binding.etTaxLabel, shop.getTaxLabel());
        setTextIfNotEqual(binding.etTaxRate, String.valueOf(shop.getTaxRate()));

        setTextIfNotEqual(binding.etBankName, shop.getBankName());
        setTextIfNotEqual(binding.etAccountNo, shop.getAccountNo());
        setTextIfNotEqual(binding.etIfsc, shop.getIfsc());
        setTextIfNotEqual(binding.etUpiId, shop.getUpiId());
        setTextIfNotEqual(binding.etTerms, shop.getTerms());

        isUpdatingUi = false;
    }

    private void setTextIfNotEqual(TextView tv, String text) {
        if (!tv.hasFocus() && !tv.getText().toString().equals(text != null ? text : "")) {
            tv.setText(text != null ? text : "");
        }
    }

    private void saveSettings() {
        Shop current = viewModel.getShop().getValue();
        if (current == null) return;

        Shop updated = new Shop();
        updated.setId(current.getId());
        updated.setEmail(current.getEmail());
        updated.setPasswordHash(current.getPasswordHash());
        updated.setPasswordSalt(current.getPasswordSalt());
        updated.setLogoPath(current.getLogoPath());
        updated.setNextInvoiceNo(current.getNextInvoiceNo());

        String name = binding.etShopName.getText() != null ? binding.etShopName.getText().toString().trim() : "";
        updated.setName(name);
        updated.setPhone(binding.etPhone.getText() != null ? binding.etPhone.getText().toString().trim() : "");
        updated.setGstin(binding.etGstin.getText() != null ? binding.etGstin.getText().toString().trim() : "");
        updated.setAddress(binding.etAddress.getText() != null ? binding.etAddress.getText().toString().trim() : "");
        updated.setCity(binding.etCity.getText() != null ? binding.etCity.getText().toString().trim() : "");
        updated.setState(binding.etState.getText() != null ? binding.etState.getText().toString().trim() : "");
        updated.setPincode(binding.etPincode.getText() != null ? binding.etPincode.getText().toString().trim() : "");
        updated.setInvoicePrefix(binding.etPrefix.getText() != null ? binding.etPrefix.getText().toString().trim() : "INV");

        boolean taxEnabled = binding.switchTax.isChecked();
        updated.setTaxEnabled(taxEnabled);
        updated.setTaxLabel(binding.etTaxLabel.getText() != null ? binding.etTaxLabel.getText().toString().trim() : "GST");

        double taxRate = current.getTaxRate();
        try {
            if (binding.etTaxRate.getText() != null) {
                taxRate = Double.parseDouble(binding.etTaxRate.getText().toString().trim());
            }
        } catch (Exception ignored) {}
        updated.setTaxRate(taxRate);

        updated.setBankName(binding.etBankName.getText() != null ? binding.etBankName.getText().toString().trim() : "");
        updated.setAccountNo(binding.etAccountNo.getText() != null ? binding.etAccountNo.getText().toString().trim() : "");
        updated.setIfsc(binding.etIfsc.getText() != null ? binding.etIfsc.getText().toString().trim() : "");
        updated.setUpiId(binding.etUpiId.getText() != null ? binding.etUpiId.getText().toString().trim() : "");
        updated.setTerms(binding.etTerms.getText() != null ? binding.etTerms.getText().toString().trim() : "");

        viewModel.save(
                updated,
                () -> ToastHelper.show(requireActivity(), getString(R.string.toast_settings_saved), false),
                err -> ToastHelper.show(requireActivity(), err, true)
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
