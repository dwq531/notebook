package com.example.notebook;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText;
    private Button registerButton;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usernameEditText = findViewById(R.id.registerUsername);
        passwordEditText = findViewById(R.id.registerPassword);
        registerButton = findViewById(R.id.registerConfirmButton);
        databaseHelper = new DatabaseHelper(this);

        registerButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else if (databaseHelper.isUsernameExists(username)) {
                showUsernameExistsDialog();
            } else {
                databaseHelper.addUser(username, password);
                Toast.makeText(RegisterActivity.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                // 打印所有用户信息
                databaseHelper.printAllUsers();
                finish();
            }
        });
    }

    private void showUsernameExistsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("用户名已存在")
                .setMessage("当前用户名已存在，请修改用户名")
                .setPositiveButton("好的", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
