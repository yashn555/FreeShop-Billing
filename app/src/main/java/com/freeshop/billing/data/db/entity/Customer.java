package com.freeshop.billing.data.db.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "customers",
    foreignKeys = @ForeignKey(
        entity = Shop.class,
        parentColumns = "id",
        childColumns = "shopId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("shopId")}
)
public class Customer {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private long shopId;
    private String name;
    private String phone;
    private String email;
    private String gstin;
    private String address;

    public Customer() {
        this.phone = "";
        this.email = "";
        this.gstin = "";
        this.address = "";
    }

    @Ignore
    public Customer(long shopId, String name, String phone, String email, String gstin, String address) {
        this.shopId = shopId;
        this.name = name;
        this.phone = phone != null ? phone : "";
        this.email = email != null ? email : "";
        this.gstin = gstin != null ? gstin : "";
        this.address = address != null ? address : "";
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getShopId() { return shopId; }
    public void setShopId(long shopId) { this.shopId = shopId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone != null ? phone : ""; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email != null ? email : ""; }
    public void setEmail(String email) { this.email = email; }

    public String getGstin() { return gstin != null ? gstin : ""; }
    public void setGstin(String gstin) { this.gstin = gstin; }

    public String getAddress() { return address != null ? address : ""; }
    public void setAddress(String address) { this.address = address; }
}
