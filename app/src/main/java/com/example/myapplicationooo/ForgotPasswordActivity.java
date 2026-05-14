package com.example.myapplicationooo;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText edtEmail;
    private Button btnResetPassword;
    private TextView tvBackToLogin;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();

        edtEmail = findViewById(R.id.edtEmail);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);
        progressBar = findViewById(R.id.forgotProgress);

        btnResetPassword.setOnClickListener(v -> resetPassword());
        tvBackToLogin.setOnClickListener(v -> finish());
    }

    private void resetPassword() {
        String email = edtEmail.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(ForgotPasswordActivity.this, "Link đặt lại mật khẩu đã được gửi đến email của bạn", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(ForgotPasswordActivity.this, "Lỗi: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
