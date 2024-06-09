package com.example.notebook.ui.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.notebook.DatabaseHelper;
import com.example.notebook.MainActivity;
import com.example.notebook.R;

public class HomeFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView profileImage;
    private TextView usernameText, signatureText, noteCountText;
    private Button updateProfileButton, logoutButton;
    private DatabaseHelper databaseHelper;
    private Uri selectedImageUri; // 用于存储选择的新头像 URI

    private AlertDialog dialog; // 用于保存对话框的引用

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        profileImage = root.findViewById(R.id.profileImage);
        usernameText = root.findViewById(R.id.usernameText);
        signatureText = root.findViewById(R.id.signatureText);
        noteCountText = root.findViewById(R.id.noteCountText);
        updateProfileButton = root.findViewById(R.id.updateProfileButton);
        logoutButton = root.findViewById(R.id.logoutButton);
        databaseHelper = new DatabaseHelper(getActivity());

        // 加载当前用户信息
        loadUserInfo();

        updateProfileButton.setOnClickListener(v -> showUpdateProfileDialog());
        logoutButton.setOnClickListener(v -> logout());

        // 统计笔记数量
        int noteCount = databaseHelper.getNoteCount();
        noteCountText.setText("笔记数量: " + noteCount);

        return root;
    }

    private void loadUserInfo() {
        // 从数据库加载用户信息并显示
        Log.d("loadUserinfo","into loadUserInfo");
        String username = databaseHelper.getCurrentUsername();
        //Log.d("loadUsername",username);
        String signature = databaseHelper.getCurrentUserSignature();

        if (username != null) {
            usernameText.setText(username);
        } else {
            usernameText.setText("未知用户");
        }

        if (signature != null) {
            signatureText.setText(signature);
        } else {
            signatureText.setText("暂无签名");
        }

        // 加载并设置个人头像
        String imageUrl = databaseHelper.getCurrentUserImageUrl();
        if (imageUrl != null) {
            profileImage.setImageURI(Uri.parse(imageUrl));
        } else {
            profileImage.setImageResource(R.drawable.default_profileimage); // 设置默认头像
        }
    }

    private void showUpdateProfileDialog() {
        // 创建对话框并设置内容视图
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_update_profile, null);
        builder.setView(dialogView);

        // 获取对话框中的视图
        ImageView dialogProfileImage = dialogView.findViewById(R.id.editProfileImage);
        EditText editUsername = dialogView.findViewById(R.id.editUsername);
        EditText editPassword = dialogView.findViewById(R.id.editPassword);
        EditText editSignature = dialogView.findViewById(R.id.editSignature);
        Button chooseImageButton = dialogView.findViewById(R.id.selectNewImageButton);
        Button saveButton = dialogView.findViewById(R.id.saveProfileButton);

        // 设置当前用户的信息
        editUsername.setText(databaseHelper.getCurrentUsername());
        editSignature.setText(databaseHelper.getCurrentUserSignature());

        // 设置当前用户密码
        String currentPassword = databaseHelper.getCurrentUserPassword();
        if (currentPassword != null) {
            editPassword.setText(currentPassword);
        }

        // 设置当前用户头像
        String imageUrl = databaseHelper.getCurrentUserImageUrl();
        if (imageUrl != null) {
            dialogProfileImage.setImageURI(Uri.parse(imageUrl));
        } else {
            dialogProfileImage.setImageResource(R.drawable.default_profileimage); // 设置默认头像
        }

        // 选择新头像的点击事件
        chooseImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        // 保存按钮点击事件
        saveButton.setOnClickListener(v -> {
            // 获取输入的新信息
            String newUsername = editUsername.getText().toString().trim();
            String newPassword = editPassword.getText().toString().trim();
            String newSignature = editSignature.getText().toString().trim();
            // 更新数据库中的用户信息
            String newImageUrl = selectedImageUri != null ? selectedImageUri.toString() : imageUrl;
            databaseHelper.updateUserInfo(newUsername, newPassword, newSignature);

            // 重新加载用户信息
            loadUserInfo();

            // 关闭对话框
            dialog.dismiss();
            Toast.makeText(getActivity(), "信息已更新", Toast.LENGTH_SHORT).show();
        });

        // 显示对话框
        dialog = builder.create();
        dialog.show();
    }

    private void logout() {
        // 实现退出登录逻辑，例如清除用户信息并跳转到登录页面
        databaseHelper.logoutCurrentUser();
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    // 处理从图库选择头像的结果
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            if (dialog != null && dialog.isShowing()) {
                // 在对话框中显示新选择的头像
                ImageView dialogProfileImage = dialog.findViewById(R.id.editProfileImage);
                if (dialogProfileImage != null) {
                    dialogProfileImage.setImageURI(selectedImageUri);
                    databaseHelper.updateUserImage(selectedImageUri.toString());
                }
            }
        }
    }
}
