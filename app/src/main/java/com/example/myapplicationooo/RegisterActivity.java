package com.example.myapplicationooo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword, edtConfirmPassword;
    private Button btnRegister;
    private TextView tvBackToLogin;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);
        progressBar = findViewById(R.id.registerProgress);

        if (btnRegister != null) {
            btnRegister.setOnClickListener(v -> registerUser());
        }
        if (tvBackToLogin != null) {
            tvBackToLogin.setOnClickListener(v -> finish());
        }
    }

    private void registerUser() {
        if (edtEmail == null || edtPassword == null || edtConfirmPassword == null) return;
        
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            checkAndCreateUserInFirestore(user.getUid());
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "Đăng ký thất bại: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkAndCreateUserInFirestore(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    if (!document.exists()) {
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("favoriteCities", new ArrayList<>());

                        db.collection("users")
                                .document(userId)
                                .set(userMap)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Firestore", "Khởi tạo dữ liệu người dùng thành công");
                                    goToMainActivity();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firestore", "Lỗi tạo dữ liệu: " + e.getMessage());
                                    goToMainActivity();
                                });
                    } else {
                        goToMainActivity();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Lỗi kiểm tra người dùng: " + e.getMessage());
                    goToMainActivity();
                });
    }

    private void goToMainActivity() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
