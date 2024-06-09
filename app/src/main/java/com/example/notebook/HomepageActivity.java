package com.example.notebook;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.example.notebook.ui.dashboard.DashboardFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.notebook.databinding.ActivityHomepageBinding;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import android.Manifest;
import android.widget.Toast;

public class HomepageActivity extends AppCompatActivity {

    private ActivityHomepageBinding binding;
    private APIEndPoint api;
    private int user_id = 0;
    private final int ADD_NOTE = 0,EDIT_NOTE=1;
    private UploadManager uploadManager;
    private  static int REQUEST_READ_EXTERNAL_STORAGE=0;
    private NavController navController;
    private Fragment navHostFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        binding = ActivityHomepageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_notebook)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        navHostFragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE);
        }


        uploadManager = new UploadManager(this);
        uploadManager.getNotes(user_id);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with your action
            } else {
                // Permission denied, inform the user that the permission is necessary
                Toast.makeText(this, "Read external storage permission is required to access files.", Toast.LENGTH_LONG).show();
            }
        }
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main);

        if (requestCode == ADD_NOTE || requestCode == EDIT_NOTE) {
            if (navHostFragment instanceof NavHostFragment) {
                List<Fragment> fragments = navHostFragment.getChildFragmentManager().getFragments();
                for (Fragment fragment : fragments) {
                    if (fragment instanceof DashboardFragment) {
                        fragment.onActivityResult(requestCode, resultCode, data);

                    }
                }
            }
        }
    }
}