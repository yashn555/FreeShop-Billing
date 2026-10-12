package com.freeshop.billing.data.db.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "shops")
public class Shop {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private String name;
    private String email;
    private String passwordHash;
    private String passwordSalt;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String gstin;
    private String logoPath;
    private String invoicePrefix;
    private int nextInvoiceNo;
    private boolean taxEnabled;
    private String taxLabel;
    private double taxRate;
    private String bankName;
    private String accountNo;
    private String ifsc;
    private String upiId;
    private String terms;

    public Shop() {
        this.phone = "";
        this.address = "";
        this.city = "";
        this.state = "";
        this.pincode = "";
        this.gstin = "";
        this.logoPath = "";
        this.invoicePrefix = "INV";
        this.nextInvoiceNo = 1;
        this.taxEnabled = false;
        this.taxLabel = "GST";
        this.taxRate = 18.0;
        this.bankName = "";
        this.accountNo = "";
        this.ifsc = "";
        this.upiId = "";
        this.terms = "";
    }

    @Ignore
    public Shop(String name, String email, String passwordHash, String passwordSalt) {
        this();
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getPasswordSalt() { return passwordSalt; }
    public void setPasswordSalt(String passwordSalt) { this.passwordSalt = passwordSalt; }

    public String getPhone() { return phone != null ? phone : ""; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address != null ? address : ""; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city != null ? city : ""; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state != null ? state : ""; }
    public void setState(String state) { this.state = state; }

    public String getPincode() { return pincode != null ? pincode : ""; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public String getGstin() { return gstin != null ? gstin : ""; }
    public void setGstin(String gstin) { this.gstin = gstin; }

    public String getLogoPath() { return logoPath != null ? logoPath : ""; }
    public void setLogoPath(String logoPath) { this.logoPath = logoPath; }

    public String getInvoicePrefix() { return invoicePrefix != null ? invoicePrefix : "INV"; }
    public void setInvoicePrefix(String invoicePrefix) { this.invoicePrefix = invoicePrefix; }

    public int getNextInvoiceNo() { return nextInvoiceNo; }
    public void setNextInvoiceNo(int nextInvoiceNo) { this.nextInvoiceNo = nextInvoiceNo; }

    public boolean isTaxEnabled() { return taxEnabled; }
    public void setTaxEnabled(boolean taxEnabled) { this.taxEnabled = taxEnabled; }

    public String getTaxLabel() { return taxLabel != null ? taxLabel : "GST"; }
    public void setTaxLabel(String taxLabel) { this.taxLabel = taxLabel; }

    public double getTaxRate() { return taxRate; }
    public void setTaxRate(double taxRate) { this.taxRate = taxRate; }

    public String getBankName() { return bankName != null ? bankName : ""; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getAccountNo() { return accountNo != null ? accountNo : ""; }
    public void setAccountNo(String accountNo) { this.accountNo = accountNo; }

    public String getIfsc() { return ifsc != null ? ifsc : ""; }
    public void setIfsc(String ifsc) { this.ifsc = ifsc; }

    public String getUpiId() { return upiId != null ? upiId : ""; }
    public void setUpiId(String upiId) { this.upiId = upiId; }

    public String getTerms() { return terms != null ? terms : ""; }
    public void setTerms(String terms) { this.terms = terms; }
}
