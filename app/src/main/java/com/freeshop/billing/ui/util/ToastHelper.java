package com.freeshop.billing.ui.util;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.freeshop.billing.R;
import com.google.android.material.card.MaterialCardView;

public class ToastHelper {

    public static void show(Activity activity, String message, boolean isError) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) return;

        View rootView = activity.findViewById(android.R.id.content);
        if (rootView == null) return;

        LayoutInflater inflater = LayoutInflater.from(activity);
        View popupView = inflater.inflate(R.layout.view_toast, null);

        MaterialCardView card = popupView.findViewById(R.id.toastCard);
        TextView tvMsg = popupView.findViewById(R.id.tvToastMessage);

        tvMsg.setText(message);
        if (isError) {
            card.setCardBackgroundColor(ContextCompat.getColor(activity, R.color.DangerRed));
        } else {
            card.setCardBackgroundColor(ContextCompat.getColor(activity, R.color.OkGreen));
        }

        PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                false
        );
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setOutsideTouchable(true);

        try {
            popupWindow.showAtLocation(rootView, Gravity.TOP | Gravity.FILL_HORIZONTAL, 0, 0);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    if (popupWindow.isShowing()) {
                        popupWindow.dismiss();
                    }
                } catch (Exception ignored) {}
            }, 2500);
        } catch (Exception ignored) {}
    }
}
