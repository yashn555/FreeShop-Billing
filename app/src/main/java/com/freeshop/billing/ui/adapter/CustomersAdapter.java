package com.freeshop.billing.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.freeshop.billing.data.db.entity.Customer;
import com.freeshop.billing.databinding.ItemCustomerBinding;

import java.util.ArrayList;
import java.util.List;

public class CustomersAdapter extends RecyclerView.Adapter<CustomersAdapter.CustomerViewHolder> {

    public interface OnCustomerDeleteListener {
        void onDelete(Customer customer);
    }

    private final List<Customer> customers = new ArrayList<>();
    private final OnCustomerDeleteListener listener;

    public CustomersAdapter(OnCustomerDeleteListener listener) {
        this.listener = listener;
    }

    public void setCustomers(List<Customer> newCustomers) {
        this.customers.clear();
        if (newCustomers != null) {
            this.customers.addAll(newCustomers);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCustomerBinding binding = ItemCustomerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CustomerViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerViewHolder holder, int position) {
        holder.bind(customers.get(position));
    }

    @Override
    public int getItemCount() {
        return customers.size();
    }

    class CustomerViewHolder extends RecyclerView.ViewHolder {
        private final ItemCustomerBinding binding;

        CustomerViewHolder(ItemCustomerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Customer customer) {
            binding.tvCustomerName.setText(customer.getName());

            if (customer.getPhone() != null && !customer.getPhone().isEmpty()) {
                binding.tvCustomerPhone.setVisibility(View.VISIBLE);
                binding.tvCustomerPhone.setText(customer.getPhone());
            } else {
                binding.tvCustomerPhone.setVisibility(View.GONE);
            }

            if (customer.getGstin() != null && !customer.getGstin().isEmpty()) {
                binding.tvCustomerGstin.setVisibility(View.VISIBLE);
                binding.tvCustomerGstin.setText("GSTIN: " + customer.getGstin());
            } else {
                binding.tvCustomerGstin.setVisibility(View.GONE);
            }

            binding.btnDeleteCustomer.setOnClickListener(v -> listener.onDelete(customer));
        }
    }
}
