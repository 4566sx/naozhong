package com.biblealarm.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import java.io.IOException;

public class AlarmService extends Service {
    
    private static final String TAG = "AlarmService";
    private static final String CHANNEL_ID = "AlarmServiceChannel";
    private static final int NOTIFICATION_ID = 1;
    
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;
    
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        Log.d(TAG, "AlarmService创建");
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "AlarmService启动，开始播放音频");
        startForeground(NOTIFICATION_ID, createNotification());
        
        // 开始播放音频和振动
        startAlarmSound();
        startVibration();
        
        return START_NOT_STICKY;
    }
    
    private void startAlarmSound() {
        try {
            // 获取今日诗篇
            PsalmManager psalmManager = new PsalmManager();
            int todayPsalm = psalmManager.getTodayPsalm();
            String psalmFileName = "psalm_" + String.format("%03d", todayPsalm);
            
            // 获取音频资源ID
            int audioResId = getResources().getIdentifier(psalmFileName, "raw", getPackageName());
            if (audioResId == 0) {
                Log.w(TAG, "找不到诗篇音频文件: " + psalmFileName + "，使用默认音频");
                audioResId = R.raw.default_psalm;
            }
            
            Log.d(TAG, "播放诗篇音频: " + psalmFileName + " (资源ID: " + audioResId + ")");
            
            // 创建MediaPlayer
            mediaPlayer = MediaPlayer.create(this, audioResId);
            if (mediaPlayer != null) {
                // 设置音频属性
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build();
                    mediaPlayer.setAudioAttributes(audioAttributes);
                } else {
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                }
                
                // 设置循环播放
                mediaPlayer.setLooping(true);
                
                // 设置音量为最大
                AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                if (audioManager != null) {
                    int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVolume, 0);
                }
                
                // 开始播放
                mediaPlayer.start();
                Log.d(TAG, "音频播放开始");
                
                // 设置播放完成监听器
                mediaPlayer.setOnCompletionListener(mp -> {
                    Log.d(TAG, "音频播放完成");
                });
                
                mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                    Log.e(TAG, "音频播放错误: what=" + what + ", extra=" + extra);
                    return false;
                });
                
            } else {
                Log.e(TAG, "无法创建MediaPlayer");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "启动闹钟音频失败: " + e.getMessage(), e);
        }
    }
    
    private void startVibration() {
        try {
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                // 振动模式：振动1秒，停止0.5秒，重复
                long[] pattern = {0, 1000, 500};
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(pattern, 0);
                } else {
                    vibrator.vibrate(pattern, 0);
                }
                Log.d(TAG, "振动开始");
            }
        } catch (Exception e) {
            Log.e(TAG, "启动振动失败: " + e.getMessage(), e);
        }
    }
    
    public void stopAlarm() {
        Log.d(TAG, "停止闹钟");
        
        // 停止音频播放
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
                mediaPlayer = null;
                Log.d(TAG, "音频播放停止");
            } catch (Exception e) {
                Log.e(TAG, "停止音频播放失败: " + e.getMessage(), e);
            }
        }
        
        // 停止振动
        if (vibrator != null) {
            vibrator.cancel();
            vibrator = null;
            Log.d(TAG, "振动停止");
        }
        
        // 停止服务
        stopSelf();
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "圣经闹钟服务",
                NotificationManager.IMPORTANCE_LOW
            );
            serviceChannel.setDescription("圣经闹钟后台服务");
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
    
    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, 
            notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        
        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("圣经闹钟")
            .setContentText("闹钟正在响铃...")
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build();
    }
    
    @Override
    public void onDestroy() {
        Log.d(TAG, "AlarmService销毁");
        stopAlarm();
        super.onDestroy();
        stopForeground(true);
    }
}