package com.freeshop.billing.data;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionStore {

    private static final String PREF_NAME = "session";
    private static final String KEY_SHOP_ID = "shopId";

    private final SharedPreferences preferences;

    public SessionStore(Context context) {
        this.preferences = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public Long getShopId() {
        long id = preferences.getLong(KEY_SHOP_ID, -1L);
        return id < 1 ? null : id;
    }

    public void setShopId(long id) {
        preferences.edit().putLong(KEY_SHOP_ID, id).apply();
    }

    public void clear() {
        preferences.edit().remove(KEY_SHOP_ID).apply();
    }
}
