package com.example.notebook;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class text_editor extends AppCompatActivity {
    Button addImgButton,addAudioButton;
    LinearLayout linearLayout;
    public static int PICK_IMAGE_REQUEST = 1,TAKE_PICTURE_REQUEST = 2;

    String currentPhotoPath;
    private File createImageFile() throws IOException {
        // 创建一个以时间戳命名的文件名
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(
                imageFileName,  /* 前缀 */
                ".jpg",         /* 后缀 */
                storageDir      /* 目录 */
        );
        currentPhotoPath = imageFile.getAbsolutePath();
        return imageFile;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_editor);
        addImgButton = findViewById(R.id.but_add_img);
        addAudioButton = findViewById(R.id.but_add_audio);
        addImgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(text_editor.this, addImgButton);
                popupMenu.getMenuInflater().inflate(R.menu.select_img_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        // 处理菜单项点击事件
                        if(item.getItemId() == R.id.take_photo){
                            Intent take_picture_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            File photoFile = null;
                            try {
                                photoFile = createImageFile();
                            } catch (IOException ex) {
                                // TODO
                            }
                            if (photoFile != null) {
                                String authority = getApplicationContext().getPackageName() + ".fileProvider";
                                Uri photoUri = FileProvider.getUriForFile(text_editor.this, authority, photoFile);
                                take_picture_intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                                startActivityForResult(take_picture_intent, TAKE_PICTURE_REQUEST);
                            }
                            return true;
                        }
                        else if(item.getItemId() == R.id.album){
                            Intent select_intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(select_intent, PICK_IMAGE_REQUEST);
                            return true;
                        }
                        else{
                            return false;
                        }
                    }
                });
                popupMenu.show();
            }
        });
        linearLayout = findViewById(R.id.linear_layout);
        //ActivityCompat.requestPermissions(text_editor.this,new String[]{Manifest.permission.CAMERA} );
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data !=null){
            Uri imageUri = data.getData();
            if(imageUri!=null)
            {
                ImageView imageView = new ImageView(this);
                // 设置 ImageView 的属性，比如宽度、高度、图片资源等
                linearLayout.addView(imageView);
                Glide.with(this).load(imageUri).into(imageView);
            }
        }
        else if(requestCode == TAKE_PICTURE_REQUEST && resultCode == RESULT_OK && data !=null){
            Uri photoUri = Uri.fromFile(new File(currentPhotoPath));
            if(photoUri!=null)
            {
                ImageView imageView = new ImageView(this);
                linearLayout.addView(imageView);
                Glide.with(this).load(photoUri).into(imageView);
            }
        }
    }
}