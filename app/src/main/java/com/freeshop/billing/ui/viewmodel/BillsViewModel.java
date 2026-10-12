package com.freeshop.billing.ui.viewmodel;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.freeshop.billing.data.db.AppDatabase;
import com.freeshop.billing.data.db.entity.Bill;
import com.freeshop.billing.data.db.entity.Shop;
import com.freeshop.billing.data.repository.BillRepository;
import com.freeshop.billing.data.repository.ShopRepository;
import com.freeshop.billing.domain.PaymentStatus;
import com.freeshop.billing.pdf.BillPdfGenerator;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BillsViewModel extends ViewModel {

    private final BillRepository repo;
    private final ShopRepository shopRepo;
    private final long shopId;
    private final Context context;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<String> query = new MutableLiveData<>("");
    private final MutableLiveData<DocTypeFilter> docTypeFilter = new MutableLiveData<>(DocTypeFilter.ALL);
    private final MutableLiveData<PaymentStatusFilter> paymentStatusFilter = new MutableLiveData<>(PaymentStatusFilter.ALL);
    private final MutableLiveData<String> dateFrom = new MutableLiveData<>(null);
    private final MutableLiveData<String> dateTo = new MutableLiveData<>(null);

    private final MediatorLiveData<List<Bill>> bills = new MediatorLiveData<>();

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public LiveData<String> getQuery() { return query; }
    public LiveData<DocTypeFilter> getDocTypeFilter() { return docTypeFilter; }
    public LiveData<PaymentStatusFilter> getPaymentStatusFilter() { return paymentStatusFilter; }
    public LiveData<String> getDateFrom() { return dateFrom; }
    public LiveData<String> getDateTo() { return dateTo; }
    public LiveData<List<Bill>> getBills() { return bills; }

    public BillsViewModel(BillRepository repo, ShopRepository shopRepo, long shopId, Context context) {
        this.repo = repo;
        this.shopRepo = shopRepo;
        this.shopId = shopId;
        this.context = context.getApplicationContext();

        LiveData<List<Bill>> allBillsLive = repo.observeAll(shopId);

        bills.addSource(allBillsLive, b -> filterBills(allBillsLive.getValue()));
        bills.addSource(query, q -> filterBills(allBillsLive.getValue()));
        bills.addSource(docTypeFilter, f -> filterBills(allBillsLive.getValue()));
        bills.addSource(paymentStatusFilter, f -> filterBills(allBillsLive.getValue()));
        bills.addSource(dateFrom, d -> filterBills(allBillsLive.getValue()));
        bills.addSource(dateTo, d -> filterBills(allBillsLive.getValue()));
    }

    private void filterBills(List<Bill> allBills) {
        if (allBills == null) {
            bills.setValue(Collections.emptyList());
            return;
        }

        String q = query.getValue() != null ? query.getValue().trim().toLowerCase() : "";
        DocTypeFilter docFilter = docTypeFilter.getValue() != null ? docTypeFilter.getValue() : DocTypeFilter.ALL;
        PaymentStatusFilter statusFilter = paymentStatusFilter.getValue() != null ? paymentStatusFilter.getValue() : PaymentStatusFilter.ALL;
        String dFrom = dateFrom.getValue();
        String dTo = dateTo.getValue();

        Long fromTime = parseDate(dFrom);
        Long toTime = parseDate(dTo);
        if (toTime != null) {
            toTime = toTime + 86400000L - 1L;
        }

        List<Bill> filtered = new ArrayList<>();
        for (Bill bill : allBills) {
            // Search query
            boolean matchesQuery = q.isEmpty()
                    || (bill.getBillNo() != null && bill.getBillNo().toLowerCase().contains(q))
                    || (bill.getCustomerName() != null && bill.getCustomerName().toLowerCase().contains(q));

            // Doc type
            boolean matchesDocType = true;
            if (docFilter == DocTypeFilter.INVOICES) {
                matchesDocType = "INVOICE".equals(bill.getDocumentType());
            } else if (docFilter == DocTypeFilter.QUOTATIONS) {
                matchesDocType = "QUOTATION".equals(bill.getDocumentType());
            }

            // Payment status
            boolean matchesStatus = true;
            PaymentStatus billStatus = bill.getPaymentStatus();
            if (statusFilter == PaymentStatusFilter.PAID) {
                matchesStatus = (billStatus == PaymentStatus.PAID);
            } else if (statusFilter == PaymentStatusFilter.PARTIAL) {
                matchesStatus = (billStatus == PaymentStatus.PARTIAL);
            } else if (statusFilter == PaymentStatusFilter.UNPAID) {
                matchesStatus = (billStatus == PaymentStatus.UNPAID);
            }

            // Date range
            boolean matchesFrom = true;
            boolean matchesTo = true;
            Long billTime = parseDate(bill.getDate());
            if (fromTime != null && billTime != null) {
                matchesFrom = (billTime >= fromTime);
            }
            if (toTime != null && billTime != null) {
                matchesTo = (billTime <= toTime);
            }

            if (matchesQuery && matchesDocType && matchesStatus && matchesFrom && matchesTo) {
                filtered.add(bill);
            }
        }

        bills.setValue(filtered);
    }

    private Long parseDate(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            Date parsed = dateFormat.parse(s.trim());
            return parsed != null ? parsed.getTime() : null;
        } catch (Exception e) {
            return null;
        }
    }

    public void setQuery(String q) { query.setValue(q); }
    public void setDocTypeFilter(DocTypeFilter f) { docTypeFilter.setValue(f); }
    public void setPaymentStatusFilter(PaymentStatusFilter f) { paymentStatusFilter.setValue(f); }
    public void setDateFrom(String d) { dateFrom.setValue(d); }
    public void setDateTo(String d) { dateTo.setValue(d); }
    public void clearDateRange() {
        dateFrom.setValue(null);
        dateTo.setValue(null);
    }

    public void viewBill(Bill bill) {
        if (bill == null || bill.getPdfPath() == null || bill.getPdfPath().isEmpty()) return;
        File file = new File(bill.getPdfPath());
        if (!file.exists()) return;

        try {
            Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(Intent.createChooser(intent, "Open PDF").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } catch (Exception ignored) {}
    }

    public void shareBill(Bill bill) {
        if (bill == null || bill.getPdfPath() == null || bill.getPdfPath().isEmpty()) return;
        File file = new File(bill.getPdfPath());
        if (!file.exists()) return;

        try {
            Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/pdf");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(Intent.createChooser(intent, "Share Bill").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } catch (Exception ignored) {}
    }

    public void deleteBill(Bill bill, Runnable onDone) {
        executor.execute(() -> {
            repo.delete(bill);
            if (bill.getPdfPath() != null && !bill.getPdfPath().isEmpty()) {
                new File(bill.getPdfPath()).delete();
            }
            if (onDone != null) {
                new Handler(Looper.getMainLooper()).post(onDone);
            }
        });
    }

    public void convertToInvoice(Bill quotation, Runnable onComplete) {
        executor.execute(() -> {
            try {
                Shop shop = shopRepo.getById(shopId);
                if (shop == null) return;

                String newBillNo = shop.getInvoicePrefix() + shop.getNextInvoiceNo();
                Bill newBill = new Bill(
                        shopId, newBillNo, quotation.getDate(), quotation.getCustomerName(),
                        quotation.getCustomerPhone(), quotation.getCustomerGstin(), quotation.getCustomerAddress(),
                        quotation.getItemsJson(), quotation.getSubtotal(), quotation.getDiscount(),
                        quotation.getTaxAmount(), quotation.getGrandTotal(), quotation.getReceived(),
                        quotation.getBalance(), quotation.getPaymentMode(), quotation.getNotes(),
                        "", System.currentTimeMillis(), "INVOICE", false
                );

                File pdfFile = BillPdfGenerator.generate(context, newBill, shop);
                newBill.setPdfPath(pdfFile.getAbsolutePath());
                repo.insert(newBill);

                // Mark original quotation as converted
                quotation.setConverted(true);
                repo.update(quotation);

                // Bump invoice counter
                shop.setNextInvoiceNo(shop.getNextInvoiceNo() + 1);
                shopRepo.update(shop);

                if (onComplete != null) {
                    new Handler(Looper.getMainLooper()).post(onComplete);
                }
            } catch (Exception ignored) {}
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
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
            return (T) new BillsViewModel(
                    new BillRepository(db.billDao()),
                    new ShopRepository(db.shopDao()),
                    shopId,
                    context
            );
        }
    }
}
