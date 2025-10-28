package com.example.afinal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION = 60 * 1000;

    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private TextInputLayout layoutEmail;
    private TextInputLayout layoutPassword;
    private Button btnLogin;
    private TextView tvCreateAccount;
    private TextView tvForgotPassword;

    private Button btnSkip;

    private SharedPreferences prefs;
    private int failedAttempts = 0;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);

        // Ánh xạ View
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        layoutEmail = findViewById(R.id.layout_email);
        layoutPassword = findViewById(R.id.layout_password);
        tvCreateAccount = findViewById(R.id.tv_create_account);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        btnSkip = findViewById(R.id.btnskip);



        tvCreateAccount.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });


        tvForgotPassword.setOnClickListener(v -> {
            showForgotPasswordDialog();
        });

        btnSkip.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        checkLockoutStatus();
        btnLogin.setOnClickListener(v -> validateAndLogin());
    }


    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset Password");
        builder.setMessage("Enter your email to receive a password reset link.");


        final EditText inputEmail = new EditText(this);
        inputEmail.setHint("Email");
        builder.setView(inputEmail);


        builder.setPositiveButton("Send", (dialog, which) -> {
            String email = inputEmail.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(LoginActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(LoginActivity.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }
            sendPasswordResetEmail(email);
        });


        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }


    private void sendPasswordResetEmail(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Password reset link sent to your email.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }


    private void validateAndLogin() {
        layoutEmail.setError(null);
        layoutPassword.setError(null);
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            layoutEmail.setError("Email không được để trống");
            etEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password) || !isValidPassword(password)) {
            String message = "";
            if(TextUtils.isEmpty(password)) {
                message = "Mật khẩu không được để trống";
            }
            if(!isValidPassword(password)) {
                message = "Mật khẩu phải có ít nhất 6 ký tự, bao gồm chữ cái, số và ký tự đặc biệt";
            }
            layoutPassword.setError(message);
            etPassword.requestFocus();
            return;
        }
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                        resetFailedAttempts();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin.", Toast.LENGTH_SHORT).show();
                        handleFailedLogin();
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

        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else hasSpecialChar = true;

            if (hasLetter && hasDigit && hasSpecialChar) {
                return true;
            }
        }

        return false;
    }


    private void checkLockoutStatus() {
        long lockoutTimestamp = prefs.getLong("lockout_timestamp", 0);
        long currentTime = System.currentTimeMillis();
        if (lockoutTimestamp > 0 && currentTime < lockoutTimestamp) {
            long remainingTime = (lockoutTimestamp - currentTime) / 1000;
            btnLogin.setEnabled(false);
            btnLogin.setText("Thử lại sau " + remainingTime + " giây");
            new Handler().postDelayed(() -> {
                btnLogin.setEnabled(true);
                btnLogin.setText("LOGIN");
            }, remainingTime * 1000);
        }
    }

    private void handleFailedLogin() {
        failedAttempts++;
        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            long lockoutTimestamp = System.currentTimeMillis() + LOCKOUT_DURATION;
            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong("lockout_timestamp", lockoutTimestamp);
            editor.apply();
            Toast.makeText(this, "Bạn đã nhập sai quá nhiều lần. Vui lòng thử lại sau 1 phút.", Toast.LENGTH_LONG).show();
            checkLockoutStatus();
        } else {
            int attemptsLeft = MAX_FAILED_ATTEMPTS - failedAttempts;
            Toast.makeText(this, "Sai thông tin! Bạn còn " + attemptsLeft + " lần thử.", Toast.LENGTH_SHORT).show();
        }
    }

    private void resetFailedAttempts() {
        failedAttempts = 0;
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("lockout_timestamp");
        editor.apply();
    }
}