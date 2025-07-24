package com.biblealarm.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {
    
    @Override
    public void onReceive(Context context, Intent intent) {
        // 启动闹钟活动
        Intent alarmIntent = new Intent(context, AlarmActivity.class);
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | 
                           Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(alarmIntent);
        
        // 启动闹钟服务
        Intent serviceIntent = new Intent(context, AlarmService.class);
        context.startForegroundService(serviceIntent);
    }
}