package com.example.myapplicationooo;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.material.bottomsheet.BottomSheetDialog;

public class LoginRequiredHelper {

    public interface OnLoginSuccess {
        void onProceed();
    }

    /**
     * Kiểm tra trạng thái đăng nhập.
     * Nếu đã đăng nhập: Chạy hành động ngay lập tức.
     * Nếu chưa (Guest): Hiện BottomSheet yêu cầu đăng nhập.
     */
    public static void checkAndProceed(Context context, OnLoginSuccess action) {
        if (AuthManager.getInstance().isLoggedIn()) {
            action.onProceed();
        } else {
            showLoginSheet(context, null);
        }
    }

    /**
     * Tương tự như trên nhưng cho phép truyền Class đích để tự động mở sau khi Login.
     */
    public static void checkAndProceed(Context context, Class<?> targetActivity) {
        if (AuthManager.getInstance().isLoggedIn()) {
            context.startActivity(new Intent(context, targetActivity));
        } else {
            showLoginSheet(context, targetActivity);
        }
    }

    private static void showLoginSheet(Context context, Class<?> targetActivity) {
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_login_required_sheet, null);
        
        view.findViewById(R.id.btnLoginNow).setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(context, LoginActivity.class);
            intent.putExtra("is_relogin", true);
            if (targetActivity != null) {
                intent.putExtra("target_activity", targetActivity.getName());
            }
            context.startActivity(intent);
        });

        view.findViewById(R.id.btnStayGuest).setOnClickListener(v -> dialog.dismiss());

        dialog.setContentView(view);
        dialog.show();
    }
}
