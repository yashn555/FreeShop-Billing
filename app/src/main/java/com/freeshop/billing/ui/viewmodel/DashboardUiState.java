package com.freeshop.billing.ui.viewmodel;

import com.freeshop.billing.data.db.entity.Customer;
import com.freeshop.billing.data.db.entity.Product;
import com.freeshop.billing.data.db.entity.Shop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DashboardUiState {
    private Shop shop;
    private List<Customer> customers = Collections.emptyList();
    private List<Product> products = Collections.emptyList();

    // Customer section
    private String customerName = "";
    private String customerPhone = "";
    private String customerGstin = "";
    private String customerAddress = "";

    // Bill details
    private String billNo = "";
    private String date = "";
    private String discount = "0";
    private String paymentMode = "CASH";
    private String received = "";
    private String notes = "";

    // Items
    private List<DraftItem> draftItems = new ArrayList<>();

    // Document Type
    private String documentType = "INVOICE";

    // Status
    private boolean generating = false;
    private String savedPdfPath = "";
    private String toastMessage = null;
    private boolean toastError = false;

    public DashboardUiState() {
        draftItems.add(new DraftItem());
    }

    public DashboardUiState copy() {
        DashboardUiState s = new DashboardUiState();
        s.shop = this.shop;
        s.customers = new ArrayList<>(this.customers);
        s.products = new ArrayList<>(this.products);
        s.customerName = this.customerName;
        s.customerPhone = this.customerPhone;
        s.customerGstin = this.customerGstin;
        s.customerAddress = this.customerAddress;
        s.billNo = this.billNo;
        s.date = this.date;
        s.discount = this.discount;
        s.paymentMode = this.paymentMode;
        s.received = this.received;
        s.notes = this.notes;
        s.draftItems = new ArrayList<>();
        for (DraftItem item : this.draftItems) {
            s.draftItems.add(item.copy());
        }
        s.documentType = this.documentType;
        s.generating = this.generating;
        s.savedPdfPath = this.savedPdfPath;
        s.toastMessage = this.toastMessage;
        s.toastError = this.toastError;
        return s;
    }

    // Getters and Setters
    public Shop getShop() { return shop; }
    public void setShop(Shop shop) { this.shop = shop; }

    public List<Customer> getCustomers() { return customers != null ? customers : Collections.emptyList(); }
    public void setCustomers(List<Customer> customers) { this.customers = customers != null ? customers : Collections.emptyList(); }

    public List<Product> getProducts() { return products != null ? products : Collections.emptyList(); }
    public void setProducts(List<Product> products) { this.products = products != null ? products : Collections.emptyList(); }

    public String getCustomerName() { return customerName != null ? customerName : ""; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone != null ? customerPhone : ""; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getCustomerGstin() { return customerGstin != null ? customerGstin : ""; }
    public void setCustomerGstin(String customerGstin) { this.customerGstin = customerGstin; }

    public String getCustomerAddress() { return customerAddress != null ? customerAddress : ""; }
    public void setCustomerAddress(String customerAddress) { this.customerAddress = customerAddress; }

    public String getBillNo() { return billNo != null ? billNo : ""; }
    public void setBillNo(String billNo) { this.billNo = billNo; }

    public String getDate() { return date != null ? date : ""; }
    public void setDate(String date) { this.date = date; }

    public String getDiscount() { return discount != null ? discount : "0"; }
    public void setDiscount(String discount) { this.discount = discount; }

    public String getPaymentMode() { return paymentMode != null ? paymentMode : "CASH"; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }

    public String getReceived() { return received != null ? received : ""; }
    public void setReceived(String received) { this.received = received; }

    public String getNotes() { return notes != null ? notes : ""; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<DraftItem> getDraftItems() { return draftItems != null ? draftItems : Collections.emptyList(); }
    public void setDraftItems(List<DraftItem> draftItems) { this.draftItems = draftItems; }

    public String getDocumentType() { return documentType != null ? documentType : "INVOICE"; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }

    public boolean isGenerating() { return generating; }
    public void setGenerating(boolean generating) { this.generating = generating; }

    public String getSavedPdfPath() { return savedPdfPath != null ? savedPdfPath : ""; }
    public void setSavedPdfPath(String savedPdfPath) { this.savedPdfPath = savedPdfPath; }

    public String getToastMessage() { return toastMessage; }
    public void setToastMessage(String toastMessage) { this.toastMessage = toastMessage; }

    public boolean isToastError() { return toastError; }
    public void setToastError(boolean toastError) { this.toastError = toastError; }

    // Derived Calculations
    public double getSubtotal() {
        double total = 0.0;
        if (draftItems != null) {
            for (DraftItem item : draftItems) {
                double q = parseDouble(item.getQty(), 0.0);
                double r = parseDouble(item.getRate(), 0.0);
                total += q * r;
            }
        }
        return total;
    }

    public double getTaxAmount() {
        if (shop != null && shop.isTaxEnabled() && draftItems != null) {
            double total = 0.0;
            for (DraftItem item : draftItems) {
                double q = parseDouble(item.getQty(), 0.0);
                double r = parseDouble(item.getRate(), 0.0);
                double t = parseDouble(item.getTaxRate(), 0.0);
                total += (q * r) * t / 100.0;
            }
            return total;
        }
        return 0.0;
    }

    public double getGrandTotal() {
        double disc = parseDouble(discount, 0.0);
        return Math.max(0.0, getSubtotal() + getTaxAmount() - disc);
    }

    public double getReceivedAmount() {
        if (received != null && !received.trim().isEmpty()) {
            try {
                return Double.parseDouble(received.trim());
            } catch (Exception ignored) {}
        }
        return "QUOTATION".equals(documentType) ? 0.0 : getGrandTotal();
    }

    public double getBalance() {
        return getGrandTotal() - getReceivedAmount();
    }

    private static double parseDouble(String str, double def) {
        if (str == null || str.trim().isEmpty()) return def;
        try {
            return Double.parseDouble(str.trim());
        } catch (Exception e) {
            return def;
        }
    }
}
