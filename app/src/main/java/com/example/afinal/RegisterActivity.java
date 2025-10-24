package com.example.afinal;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth; // <-- Thêm thư viện Firebase Auth

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLoginNow;

    private TextInputLayout layoutUsername;
    private TextInputLayout layoutEmail;
    private TextInputLayout layoutPassword;
    private TextInputLayout layoutConfirmPassword;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        mAuth = FirebaseAuth.getInstance();


        etUsername = findViewById(R.id.et_username_register);
        etEmail = findViewById(R.id.et_email_register);
        etPassword = findViewById(R.id.et_password_register);
        etConfirmPassword = findViewById(R.id.et_confirm_password_register);
        layoutUsername = findViewById(R.id.layout_username);
        layoutEmail = findViewById(R.id.layout_email);
        layoutPassword = findViewById(R.id.layout_password);
        layoutConfirmPassword = findViewById(R.id.layout_confirm_password);
        btnRegister = findViewById(R.id.btn_register);
        tvLoginNow = findViewById(R.id.tv_login_now);


        btnRegister.setOnClickListener(v -> handleRegister());


        tvLoginNow.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void handleRegister() {
        layoutUsername.setError(null);
        layoutEmail.setError(null);
        layoutPassword.setError(null);
        layoutConfirmPassword.setError(null);
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            layoutUsername.setError("Tên đăng nhập không được để trống");
            etUsername.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            layoutEmail.setError("Email không được để trống");
            etEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            layoutEmail.setError("Vui lòng nhập email hợp lệ");
            etEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password) || !isValidPassword(password)) {
            String message = "";
            if(TextUtils.isEmpty(password)) {
                message = "Mật khẩu không được để trống";
            }
            if(!isValidPassword(password)){
                message = "Mật khẩu phải có ít nhất 6 ký tự, bao gồm chữ cái, số và ký tự đặc biệt";
            }
            layoutPassword.setError(message);
            etPassword.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(confirmPassword)) {
            layoutConfirmPassword.setError("Vui lòng xác nhận mật khẩu");
            etConfirmPassword.requestFocus();
            return;
        }
        if (!password.equals(confirmPassword)) {
            layoutConfirmPassword.setError("Mật khẩu không khớp");
            etConfirmPassword.requestFocus();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Lỗi không xác định";
                        Toast.makeText(RegisterActivity.this, "Đăng ký thất bại: " + errorMessage, Toast.LENGTH_LONG).show();
                        Log.e("FirebaseError", "Lỗi đăng ký: ", task.getException());
                    }
                });
    }

    private boolean isValidPassword(String password) {
        if (password.length() < 6) {
            return false;
        }

        Boolean hasLetter = false;
        Boolean hasDigit = false;
        Boolean hasSpecialChar = false;

        for(char c : password.toCharArray()) {
            if(Character.isLetter(c)) {
                hasLetter = true;
            } else if(Character.isDigit(c)) {
                hasDigit = true;
            } else hasSpecialChar = true;

            if(hasLetter && hasDigit && hasSpecialChar) {
                return true;
            }
        }
        return false;
        }
    }
