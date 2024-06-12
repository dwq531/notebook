package com.example.notebook;

import android.Manifest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.app.Dialog;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.DragEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class text_editor extends AppCompatActivity {
    Button addImgButton,addAudioButton,addTextButton,returnButton,AIButton;
    Button folderButton;
    LinearLayout linearLayout;
    public static int PICK_IMAGE_REQUEST = 1,TAKE_PICTURE_REQUEST = 2,PICK_AUDIO_REQUEST = 3,RECORD_PERMISSION=4,CAMERA_PERMISSION=5,REQUEST_READ_MEDIA_AUDIO=6;
    private static int TEXT=0,IMAGE=1,AUDIO=2;
    private MediaPlayer mediaPlayer=null;
    private View playingAudioBlock = null;
    private View recodingBlock = null;
    private MediaRecorder mediaRecorder = null;
    private Handler handler = new Handler();
    private long startTime = 0;
    private TextView audioDuration;
    private DatabaseHelper databaseHelper;
    private long note_id,user_id;
    private  UploadManager uploadManager;


    String currentPhotoPath,currentAudioPath;
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
    private File createAudioFile() throws IOException {
        // 创建一个以时间戳命名的文件名
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "AUDIO_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File audioFile = File.createTempFile(
                imageFileName,  /* 前缀 */
                ".3gp",         /* 后缀 */
                storageDir      /* 目录 */
        );
        currentAudioPath = audioFile.getAbsolutePath();
        return audioFile;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        uploadManager = new UploadManager(this);
        setContentView(R.layout.activity_text_editor);
        databaseHelper = new DatabaseHelper(this);
        addImgButton = findViewById(R.id.but_add_img);
        addAudioButton = findViewById(R.id.but_add_audio);
        addTextButton = findViewById(R.id.but_add_text);
        AIButton = findViewById(R.id.but_ai);
        returnButton = findViewById(R.id.but_back);
        folderButton = findViewById(R.id.but_folder);
        linearLayout = findViewById(R.id.linear_layout);
        // 绑定按钮点击事件
        addImgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(text_editor.this, addImgButton);
                popupMenu.getMenuInflater().inflate(R.menu.select_img_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        // 拍摄照片
                        if(item.getItemId() == R.id.take_photo){
                            if (ContextCompat.checkSelfPermission(text_editor.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(text_editor.this, new String[]{
                                        Manifest.permission.CAMERA,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                        Manifest.permission.READ_EXTERNAL_STORAGE
                                }, CAMERA_PERMISSION);
                            }
                            else {
                                startCamera();
                            }

                            return true;
                        }
                        // 从相册选择照片
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
        addAudioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(text_editor.this, addAudioButton);
                popupMenu.getMenuInflater().inflate(R.menu.select_audio_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        // 录音
                        if(item.getItemId() == R.id.record){
                            if(mediaRecorder != null)
                                return false;// todo:能不能把菜单设成无效的
                            if (ContextCompat.checkSelfPermission(text_editor.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                                    ContextCompat.checkSelfPermission(text_editor.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                                    ContextCompat.checkSelfPermission(text_editor.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(text_editor.this, new String[]{
                                        Manifest.permission.RECORD_AUDIO,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                        Manifest.permission.READ_EXTERNAL_STORAGE
                                }, RECORD_PERMISSION);

                            }
                            else{
                                startRecord();
                            }
                            return true;
                        }
                        // 选择音频文件
                        else if(item.getItemId() == R.id.audio_file){
                            if (ContextCompat.checkSelfPermission(text_editor.this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(text_editor.this, new String[]{Manifest.permission.READ_MEDIA_AUDIO}, REQUEST_READ_MEDIA_AUDIO);
                            }else{
                                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                intent.setType("audio/*");
                                startActivityForResult(intent, PICK_AUDIO_REQUEST);
                            }
                            return true;
                        }
                        else {
                            return false;
                        }
                    }
                });
                popupMenu.show();
            }
        });
        addTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View textBlock = getLayoutInflater().inflate(R.layout.text_layout, linearLayout, false);
                linearLayout.addView(textBlock);
                setDrag(textBlock);
                long content_id = databaseHelper.addContent(note_id,"",TEXT,linearLayout.indexOfChild(textBlock));
                textBlock.setTag(content_id);
                EditText editText = textBlock.findViewById(R.id.text_content);
                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}
                    @Override
                    public void afterTextChanged(Editable s) {
                        Log.d("text_editor",s.toString());
                        databaseHelper.updateContent(content_id, s.toString());
                        databaseHelper.updateNoteVersion(note_id);
                    }
                });
            }
        });
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadManager.uploadNote(note_id);
                TextView title = findViewById(R.id.text_title);
                TextView time = findViewById(R.id.create_time);
                Intent returnIntent = new Intent();
                returnIntent.putExtra("title",title.getText().toString());
                returnIntent.putExtra("time",time.getText().toString());
                returnIntent.putExtra("note_id",note_id);
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });
        folderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFolderDialog();
            }
        });
        AIButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(text_editor.this, AIButton);
                popupMenu.getMenuInflater().inflate(R.menu.select_ai_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if(item.getItemId()==R.id.generate_title){
                            Toast.makeText(text_editor.this, "开始生成标题，可能需要一段时间，莫急", Toast.LENGTH_SHORT).show();
                            AI ai = new AI(text_editor.this);
                            ai.generate_title(note_id,(String title)->{
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        EditText editTitle = findViewById(R.id.text_title);
                                        editTitle.setText(title);
                                    }
                                });
                            });
                            return true;
                        }else if(item.getItemId()==R.id.generate_summary){
                            Toast.makeText(text_editor.this, "开始生成总结，可能需要一段时间，莫急", Toast.LENGTH_SHORT).show();
                            AI ai = new AI(text_editor.this);
                            ai.generate_summary(note_id,(String text)->{
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        View textBlock = getLayoutInflater().inflate(R.layout.text_layout, linearLayout, false);
                                        linearLayout.addView(textBlock);
                                        setDrag(textBlock);
                                        long content_id = databaseHelper.addContent(note_id,text,TEXT,linearLayout.indexOfChild(textBlock));
                                        textBlock.setTag(content_id);
                                        EditText editText = textBlock.findViewById(R.id.text_content);
                                        editText.setText(text);
                                        editText.addTextChangedListener(new TextWatcher() {
                                            @Override
                                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                                            @Override
                                            public void onTextChanged(CharSequence s, int start, int before, int count) {}
                                            @Override
                                            public void afterTextChanged(Editable s) {
                                                Log.d("text_editor",s.toString());
                                                databaseHelper.updateContent(content_id, s.toString());
                                                databaseHelper.updateNoteVersion(note_id);
                                            }
                                        });
                                    }
                                });

                            });
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
        // 获取笔记内容
        Intent intent = getIntent();
        note_id = intent.getLongExtra("note_id",-1);
        user_id = intent.getIntExtra("user_id",-1);
        Log.d("text_editor",String.valueOf(note_id));
        Note note = databaseHelper.getNote(note_id);
        TextView createTime = findViewById(R.id.create_time);
        createTime.setText(note.create_time);
        EditText title = findViewById(R.id.text_title);
        title.setText(note.title);
        title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                databaseHelper.updateTitle(note_id,s.toString());
            }
        });
        List<Content> contents = databaseHelper.getContentList(note_id);
        for(Content content:contents){
            if(content.type == TEXT){
                View textBlock = getLayoutInflater().inflate(R.layout.text_layout, linearLayout, false);
                textBlock.setTag(content.content_id);
                EditText editText = textBlock.findViewById(R.id.text_content);
                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}
                    @Override
                    public void afterTextChanged(Editable s) {
                        Log.d("text_editor",s.toString());
                        databaseHelper.updateContent(content.content_id, s.toString());
                        databaseHelper.updateNoteVersion(note_id);
                    }
                });
                linearLayout.addView(textBlock);
                editText.setText(content.content);
                setDrag(textBlock);
            }
            else if(content.type == IMAGE){
                ImageView imageView = new ImageView(this);
                imageView.setTag(content.content_id);
                setDrag(imageView);
                linearLayout.addView(imageView);
                Log.d("text_editor",content.content);
                Glide.with(this).load(content.content).into(imageView);
            }
            else if(content.type == AUDIO){
                View audioBlock = getLayoutInflater().inflate(R.layout.audio_player_layout, linearLayout, false);
                audioBlock.setTag(content.content_id);
                setupAudioBlock(audioBlock);
                linearLayout.addView(audioBlock);
                setDrag(audioBlock);
            }
        }

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK){
            if(data == null)
                return;
            Uri imageUri = data.getData();
            if(imageUri!=null)
            {
                ImageView imageView = new ImageView(this);
                linearLayout.addView(imageView);
                long content_id = databaseHelper.addContent(note_id,imageUri.toString(),IMAGE,linearLayout.indexOfChild(imageView));
                imageView.setTag(content_id);
                Glide.with(this).load(imageUri).into(imageView);
                setDrag(imageView);
            }
        }
        else if(requestCode == TAKE_PICTURE_REQUEST && resultCode == RESULT_OK ){
            Uri photoUri = Uri.fromFile(new File(currentPhotoPath));
            if(photoUri!=null)
            {
                ImageView imageView = new ImageView(this);
                linearLayout.addView(imageView);
                long content_id = databaseHelper.addContent(note_id,photoUri.toString(),IMAGE,linearLayout.indexOfChild(imageView));
                imageView.setTag(content_id);
                Glide.with(this).load(photoUri).into(imageView);
                setDrag(imageView);
            }
        } else if (requestCode == PICK_AUDIO_REQUEST) {
            if(data == null)
                return;
            Uri audioUri = data.getData();
            View audioBlock = getLayoutInflater().inflate(R.layout.audio_player_layout, linearLayout, false);
            linearLayout.addView(audioBlock);
            long content_id=databaseHelper.addContent(note_id,audioUri.toString(),AUDIO,linearLayout.indexOfChild(audioBlock));
            audioBlock.setTag(content_id);
            setupAudioBlock(audioBlock);
            setDrag(audioBlock);

        }
    }

    private void setupAudioBlock(View audioBlock) {
        ImageButton playButton = audioBlock.findViewById(R.id.playButton);
        ProgressBar audioProgressBar = audioBlock.findViewById(R.id.audioProgressBar);
        TextView audioDuration = audioBlock.findViewById(R.id.audioDuration);
        Uri audioUri = Uri.parse(databaseHelper.getContent((Long) audioBlock.getTag()).content);
        try{
            // 获取时长
            releaseMediaPlayer();
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(text_editor.this,audioUri);
            mediaPlayer.prepare();
            int duration = mediaPlayer.getDuration();
            audioProgressBar.setMax(duration);
            String durationString = "/"+formatDuration(duration);
            audioDuration.setText(durationString);
            mediaPlayer.release();
            mediaPlayer = null;
        }catch (IOException e) {
            e.printStackTrace();
        }
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Uri audioUri = Uri.parse(databaseHelper.getContent((Long) audioBlock.getTag()).content);
                // 如果正在播放自己
                if(mediaPlayer!= null && playingAudioBlock == audioBlock) {
                    Log.d("mediaPlayer", "Pause!");
                    if (mediaPlayer.isPlaying())// 播放-》暂停
                    {
                        mediaPlayer.pause();
                        playButton.setImageResource(R.drawable.play);
                        handler.removeCallbacks(updateSeekBarRunnable);
                    } else {// 暂停-》播放
                        mediaPlayer.start();
                        playButton.setImageResource(R.drawable.pause);
                        handler.post(updateSeekBarRunnable);
                    }
                }
                else {// 如果没在播放自己，停止之前的，切换播放
                    Log.d("mediaPlayer", "stop and play!");
                    if(playingAudioBlock!=null){
                        ImageButton but = playingAudioBlock.findViewById(R.id.playButton);
                        but.setImageResource(R.drawable.play);
                        TextView timeView = playingAudioBlock.findViewById(R.id.time);
                        timeView.setText("0:00");
                        ProgressBar audioSeekBar = playingAudioBlock.findViewById(R.id.audioProgressBar);
                        audioSeekBar.setProgress(0);
                    }
                    playingAudioBlock = audioBlock;
                    playAudio(audioUri, playButton);
                }
            }
        });

    }
    private void setupRecordBlock(View recordBlock){
        ImageButton stopButton = recordBlock.findViewById(R.id.stopButton);
        recodingBlock = recordBlock;
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                releaseMediaRecorder();
                // 把录音块换成音频文件块
                View audioBlock = getLayoutInflater().inflate(R.layout.audio_player_layout, linearLayout, false);
                audioBlock.setTag(recordBlock.getTag());
                setupAudioBlock(audioBlock);
                int index = linearLayout.indexOfChild(recordBlock);
                linearLayout.removeView(recordBlock);
                linearLayout.addView(audioBlock, index);
                setDrag(audioBlock);

            }
        });
    }
    private void startRecord(){
        Uri audioUri;
        try {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            File audioFile = createAudioFile();
            String authority = getApplicationContext().getPackageName() + ".fileProvider";
            audioUri = FileProvider.getUriForFile(text_editor.this, authority, audioFile);
            mediaRecorder.setOutputFile(currentAudioPath);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.prepare();
            mediaRecorder.start();// 开始录音

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        View recordBlock = getLayoutInflater().inflate(R.layout.record_layout, linearLayout, false);
        linearLayout.addView(recordBlock);
        long content_id = databaseHelper.addContent(note_id,audioUri.toString(),AUDIO,linearLayout.indexOfChild(recordBlock));
        recordBlock.setTag(content_id);
        setupRecordBlock(recordBlock);
        setDrag(recordBlock);
        startTime = System.currentTimeMillis();
        audioDuration = recordBlock.findViewById(R.id.audioDuration);
        handler.post(updateDurationRunnable);// 更新时长
    }
    private void startCamera(){
        Intent take_picture_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
        if (photoFile != null) {
            String authority = getApplicationContext().getPackageName() + ".fileProvider";
            Uri photoUri = FileProvider.getUriForFile(text_editor.this, authority, photoFile);
            take_picture_intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(take_picture_intent, TAKE_PICTURE_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RECORD_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRecord();
            } else {
                Toast.makeText(this, "record audio permission is required to access files.", Toast.LENGTH_LONG).show();
            }
        }
        else if(requestCode == CAMERA_PERMISSION){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            }else{
                Toast.makeText(this, "Camera permission is required to access files.", Toast.LENGTH_LONG).show();
            }
        }
        else if (requestCode == REQUEST_READ_MEDIA_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("audio/*");
                startActivityForResult(intent, PICK_AUDIO_REQUEST);
            } else {
                Toast.makeText(this, "Read media audio permission is required to access files.", Toast.LENGTH_LONG).show();
            }
        }
    }
    private String formatDuration(long duration) {
        long seconds = (duration / 1000) % 60;
        long minutes = (duration / 1000) / 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
    private final Runnable updateDurationRunnable = new Runnable() {
        @Override
        public void run() {
            long elapsedTime = System.currentTimeMillis() - startTime;

            audioDuration.setText(formatDuration(elapsedTime));

            // Repeat this runnable code block again every 1 second
            handler.postDelayed(this, 1000);
        }
    };
    private final Runnable updateSeekBarRunnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                ProgressBar audioSeekBar = playingAudioBlock.findViewById(R.id.audioProgressBar);
                audioSeekBar.setProgress(currentPosition);
                TextView timeView = playingAudioBlock.findViewById(R.id.time);
                timeView.setText(formatDuration(currentPosition));
                handler.postDelayed(this, 1000); // Update every second
            }
        }
    };
    private void releaseMediaPlayer(){
        if(mediaPlayer != null){
            // 停止播放
            if(mediaPlayer.isPlaying()){
                ImageButton but = playingAudioBlock.findViewById(R.id.playButton);
                but.setImageResource(R.drawable.play);
                TextView time = playingAudioBlock.findViewById(R.id.time);
                time.setText("0:00");
                handler.removeCallbacks(updateDurationRunnable);// 停止更新时长
                // todo:进度条归零
                mediaPlayer.stop();
                mediaPlayer.reset();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
    private void releaseMediaRecorder(){
        if(mediaRecorder != null){
            // 停止录音
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            recodingBlock = null;
            handler.removeCallbacks(updateDurationRunnable);// 停止更新时长
        }
    }
    private void playAudio(Uri audioUri,ImageButton playButton){
        releaseMediaPlayer();
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    handler.removeCallbacks(updateSeekBarRunnable);
                    ProgressBar audioSeekBar = playingAudioBlock.findViewById(R.id.audioProgressBar);
                    audioSeekBar.setProgress(0);
                    playButton.setImageResource(R.drawable.play);
                    TextView time = playingAudioBlock.findViewById(R.id.time);
                    time.setText("0:00");
                    handler.removeCallbacks(updateSeekBarRunnable);
                }
            });
            mediaPlayer.setDataSource(text_editor.this, audioUri);
            mediaPlayer.prepare();
            mediaPlayer.start();
            playButton.setImageResource(R.drawable.pause);
            handler.post(updateSeekBarRunnable);// 更新时长
            //updateSeekBar();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void setDrag(View view){
        // 长按弹出菜单
        final PopupMenu[] popup = new PopupMenu[1];
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                popup[0] = new PopupMenu(text_editor.this, v);
                // Inflate the menu from xml
                popup[0].getMenuInflater().inflate(R.menu.edit_view_menu, popup[0].getMenu());
                popup[0].setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        if(item.getItemId() == R.id.delete){
                            if(recodingBlock == v){
                                // 停止录音
                                releaseMediaRecorder();
                            } else if (playingAudioBlock == v) {
                                // 停止播放
                                releaseMediaPlayer();
                            }
                            // 删除view
                            linearLayout.removeView(v);
                            long content_id = (long) v.getTag();
                            databaseHelper.deleteContent(content_id);
                            uploadManager.delete_content(content_id);
                            databaseHelper.updateNoteVersion(note_id);
                        }
                        return true;
                    }
                });
                // Show the popup menu
                popup[0].show();
                return true;
            }
        });
        // 长按拖拽
        view.setOnTouchListener(new View.OnTouchListener(){

            private long touchstart;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        touchstart = System.currentTimeMillis();
                        return false;  // 返回 false 让长按事件可以被触发
                    case MotionEvent.ACTION_MOVE:
                        if (System.currentTimeMillis() - touchstart > ViewConfiguration.getLongPressTimeout()) {
                            ClipData clipData = ClipData.newPlainText("", "");
                            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
                            v.startDragAndDrop(clipData, shadowBuilder, v, 0);
                            if(popup[0]!=null)
                                popup[0].dismiss();
                            return true;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        touchstart = 0;
                        break;
                }
                return false;
            }
        });
        view.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                switch (event.getAction()) {
                    case DragEvent.ACTION_DROP:
                        View draggedView = (View) event.getLocalState();
                        ViewGroup owner = (ViewGroup) draggedView.getParent();
                        int beforePosition = linearLayout.indexOfChild(draggedView);
                        int dropPosition = linearLayout.indexOfChild(view);
                        owner.removeView(draggedView);
                        long content_id = (long) draggedView.getTag();
                        databaseHelper.updateContentPosition(content_id,dropPosition,beforePosition,true);
                        databaseHelper.updateNoteVersion(note_id);
                        linearLayout.addView(draggedView, dropPosition);

                        List<Content> contents =databaseHelper.getContentList(note_id);
                        draggedView.setVisibility(View.VISIBLE);
                        break;
                    case DragEvent.ACTION_DRAG_ENDED:
                        View view = (View) event.getLocalState();
                        view.setVisibility(View.VISIBLE);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mediaPlayer != null){
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if(mediaRecorder != null){
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    private void showFolderDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_folder_selection);
        dialog.show();
    }

}