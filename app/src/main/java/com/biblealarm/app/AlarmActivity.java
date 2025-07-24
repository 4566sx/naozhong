package com.biblealarm.app;

import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;

public class AlarmActivity extends AppCompatActivity {
    
    private static final String TAG = "AlarmActivity";
    
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
        
        Log.d(TAG, "AlarmActivity启动");
        
        // 设置在锁屏上显示
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                           WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                           WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                           WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        setContentView(R.layout.activity_alarm);
        
        initViews();
        acquireWakeLock();
        setupPsalmDisplay();
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
    
    private void setupPsalmDisplay() {
        psalmManager = new PsalmManager();
        currentPsalm = psalmManager.getTodayPsalm();
        Log.d(TAG, "显示今日诗篇: 第" + currentPsalm + "篇");
    }
    
    private void displayPsalmContent() {
        if (psalmManager != null) {
            psalmTitleText.setText("诗篇 第" + currentPsalm + "篇");
            String content = psalmManager.getPsalmContent(currentPsalm);
            psalmContentText.setText(content);
        }
    }
    
    private void stopAlarm() {
        Log.d(TAG, "用户点击停止闹钟");
        
        // 停止AlarmService
        Intent serviceIntent = new Intent(this, AlarmService.class);
        stopService(serviceIntent);
        
        releaseWakeLock();
        finish();
    }
    
    private void snoozeAlarm() {
        Log.d(TAG, "用户点击贪睡闹钟");
        
        // 停止当前AlarmService
        Intent serviceIntent = new Intent(this, AlarmService.class);
        stopService(serviceIntent);
        
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
        Log.d(TAG, "AlarmActivity销毁");
        
        // 确保停止AlarmService
        Intent serviceIntent = new Intent(this, AlarmService.class);
        stopService(serviceIntent);
        
        releaseWakeLock();
    }
    
    @Override
    public void onBackPressed() {
        // 防止用户通过返回键关闭闹钟
        // 必须点击停止或贪睡按钮
    }
}