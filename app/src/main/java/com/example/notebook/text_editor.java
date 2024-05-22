package com.example.notebook;

import android.Manifest;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class text_editor extends AppCompatActivity {
    Button addImgButton,addAudioButton,addTextButton,returnButton;
    LinearLayout linearLayout;
    public static int PICK_IMAGE_REQUEST = 1,TAKE_PICTURE_REQUEST = 2,PICK_AUDIO_REQUEST = 3,REQUEST_CODE_PERMISSION=4;
    private MediaPlayer mediaPlayer=null;
    private View playingAudioBlock = null;
    private MediaRecorder mediaRecorder = null;
    private Handler handler = new Handler();
    private long startTime = 0;
    private TextView audioDuration;


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
        setContentView(R.layout.activity_text_editor);
        addImgButton = findViewById(R.id.but_add_img);
        addAudioButton = findViewById(R.id.but_add_audio);
        addTextButton = findViewById(R.id.but_add_text);
        returnButton = findViewById(R.id.but_back);
        requestRecordPermissions();
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
                                return false;
                            }
                            View recordBlock = getLayoutInflater().inflate(R.layout.record_layout, linearLayout, false);
                            recordBlock.setTag(audioUri);
                            Log.d("AudioURI", "Audio URI: " + audioUri.toString());
                            setupRecordBlock(recordBlock);
                            linearLayout.addView(recordBlock);
                            startTime = System.currentTimeMillis();
                            audioDuration = recordBlock.findViewById(R.id.audioDuration);
                            handler.post(updateDurationRunnable);// 更新时长
                            return true;
                        }
                        // 选择音频文件
                        else if(item.getItemId() == R.id.audio_file){
                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                            intent.setType("audio/*");
                            startActivityForResult(intent, PICK_AUDIO_REQUEST);
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
            }
        });
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView title = findViewById(R.id.text_title);
                TextView time = findViewById(R.id.create_time);
                Intent returnIntent = new Intent();
                returnIntent.putExtra("title",title.getText().toString());
                returnIntent.putExtra("time",time.getText().toString());
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });
        linearLayout = findViewById(R.id.linear_layout);
        TextView createTime = findViewById(R.id.create_time);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        createTime.setText(dateFormat.format(calendar.getTime()));
        //ActivityCompat.requestPermissions(text_editor.this,new String[]{Manifest.permission.CAMERA} );
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(data == null)
            return;
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK){
            Uri imageUri = data.getData();
            if(imageUri!=null)
            {
                ImageView imageView = new ImageView(this);
                // 设置 ImageView 的属性，比如宽度、高度、图片资源等
                linearLayout.addView(imageView);
                Glide.with(this).load(imageUri).into(imageView);
            }
        }
        else if(requestCode == TAKE_PICTURE_REQUEST && resultCode == RESULT_OK ){
            Uri photoUri = Uri.fromFile(new File(currentPhotoPath));
            if(photoUri!=null)
            {
                ImageView imageView = new ImageView(this);
                linearLayout.addView(imageView);
                Glide.with(this).load(photoUri).into(imageView);
            }
        } else if (requestCode == PICK_AUDIO_REQUEST) {
            Uri audioUri = data.getData();
            View audioBlock = getLayoutInflater().inflate(R.layout.audio_player_layout, linearLayout, false);
            audioBlock.setTag(audioUri);
            setupAudioBlock(audioBlock);
            linearLayout.addView(audioBlock);
        }
    }

    private void setupAudioBlock(View audioBlock) {
        ImageButton playButton = audioBlock.findViewById(R.id.playButton);
        ProgressBar audioProgressBar = audioBlock.findViewById(R.id.audioProgressBar);
        TextView audioDuration = audioBlock.findViewById(R.id.audioDuration);
        Uri audioUri = (Uri) audioBlock.getTag();
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
                Uri audioUri = (Uri) audioBlock.getTag();
                Log.d("AudioURI", "Audio URI: " + audioUri.toString());
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
        TextView duration = recordBlock.findViewById(R.id.audioDuration);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
                // 把录音块换成音频文件块
                View audioBlock = getLayoutInflater().inflate(R.layout.audio_player_layout, linearLayout, false);
                audioBlock.setTag(recordBlock.getTag());
                Log.d("AudioURI", "Audio URI: " + recordBlock.getTag().toString());
                setupAudioBlock(audioBlock);
                handler.removeCallbacks(updateDurationRunnable);// 停止更新时长
                int index = linearLayout.indexOfChild(recordBlock);
                linearLayout.removeView(recordBlock);
                linearLayout.addView(audioBlock, index);

            }
        });
    }
    private void requestRecordPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, REQUEST_CODE_PERMISSION);
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
}