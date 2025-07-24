package com.biblealarm.app;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.util.Calendar;

public class TestAlarmActivity extends Activity {
    
    private static final String TAG = "TestAlarm";
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 创建简单的界面
        Button testButton = new Button(this);
        testButton.setText("测试闹钟（30秒后）");
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTestAlarm();
            }
        });
        
        setContentView(testButton);
        
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    }
    
    private void setTestAlarm() {
        Intent intent = new Intent(this, TestAlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        // 设置30秒后的闹钟
        long triggerTime = System.currentTimeMillis() + 30000;
        
        try {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            Toast.makeText(this, "测试闹钟已设置，30秒后响起", Toast.LENGTH_LONG).show();
            Log.d(TAG, "测试闹钟设置成功，时间：" + triggerTime);
        } catch (Exception e) {
            Toast.makeText(this, "设置失败：" + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "设置闹钟失败", e);
        }
    }
    
    public static class TestAlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "测试闹钟触发！");
            
            // 播放系统默认铃声
            try {
                MediaPlayer mp = MediaPlayer.create(context, android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI);
                if (mp != null) {
                    mp.start();
                    Log.d(TAG, "开始播放铃声");
                } else {
                    Log.e(TAG, "无法创建MediaPlayer");
                }
            } catch (Exception e) {
                Log.e(TAG, "播放铃声失败", e);
            }
            
            // 显示Toast
            Toast.makeText(context, "测试闹钟响了！", Toast.LENGTH_LONG).show();
        }
    }
}