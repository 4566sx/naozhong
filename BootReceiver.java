package com.biblealarm.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class BootReceiver extends BroadcastReceiver {
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) ||
            Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction())) {
            
            // 重新设置闹钟
            restoreAlarms(context);
        }
    }
    
    private void restoreAlarms(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE);
        boolean alarmSet = prefs.getBoolean("alarm_set", false);
        
        if (alarmSet) {
            int hour = prefs.getInt("alarm_hour", 7);
            int minute = prefs.getInt("alarm_minute", 0);
            
            // 使用AlarmUtils重新设置闹钟
            AlarmUtils.setDailyAlarm(context, hour, minute);
        }
    }
}