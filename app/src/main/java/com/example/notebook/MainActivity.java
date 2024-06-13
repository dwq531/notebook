package com.example.notebook;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.notebook.databinding.ActivityMainBinding;

import android.util.Log;
import android.widget.Button;
import android.content.Intent;
import android.widget.EditText;
import android.widget.Toast;

import java.util.concurrent.CountDownLatch;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private EditText usernameEditText, passwordEditText;
    private Button loginButton, registerButton;
    private DatabaseHelper databaseHelper;
    private UploadManager uploadManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_main);

        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        databaseHelper = new DatabaseHelper(this);
        uploadManager = new UploadManager(this);
        loginButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "请填写用户名或密码", Toast.LENGTH_SHORT).show();
            } else {
                // 查询云端服务器的用户信息
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("http://183.173.97.190s:8000/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                APIEndPoint api = retrofit.create(APIEndPoint.class);
                Call<User> call = api.get_user_by_name(username);
                call.enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        if (response.isSuccessful()) {
                            User user = response.body();
                            if(user == null) {
                                Toast.makeText(MainActivity.this, "用户名或密码不正确", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            User localUser = databaseHelper.getUser(user.user_id);
                            if(localUser==null ){
                                databaseHelper.addUser(user.user_id,user.username,user.password,user.signatrue,user.image_url,user.version);
                                uploadManager.downloadImg(user.user_id);
                            } else if (localUser.version<user.version) {
                                databaseHelper.updateUserInfo(user.username,user.password,user.signatrue);
                                uploadManager.downloadImg(user.user_id);
                            }
                            if (databaseHelper.checkUser(username, password)) {
                                Toast.makeText(MainActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                                // 登录成功后跳转到主页
                                Intent intent = new Intent(MainActivity.this, HomepageActivity.class);
                                startActivity(intent);
                                finish();

                            }
                            Log.d("API","Response: " + response.body().toString());
                        }
                        else{
                            Log.d("API","Error: " + response.errorBody().toString());
                        }
                    }
                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        Log.d("API","Failure: " + t.getMessage());
                        if (databaseHelper.checkUser(username, password)) {
                            Toast.makeText(MainActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                            // 登录成功后跳转到主页
                            Intent intent = new Intent(MainActivity.this, HomepageActivity.class);
                            startActivity(intent);
                            finish();

                        }
                    }
                });

            }
        });

        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}
