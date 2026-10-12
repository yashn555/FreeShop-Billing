package com.freeshop.billing.data.db.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.freeshop.billing.domain.PaymentStatus;

@Entity(
    tableName = "bills",
    foreignKeys = @ForeignKey(
        entity = Shop.class,
        parentColumns = "id",
        childColumns = "shopId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("shopId")}
)
public class Bill {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private long shopId;
    private String billNo;
    private String date;                  // DD/MM/YYYY
    private String customerName;
    private String customerPhone;
    private String customerGstin;
    private String customerAddress;
    private String itemsJson;             // JSON array of BillItem
    private double subtotal;
    private double discount;
    private double taxAmount;
    private double grandTotal;
    private double received;
    private double balance;
    private String paymentMode;
    private String notes;
    private String pdfPath;
    private long createdAt;
    private String documentType;
    private boolean isConverted;

    public Bill() {
        this.customerPhone = "";
        this.customerGstin = "";
        this.customerAddress = "";
        this.discount = 0.0;
        this.taxAmount = 0.0;
        this.paymentMode = "CASH";
        this.notes = "";
        this.pdfPath = "";
        this.createdAt = System.currentTimeMillis();
        this.documentType = "INVOICE";
        this.isConverted = false;
    }

    @Ignore
    public Bill(long shopId, String billNo, String date, String customerName,
                String customerPhone, String customerGstin, String customerAddress,
                String itemsJson, double subtotal, double discount, double taxAmount,
                double grandTotal, double received, double balance, String paymentMode,
                String notes, String pdfPath, long createdAt, String documentType, boolean isConverted) {
        this.shopId = shopId;
        this.billNo = billNo;
        this.date = date;
        this.customerName = customerName;
        this.customerPhone = customerPhone != null ? customerPhone : "";
        this.customerGstin = customerGstin != null ? customerGstin : "";
        this.customerAddress = customerAddress != null ? customerAddress : "";
        this.itemsJson = itemsJson;
        this.subtotal = subtotal;
        this.discount = discount;
        this.taxAmount = taxAmount;
        this.grandTotal = grandTotal;
        this.received = received;
        this.balance = balance;
        this.paymentMode = paymentMode != null ? paymentMode : "CASH";
        this.notes = notes != null ? notes : "";
        this.pdfPath = pdfPath != null ? pdfPath : "";
        this.createdAt = createdAt > 0 ? createdAt : System.currentTimeMillis();
        this.documentType = (documentType != null && !documentType.isEmpty()) ? documentType : "INVOICE";
        this.isConverted = isConverted;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getShopId() { return shopId; }
    public void setShopId(long shopId) { this.shopId = shopId; }

    public String getBillNo() { return billNo; }
    public void setBillNo(String billNo) { this.billNo = billNo; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone != null ? customerPhone : ""; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getCustomerGstin() { return customerGstin != null ? customerGstin : ""; }
    public void setCustomerGstin(String customerGstin) { this.customerGstin = customerGstin; }

    public String getCustomerAddress() { return customerAddress != null ? customerAddress : ""; }
    public void setCustomerAddress(String customerAddress) { this.customerAddress = customerAddress; }

    public String getItemsJson() { return itemsJson; }
    public void setItemsJson(String itemsJson) { this.itemsJson = itemsJson; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }

    public double getTaxAmount() { return taxAmount; }
    public void setTaxAmount(double taxAmount) { this.taxAmount = taxAmount; }

    public double getGrandTotal() { return grandTotal; }
    public void setGrandTotal(double grandTotal) { this.grandTotal = grandTotal; }

    public double getReceived() { return received; }
    public void setReceived(double received) { this.received = received; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public String getPaymentMode() { return paymentMode != null ? paymentMode : "CASH"; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }

    public String getNotes() { return notes != null ? notes : ""; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getPdfPath() { return pdfPath != null ? pdfPath : ""; }
    public void setPdfPath(String pdfPath) { this.pdfPath = pdfPath; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public String getDocumentType() { return documentType != null ? documentType : "INVOICE"; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }

    public boolean isConverted() { return isConverted; }
    public void setConverted(boolean converted) { isConverted = converted; }

    @Ignore
    public PaymentStatus getPaymentStatus() {
        if (received >= grandTotal) {
            return PaymentStatus.PAID;
        } else if (received > 0.0) {
            return PaymentStatus.PARTIAL;
        } else {
            return PaymentStatus.UNPAID;
        }
    }
}
