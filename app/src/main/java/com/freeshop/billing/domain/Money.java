package com.freeshop.billing.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class Money {

    private Money() {}

    public static String format(double amount) {
        long rupees = (long) amount;
        int paise = (int) Math.round((Math.abs(amount) - Math.abs((double) rupees)) * 100);
        return paise == 0
                ? String.format(Locale.getDefault(), "Rs %.0f", amount)
                : String.format(Locale.getDefault(), "Rs %.2f", amount);
    }

    public static String amountInWords(double amount) {
        long rupees = (long) amount;
        int paise = (int) Math.round((amount - (double) rupees) * 100);
        String rupeesWords = toIndianWords(rupees);
        if (paise == 0) {
            return "Rupees " + rupeesWords + " Only";
        } else {
            String paiseWords = toIndianWords((long) paise);
            return "Rupees " + rupeesWords + " And " + paiseWords + " Paise Only";
        }
    }

    private static final String[] ONES = {
            "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
            "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen",
            "Seventeen", "Eighteen", "Nineteen"
    };

    private static final String[] TENS = {
            "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    };

    private static String twoDigits(long n) {
        if (n == 0L) {
            return "";
        } else if (n < 20L) {
            return ONES[(int) n];
        } else {
            int tensIndex = (int) (n / 10);
            int onesIndex = (int) (n % 10);
            return TENS[tensIndex] + (onesIndex != 0 ? " " + ONES[onesIndex] : "");
        }
    }

    private static String threeDigits(long n) {
        long h = n / 100;
        long rem = n % 100;
        String hundredPart = h > 0 ? ONES[(int) h] + " Hundred" : "";
        String remPart = twoDigits(rem);
        if (!hundredPart.isEmpty() && !remPart.isEmpty()) {
            return hundredPart + " " + remPart;
        } else if (!hundredPart.isEmpty()) {
            return hundredPart;
        } else {
            return remPart;
        }
    }

    private static String toIndianWords(long n) {
        if (n == 0L) return "Zero";
        long num = n;
        long crore = num / 10_000_000L;
        num %= 10_000_000L;
        long lakh = num / 100_000L;
        num %= 100_000L;
        long thousand = num / 1_000L;
        num %= 1_000L;

        List<String> parts = new ArrayList<>();
        if (crore > 0) parts.add(twoDigits(crore) + " Crore");
        if (lakh > 0) parts.add(twoDigits(lakh) + " Lakh");
        if (thousand > 0) parts.add(twoDigits(thousand) + " Thousand");
        if (num > 0) {
            String td = threeDigits(num);
            if (!td.isEmpty()) parts.add(td);
        }
        return String.join(" ", parts);
    }
}
