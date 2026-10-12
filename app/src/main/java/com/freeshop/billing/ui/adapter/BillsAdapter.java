package com.freeshop.billing.ui.adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.freeshop.billing.data.db.entity.Bill;
import com.freeshop.billing.databinding.ItemBillCardBinding;
import com.freeshop.billing.domain.Money;
import com.freeshop.billing.domain.PaymentStatus;

import java.util.ArrayList;
import java.util.List;

public class BillsAdapter extends RecyclerView.Adapter<BillsAdapter.BillViewHolder> {

    public interface OnBillActionListener {
        void onView(Bill bill);
        void onShare(Bill bill);
        void onDelete(Bill bill);
        void onConvert(Bill bill);
    }

    private final List<Bill> bills = new ArrayList<>();
    private final OnBillActionListener listener;

    public BillsAdapter(OnBillActionListener listener) {
        this.listener = listener;
    }

    public void setBills(List<Bill> newBills) {
        this.bills.clear();
        if (newBills != null) {
            this.bills.addAll(newBills);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBillCardBinding binding = ItemBillCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new BillViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BillViewHolder holder, int position) {
        holder.bind(bills.get(position));
    }

    @Override
    public int getItemCount() {
        return bills.size();
    }

    class BillViewHolder extends RecyclerView.ViewHolder {
        private final ItemBillCardBinding binding;

        BillViewHolder(ItemBillCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Bill bill) {
            binding.tvBillNo.setText(bill.getBillNo());

            boolean isQuotation = "QUOTATION".equals(bill.getDocumentType());
            binding.tvDocBadge.setVisibility(isQuotation ? View.VISIBLE : View.GONE);

            // Payment status badge
            PaymentStatus status = bill.getPaymentStatus();
            int bgColor;
            int textColor;
            if (status == PaymentStatus.PAID) {
                bgColor = Color.parseColor("#E8F7EE");
                textColor = Color.parseColor("#15803D");
            } else if (status == PaymentStatus.PARTIAL) {
                bgColor = Color.parseColor("#FEF3C7");
                textColor = Color.parseColor("#B45309");
            } else {
                bgColor = Color.parseColor("#FDEAEA");
                textColor = Color.parseColor("#C0392B");
            }

            GradientDrawable statusDrawable = new GradientDrawable();
            statusDrawable.setCornerRadius(8f);
            statusDrawable.setColor(bgColor);
            binding.tvStatusBadge.setBackground(statusDrawable);
            binding.tvStatusBadge.setTextColor(textColor);
            binding.tvStatusBadge.setText(status.name());

            binding.tvCustomerName.setText(bill.getCustomerName());
            binding.tvBillDate.setText(bill.getDate());
            binding.tvGrandTotal.setText(Money.format(bill.getGrandTotal()));

            if (isQuotation && bill.isConverted()) {
                binding.tvConvertedLabel.setVisibility(View.VISIBLE);
            } else {
                binding.tvConvertedLabel.setVisibility(View.GONE);
            }

            if (!isQuotation && bill.getBalance() > 0.01) {
                binding.tvBalance.setVisibility(View.VISIBLE);
                binding.tvBalance.setText("Balance: " + Money.format(bill.getBalance()));
            } else {
                binding.tvBalance.setVisibility(View.GONE);
            }

            binding.btnConvert.setVisibility(isQuotation && !bill.isConverted() ? View.VISIBLE : View.GONE);

            binding.btnView.setOnClickListener(v -> listener.onView(bill));
            binding.btnShare.setOnClickListener(v -> listener.onShare(bill));
            binding.btnDelete.setOnClickListener(v -> listener.onDelete(bill));
            binding.btnConvert.setOnClickListener(v -> listener.onConvert(bill));
        }
    }
}
