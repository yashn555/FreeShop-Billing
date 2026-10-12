package com.freeshop.billing.ui.viewmodel;

public class DraftItem {
    private final long id;
    private String name;
    private String hsn;
    private String qty;
    private String rate;
    private String taxRate;

    public DraftItem() {
        this.id = System.nanoTime();
        this.name = "";
        this.hsn = "";
        this.qty = "1";
        this.rate = "";
        this.taxRate = "0";
    }

    public DraftItem(long id, String name, String hsn, String qty, String rate, String taxRate) {
        this.id = id;
        this.name = name != null ? name : "";
        this.hsn = hsn != null ? hsn : "";
        this.qty = (qty != null && !qty.isEmpty()) ? qty : "1";
        this.rate = rate != null ? rate : "";
        this.taxRate = taxRate != null ? taxRate : "0";
    }

    public long getId() { return id; }

    public String getName() { return name != null ? name : ""; }
    public void setName(String name) { this.name = name; }

    public String getHsn() { return hsn != null ? hsn : ""; }
    public void setHsn(String hsn) { this.hsn = hsn; }

    public String getQty() { return qty != null ? qty : "1"; }
    public void setQty(String qty) { this.qty = qty; }

    public String getRate() { return rate != null ? rate : ""; }
    public void setRate(String rate) { this.rate = rate; }

    public String getTaxRate() { return taxRate != null ? taxRate : "0"; }
    public void setTaxRate(String taxRate) { this.taxRate = taxRate; }

    public DraftItem copy() {
        return new DraftItem(this.id, this.name, this.hsn, this.qty, this.rate, this.taxRate);
    }
}
