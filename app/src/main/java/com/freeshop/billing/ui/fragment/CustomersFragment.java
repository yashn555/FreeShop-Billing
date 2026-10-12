package com.freeshop.billing.ui.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
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
import com.freeshop.billing.data.db.entity.Customer;
import com.freeshop.billing.databinding.FragmentCustomersBinding;
import com.freeshop.billing.ui.adapter.CustomersAdapter;
import com.freeshop.billing.ui.util.ToastHelper;
import com.freeshop.billing.ui.viewmodel.CustomersViewModel;

public class CustomersFragment extends Fragment {

    private FragmentCustomersBinding binding;
    private CustomersViewModel viewModel;
    private CustomersAdapter adapter;
    private long shopId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            CustomersFragmentArgs args = CustomersFragmentArgs.fromBundle(getArguments());
            shopId = args.getShopId();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCustomersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = Navigation.findNavController(view);
        viewModel = new ViewModelProvider(this, new CustomersViewModel.Factory(requireContext(), shopId))
                .get(CustomersViewModel.class);

        binding.topBar.topBar.setTitle(R.string.title_customers);
        binding.topBar.topBar.setNavigationIcon(R.drawable.ic_arrow_back);
        binding.topBar.topBar.setNavigationOnClickListener(v -> navController.popBackStack());

        adapter = new CustomersAdapter(this::showDeleteDialog);
        binding.rvCustomers.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvCustomers.setAdapter(adapter);

        binding.btnAddCustomer.setOnClickListener(v -> {
            String name = binding.etCustomerName.getText() != null ? binding.etCustomerName.getText().toString() : "";
            String phone = binding.etCustomerPhone.getText() != null ? binding.etCustomerPhone.getText().toString() : "";
            String email = binding.etCustomerEmail.getText() != null ? binding.etCustomerEmail.getText().toString() : "";
            String gstin = binding.etCustomerGstin.getText() != null ? binding.etCustomerGstin.getText().toString() : "";
            String address = binding.etCustomerAddress.getText() != null ? binding.etCustomerAddress.getText().toString() : "";

            viewModel.add(name, phone, email, gstin, address, () -> {
                binding.etCustomerName.setText("");
                binding.etCustomerPhone.setText("");
                binding.etCustomerEmail.setText("");
                binding.etCustomerGstin.setText("");
                binding.etCustomerAddress.setText("");
                ToastHelper.show(requireActivity(), getString(R.string.toast_customer_added), false);
            });
        });

        viewModel.getCustomers().observe(getViewLifecycleOwner(), customers -> {
            adapter.setCustomers(customers);
            boolean isEmpty = (customers == null || customers.isEmpty());
            binding.rvCustomers.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            binding.emptyState.layoutEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            binding.emptyState.ivEmptyIcon.setImageResource(R.drawable.ic_people);
            binding.emptyState.tvEmptyMessage.setText(R.string.no_customers_yet);
        });

        viewModel.getError().observe(getViewLifecycleOwner(), err -> {
            if (err != null) {
                ToastHelper.show(requireActivity(), err, true);
                viewModel.clearError();
            }
        });
    }

    private void showDeleteDialog(Customer customer) {
        String msg = getString(R.string.dialog_delete_customer_msg, customer.getName());
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.dialog_delete_customer_title)
                .setMessage(msg)
                .setPositiveButton(R.string.btn_delete, (dialog, which) -> {
                    viewModel.delete(customer);
                    ToastHelper.show(requireActivity(), getString(R.string.toast_customer_deleted), false);
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
