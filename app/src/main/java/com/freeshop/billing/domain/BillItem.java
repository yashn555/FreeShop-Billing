package com.freeshop.billing.domain;

public class BillItem {
    private String name;
    private String hsn;
    private double qty;
    private double rate;
    private double taxRate;
    private String unit;

    public BillItem(String name, String hsn, double qty, double rate, double taxRate, String unit) {
        this.name = name != null ? name : "";
        this.hsn = hsn != null ? hsn : "";
        this.qty = qty;
        this.rate = rate;
        this.taxRate = taxRate;
        this.unit = (unit != null && !unit.isEmpty()) ? unit : "PCS";
    }

    public BillItem(String name, String hsn, double qty, double rate, double taxRate) {
        this(name, hsn, qty, rate, taxRate, "PCS");
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getHsn() { return hsn; }
    public void setHsn(String hsn) { this.hsn = hsn; }

    public double getQty() { return qty; }
    public void setQty(double qty) { this.qty = qty; }

    public double getRate() { return rate; }
    public void setRate(double rate) { this.rate = rate; }

    public double getTaxRate() { return taxRate; }
    public void setTaxRate(double taxRate) { this.taxRate = taxRate; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public double getLineTotal() {
        return qty * rate;
    }

    public double getTaxAmount() {
        return getLineTotal() * taxRate / 100.0;
    }
}
