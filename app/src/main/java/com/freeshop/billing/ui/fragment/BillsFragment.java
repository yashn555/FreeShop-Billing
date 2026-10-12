package com.freeshop.billing.ui.fragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.freeshop.billing.R;
import com.freeshop.billing.data.db.entity.Bill;
import com.freeshop.billing.databinding.FragmentBillsBinding;
import com.freeshop.billing.ui.adapter.BillsAdapter;
import com.freeshop.billing.ui.util.ToastHelper;
import com.freeshop.billing.ui.viewmodel.BillsViewModel;
import com.freeshop.billing.ui.viewmodel.DocTypeFilter;
import com.freeshop.billing.ui.viewmodel.PaymentStatusFilter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class BillsFragment extends Fragment {

    private FragmentBillsBinding binding;
    private BillsViewModel viewModel;
    private BillsAdapter adapter;
    private long shopId;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            BillsFragmentArgs args = BillsFragmentArgs.fromBundle(getArguments());
            shopId = args.getShopId();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBillsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = Navigation.findNavController(view);
        viewModel = new ViewModelProvider(this, new BillsViewModel.Factory(requireContext(), shopId))
                .get(BillsViewModel.class);

        binding.topBar.topBar.setTitle(R.string.title_bills_history);
        binding.topBar.topBar.setNavigationIcon(R.drawable.ic_arrow_back);
        binding.topBar.topBar.setNavigationOnClickListener(v -> navController.popBackStack());

        setupRecyclerView();
        setupSearch();
        setupChips();
        setupDatePickers();

        viewModel.getBills().observe(getViewLifecycleOwner(), bills -> {
            adapter.setBills(bills);
            boolean isEmpty = (bills == null || bills.isEmpty());
            binding.rvBills.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            binding.emptyState.layoutEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);

            if (isEmpty) {
                String q = binding.etSearch.getText() != null ? binding.etSearch.getText().toString() : "";
                String dFrom = viewModel.getDateFrom().getValue();
                String dTo = viewModel.getDateTo().getValue();
                boolean isFiltered = !q.isEmpty() || dFrom != null || dTo != null;
                binding.emptyState.tvEmptyMessage.setText(isFiltered ? R.string.no_matching_bills : R.string.no_bills_yet);
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new BillsAdapter(new BillsAdapter.OnBillActionListener() {
            @Override
            public void onView(Bill bill) {
                viewModel.viewBill(bill);
            }

            @Override
            public void onShare(Bill bill) {
                viewModel.shareBill(bill);
            }

            @Override
            public void onDelete(Bill bill) {
                showDeleteDialog(bill);
            }

            @Override
            public void onConvert(Bill bill) {
                viewModel.convertToInvoice(bill, () ->
                        ToastHelper.show(requireActivity(), getString(R.string.toast_converted_to_invoice), false));
            }
        });

        binding.rvBills.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvBills.setAdapter(adapter);
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int count, int after) {
                viewModel.setQuery(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupChips() {
        binding.chipGroupDocType.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chipInvoices)) {
                viewModel.setDocTypeFilter(DocTypeFilter.INVOICES);
            } else if (checkedIds.contains(R.id.chipQuotations)) {
                viewModel.setDocTypeFilter(DocTypeFilter.QUOTATIONS);
            } else {
                viewModel.setDocTypeFilter(DocTypeFilter.ALL);
            }
        });

        binding.chipGroupStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chipPaid)) {
                viewModel.setPaymentStatusFilter(PaymentStatusFilter.PAID);
            } else if (checkedIds.contains(R.id.chipPartial)) {
                viewModel.setPaymentStatusFilter(PaymentStatusFilter.PARTIAL);
            } else if (checkedIds.contains(R.id.chipUnpaid)) {
                viewModel.setPaymentStatusFilter(PaymentStatusFilter.UNPAID);
            } else {
                viewModel.setPaymentStatusFilter(PaymentStatusFilter.ALL);
            }
        });
    }

    private void setupDatePickers() {
        binding.btnDateFrom.setOnClickListener(v -> showDatePicker(viewModel.getDateFrom().getValue(), dateStr -> {
            viewModel.setDateFrom(dateStr);
            binding.btnDateFrom.setText("From: " + dateStr);
            binding.btnClearDates.setVisibility(View.VISIBLE);
        }));

        binding.btnDateTo.setOnClickListener(v -> showDatePicker(viewModel.getDateTo().getValue(), dateStr -> {
            viewModel.setDateTo(dateStr);
            binding.btnDateTo.setText("To: " + dateStr);
            binding.btnClearDates.setVisibility(View.VISIBLE);
        }));

        binding.btnClearDates.setOnClickListener(v -> {
            viewModel.clearDateRange();
            binding.btnDateFrom.setText(R.string.btn_from_date);
            binding.btnDateTo.setText(R.string.btn_to_date);
            binding.btnClearDates.setVisibility(View.GONE);
        });
    }

    private void showDatePicker(String initialDateStr, OnDateSelectedListener listener) {
        Calendar cal = Calendar.getInstance();
        if (initialDateStr != null && !initialDateStr.trim().isEmpty()) {
            try {
                Date d = dateFormat.parse(initialDateStr.trim());
                if (d != null) cal.setTime(d);
            } catch (Exception ignored) {}
        }

        new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    String formatted = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year);
                    listener.onDateSelected(formatted);
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private interface OnDateSelectedListener {
        void onDateSelected(String date);
    }

    private void showDeleteDialog(Bill bill) {
        String msg = getString(R.string.dialog_delete_bill_msg, bill.getBillNo());
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.dialog_delete_bill_title)
                .setMessage(msg)
                .setPositiveButton(R.string.btn_delete, (dialog, which) ->
                        viewModel.deleteBill(bill, () ->
                                ToastHelper.show(requireActivity(), getString(R.string.toast_bill_deleted), false)))
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
