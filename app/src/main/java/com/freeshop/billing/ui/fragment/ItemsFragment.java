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
import com.freeshop.billing.data.db.entity.Product;
import com.freeshop.billing.databinding.FragmentItemsBinding;
import com.freeshop.billing.ui.adapter.ProductsAdapter;
import com.freeshop.billing.ui.util.ToastHelper;
import com.freeshop.billing.ui.viewmodel.ItemsViewModel;

public class ItemsFragment extends Fragment {

    private FragmentItemsBinding binding;
    private ItemsViewModel viewModel;
    private ProductsAdapter adapter;
    private long shopId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            ItemsFragmentArgs args = ItemsFragmentArgs.fromBundle(getArguments());
            shopId = args.getShopId();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentItemsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = Navigation.findNavController(view);
        viewModel = new ViewModelProvider(this, new ItemsViewModel.Factory(requireContext(), shopId))
                .get(ItemsViewModel.class);

        binding.topBar.topBar.setTitle(R.string.title_items);
        binding.topBar.topBar.setNavigationIcon(R.drawable.ic_arrow_back);
        binding.topBar.topBar.setNavigationOnClickListener(v -> navController.popBackStack());

        adapter = new ProductsAdapter(this::showDeleteDialog);
        binding.rvItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvItems.setAdapter(adapter);

        binding.btnAddItem.setOnClickListener(v -> {
            String name = binding.etItemName.getText() != null ? binding.etItemName.getText().toString() : "";
            String unit = binding.etItemUnit.getText() != null ? binding.etItemUnit.getText().toString() : "PCS";
            String price = binding.etItemPrice.getText() != null ? binding.etItemPrice.getText().toString() : "";
            String taxRate = binding.etItemTaxRate.getText() != null ? binding.etItemTaxRate.getText().toString() : "0";
            String hsn = binding.etItemHsn.getText() != null ? binding.etItemHsn.getText().toString() : "";

            viewModel.add(name, unit, price, taxRate, hsn, () -> {
                binding.etItemName.setText("");
                binding.etItemUnit.setText("PCS");
                binding.etItemPrice.setText("");
                binding.etItemTaxRate.setText("0");
                binding.etItemHsn.setText("");
                ToastHelper.show(requireActivity(), getString(R.string.toast_item_added), false);
            });
        });

        viewModel.getItems().observe(getViewLifecycleOwner(), items -> {
            adapter.setProducts(items);
            boolean isEmpty = (items == null || items.isEmpty());
            binding.rvItems.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            binding.emptyState.layoutEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            binding.emptyState.ivEmptyIcon.setImageResource(R.drawable.ic_inventory);
            binding.emptyState.tvEmptyMessage.setText(R.string.no_items_yet);
        });

        viewModel.getError().observe(getViewLifecycleOwner(), err -> {
            if (err != null) {
                ToastHelper.show(requireActivity(), err, true);
                viewModel.clearError();
            }
        });
    }

    private void showDeleteDialog(Product product) {
        String msg = getString(R.string.dialog_delete_item_msg, product.getName());
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.dialog_delete_item_title)
                .setMessage(msg)
                .setPositiveButton(R.string.btn_delete, (dialog, which) -> {
                    viewModel.delete(product);
                    ToastHelper.show(requireActivity(), getString(R.string.toast_item_deleted), false);
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
