package com.freeshop.billing.ui.adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.freeshop.billing.data.db.entity.Product;
import com.freeshop.billing.databinding.ItemDraftBinding;
import com.freeshop.billing.domain.Money;
import com.freeshop.billing.ui.viewmodel.DraftItem;

import java.util.ArrayList;
import java.util.List;

public class DraftItemsAdapter extends RecyclerView.Adapter<DraftItemsAdapter.DraftItemViewHolder> {

    public interface OnDraftItemChangeListener {
        void onItemChanged(DraftItem item);
        void onRemoveItem(long itemId);
        void onProductAutofill(long draftId, Product product);
    }

    private final Context context;
    private final List<DraftItem> items = new ArrayList<>();
    private final List<Product> products = new ArrayList<>();
    private boolean taxEnabled = false;
    private final OnDraftItemChangeListener listener;

    public DraftItemsAdapter(Context context, OnDraftItemChangeListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setItems(List<DraftItem> newItems, boolean taxEnabled, List<Product> products) {
        this.items.clear();
        if (newItems != null) {
            this.items.addAll(newItems);
        }
        this.taxEnabled = taxEnabled;
        this.products.clear();
        if (products != null) {
            this.products.addAll(products);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DraftItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDraftBinding binding = ItemDraftBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new DraftItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DraftItemViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class DraftItemViewHolder extends RecyclerView.ViewHolder {
        private final ItemDraftBinding binding;
        private boolean isBinding = false;

        DraftItemViewHolder(ItemDraftBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.btnRemoveItem.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && items.size() > 1) {
                    listener.onRemoveItem(items.get(pos).getId());
                }
            });

            binding.actvItemName.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    if (isBinding) return;
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        DraftItem item = items.get(pos);
                        item.setName(s.toString());
                        listener.onItemChanged(item);
                    }
                }
            });

            binding.etItemHsn.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    if (isBinding) return;
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        DraftItem item = items.get(pos);
                        item.setHsn(s.toString());
                        listener.onItemChanged(item);
                    }
                }
            });

            binding.etItemQty.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    if (isBinding) return;
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        DraftItem item = items.get(pos);
                        item.setQty(s.toString());
                        updateLineTotal(item);
                        listener.onItemChanged(item);
                    }
                }
            });

            binding.etItemRate.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    if (isBinding) return;
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        DraftItem item = items.get(pos);
                        item.setRate(s.toString());
                        updateLineTotal(item);
                        listener.onItemChanged(item);
                    }
                }
            });

            binding.etItemTaxRate.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    if (isBinding) return;
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        DraftItem item = items.get(pos);
                        item.setTaxRate(s.toString());
                        listener.onItemChanged(item);
                    }
                }
            });
        }

        void bind(DraftItem item) {
            isBinding = true;

            binding.etItemTaxRate.setVisibility(taxEnabled ? View.VISIBLE : View.GONE);

            // Autocomplete for items
            List<String> productNames = new ArrayList<>();
            for (Product p : products) {
                productNames.add(p.getName());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, productNames);
            binding.actvItemName.setAdapter(adapter);

            binding.actvItemName.setOnItemClickListener((parent, view, pos, id) -> {
                String selectedName = (String) parent.getItemAtPosition(pos);
                for (Product p : products) {
                    if (p.getName().equalsIgnoreCase(selectedName)) {
                        listener.onProductAutofill(item.getId(), p);
                        break;
                    }
                }
            });

            if (!binding.actvItemName.getText().toString().equals(item.getName())) {
                binding.actvItemName.setText(item.getName());
            }
            if (!binding.etItemHsn.getText().toString().equals(item.getHsn())) {
                binding.etItemHsn.setText(item.getHsn());
            }
            if (!binding.etItemQty.getText().toString().equals(item.getQty())) {
                binding.etItemQty.setText(item.getQty());
            }
            if (!binding.etItemRate.getText().toString().equals(item.getRate())) {
                binding.etItemRate.setText(item.getRate());
            }
            if (!binding.etItemTaxRate.getText().toString().equals(item.getTaxRate())) {
                binding.etItemTaxRate.setText(item.getTaxRate());
            }

            updateLineTotal(item);
            binding.btnRemoveItem.setVisibility(items.size() > 1 ? View.VISIBLE : View.INVISIBLE);

            isBinding = false;
        }

        private void updateLineTotal(DraftItem item) {
            double q = 0.0;
            double r = 0.0;
            try { q = Double.parseDouble(item.getQty()); } catch (Exception ignored) {}
            try { r = Double.parseDouble(item.getRate()); } catch (Exception ignored) {}
            binding.tvLineTotal.setText(Money.format(q * r));
        }
    }

    private static abstract class SimpleTextWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int count, int after) {}
    }
}
