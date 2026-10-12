package com.freeshop.billing.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.freeshop.billing.data.db.entity.Product;
import com.freeshop.billing.databinding.ItemProductBinding;
import com.freeshop.billing.domain.Money;

import java.util.ArrayList;
import java.util.List;

public class ProductsAdapter extends RecyclerView.Adapter<ProductsAdapter.ProductViewHolder> {

    public interface OnProductDeleteListener {
        void onDelete(Product product);
    }

    private final List<Product> products = new ArrayList<>();
    private final OnProductDeleteListener listener;

    public ProductsAdapter(OnProductDeleteListener listener) {
        this.listener = listener;
    }

    public void setProducts(List<Product> newProducts) {
        this.products.clear();
        if (newProducts != null) {
            this.products.addAll(newProducts);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductBinding binding = ItemProductBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        holder.bind(products.get(position));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        private final ItemProductBinding binding;

        ProductViewHolder(ItemProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Product product) {
            binding.tvProductName.setText(product.getName());
            binding.tvProductPriceUnit.setText(product.getUnit() + "  " + Money.format(product.getPrice()));

            if (product.getTaxRate() > 0) {
                binding.tvProductTax.setVisibility(View.VISIBLE);
                binding.tvProductTax.setText("Tax: " + product.getTaxRate() + "%");
            } else {
                binding.tvProductTax.setVisibility(View.GONE);
            }

            if (product.getHsn() != null && !product.getHsn().isEmpty()) {
                binding.tvProductHsn.setVisibility(View.VISIBLE);
                binding.tvProductHsn.setText("HSN: " + product.getHsn());
            } else {
                binding.tvProductHsn.setVisibility(View.GONE);
            }

            binding.btnDeleteProduct.setOnClickListener(v -> listener.onDelete(product));
        }
    }
}
