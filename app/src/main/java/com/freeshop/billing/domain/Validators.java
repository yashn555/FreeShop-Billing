package com.freeshop.billing.domain;

import java.util.List;

public final class Validators {

    private Validators() {}

    public static String validateCustomerName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "Walk-in Customer";
        }
        return name.trim();
    }

    public static String validateBillNo(String billNo) {
        if (billNo == null || billNo.trim().isEmpty()) {
            return "Bill number is required";
        }
        return null;
    }

    public static String validateItems(List<BillItem> items) {
        if (items == null || items.isEmpty()) {
            return "Add at least one item";
        }
        for (int i = 0; i < items.size(); i++) {
            BillItem item = items.get(i);
            if (item.getName() == null || item.getName().trim().isEmpty()) {
                return "Item " + (i + 1) + ": name is required";
            }
            if (item.getQty() <= 0) {
                return "Item " + (i + 1) + ": quantity must be > 0";
            }
            if (item.getRate() < 0) {
                return "Item " + (i + 1) + ": rate must be >= 0";
            }
            if (item.getTaxRate() < 0) {
                return "Item " + (i + 1) + ": tax rate must be >= 0";
            }
        }
        return null;
    }

    public static String validateDiscount(double discount, double subtotal) {
        if (discount < 0) {
            return "Discount must be >= 0";
        }
        if (discount > subtotal) {
            return "Discount cannot exceed subtotal";
        }
        return null;
    }

    public static String validateReceived(double received) {
        if (received < 0) {
            return "Received amount must be >= 0";
        }
        return null;
    }
}
