package com.freeshop.billing.ui.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.freeshop.billing.R;
import com.freeshop.billing.data.db.entity.Customer;
import com.freeshop.billing.data.db.entity.Product;
import com.freeshop.billing.data.db.entity.Shop;
import com.freeshop.billing.databinding.FragmentDashboardBinding;
import com.freeshop.billing.domain.Money;
import com.freeshop.billing.ui.adapter.DraftItemsAdapter;
import com.freeshop.billing.ui.util.ToastHelper;
import com.freeshop.billing.ui.viewmodel.AuthViewModel;
import com.freeshop.billing.ui.viewmodel.DashboardUiState;
import com.freeshop.billing.ui.viewmodel.DashboardViewModel;
import com.freeshop.billing.ui.viewmodel.DraftItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private DashboardViewModel viewModel;
    private AuthViewModel authViewModel;
    private DraftItemsAdapter itemsAdapter;
    private long shopId;
    private boolean isUpdatingUi = false;

    private static final List<String> PAYMENT_MODES = Arrays.asList("CASH", "UPI", "CARD", "NET BANKING", "CREDIT");

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            DashboardFragmentArgs args = DashboardFragmentArgs.fromBundle(getArguments());
            shopId = args.getShopId();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = Navigation.findNavController(view);
        viewModel = new ViewModelProvider(this, new DashboardViewModel.Factory(requireContext(), shopId))
                .get(DashboardViewModel.class);
        authViewModel = new ViewModelProvider(this, new AuthViewModel.Factory(requireContext()))
                .get(AuthViewModel.class);

        setupTopBar(navController);
        setupRecyclerView();
        setupInputListeners();
        setupPaymentModeDropdown();
        setupDocumentTypeToggle();
        setupActions();
        setupKeyShortcut();

        viewModel.getState().observe(getViewLifecycleOwner(), this::renderState);
    }

    private void setupTopBar(NavController navController) {
        binding.topBar.topBar.inflateMenu(R.menu.menu_dashboard);
        binding.topBar.topBar.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_customers) {
                DashboardFragmentDirections.ActionDashboardFragmentToCustomersFragment action =
                        DashboardFragmentDirections.actionDashboardFragmentToCustomersFragment(shopId);
                navController.navigate(action);
                return true;
            } else if (itemId == R.id.action_items) {
                DashboardFragmentDirections.ActionDashboardFragmentToItemsFragment action =
                        DashboardFragmentDirections.actionDashboardFragmentToItemsFragment(shopId);
                navController.navigate(action);
                return true;
            } else if (itemId == R.id.action_bills) {
                DashboardFragmentDirections.ActionDashboardFragmentToBillsFragment action =
                        DashboardFragmentDirections.actionDashboardFragmentToBillsFragment(shopId);
                navController.navigate(action);
                return true;
            } else if (itemId == R.id.action_settings) {
                DashboardFragmentDirections.ActionDashboardFragmentToSettingsFragment action =
                        DashboardFragmentDirections.actionDashboardFragmentToSettingsFragment(shopId);
                navController.navigate(action);
                return true;
            } else if (itemId == R.id.action_logout) {
                authViewModel.logout(() ->
                        navController.navigate(R.id.action_dashboardFragment_to_landingFragment));
                return true;
            }
            return false;
        });
    }

    private void setupRecyclerView() {
        itemsAdapter = new DraftItemsAdapter(requireContext(), new DraftItemsAdapter.OnDraftItemChangeListener() {
            @Override
            public void onItemChanged(DraftItem item) {
                viewModel.updateItem(item.getId(), item);
            }

            @Override
            public void onRemoveItem(long itemId) {
                viewModel.removeItem(itemId);
            }

            @Override
            public void onProductAutofill(long draftId, Product product) {
                viewModel.autofillProduct(draftId, product);
            }
        });

        binding.rvDraftItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvDraftItems.setAdapter(itemsAdapter);
        binding.btnAddRow.setOnClickListener(v -> viewModel.addItem());
    }

    private void setupInputListeners() {
        binding.actvCustomerName.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (!isUpdatingUi) viewModel.updateCustomerName(s.toString());
            }
        });

        binding.etCustomerPhone.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (!isUpdatingUi) viewModel.updateCustomerPhone(s.toString());
            }
        });

        binding.etCustomerGstin.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (!isUpdatingUi) viewModel.updateCustomerGstin(s.toString());
            }
        });

        binding.etCustomerAddress.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (!isUpdatingUi) viewModel.updateCustomerAddress(s.toString());
            }
        });

        binding.etBillNo.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (!isUpdatingUi) viewModel.updateBillNo(s.toString());
            }
        });

        binding.etDate.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (!isUpdatingUi) viewModel.updateDate(s.toString());
            }
        });

        binding.etDiscount.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (!isUpdatingUi) viewModel.updateDiscount(s.toString());
            }
        });

        binding.etReceived.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (!isUpdatingUi) viewModel.updateReceived(s.toString());
            }
        });

        binding.etNotes.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (!isUpdatingUi) viewModel.updateNotes(s.toString());
            }
        });
    }

    private void setupPaymentModeDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, PAYMENT_MODES);
        binding.actvPaymentMode.setAdapter(adapter);
        binding.actvPaymentMode.setOnItemClickListener((parent, view, position, id) -> {
            String selected = PAYMENT_MODES.get(position);
            viewModel.updatePaymentMode(selected);
        });
    }

    private void setupDocumentTypeToggle() {
        binding.btnTypeInvoice.setOnClickListener(v -> viewModel.updateDocumentType("INVOICE"));
        binding.btnTypeQuotation.setOnClickListener(v -> viewModel.updateDocumentType("QUOTATION"));
    }

    private void setupActions() {
        binding.btnReset.setOnClickListener(v -> showResetDialog());
        binding.btnGenerate.setOnClickListener(v -> viewModel.generate(null));
    }

    private void showResetDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.dialog_reset_title)
                .setMessage(R.string.dialog_reset_message)
                .setPositiveButton(R.string.btn_reset, (dialog, which) -> viewModel.resetForm())
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void setupKeyShortcut() {
        binding.dashboardRoot.setFocusableInTouchMode(true);
        binding.dashboardRoot.requestFocus();
        binding.dashboardRoot.setOnKeyListener((v, keyCode, event) -> {
            if (event.isCtrlPressed() && keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                viewModel.generate(null);
                return true;
            }
            return false;
        });
    }

    private void renderState(DashboardUiState state) {
        if (state == null) return;
        isUpdatingUi = true;

        boolean isQuotation = "QUOTATION".equals(state.getDocumentType());
        binding.topBar.topBar.setTitle(isQuotation ? R.string.title_new_quotation : R.string.title_new_bill);

        // Document type toggle styling
        if (isQuotation) {
            binding.btnTypeQuotation.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.AccentNavy));
            binding.btnTypeQuotation.setTextColor(ContextCompat.getColor(requireContext(), R.color.CardWhite));
            binding.btnTypeInvoice.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.transparent));
            binding.btnTypeInvoice.setTextColor(ContextCompat.getColor(requireContext(), R.color.TextDark));
        } else {
            binding.btnTypeInvoice.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.AccentNavy));
            binding.btnTypeInvoice.setTextColor(ContextCompat.getColor(requireContext(), R.color.CardWhite));
            binding.btnTypeQuotation.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.transparent));
            binding.btnTypeQuotation.setTextColor(ContextCompat.getColor(requireContext(), R.color.TextDark));
        }

        binding.tvDetailsSectionTitle.setText(isQuotation ? R.string.section_quotation_details : R.string.section_bill_details);
        binding.layoutPaymentMode.setVisibility(isQuotation ? View.GONE : View.VISIBLE);
        binding.layoutReceived.setVisibility(isQuotation ? View.GONE : View.VISIBLE);
        binding.rowSumReceived.setVisibility(isQuotation ? View.GONE : View.VISIBLE);
        binding.rowSumBalance.setVisibility(isQuotation ? View.GONE : View.VISIBLE);

        // Customer Autocomplete setup
        List<String> customerNames = new ArrayList<>();
        for (Customer c : state.getCustomers()) {
            customerNames.add(c.getName() + (c.getPhone().isEmpty() ? "" : " | " + c.getPhone()));
        }
        ArrayAdapter<String> customerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, customerNames);
        binding.actvCustomerName.setAdapter(customerAdapter);
        binding.actvCustomerName.setOnItemClickListener((parent, view, position, id) -> {
            if (position < state.getCustomers().size()) {
                viewModel.selectCustomer(state.getCustomers().get(position));
            }
        });

        // Sync inputs if not focused
        setTextIfNotEqual(binding.actvCustomerName, state.getCustomerName());
        setTextIfNotEqual(binding.etCustomerPhone, state.getCustomerPhone());
        setTextIfNotEqual(binding.etCustomerGstin, state.getCustomerGstin());
        setTextIfNotEqual(binding.etCustomerAddress, state.getCustomerAddress());
        setTextIfNotEqual(binding.etBillNo, state.getBillNo());
        setTextIfNotEqual(binding.etDate, state.getDate());
        setTextIfNotEqual(binding.etDiscount, state.getDiscount());
        setTextIfNotEqual(binding.actvPaymentMode, state.getPaymentMode());
        setTextIfNotEqual(binding.etReceived, state.getReceived());
        setTextIfNotEqual(binding.etNotes, state.getNotes());

        // Items table
        boolean taxEnabled = state.getShop() != null && state.getShop().isTaxEnabled();
        binding.tvHeaderTaxRate.setVisibility(taxEnabled ? View.VISIBLE : View.GONE);
        itemsAdapter.setItems(state.getDraftItems(), taxEnabled, state.getProducts());

        // Live Summary Card
        binding.tvSumCustomer.setText(state.getCustomerName().trim().isEmpty() ? getString(R.string.walk_in_customer) : state.getCustomerName().trim());
        binding.tvSumBillNo.setText(state.getBillNo());
        binding.tvSumDate.setText(state.getDate());

        // Subtotal, Discount, Tax
        double subtotal = state.getSubtotal();
        binding.tvSumSubtotal.setText(Money.format(subtotal));

        double discount = 0.0;
        try { discount = Double.parseDouble(state.getDiscount()); } catch (Exception ignored) {}
        if (discount > 0) {
            binding.rowSumDiscount.setVisibility(View.VISIBLE);
            binding.tvSumDiscount.setText("- " + Money.format(discount));
        } else {
            binding.rowSumDiscount.setVisibility(View.GONE);
        }

        double tax = state.getTaxAmount();
        if (taxEnabled && tax > 0) {
            binding.rowSumTax.setVisibility(View.VISIBLE);
            String label = (state.getShop() != null && !state.getShop().getTaxLabel().isEmpty()) ? state.getShop().getTaxLabel() : "Tax";
            binding.tvSumTaxLabel.setText(label);
            binding.tvSumTaxAmount.setText(Money.format(tax));
        } else {
            binding.rowSumTax.setVisibility(View.GONE);
        }

        double grandTotal = state.getGrandTotal();
        binding.tvSumGrandTotal.setText(Money.format(grandTotal));

        if (!isQuotation) {
            binding.tvSumReceived.setText(Money.format(state.getReceivedAmount()));
            double balance = state.getBalance();
            binding.tvSumBalance.setText(Money.format(balance));
            binding.tvSumBalance.setTextColor(ContextCompat.getColor(requireContext(), balance > 0.01 ? R.color.DangerRed : R.color.OkGreen));
        }

        binding.tvSumAmountInWords.setText(Money.amountInWords(grandTotal));

        binding.layoutSavedPdfIndicator.setVisibility(
                (state.getSavedPdfPath() != null && !state.getSavedPdfPath().isEmpty()) ? View.VISIBLE : View.GONE);

        // Generating Overlay
        binding.loadingOverlay.setVisibility(state.isGenerating() ? View.VISIBLE : View.GONE);

        // Toast handling
        if (state.getToastMessage() != null) {
            ToastHelper.show(requireActivity(), state.getToastMessage(), state.isToastError());
            viewModel.clearToast();
        }

        isUpdatingUi = false;
    }

    private void setTextIfNotEqual(TextView tv, String text) {
        if (!tv.hasFocus() && !tv.getText().toString().equals(text)) {
            tv.setText(text);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private static abstract class SimpleTextWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int count, int after) {}
    }
}
