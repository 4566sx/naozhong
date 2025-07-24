package com.biblealarm.app;

import android.app.KeyguardManager;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;

public class AlarmActivity extends AppCompatActivity {
    
    private MediaPlayer mediaPlayer;
    private TextView psalmTitleText;
    private TextView psalmContentText;
    private Button stopButton;
    private Button snoozeButton;
    
    private PowerManager.WakeLock wakeLock;
    private PsalmManager psalmManager;
    private int currentPsalm;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 设置在锁屏上显示
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                           WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                           WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                           WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        setContentView(R.layout.activity_alarm);
        
        initViews();
        acquireWakeLock();
        playPsalmAudio();
        displayPsalmContent();
    }
    
    private void initViews() {
        psalmTitleText = findViewById(R.id.psalmTitleText);
        psalmContentText = findViewById(R.id.psalmContentText);
        stopButton = findViewById(R.id.stopButton);
        snoozeButton = findViewById(R.id.snoozeButton);
        
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAlarm();
            }
        });
        
        snoozeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snoozeAlarm();
            }
        });
    }
    
    private void acquireWakeLock() {
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BibleAlarm:WakeLock");
        wakeLock.acquire(10 * 60 * 1000L); // 10分钟
    }
    
    private void playPsalmAudio() {
        psalmManager = new PsalmManager();
        currentPsalm = psalmManager.getTodayPsalm();
        
        try {
            mediaPlayer = new MediaPlayer();
            String audioPath = "android.resource://" + getPackageName() + "/" + 
                             psalmManager.getPsalmAudioResource(currentPsalm);
            mediaPlayer.setDataSource(this, android.net.Uri.parse(audioPath));
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
            // 如果音频播放失败，使用默认铃声
            playDefaultRingtone();
        }
    }
    
    private void playDefaultRingtone() {
        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.default_psalm);
            if (mediaPlayer != null) {
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void displayPsalmContent() {
        if (psalmManager != null) {
            psalmTitleText.setText("诗篇 第" + currentPsalm + "篇");
            String content = psalmManager.getPsalmContent(currentPsalm);
            psalmContentText.setText(content);
        }
    }
    
    private void stopAlarm() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        
        releaseWakeLock();
        finish();
    }
    
    private void snoozeAlarm() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        
        // 设置5分钟后再次响铃
        AlarmUtils.setSnoozeAlarm(this, 5);
        
        releaseWakeLock();
        finish();
    }
    
    private void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        releaseWakeLock();
    }
    
    @Override
    public void onBackPressed() {
        // 防止用户通过返回键关闭闹钟
        // 必须点击停止或贪睡按钮
    }
}