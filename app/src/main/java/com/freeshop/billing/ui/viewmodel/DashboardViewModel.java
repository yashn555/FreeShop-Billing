package com.freeshop.billing.ui.viewmodel;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.freeshop.billing.data.db.AppDatabase;
import com.freeshop.billing.data.db.entity.Bill;
import com.freeshop.billing.data.db.entity.Customer;
import com.freeshop.billing.data.db.entity.Product;
import com.freeshop.billing.data.db.entity.Shop;
import com.freeshop.billing.data.repository.BillRepository;
import com.freeshop.billing.data.repository.CustomerRepository;
import com.freeshop.billing.data.repository.ProductRepository;
import com.freeshop.billing.data.repository.ShopRepository;
import com.freeshop.billing.domain.BillItem;
import com.freeshop.billing.domain.Validators;
import com.freeshop.billing.pdf.BillPdfGenerator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DashboardViewModel extends ViewModel {

    private final long shopId;
    private final ShopRepository shopRepo;
    private final CustomerRepository customerRepo;
    private final ProductRepository productRepo;
    private final BillRepository billRepo;
    private final Context context;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<DashboardUiState> state = new MutableLiveData<>(new DashboardUiState());
    public LiveData<DashboardUiState> getState() { return state; }

    private final LiveData<Shop> shopLiveData;
    private final LiveData<List<Customer>> customersLiveData;
    private final LiveData<List<Product>> productsLiveData;

    private final Observer<Shop> shopObserver = shop -> {
        if (shop != null) {
            DashboardUiState s = getCurrentState().copy();
            s.setShop(shop);
            if (s.getBillNo().isEmpty()) {
                s.setBillNo(shop.getInvoicePrefix() + shop.getNextInvoiceNo());
            }
            if (s.getDate().isEmpty()) {
                s.setDate(todayStr());
            }
            state.setValue(s);
        }
    };

    private final Observer<List<Customer>> customersObserver = list -> {
        DashboardUiState s = getCurrentState().copy();
        s.setCustomers(list);
        state.setValue(s);
    };

    private final Observer<List<Product>> productsObserver = list -> {
        DashboardUiState s = getCurrentState().copy();
        s.setProducts(list);
        state.setValue(s);
    };

    public DashboardViewModel(long shopId, ShopRepository shopRepo, CustomerRepository customerRepo,
                              ProductRepository productRepo, BillRepository billRepo, Context context) {
        this.shopId = shopId;
        this.shopRepo = shopRepo;
        this.customerRepo = customerRepo;
        this.productRepo = productRepo;
        this.billRepo = billRepo;
        this.context = context.getApplicationContext();

        this.shopLiveData = shopRepo.observe(shopId);
        this.customersLiveData = customerRepo.observeAll(shopId);
        this.productsLiveData = productRepo.observeAll(shopId);

        this.shopLiveData.observeForever(shopObserver);
        this.customersLiveData.observeForever(customersObserver);
        this.productsLiveData.observeForever(productsObserver);
    }

    private DashboardUiState getCurrentState() {
        DashboardUiState s = state.getValue();
        return s != null ? s : new DashboardUiState();
    }

    public void updateDocumentType(String type) {
        DashboardUiState s = getCurrentState().copy();
        s.setDocumentType(type);
        state.setValue(s);
    }

    public void updateCustomerName(String v) {
        DashboardUiState s = getCurrentState().copy();
        s.setCustomerName(v);
        state.setValue(s);
    }

    public void updateCustomerPhone(String v) {
        DashboardUiState s = getCurrentState().copy();
        s.setCustomerPhone(v);
        state.setValue(s);
    }

    public void updateCustomerGstin(String v) {
        DashboardUiState s = getCurrentState().copy();
        s.setCustomerGstin(v);
        state.setValue(s);
    }

    public void updateCustomerAddress(String v) {
        DashboardUiState s = getCurrentState().copy();
        s.setCustomerAddress(v);
        state.setValue(s);
    }

    public void selectCustomer(Customer c) {
        DashboardUiState s = getCurrentState().copy();
        s.setCustomerName(c.getName());
        s.setCustomerPhone(c.getPhone());
        s.setCustomerGstin(c.getGstin());
        s.setCustomerAddress(c.getAddress());
        state.setValue(s);
    }

    public void updateBillNo(String v) {
        DashboardUiState s = getCurrentState().copy();
        s.setBillNo(v);
        state.setValue(s);
    }

    public void updateDate(String v) {
        DashboardUiState s = getCurrentState().copy();
        s.setDate(v);
        state.setValue(s);
    }

    public void updateDiscount(String v) {
        DashboardUiState s = getCurrentState().copy();
        s.setDiscount(v);
        state.setValue(s);
    }

    public void updatePaymentMode(String v) {
        DashboardUiState s = getCurrentState().copy();
        s.setPaymentMode(v);
        state.setValue(s);
    }

    public void updateReceived(String v) {
        DashboardUiState s = getCurrentState().copy();
        s.setReceived(v);
        state.setValue(s);
    }

    public void updateNotes(String v) {
        DashboardUiState s = getCurrentState().copy();
        s.setNotes(v);
        state.setValue(s);
    }

    public void updateItem(long id, DraftItem item) {
        DashboardUiState s = getCurrentState().copy();
        List<DraftItem> updated = new ArrayList<>();
        for (DraftItem d : s.getDraftItems()) {
            if (d.getId() == id) {
                updated.add(item);
            } else {
                updated.add(d);
            }
        }
        s.setDraftItems(updated);
        state.setValue(s);
    }

    public void addItem() {
        DashboardUiState s = getCurrentState().copy();
        List<DraftItem> updated = new ArrayList<>(s.getDraftItems());
        updated.add(new DraftItem());
        s.setDraftItems(updated);
        state.setValue(s);
    }

    public void removeItem(long id) {
        DashboardUiState s = getCurrentState().copy();
        List<DraftItem> updated = new ArrayList<>();
        for (DraftItem d : s.getDraftItems()) {
            if (d.getId() != id) {
                updated.add(d);
            }
        }
        s.setDraftItems(updated);
        state.setValue(s);
    }

    public void autofillProduct(long draftId, Product p) {
        DashboardUiState s = getCurrentState().copy();
        List<DraftItem> updated = new ArrayList<>();
        for (DraftItem d : s.getDraftItems()) {
            if (d.getId() == draftId) {
                DraftItem copy = d.copy();
                copy.setName(p.getName());
                copy.setHsn(p.getHsn());
                copy.setRate(String.valueOf(p.getPrice()));
                copy.setTaxRate(String.valueOf(p.getTaxRate()));
                updated.add(copy);
            } else {
                updated.add(d);
            }
        }
        s.setDraftItems(updated);
        state.setValue(s);
    }

    public interface OnGenerateSuccess {
        void onSuccess(String pdfPath);
    }

    public void generate(OnGenerateSuccess callback) {
        DashboardUiState s = getCurrentState().copy();
        Shop shop = s.getShop();
        if (shop == null) {
            toast("Shop not loaded", true);
            return;
        }

        // Validators
        String customerName = Validators.validateCustomerName(s.getCustomerName());
        String billNoError = Validators.validateBillNo(s.getBillNo());
        if (billNoError != null) {
            toast(billNoError, true);
            return;
        }

        List<BillItem> items = new ArrayList<>();
        for (DraftItem d : s.getDraftItems()) {
            double q = parseDouble(d.getQty(), 0.0);
            double r = parseDouble(d.getRate(), 0.0);
            double t = parseDouble(d.getTaxRate(), 0.0);
            items.add(new BillItem(d.getName(), d.getHsn(), q, r, t));
        }

        String itemError = Validators.validateItems(items);
        if (itemError != null) {
            toast(itemError, true);
            return;
        }

        double discountVal = parseDouble(s.getDiscount(), 0.0);
        String discError = Validators.validateDiscount(discountVal, s.getSubtotal());
        if (discError != null) {
            toast(discError, true);
            return;
        }

        double receivedVal = parseDouble(s.getReceived(), "QUOTATION".equals(s.getDocumentType()) ? 0.0 : s.getGrandTotal());
        String recError = Validators.validateReceived(receivedVal);
        if (recError != null) {
            toast(recError, true);
            return;
        }

        s.setGenerating(true);
        state.setValue(s);

        executor.execute(() -> {
            try {
                if (!billRepo.isBillNoUnique(shopId, s.getBillNo())) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        DashboardUiState curr = getCurrentState().copy();
                        curr.setGenerating(false);
                        state.setValue(curr);
                        toast("Bill number '" + s.getBillNo() + "' already exists", true);
                    });
                    return;
                }

                double grandTotal = s.getGrandTotal();
                double balance = grandTotal - receivedVal;

                JSONArray arr = new JSONArray();
                for (BillItem it : items) {
                    JSONObject obj = new JSONObject();
                    obj.put("name", it.getName());
                    obj.put("hsn", it.getHsn());
                    obj.put("qty", it.getQty());
                    obj.put("rate", it.getRate());
                    obj.put("taxRate", it.getTaxRate());
                    obj.put("unit", it.getUnit());
                    arr.put(obj);
                }
                String itemsJson = arr.toString();

                String dateStr = s.getDate().isEmpty() ? todayStr() : s.getDate();
                Bill bill = new Bill(
                        shopId, s.getBillNo(), dateStr, customerName,
                        s.getCustomerPhone(), s.getCustomerGstin(), s.getCustomerAddress(),
                        itemsJson, s.getSubtotal(), discountVal, s.getTaxAmount(),
                        grandTotal, receivedVal, balance, s.getPaymentMode(),
                        s.getNotes(), "", System.currentTimeMillis(), s.getDocumentType(), false
                );

                // Generate PDF first
                File pdfFile = BillPdfGenerator.generate(context, bill, shop);
                bill.setPdfPath(pdfFile.getAbsolutePath());
                billRepo.insert(bill);

                // Increment invoice counter
                shop.setNextInvoiceNo(shop.getNextInvoiceNo() + 1);
                shopRepo.update(shop);

                new Handler(Looper.getMainLooper()).post(() -> {
                    DashboardUiState curr = getCurrentState().copy();
                    curr.setGenerating(false);
                    curr.setSavedPdfPath(pdfFile.getAbsolutePath());
                    state.setValue(curr);

                    String docLabel = "QUOTATION".equals(s.getDocumentType()) ? "Quotation" : "Bill";
                    toast(docLabel + " generated and saved as PDF", false);

                    // Open PDF via FileProvider
                    try {
                        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", pdfFile);
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(uri, "application/pdf");
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(Intent.createChooser(intent, "Open Bill PDF").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    } catch (Exception ignored) {}

                    if (callback != null) {
                        callback.onSuccess(pdfFile.getAbsolutePath());
                    }
                });

            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    DashboardUiState curr = getCurrentState().copy();
                    curr.setGenerating(false);
                    state.setValue(curr);
                    toast("Error: " + e.getMessage(), true);
                });
            }
        });
    }

    public void resetForm() {
        DashboardUiState current = getCurrentState();
        Shop shop = current.getShop();
        String nextBillNo = (shop != null) ? (shop.getInvoicePrefix() + shop.getNextInvoiceNo()) : "";

        DashboardUiState reset = new DashboardUiState();
        reset.setShop(shop);
        reset.setCustomers(current.getCustomers());
        reset.setProducts(current.getProducts());
        reset.setBillNo(nextBillNo);
        reset.setDate(todayStr());
        reset.setDocumentType("INVOICE");
        state.setValue(reset);
    }

    public void clearToast() {
        DashboardUiState s = getCurrentState().copy();
        s.setToastMessage(null);
        state.setValue(s);
    }

    private void toast(String msg, boolean error) {
        DashboardUiState s = getCurrentState().copy();
        s.setToastMessage(msg);
        s.setToastError(error);
        state.setValue(s);
    }

    private static String todayStr() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date());
    }

    private static double parseDouble(String str, double def) {
        if (str == null || str.trim().isEmpty()) return def;
        try {
            return Double.parseDouble(str.trim());
        } catch (Exception e) {
            return def;
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        shopLiveData.removeObserver(shopObserver);
        customersLiveData.removeObserver(customersObserver);
        productsLiveData.removeObserver(productsObserver);
        executor.shutdown();
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final Context context;
        private final long shopId;

        public Factory(Context context, long shopId) {
            this.context = context.getApplicationContext();
            this.shopId = shopId;
        }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            AppDatabase db = AppDatabase.getInstance(context);
            return (T) new DashboardViewModel(
                    shopId,
                    new ShopRepository(db.shopDao()),
                    new CustomerRepository(db.customerDao()),
                    new ProductRepository(db.productDao()),
                    new BillRepository(db.billDao()),
                    context
            );
        }
    }
}
