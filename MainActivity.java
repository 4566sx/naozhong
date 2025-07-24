package com.biblealarm.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    
    private TimePicker timePicker;
    private Button setAlarmButton;
    private Button cancelAlarmButton;
    private TextView statusText;
    private TextView todayPsalmText;
    
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    private SharedPreferences sharedPreferences;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        initAlarmManager();
        updateTodayPsalm();
        updateAlarmStatus();
    }
    
    private void initViews() {
        timePicker = findViewById(R.id.timePicker);
        setAlarmButton = findViewById(R.id.setAlarmButton);
        cancelAlarmButton = findViewById(R.id.cancelAlarmButton);
        statusText = findViewById(R.id.statusText);
        todayPsalmText = findViewById(R.id.todayPsalmText);
        
        timePicker.setIs24HourView(true);
        
        setAlarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAlarm();
            }
        });
        
        cancelAlarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelAlarm();
            }
        });
    }
    
    private void initAlarmManager() {
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        sharedPreferences = getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE);
        
        Intent intent = new Intent(this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
    
    private void setAlarm() {
        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();
        
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        
        // 如果设置的时间已经过了今天，则设置为明天
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        
        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, 
                calendar.getTimeInMillis(), pendingIntent);
            
            // 保存闹钟设置
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("alarm_set", true);
            editor.putInt("alarm_hour", hour);
            editor.putInt("alarm_minute", minute);
            editor.apply();
            
            String timeString = String.format("%02d:%02d", hour, minute);
            statusText.setText("闹钟已设置：" + timeString);
            Toast.makeText(this, "闹钟设置成功！", Toast.LENGTH_SHORT).show();
            
        } catch (SecurityException e) {
            Toast.makeText(this, "设置闹钟失败，请检查权限", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void cancelAlarm() {
        if (alarmManager != null && pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("alarm_set", false);
            editor.apply();
            
            statusText.setText("闹钟已取消");
            Toast.makeText(this, "闹钟已取消", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void updateAlarmStatus() {
        boolean alarmSet = sharedPreferences.getBoolean("alarm_set", false);
        if (alarmSet) {
            int hour = sharedPreferences.getInt("alarm_hour", 7);
            int minute = sharedPreferences.getInt("alarm_minute", 0);
            String timeString = String.format("%02d:%02d", hour, minute);
            statusText.setText("闹钟已设置：" + timeString);
            
            // 更新TimePicker显示
            timePicker.setHour(hour);
            timePicker.setMinute(minute);
        } else {
            statusText.setText("未设置闹钟");
        }
    }
    
    private void updateTodayPsalm() {
        PsalmManager psalmManager = new PsalmManager();
        int todayPsalm = psalmManager.getTodayPsalm();
        todayPsalmText.setText("今日诗篇：第 " + todayPsalm + " 篇");
    }
}