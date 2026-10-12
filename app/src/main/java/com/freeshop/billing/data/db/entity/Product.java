package com.freeshop.billing.data.db.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "products",
    foreignKeys = @ForeignKey(
        entity = Shop.class,
        parentColumns = "id",
        childColumns = "shopId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("shopId")}
)
public class Product {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private long shopId;
    private String name;
    private String unit;
    private double price;
    private double taxRate;
    private String hsn;

    public Product() {
        this.unit = "PCS";
        this.price = 0.0;
        this.taxRate = 0.0;
        this.hsn = "";
    }

    @Ignore
    public Product(long shopId, String name, String unit, double price, double taxRate, String hsn) {
        this.shopId = shopId;
        this.name = name;
        this.unit = (unit != null && !unit.isEmpty()) ? unit : "PCS";
        this.price = price;
        this.taxRate = taxRate;
        this.hsn = hsn != null ? hsn : "";
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getShopId() { return shopId; }
    public void setShopId(long shopId) { this.shopId = shopId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUnit() { return unit != null ? unit : "PCS"; }
    public void setUnit(String unit) { this.unit = unit; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getTaxRate() { return taxRate; }
    public void setTaxRate(double taxRate) { this.taxRate = taxRate; }

    public String getHsn() { return hsn != null ? hsn : ""; }
    public void setHsn(String hsn) { this.hsn = hsn; }
}
